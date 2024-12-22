package net.asian.civiliansmod;

import net.asian.civiliansmod.entity.NPCEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CiviliansMod implements ModInitializer {

    public static final String MOD_ID = "civiliansmod"; // Unique mod ID
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID); // Initialize Logger
    // Registering NPC Entity
    public static final EntityType<NPCEntity> NPC_ENTITY = Registry.register(
            Registries.ENTITY_TYPE, // Use updated 'Registries.ENTITY_TYPE'
            Identifier.of(MOD_ID, "npc"), // Identifier: 'civiliansmod:npc'
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, NPCEntity::new) // Updated to provide SpawnGroup and Entity Factory
                    .dimensions(EntityDimensions.changing(0.6f, 1.8f)) // Use 'changing()' for dynamic entity dimensions
                    .build() // Finalize the EntityType building
    );

    @Override
    public void onInitialize() {
        // Register default attributes for the NPC Entity
        FabricDefaultAttributeRegistry.register(NPC_ENTITY, NPCEntity.createAttributes());
        ModItems.registerModItems();

        System.out.println("[CiviliansMod] NPC Entity has been registered successfully!");
    }
}