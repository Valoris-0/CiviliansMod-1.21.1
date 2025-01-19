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
import net.minecraft.entity.Entity;

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
        // Container dimensions
        int containerWidth = 256;
        int containerHeight = 166;
        int containerX = (this.width - containerWidth) / 2;
        int containerY = (this.height - containerHeight) / 2;

        // Total rows and visible rows calculation
        int totalRows = 15; // Total number of rows
        int visibleRows = (containerHeight - 55) / ENTITY_SPACING; // Adjust relative to the container height

        this.maxScrollOffset = Math.max(0, (totalRows - visibleRows) * ENTITY_SPACING);

        // Scroll bar total height based on the container
        int scrollBarTotalHeight = containerHeight - 55; // Leave padding inside the container
        float visiblePercentage = (float) visibleRows / totalRows;

        // Scroll bar handle height and vertical position calculation
        this.scrollbarHeight = Math.max((int) (visiblePercentage * scrollBarTotalHeight), 15);
        this.scrollbarY = containerY + 40 + (int) ((float) this.scrollOffset / this.maxScrollOffset * (scrollBarTotalHeight - this.scrollbarHeight));
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
        renderCenterPreview(context, mouseX, mouseY);

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

        int containerWidth = 256;
        int containerHeight = 166;
        int containerX = (this.width - containerWidth) / 2;
        int containerY = (this.height - containerHeight) / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Default"), button -> {
            isDefaultTab = true;
            scrollOffset = 0;
            updateScrollBarDimensions();
        }).dimensions(containerX + 82, containerY + 24, 40, 12).build());

        // Add Slim tab button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Slim"), button -> {
            isDefaultTab = false;
            scrollOffset = 0;
            updateScrollBarDimensions();
        }).dimensions(containerX + 123, containerY + 24, 40, 12).build());


        String currentName = npc.getCustomName() != null ? npc.getCustomName().getString() : ""; // Use NPC's current name or empty string
        this.nameInputField = new TextFieldWidget(
                this.textRenderer,
                containerX + 5, containerY + 28, 62, 14, Text.literal("Enter NPC Name")
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
        int containerWidth = 256;
        int containerHeight = 166;
        int containerX = (this.width - containerWidth) / 2;
        int containerY = (this.height - containerHeight) / 2;

        int scrollBarX = containerX + 70; // Positioned near the right edge of the container
        int scrollBarY = containerY + 38; // Start 10 pixels below the top of the container
        int scrollBarHeight = containerHeight - 51; // Adjust for padding (20 pixels)

        context.fill(scrollBarX, scrollBarY, scrollBarX + 6, scrollBarY + scrollBarHeight, 0xFF202020);
        context.fill(scrollBarX + 1, this.scrollbarY, scrollBarX + 5, this.scrollbarY + this.scrollbarHeight, 0xFFAAAAAA);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Container dimensions
        int containerWidth = 256;
        int containerHeight = 166;
        int containerX = (this.width - containerWidth) / 2;
        int containerY = (this.height - containerHeight) / 2;

        // Scroll bar position
        int scrollBarX = containerX + 70; // Match `renderVanillaScrollBar`
        int scrollBarY = containerY + 40; // Match `renderVanillaScrollBar`

        // Check if clicking within the scroll handle
        if (mouseX >= scrollBarX && mouseX <= scrollBarX + 6 && mouseY >= this.scrollbarY && mouseY <= this.scrollbarY + this.scrollbarHeight) {
            this.isScrolling = true;

            // Capture the click offset within the scroll handle
            this.scrollbarGrabOffset = (int) (mouseY - this.scrollbarY);
            return true;
        }

        // Check if a model is clicked
        if (button == 0) { // Left mouse button
            int panelX = isDefaultTab ? (containerX + 10) : (containerX + COLUMN_WIDTH + 30);

            // Detect which variant is clicked based on the selected tab
            int clickedVariant = detectClickedVariant(mouseX, mouseY, panelX, isDefaultTab);

            if (clickedVariant != -1) {
                this.selectedVariant = clickedVariant;
                this.npc.setVariant(clickedVariant); // Update NPC variant immediately

                npc.writeCustomDataToNbt(npc.writeNbt(new NbtCompound())); // Save changes to ensure they persist
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }


    private int detectClickedVariant(double mouseX, double mouseY, int panelX, boolean isDefaultTab) {
        // Container dimensions
        int containerWidth = 255;
        int containerHeight = 166;
        int containerX = (this.width - containerWidth) / 2;
        int containerY = (this.height - containerHeight) / 2;

        // Starting variant index based on the active tab
        int startVariantIndex = isDefaultTab ? 0 : 44;
        int endVariantIndex = isDefaultTab ? 43 : 87;

        int startY = containerY + 39; // Matches where variants start rendering

        // Column setup
        int columnWidth = (COLUMN_WIDTH / 3) - 10; // Adjusted for columns in renderVariants
        int columnOffset = 5;

        // Fine-tune X offsets
        int xRightOffset = isDefaultTab ? 76 : -76; // Shift Slim tab to the left

        // Strict container boundaries
        if (mouseY < containerY || mouseY > containerY + containerHeight) {
            return -1; // Mouse click is entirely outside the vertical container area
        }

        // Loop through all rendered variants
        for (int i = startVariantIndex; i <= endVariantIndex; i++) {
            // Current variant's row and column
            int rowIndex = (i - startVariantIndex) / 3; // Determine row
            int columnIndex = (i - startVariantIndex) % 3; // Determine column

            // Variant's calculated position
            int xPosition = panelX + columnIndex * (columnWidth + columnOffset) + xRightOffset;
            int yPosition = startY + rowIndex * ENTITY_SPACING - scrollOffset;

            // Extra check: Skip rows rendered above the visible container
            if (yPosition < containerY || yPosition + ENTITY_SPACING > containerY + containerHeight) {
                continue; // Skip variants not actually visible
            }

            // Check if the mouse position falls within the variant's hover box
            if (mouseX >= xPosition && mouseX <= xPosition + columnWidth &&
                    mouseY >= yPosition && mouseY <= yPosition + ENTITY_SPACING) {
                return i; // Return the clicked variant index
            }
        }

        return -1; // No variant was clicked
    }


    private void renderCenterPreview(DrawContext context, int mouseX, int mouseY) {
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

        // Calculate head rotation to follow the mouse
        float deltaX = (float) (previewX - mouseX); // Invert the direction of movement on the X-axis
        float deltaY = (mouseY - previewY) + 50.0F;

        // Set head yaw (horizontal rotation) and pitch (vertical rotation) for more subtle movements
        float sensitivityFactor = 3.0F; // Higher value means more subtle movements
        float headYaw = ((float) Math.atan2(deltaX, 50.0) * (180F / (float) Math.PI)) / sensitivityFactor;
        float pitch = ((float) Math.atan2(deltaY, 50.0) * (180F / (float) Math.PI)) / sensitivityFactor;

        // Clamp the pitch to prevent extreme angles (e.g., head flipping)
        pitch = Math.max(-30.0F, Math.min(30.0F, pitch)); // Limits pitch to -30 to +30 degrees

        // Set the NPC's head rotation
        previewNPC.setHeadYaw(headYaw); // Adjust headYaw for smoother turning behavior
        previewNPC.setPitch(pitch);    // Vertical up-down movement adjustments

        // Render the entity
        renderEntity(context.getMatrices(), previewX, previewY, 35, previewNPC, 180.0F);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.isScrolling) {

            int containerWidth = 256;
            int containerHeight = 166;
            int containerX = (this.width - containerWidth) / 2;
            int containerY = (this.height - containerHeight) / 2;

            // Scroll bar position and height
            int scrollBarY = containerY + 40; // Match `renderVanillaScrollBar` and `updateScrollBarDimensions`
            int scrollBarHeight = containerHeight - 55;

            // Adjust relativeY to account for the grab offset
            float relativeY = (float) (mouseY - scrollBarY - this.scrollbarGrabOffset);
            float scrollPercent = relativeY / (scrollBarHeight - this.scrollbarHeight);

            // Calculate new scrollOffset and clamp
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
        // Container dimensions
        int containerWidth = 256;
        int containerHeight = 166;
        int containerX = (this.width - containerWidth) / 2;
        int containerY = (this.height - containerHeight) / 2;

        // Initial Y position relative to the container
        int startY = containerY + 59;


        panelX = containerX + 78; // Position Default tab models within the container

        // Adjust spacing for columns for better alignment
        int columnWidth = (COLUMN_WIDTH / 3) - 10; // Reduced width to bring columns closer
        int columnOffset = 5; // Fine-tune additional space between columns

        // Render variants in the correct range
        int startVariantIndex = isDefault ? 0 : 44;
        int endVariantIndex = isDefault ? 43 : 87;

        for (int i = startVariantIndex; i <= endVariantIndex; i++) {
            // Compute the row and column positions for each variant
            int rowIndex = (i - startVariantIndex) / 3; // Divide into groups of 3 per row
            int columnIndex = (i - startVariantIndex) % 3; // Determine which column the model is in
            int xPosition = panelX + columnIndex * (columnWidth + columnOffset); // Adjust horizontal position
            int yPosition = startY + rowIndex * ENTITY_SPACING - scrollOffset; // Adjust vertical position

            // Skip rows that are completely out of container bounds
            if (yPosition + ENTITY_SPACING < containerY || yPosition > containerY + containerHeight) {
                continue; // Don't render if the row is invisible
            }

            // Render the model for the current variant
            renderVariantPreview(context, xPosition, yPosition, i, mouseX, mouseY);
        }
    }

    private void renderVariantPreview(DrawContext context, int x, int y, int variantIndex, int mouseX, int mouseY) {
        if (variantIndex > 87) return; // Skip invalid indices
        NPCEntity previewNPC = createPreviewNPC(variantIndex);

        // Container dimensions
        int containerWidth = 256;
        int containerHeight = 166;
        int containerX = (this.width - containerWidth) / 2;
        int containerY = (this.height - containerHeight) / 2;

        // Clip rendering to the container bounds
        int minX = containerX;
        int maxX = containerX + containerWidth;
        int minY = containerY;
        int maxY = containerY + containerHeight;

        // Adjust the hover box dimensions
        int adjustedX = x + 6; // Narrow the hover box by reducing 1 pixel from the left
        int adjustedY = y - 21; // Move the top of the box higher
        int entityWidth = (ENTITY_PREVIEW_SIZE * 2) - 12; // Reduce the width by 2 pixels
        int entityHeight = ENTITY_SPACING - 6; // Reduce the height to stop the bottom from going too low

        // Ensure the variant preview stays within the container bounds
        if (adjustedX + entityWidth > maxX || adjustedX < minX) return; // Skip rendering if out of bounds horizontally
        if (adjustedY + entityHeight > maxY || adjustedY < minY) return; // Skip rendering if out of bounds vertically

        // Render the entity preview
        renderEntity(context.getMatrices(), x + ENTITY_PREVIEW_SIZE, y + (ENTITY_SPACING / 2), ENTITY_PREVIEW_SIZE, previewNPC, 145.0F);

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

    private void renderEntity(MatrixStack matrices, int x, int y, int scale, Entity entity, float rotation) {
        EntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();

        matrices.push();

        // Translate into GUI space (position the entity)
        matrices.translate(x, y, 50.0); // Depth is 50.0 to prevent clipping issues in GUI

        // Scale the entity down (so it fits the GUI)
        matrices.scale(scale, -scale, scale); // Note the negative Y scale to fix upside-down rendering

        // Rotate to face the player, add custom rotation
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F + rotation));

        // Render the entity with maximum brightness (to avoid dim lighting)
        int lightOverride = 15728880; // Max brightness (sky + block light)

        dispatcher.render(
                entity,
                0.0, // X position in world space
                0.0, // Y position in world space
                0.0, // Z position in world space
                0.0F, // No head yaw
                1.0F, // Partial tick (unused in GUI)
                matrices,
                MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers(),
                lightOverride // Ensure maximum brightness for rendering
        );

        matrices.pop();
    }

}