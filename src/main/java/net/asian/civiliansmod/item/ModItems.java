package net.asian.civiliansmod.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups; // Correct creative tabs
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    // Helper method to register items
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of("civiliansmod:" + name), item);
    }

    // Call this method to initialize items
    public static void registerModItems() {
        System.out.println("Registering Mod Items for CiviliansMod");

        // Add additional item registrations here later if needed
    }
}