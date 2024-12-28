package net.asian.civiliansmod.renderer;

import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.model.NPCModel;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class NPCRenderer extends MobEntityRenderer<NPCEntity, NPCModel<NPCEntity>> {
    // Entity model layers for default and slim models
    public static final EntityModelLayer DEFAULT_ENTITY_MODEL_LAYER =
            new EntityModelLayer(Identifier.of("civiliansmod", "npc_default"), "main");
    public static final EntityModelLayer SLIM_ENTITY_MODEL_LAYER =
            new EntityModelLayer(Identifier.of("civiliansmod", "npc_slim"), "main");

    // Cached models for performance
    private final NPCModel<NPCEntity> defaultModel;
    private final NPCModel<NPCEntity> slimModel;

    // Constructor
    public NPCRenderer(EntityRendererFactory.Context context) {
        // Set the default model and shadow size
        super(context, new NPCModel<>(context.getPart(DEFAULT_ENTITY_MODEL_LAYER), false), 0.5F);

        // Cache both default and slim models for reuse
        this.defaultModel = new NPCModel<>(context.getPart(DEFAULT_ENTITY_MODEL_LAYER), false);
        this.slimModel = new NPCModel<>(context.getPart(SLIM_ENTITY_MODEL_LAYER), true);
    }

    /**
     * Dynamically assigns the appropriate texture based on the NPC's variant.
     */
    @Override
    public Identifier getTexture(NPCEntity entity) {
        int variant = entity.getVariant();

        // Map variants to textures
        return switch (variant) {
            case 0 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_0.png");
            case 1 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_1.png");
            case 2 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_2.png");
            case 3 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_3.png");
            case 4 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_4.png");
            case 5 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_5.png");
            case 6 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_6.png");
            case 7 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_7.png");
            case 8 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_8.png");
            case 9 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_9.png");
            case 10 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_10.png");
            case 11 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_11.png");
            case 12 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_12.png");
            case 13 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_13.png");
            case 14 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_14.png");
            case 15 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_15.png");
            case 16 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_16.png");
            case 17 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_17.png");
            case 18 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_18.png");
            case 19 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_19.png");
            case 20 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_20.png");
            case 21 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_21.png");
            case 22 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_22.png");
            case 23 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_23.png");
            case 24 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_24.png");
            case 25 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_25.png");

            case 26 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_0.png");
            case 27 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_1.png");
            case 28 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_2.png");
            case 29 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_3.png");
            case 30 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_4.png");
            case 31 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_5.png");
            case 32 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_6.png");
            case 33 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_7.png");
            case 34 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_8.png");
            case 35 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_9.png");
            case 36 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_10.png");
            case 37 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_11.png");
            case 38 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_12.png");
            case 39 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_13.png");
            case 40 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_14.png");
            case 41 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_15.png");
            case 42 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_16.png");
            case 43 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_17.png");
            case 44 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_18.png");
            case 45 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_19.png");
            case 46 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_20.png");
            case 47 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_21.png");
            case 48 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_22.png");
            case 49 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_23.png");
            case 50 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_24.png");
            case 51 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_25.png");
            default -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_0.png"); // Fallback
        };
    }

    /**
     * Adjusts the rendering model (default vs slim) dynamically based on the entity's variant.
     */
    @Override
    public void render(
            NPCEntity entity,
            float entityYaw,
            float partialTicks,
            MatrixStack matrices,
            net.minecraft.client.render.VertexConsumerProvider vertexConsumers,
            int light) {
        // Determine the model to use based on variant (slim = variants 3–5)
        this.model = entity.isSlim() ? slimModel : defaultModel;

        // Render the entity using the selected model
        super.render(entity, entityYaw, partialTicks, matrices, vertexConsumers, light);
    }

    /**
     * Scales the NPC entity slightly for both slim and default models.
     */
    @Override
    protected void scale(NPCEntity entity, MatrixStack matrices, float amount) {
        float scale = 0.945F; // Uniform scaling for consistency
        matrices.scale(scale, scale, scale);
        super.scale(entity, matrices, amount);
    }
}