package net.asian.civiliansmod.model;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;

public class NPCModel<T extends LivingEntity> extends PlayerEntityModel<T> {

    public NPCModel(ModelPart root) {
        super(root, false); // Always uses the default Steve model (not slim)
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();

        // Head part (including hat as a sibling)
        root.addChild(
                "head",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F)
        );

        root.addChild(
                "hat",
                ModelPartBuilder.create()
                        .uv(32, 0)
                        .cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.5F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F)
        );

        // Ear and Cloak parts (empty)
        root.addChild("ear", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        root.addChild("cloak", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        // Body
        root.addChild(
                "body",
                ModelPartBuilder.create()
                        .uv(16, 16)
                        .cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F)
        );

        // Right Arm
        root.addChild(
                "right_arm",
                ModelPartBuilder.create()
                        .uv(40, 16)
                        .cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(-5.0F, 2.0F, 0.0F)
        );

        // Left Arm
        root.addChild(
                "left_arm",
                ModelPartBuilder.create()
                        .uv(40, 16)
                        .mirrored()
                        .cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(5.0F, 2.0F, 0.0F)
        );

        // Right Leg
        root.addChild(
                "right_leg",
                ModelPartBuilder.create()
                        .uv(0, 16)
                        .cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(-1.9F, 12.0F, 0.0F)
        );

        // Left Leg
        root.addChild(
                "left_leg",
                ModelPartBuilder.create()
                        .uv(0, 16)
                        .mirrored()
                        .cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                ModelTransform.pivot(1.9F, 12.0F, 0.0F)
        );

        // Right and Left Sleeves (empty placeholders for now)
        root.addChild("right_sleeve", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        root.addChild("left_sleeve", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        // Pants (empty placeholder for now)
        root.addChild("right_pants", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
        root.addChild("left_pants", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        // Jacket (empty placeholder for now)
        root.addChild("jacket", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }
}