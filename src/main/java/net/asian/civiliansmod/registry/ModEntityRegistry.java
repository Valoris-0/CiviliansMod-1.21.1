package net.asian.civiliansmod.registry;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.CiviliansMod;

public class ModEntityRegistry {
    // Register the NPC_ENTITY with the Registry system
    public static final EntityType<NPCEntity> NPC_ENTITY = Registry.register(
            Registries.ENTITY_TYPE, // Use Registries.ENTITY_TYPE for modern versions
            Identifier.of(CiviliansMod.MOD_ID, "npc"), // Namespace and entity name
            EntityType.Builder.create(NPCEntity::new, SpawnGroup.MISC) // Entity logic and type group
                    .dimensions(0.6F, 1.8F)
                    .build("npc")
    );

    public static void registerModEntities() {
        // Log successful registration
        CiviliansMod.LOGGER.info("Registering Mod Entities for " + CiviliansMod.MOD_ID);
    }
}