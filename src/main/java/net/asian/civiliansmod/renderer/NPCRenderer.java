package net.asian.civiliansmod.renderer;

import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.model.NPCModel;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class NPCRenderer extends MobEntityRenderer<NPCEntity, NPCModel<NPCEntity>> {

    // Single texture path
    private static final Identifier TEXTURE = Identifier.of("civiliansmod", "textures/entity/npc/npc_2.png");

    // Entity model layer definition
    public static final EntityModelLayer ENTITY_MODEL_LAYER =
            new EntityModelLayer(Identifier.of("civiliansmod", "npc"), "main");

    // Class-level context for reuse
    private final EntityRendererFactory.Context context;

    public NPCRenderer(EntityRendererFactory.Context context) {
        super(context, createModel(context), 0.5F); // Default shadow size of 0.5
        this.context = context; // Save the context for later use
    }

    /**
     * Dynamically creates the NPC model, always using non-slim arms.
     * @param context Renderer factory context.
     * @return The NPCModel instance.
     */
    private static NPCModel<NPCEntity> createModel(EntityRendererFactory.Context context) {
        return new NPCModel<>(context.getPart(ENTITY_MODEL_LAYER));
    }

    @Override
    public Identifier getTexture(NPCEntity entity) {
        return TEXTURE; // Always return a static texture
    }

    @Override
    protected void scale(NPCEntity entity, MatrixStack matrices, float amount) {
        float scale = 1.0F; // Default to normal scaling
        matrices.scale(scale, scale, scale);

        super.scale(entity, matrices, amount); // Call parent scale method
    }
}