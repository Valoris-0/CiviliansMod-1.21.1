package net.asian.civiliansmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.asian.civiliansmod.renderer.NPCRenderer;
import net.asian.civiliansmod.model.NPCModel;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class CiviliansModClient implements ClientModInitializer {

    // Define the NPC model layer
    public static final EntityModelLayer NPC_LAYER = new EntityModelLayer(Identifier.of("civiliansmod", "npc"), "main");

    @Override
    public void onInitializeClient() {
        // Register the NPC entity renderer
        EntityRendererRegistry.register(CiviliansMod.NPC_ENTITY, NPCRenderer::new);

        // Register the NPC model layer, using the custom NPCModel
        EntityModelLayerRegistry.registerModelLayer(NPC_LAYER, NPCModel::getTexturedModelData);
        System.out.println("Registering model layer: " + NPC_LAYER.toString());
        System.out.println("[CiviliansMod] Client Initialized");
    }
}