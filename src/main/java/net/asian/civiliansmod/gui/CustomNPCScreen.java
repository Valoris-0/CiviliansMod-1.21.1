package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.networking.NPCDataPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;

public class CustomNPCScreen extends Screen {
    private final NPCEntity npc;

    // Layout constants
    private static final int ENTITY_PREVIEW_SIZE = 25; // Downscaled preview
    private static final int ENTITY_SPACING = 60;     // Adjusted spacing
    private static final int COLUMN_WIDTH = 130;
    private int selectedVariant; // No variant is selected by default
    private int scrollOffset = 0;  // Current scroll offset
    private int maxScrollOffset;  // Maximum allowed scroll offset
    private boolean isScrolling = false; // True if currently dragging the scrollbar
    private int scrollbarHeight = 0;
    private int scrollbarY = 0;
    private final int originalVariant;
    private int scrollbarGrabOffset = 0;
    private boolean isDefaultTab = true;

    private TextFieldWidget nameInputField;

    public CustomNPCScreen(NPCEntity npc) {

        super(Text.literal("Change NPC Variant"));
        this.npc = npc;
        this.originalVariant = npc.getVariant(); // Save the current variant to initialize the preview
        this.selectedVariant = -1; // No new skin is selected yet
    }


    private void updateScrollBarDimensions() {
        int totalRows = 15;
        int visibleRows = (this.height - 100) / ENTITY_SPACING;

        this.maxScrollOffset = Math.max(0, (totalRows - visibleRows) * ENTITY_SPACING);

        int scrollBarTotalHeight = this.height - 161;
        float visiblePercentage = (float) visibleRows / totalRows;

        this.scrollbarHeight = Math.max((int) (visiblePercentage * scrollBarTotalHeight), 15);
        this.scrollbarY = 93 + (int) ((float) this.scrollOffset / this.maxScrollOffset * (scrollBarTotalHeight - this.scrollbarHeight));
    }


    private void drawMainContainer(DrawContext context) {
        // Texture Identifier moved here
        Identifier guiTexture = Identifier.of("civiliansmod", "textures/gui/gui.png");

        // Define the container size (ensure it matches the dimensions of 'gui.png')
        int containerWidth = 256; // Width of 'gui.png'
        int containerHeight = 166; // Height of 'gui.png'

        // Calculate the position to center the container on the screen
        int containerX = (this.width - containerWidth) / 2;
        int containerY = (this.height - containerHeight) / 2;

        // Draw the container texture (centered)
        context.drawTexture(guiTexture, containerX, containerY, 0, 0, containerWidth, containerHeight, containerWidth, containerHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render the default elements
        super.render(context, mouseX, mouseY, delta);

        // Render the custom GUI container (Your GUI background)
        this.drawMainContainer(context);

        // Center text
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Civilian Customizer"), this.width / 2, 20, 0xFFFFFF);

        // Render center preview and variants
        renderCenterPreview(context);

        if (isDefaultTab) {
            renderVariants(context, mouseX, mouseY, delta, true, scrollOffset, this.width / 2 - COLUMN_WIDTH - 50);
        } else {
            renderVariants(context, mouseX, mouseY, delta, false, scrollOffset, this.width / 2 + 50);
        }

        // Render the scroll bar
        renderVanillaScrollBar(context);

        // Render the name input field
        this.nameInputField.render(context, mouseX, mouseY, delta);

        // Render the buttons last to bring them to the front
        for (var button : this.children()) {
            if (button instanceof ButtonWidget) {
                ((ButtonWidget) button).render(context, mouseX, mouseY, delta);
            }
        }
    }
    @Override
    protected void init() {
        super.init();

        // Add Default tab button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Default"), button -> {
                    isDefaultTab = true; // Switch to Default tab
                    scrollOffset = 0; // Reset scrolling for the tab
                    updateScrollBarDimensions(); // Recalculate scroll bars for the current tab
                }).dimensions(this.width / 2 - 46, 78,40,12).build()
        );

        // Add Slim tab button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Slim"), button -> {
                    isDefaultTab = false; // Switch to Slim tab
                    scrollOffset = 0; // Reset scrolling for the tab
                    updateScrollBarDimensions(); // Recalculate scroll bars for the current tab
                }).dimensions(this.width / 2 , 78, 40, 12).build()
        );

        String currentName = npc.getCustomName() != null ? npc.getCustomName().getString() : ""; // Use NPC's current name or empty string
        this.nameInputField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 - 122, 80, 60, 14, Text.literal("Enter NPC Name")
        );
        this.nameInputField.setText(currentName); // Pre-fill the text field with the NPC's current name
        this.nameInputField.setMaxLength(32); // Limit to 32 characters
        this.addSelectableChild(this.nameInputField);

        int totalRows = 22; // Default + Slim = 21 rows for each panel
        int visibleRows = (this.height - 100) / ENTITY_SPACING; // Rows that fit on screen at once

        // maxScrollOffset is based on rows that are not visible
        this.maxScrollOffset = Math.max(0, (totalRows - visibleRows) * ENTITY_SPACING);

        // Update scroll bar dimensions
        updateScrollBarDimensions();
    }

    @Override
    public void close() {
        if (MinecraftClient.getInstance().player != null) {

            NPCDataPayload payload = new NPCDataPayload(
                    npc.getUuid(),
                    nameInputField.getText(),
                    npc.getVariant()
            );
            ClientPlayNetworking.send(payload);
        }
        super.close();
    }
    private void renderVanillaScrollBar(DrawContext context) {
        int scrollBarX = 198; // X position (leftmost side of the screen)
        int scrollBarY = 93; // Start of the scrollable area
        int scrollBarHeight = this.height - 161; // Total height available for scrolling

        // Draw the scroll bar track (background) - dark gray
        context.fill(scrollBarX, scrollBarY, scrollBarX + 6, scrollBarY + scrollBarHeight, 0xFF202020); // Dark gray background

        // Calculate the position and size of the scroll handle
        int handleTop = this.scrollbarY; // Dynamic Y position of the handle
        int handleHeight = this.scrollbarHeight; // Height of the scroll handle

        // Draw the scroll handle (beveled edges)
        context.fill(scrollBarX + 1, handleTop, scrollBarX + 5, handleTop + handleHeight, 0xFFAAAAAA); // Light gray center
        context.fill(scrollBarX, handleTop, scrollBarX + 1, handleTop + handleHeight, 0xFF888888); // Left edge (darker)
        context.fill(scrollBarX + 5, handleTop, scrollBarX + 6, handleTop + handleHeight, 0xFF888888); // Right edge (darker)
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Scroll bar position variables
        int scrollBarX = 198;

        // Check if clicking within the scroll handle
        if (mouseX >= scrollBarX && mouseX <= scrollBarX + 6 && mouseY >= this.scrollbarY && mouseY <= this.scrollbarY + this.scrollbarHeight) {
            this.isScrolling = true;

            // Capture the click offset within the scroll handle
            this.scrollbarGrabOffset = (int) (mouseY - this.scrollbarY);
            return true;
        }

        // Check if a model is clicked
        if (button == 0) { // Left mouse button
            int lineX = this.width / 2;

            // Determine the X position of the correct panel (Default or Slim)
            int panelX = isDefaultTab ? (lineX - COLUMN_WIDTH - 50) : (lineX + 50);

            // Detect which variant is clicked based on the selected tab
            int clickedVariant = detectClickedVariant(mouseX, mouseY, panelX, isDefaultTab);

            if (clickedVariant != -1) {
                this.selectedVariant = clickedVariant;
                this.npc.setVariant(clickedVariant); // Update NPC variant immediately
                npc.writeCustomDataToNbt(npc.writeNbt(new NbtCompound()));

                // Save changes to ensure they persist
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }


    private int detectClickedVariant(double mouseX, double mouseY, int panelX, boolean isDefault) {
        int startVariantIndex = isDefault ? 0 : 44; // Start index based on tab
        int endVariantIndex = isDefault ? 43 : 87; // End index based on tab

        int startY = 115; // Starting Y position for skins
        int columnWidth = (COLUMN_WIDTH / 3) - 10; // Match render calculation
        int columnOffset = 5; // Match spacing calculation from renderVariants

        // Align the panelX based on the tab
        panelX += isDefault ? 130 : -100; // Adjust panelX just like in renderVariants

        // Add offsets for fine-tuning the clickable area's position
        int xRightOffset = 15;
        int yUpOffset = -19;

        // Loop through the variants in the tab
        for (int i = startVariantIndex; i <= endVariantIndex; i++) {
            int rowIndex = (i - startVariantIndex) / 3; // Determine the row
            int columnIndex = (i - startVariantIndex) % 3; // Determine the column

            int xPosition = panelX + columnIndex * (columnWidth + columnOffset) + xRightOffset; // X position in the row
            int yPosition = startY + rowIndex * ENTITY_SPACING - scrollOffset + yUpOffset; // Y position (moved up)

            // **New Visibility Check**
            // Skip rows that are out of view (same condition as rendering logic)
            if (yPosition + ENTITY_SPACING < 150 || yPosition > this.height - 100) {
                continue; // Skip this variant – it is off-screen
            }

            // Adjust the click area to better match the rendered entity sizes
            int hoverBoxWidth = columnWidth - 10; // Slightly smaller than the column width
            int hoverBoxHeight = ENTITY_SPACING - 10; // Slightly smaller than row spacing

            // Check if the mouse overlaps this hover box
            if (mouseX >= xPosition && mouseX <= xPosition + hoverBoxWidth &&
                    mouseY >= yPosition && mouseY <= yPosition + hoverBoxHeight) {
                return i; // Return the clicked variant
            }
        }
        return -1; // No variant clicked
    }


    private void renderCenterPreview(DrawContext context) {
        // Determine which skin/variant to preview
        int variantToRender = (selectedVariant == -1) ? originalVariant : selectedVariant;

        // Create the preview NPC entity with the selected skin/variant
        NPCEntity previewNPC = createPreviewNPC(variantToRender);

        // GUI size and position
        int guiWidth = 256;
        int guiHeight = 166;
        int guiX = (this.width - guiWidth) / 2;
        int guiY = (this.height - guiHeight) / 2;

        // Adjust preview position to be "middle-left" within the GUI
        int previewX = guiX + 36; // Position inside the GUI on the left side
        int previewY = guiY + (guiHeight / 2) + 35; // Center vertically with slight downward offset

        renderEntity(context.getMatrices(), previewX, previewY, 35, previewNPC);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.isScrolling) {
            // Start of scroll bar and total height available for the track
            int scrollBarY = 93;
            int scrollBarHeight = this.height - 161;

            // Adjust relativeY to account for the grab offset
            float relativeY = (float) (mouseY - scrollBarY - this.scrollbarGrabOffset);
            float scrollPercent = relativeY / (scrollBarHeight - this.scrollbarHeight);

            // Calculate new scrollOffset and clamp to the nearest row


            this.scrollOffset = Math.max(0, Math.min((int) (scrollPercent * maxScrollOffset), maxScrollOffset));

            // Snap scroll offset to the nearest row
            this.scrollOffset = (this.scrollOffset / ENTITY_SPACING) * ENTITY_SPACING;

            updateScrollBarDimensions();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isScrolling = false;

        // Reset the grab offset after releasing the scroll bar
        this.scrollbarGrabOffset = 0;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void renderVariants(DrawContext context, int mouseX, int mouseY, float ignoredDelta, boolean isDefault, int scrollOffset, int panelX) {
        int startY = 115;

        // Adjust panelX if Default tab is selected
        if (isDefault) {
            panelX += 130; // Move Default models to the right if needed
        }
    else {
        panelX -=  100;
        }
        // Adjust spacing for columns to make them closer together
        int columnWidth = (COLUMN_WIDTH / 3) - 10; // Reduce width slightly to bring columns closer together (use `-10` as an offset)
        int columnOffset = 5; // Fine tune additional space between columns (optional)

        // Render variants in the correct range
        int startVariantIndex = isDefault ? 0 : 44;
        int endVariantIndex = isDefault ? 43 : 87;

        for (int i = startVariantIndex; i <= endVariantIndex; i++) {
            int rowIndex = (i - startVariantIndex) / 3; // Divide models into groups of 3 for rows
            int columnIndex = (i - startVariantIndex) % 3; // Determine which column the model should be in
            int xPosition = panelX + columnIndex * (columnWidth + columnOffset); // Adjust position with reduced spacing
            int yPosition = startY + rowIndex * ENTITY_SPACING - scrollOffset; // Vertical position

            // Skip rows out of visible range
            if (yPosition + ENTITY_SPACING < 150 || yPosition > this.height - 60) {
                continue; // Skip rows that are out of bounds or render above the threshold
            }

            // Render the model for the current variant
            renderVariantPreview(context, xPosition, yPosition, i, mouseX, mouseY);

        }
    }

    private void renderVariantPreview(DrawContext context, int x, int y, int variantIndex, int mouseX, int mouseY) {
        if (variantIndex > 87) return; // Skip invalid indices
        NPCEntity previewNPC = createPreviewNPC(variantIndex);

        // Render the entity first
        renderEntity(context.getMatrices(), x + ENTITY_PREVIEW_SIZE, y + (ENTITY_SPACING / 2), ENTITY_PREVIEW_SIZE, previewNPC);

        // Adjust the hover box dimensions
        int adjustedX = x + 6; // Narrow the hover box by reducing 1 pixel from the left
        int adjustedY = y - 22; // Move the top of the box higher
        int entityWidth = (ENTITY_PREVIEW_SIZE * 2) - 12; // Reduce the width by 2 pixels
        int entityHeight = ENTITY_SPACING -3 ; // Reduce the height to stop the bottom from going too low

        // Check if the mouse is hovering over this variant
        if (mouseX >= adjustedX && mouseX <= adjustedX + entityWidth
                && mouseY >= adjustedY && mouseY <= adjustedY + entityHeight) {
            // Draw a white rectangle outline around the entity preview by filling in each edge
            int outlineThickness = 1; // Thickness of the outline

            // Top border
            context.fill(adjustedX, adjustedY,
                    adjustedX + entityWidth, adjustedY + outlineThickness,
                    0xFFFFFFFF);
            // Bottom border
            context.fill(adjustedX, adjustedY + entityHeight - outlineThickness,
                    adjustedX + entityWidth, adjustedY + entityHeight,
                    0xFFFFFFFF);
            // Left border
            context.fill(adjustedX, adjustedY,
                    adjustedX + outlineThickness, adjustedY + entityHeight,
                    0xFFFFFFFF);
            // Right border
            context.fill(adjustedX + entityWidth - outlineThickness, adjustedY,
                    adjustedX + entityWidth, adjustedY + entityHeight,
                    0xFFFFFFFF);
        }
    }

    private NPCEntity createPreviewNPC(int variantIndex) {
        World world = MinecraftClient.getInstance().world;

        @SuppressWarnings("unchecked")// Create a new preview NPC
        NPCEntity previewNPC = new NPCEntity((EntityType<? extends PathAwareEntity>) npc.getType(), world );

        // Set the variant to the current index (this determines slim/default model)
        previewNPC.setVariant(variantIndex);

        // These properties disable animations and sounds during preview
        previewNPC.setAiDisabled(true);
        previewNPC.setSilent(true);
        previewNPC.setHeadYaw(0.0F);

        return previewNPC;
    }

    private void renderEntity(MatrixStack matrices, int x, int y, int scale, NPCEntity entity) {
        EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();

        matrices.push();
        matrices.translate(x, y, 10.0);
        matrices.scale(scale, scale, scale);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0F));

        dispatcher.render(
                entity,
                0.0,
                0.0,
                0.0,
                0.0F,
                1.0F,
                matrices,
                MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(),
                15728880
        );

        matrices.pop();
    }

}