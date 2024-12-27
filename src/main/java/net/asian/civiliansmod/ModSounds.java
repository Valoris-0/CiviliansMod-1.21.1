package net.asian.civiliansmod;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries; // Correct imports
import net.minecraft.registry.Registry; // Correct imports

public class ModSounds {
    public static final Identifier NPC_CONVERSION_ID = Identifier.of("civiliansmod", "npc_conversion");
    public static final SoundEvent NPC_CONVERSION_SOUND = SoundEvent.of(NPC_CONVERSION_ID);

    public static void registerSounds() {
        // Register the sound event using SoundEvent registry
        Registry.register(Registries.SOUND_EVENT, NPC_CONVERSION_ID, NPC_CONVERSION_SOUND);
    }
}