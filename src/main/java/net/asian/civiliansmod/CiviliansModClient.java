package net.asian.civiliansmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.asian.civiliansmod.renderer.NPCRenderer;
import net.asian.civiliansmod.model.NPCModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class CiviliansModClient implements ClientModInitializer {

    // Define the NPC model layer
    public static final EntityModelLayer DEFAULT_ENTITY_MODEL_LAYER =
            new EntityModelLayer(Identifier.of("civiliansmod", "npc_default"), "main");
    public static final EntityModelLayer SLIM_ENTITY_MODEL_LAYER =
            new EntityModelLayer(Identifier.of("civiliansmod", "npc_slim"), "main");

    @Override
    public void onInitializeClient() {
        // Register the renderer for the entity
        EntityRendererRegistry.register(CiviliansMod.NPC_ENTITY, NPCRenderer::new);

        // Register BOTH model layers with the correct TexturedModelData
        EntityModelLayerRegistry.registerModelLayer(DEFAULT_ENTITY_MODEL_LAYER, () -> NPCModel.getTexturedModelData(false));
        EntityModelLayerRegistry.registerModelLayer(SLIM_ENTITY_MODEL_LAYER, () -> NPCModel.getTexturedModelData(true));

        CiviliansMod.LOGGER.info("[CiviliansMod] Model layers registered!");
    }
}