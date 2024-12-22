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

    // Store the EntityRendererFactory.Context in a private field for later use
    private final EntityRendererFactory.Context rendererContext;

    // Constructor
    public NPCRenderer(EntityRendererFactory.Context context) {
        super(context, createModel(context, false), 0.5F); // Default shadow size of 0.5
        this.rendererContext = context; // Save the context for later use
    }

    /**
     * Fetches and returns the appropriate texture for the NPCEntity.
     */
    @Override
    public Identifier getTexture(NPCEntity entity) {
        int variant = entity.getVariant(); // Fetch the variant to select texture

        // Assign textures for default and slim models
        return switch (variant) {
            case 0 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_0.png");
            case 1 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_1.png");
            case 2 -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_2.png");
            case 3 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_0.png");
            case 4 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_1.png");
            case 5 -> Identifier.of("civiliansmod", "textures/entity/npc/slim/slim_2.png");
            default -> Identifier.of("civiliansmod", "textures/entity/npc/default/default_0.png"); // Fallback
        };
    }

    /**
     * Creates the model dynamically based on whether the entity uses a slim variant or not.
     *
     * @param context RendererFactory context
     * @param slim    Whether the model is slim or default (true = slim)
     * @return A new NPCModel instance
     */
    private static NPCModel<NPCEntity> createModel(EntityRendererFactory.Context context, boolean slim) {
        return new NPCModel<>(context.getPart(slim ? SLIM_ENTITY_MODEL_LAYER : DEFAULT_ENTITY_MODEL_LAYER), slim);
    }

    /**
     * Adjusts the scaling for the NPC entity before rendering.
     */
    @Override
    protected void scale(NPCEntity entity, MatrixStack matrices, float amount) {
        float scale = 0.945F; // Consistent scaling for both slim and default models
        matrices.scale(scale, scale, scale);

        super.scale(entity, matrices, amount);
    }

    /**
     * Overridden render method to dynamically assign the slim or default model.
     * This ensures the correct model type is used based on the texture selected.
     */
    @Override
    public void render(NPCEntity entity, float entityYaw, float partialTicks, MatrixStack matrices,
                       net.minecraft.client.render.VertexConsumerProvider vertexConsumers, int light) {
        // Determine if the entity uses a slim model (variants 3, 4, 5 are slim)
        boolean slim = entity.getVariant() >= 3 && entity.getVariant() <= 5;

        // Use the rendererContext field to access the context and create the appropriate model
        this.model = createModel(this.rendererContext, slim);

        super.render(entity, entityYaw, partialTicks, matrices, vertexConsumers, light);
    }
}