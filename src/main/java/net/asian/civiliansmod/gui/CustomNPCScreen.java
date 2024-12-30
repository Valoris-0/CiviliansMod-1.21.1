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
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;

public class CustomNPCScreen extends Screen {
    private final NPCEntity npc;

    // Layout constants
    private static final int ENTITY_PREVIEW_SIZE = 20; // Downscaled preview
    private static final int ENTITY_SPACING = 60;     // Adjusted spacing
    private static final int COLUMN_WIDTH = 130;
    private int selectedVariant = -1; // No variant is selected by default
    private int scrollOffset = 0;  // Current scroll offset
    private int maxScrollOffset;  // Maximum allowed scroll offset
    private boolean isScrolling = false; // True if currently dragging the scrollbar
    private int scrollbarHeight = 0;
    private int scrollbarY = 0;

    private TextFieldWidget nameInputField;

    public CustomNPCScreen(NPCEntity npc) {
        super(Text.literal("Change NPC Variant"));
        this.npc = npc;
    }

    @Override
    protected void init() {
        // Existing UI setup
        String currentName = npc.getCustomName() != null ? npc.getCustomName().getString() : ""; // Use NPC's current name or empty string
        this.nameInputField = new TextFieldWidget(
                this.textRenderer,
                this.width / 2 - 100,
                40,
                200,
                20,
                Text.literal("Enter NPC Name")
        );
        this.nameInputField.setText(currentName); // Pre-fill the text field with the NPC's current name
        this.nameInputField.setMaxLength(32); // Limit to 32 characters
        this.addSelectableChild(this.nameInputField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Set Name"), button -> {
            String inputName = nameInputField.getText();
            if (!inputName.isEmpty()) {
                npc.setCustomName(Text.literal(inputName));
                npc.writeCustomDataToNbt(npc.writeNbt(new NbtCompound()));
            }
        }).dimensions(this.width / 2 - 50, 70, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Close"), button -> this.close())
                .dimensions(this.width / 2 - 75, this.height - 40, 150, 20)
                .build());

        // Calculate the maximum scroll offset correctly:
        // Each type has 13 rows (26 models split into pairs in two columns)
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
    private void updateScrollBarDimensions() {
        int scrollBarTotalHeight = this.height - 100; // Available height for the scroll bar track
        float visiblePercentage = (float) (this.height - 100) / ((this.maxScrollOffset + this.height - 100)); // Ratio of visible content

        // Calculate the height of the scroll handle
        this.scrollbarHeight = Math.max((int) (visiblePercentage * scrollBarTotalHeight), 15); // 15px minimum for usability

        // Calculate the top position of the scroll handle based on scrollOffset
        this.scrollbarY = 50 + (int) (((float) this.scrollOffset / this.maxScrollOffset) * (scrollBarTotalHeight - this.scrollbarHeight));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render background overlay
        context.fill(0, 0, this.width, this.height, 0x88000000); // Dark semi-transparent background
        super.render(context, mouseX, mouseY, delta);

        // Render the title text
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Enter Custom NPC Name:"), this.width / 2, 20, 0xFFFFFF);

        // Render the vertical line (center divider)
        int lineX = this.width / 2; // X coordinate for the center line
        context.fill(lineX - 1, 0, lineX + 1, this.height, 0x00FFFFFF); // Fully transparent color

        // Adjusted layout for left and right panels
        int leftPanelPadding = 100;
        int rightPanelPadding = 120;

        int leftPanelX = lineX - COLUMN_WIDTH - leftPanelPadding;
        int rightPanelX = lineX + rightPanelPadding;

        // Render Default Models (left panel)
        renderVariants(context, mouseX, mouseY, delta, true, scrollOffset, leftPanelX);

        // Render Slim Models (right panel)
        renderVariants(context, mouseX, mouseY, delta, false, scrollOffset, rightPanelX);

        // Render vanilla-like scroll bar
        renderVanillaScrollBar(context);

        // Render the name input field
        this.nameInputField.render(context, mouseX, mouseY, delta);

        // Render the selected model in the center of the screen
        renderCenterPreview(context);
    }

    private void renderVanillaScrollBar(DrawContext context) {
        int scrollBarX = 10; // X position (leftmost side of the screen)
        int scrollBarY = 50; // Start of the scrollable area
        int scrollBarHeight = this.height - 100; // Total height available for scrolling

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
        // Check if clicking the scroll bar
        int scrollBarX = 10;
        int scrollBarY = 50;
        int scrollBarHeight = this.height - 100;

        if (mouseX >= scrollBarX && mouseX <= scrollBarX + 6 && mouseY >= scrollBarY && mouseY <= scrollBarY + scrollBarHeight) {
            this.isScrolling = true; // Set scrolling to true
            return true;
        }

        // Check if a model is clicked
        if (button == 0) { // Left mouse button
            int lineX = this.width / 2;
            int leftPanelX = lineX - COLUMN_WIDTH - 100;
            int rightPanelX = lineX + 120;

            // Check for clicks on either Default or Slim panel
            int clickedVariant = detectClickedVariant(mouseX, mouseY, leftPanelX, true);
            if (clickedVariant == -1) {
                clickedVariant = detectClickedVariant(mouseX, mouseY, rightPanelX, false);
            }

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
        int startVariantIndex = isDefault ? 0 : 44;
        int endVariantIndex = isDefault ? 43 : 87;

        int startY = 50;

        mouseY -= -15;

        // Loop through all variants
        for (int i = startVariantIndex; i <= endVariantIndex; i += 2) {
            int rowIndex = (i - startVariantIndex) / 2;
            int yPosition = startY + rowIndex * ENTITY_SPACING - scrollOffset;

            if (yPosition + ENTITY_SPACING < 0 || yPosition > this.height) {
                continue; // Skip rows out of visible range
            }

            // Check if mouse is within the bounds of either column
            int columnWidthHalf = COLUMN_WIDTH / 2;
            if (mouseX >= panelX && mouseX <= panelX + columnWidthHalf && mouseY >= yPosition && mouseY <= yPosition + ENTITY_SPACING) {
                return i;
            } else if (mouseX >= panelX + columnWidthHalf && mouseX <= panelX + COLUMN_WIDTH && mouseY >= yPosition && mouseY <= yPosition + ENTITY_SPACING) {
                return i + 1;
            }
        }

        return -1; // No variant clicked
    }

    private void renderCenterPreview(DrawContext context) {
        if (selectedVariant != -1) {
            NPCEntity previewNPC = createPreviewNPC(selectedVariant); // Create an NPC with the selected variant

            int centerX = this.width / 2; // Center horizontally
            int centerY = this.height / 2 + 73; // Adjusted downward by 50 pixels

            renderEntity(context.getMatrices(), centerX, centerY, 55, previewNPC); // Render the NPC larger and lower
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.isScrolling) {
            int scrollBarY = 50; // Start of the scroll bar
            int scrollBarHeight = this.height - 100; // Available height for the scrollbar track

            // Calculate how far the user is dragging relative to the scroll track
            float relativeY = (float) (mouseY - scrollBarY);
            float scrollPercent = relativeY / (scrollBarHeight - this.scrollbarHeight);

            // Update scrollOffset and clamp it to valid values
            this.scrollOffset = Math.max(0, Math.min((int) (scrollPercent * maxScrollOffset), maxScrollOffset));
            updateScrollBarDimensions();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }


    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isScrolling = false; // Reset scrolling state
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void renderVariants(DrawContext context, int mouseX, int mouseY, float delta, boolean isDefault, int scrollOffset, int panelX) {
        String title = isDefault ? "Default Models" : "  Slim Models";
        context.drawTextWithShadow(
                this.textRenderer,
                title,
                panelX + (COLUMN_WIDTH / 2) - 50, // Center text within panel
                20,
                0xFFFFFF
        );

        int startY = 50; // Ensure models render below this Y-coordinate, adjust as needed to avoid overlapping the text

        // Render variants in the correct range
        int startVariantIndex = isDefault ? 0 : 44;
        int endVariantIndex = isDefault ? 43 : 87;

        for (int i = startVariantIndex; i <= endVariantIndex; i += 2) {
            int rowIndex = (i - startVariantIndex) / 2;
            int yPosition = startY + rowIndex * ENTITY_SPACING - scrollOffset;

            // Skip rows out of visible range or that would render above the cutoff
            if (yPosition + ENTITY_SPACING < 100 || yPosition > this.height) {
                continue; // Skip rows that are out of bounds or render above the threshold
            }

            renderVariantPreview(context, panelX, yPosition, i, mouseX, mouseY);
            renderVariantPreview(context, panelX + COLUMN_WIDTH / 2, yPosition, i + 1, mouseX, mouseY);
        }
    }

    private void renderVariantPreview(DrawContext context, int x, int y, int variantIndex, int mouseX, int mouseY) {
        if (variantIndex > 87) return; // Skip invalid indices
        NPCEntity previewNPC = createPreviewNPC(variantIndex);

        // Render the entity first
        renderEntity(context.getMatrices(), x + ENTITY_PREVIEW_SIZE, y + (ENTITY_SPACING / 2), ENTITY_PREVIEW_SIZE, previewNPC);

        // Adjust the hover box dimensions
        int hoverOffsetY = 12; // Lift the top higher by 12 pixels
        int adjustedX = x + 1; // Narrow the hover box by reducing 1 pixel from the left
        int adjustedY = y - hoverOffsetY; // Move the top of the box higher
        int entityWidth = (ENTITY_PREVIEW_SIZE * 2) - 2; // Reduce the width by 2 pixels
        int entityHeight = ENTITY_SPACING - hoverOffsetY; // Reduce the height to stop the bottom from going too low

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

        // Create a new preview NPC
        NPCEntity previewNPC = new NPCEntity((EntityType<? extends PathAwareEntity>) npc.getType(), world);

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