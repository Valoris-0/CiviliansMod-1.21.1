package net.asian.civiliansmod.renderer;

import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.model.NPCModel;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class NPCRenderer extends MobEntityRenderer<NPCEntity, NPCModel<NPCEntity>> {
    // Entity model layer definition
    public static final EntityModelLayer ENTITY_MODEL_LAYER =
            new EntityModelLayer(Identifier.of("civiliansmod", "npc"), "main");

    // Constructor
    public NPCRenderer(EntityRendererFactory.Context context) {
        super(context, createModel(context), 0.5F); // Default shadow size of 0.5
    }

    /**
     * Fetch and return the texture for the specified NPC entity.
     */
    @Override
    public Identifier getTexture(NPCEntity entity) {
        int variant = entity.getVariant(); // Fetch the variant from the entity
        return variant == 0
                ? Identifier.of("civiliansmod", "textures/entity/npc/variant_0.png")
                : Identifier.of("civiliansmod", "textures/entity/npc/variant_1.png");
    }

    /**
     * Creates the NPC model dynamically.
     * @param context Renderer factory context.
     * @return The NPCModel instance.
     */
    private static NPCModel<NPCEntity> createModel(EntityRendererFactory.Context context) {
        return new NPCModel<>(context.getPart(ENTITY_MODEL_LAYER));
    }

    /**
     * Adjust scaling for the NPC entity before rendering.
     */
    @Override
    protected void scale(NPCEntity entity, MatrixStack matrices, float amount) {
        float scale = switch (entity.getVariant()) {
            case 0 -> 0.945F;
            case 1 -> 0.945F;
            default -> 0.945F; // Default for new variants
        };
        matrices.scale(scale, scale, scale);

        super.scale(entity, matrices, amount);
    }
}