package net.asian.civiliansmod;

import net.asian.civiliansmod.entity.NPCEntity;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class NPCConversionHandler {

    // Register the event listener during mod initialization
    public static void register() {
        UseEntityCallback.EVENT.register(NPCConversionHandler::onEntityInteract);
    }

    // Event callback for entity interaction
    private static ActionResult onEntityInteract(net.minecraft.entity.player.PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {

        // Ensure the interaction is server-side
        if (!world.isClient && hand == Hand.MAIN_HAND) {
            // Check if the entity is an unassigned Villager
            if (entity.getType() == EntityType.VILLAGER && entity instanceof VillagerEntity villager) {
                // Only allow if Villager has no profession
                if (villager.getVillagerData().getProfession() == VillagerProfession.NONE) {
                    // Check if the player is holding the NPC Totem
                    ItemStack heldItem = player.getStackInHand(hand);
                    if (heldItem.isOf(ModItems.NPC_TOTEM)) {
                        // Perform conversion logic
                        if (world instanceof ServerWorld serverWorld) {
                            convertVillagerToNPC(villager, serverWorld);

                            // Decrement the NPC Totem item by 1
                            if (!player.isCreative()) {
                                heldItem.decrement(1);
                            }
                            return ActionResult.SUCCESS; // Indicate the action was handled
                        }
                    }
                } else {
                    // Notify the player that they cannot convert this Villager
                    player.sendMessage(
                            net.minecraft.text.Text.literal("This Villager is connected to a job site and cannot be converted."),
                            true // Show the message in the action bar
                    );
                }
            }
        }

        return ActionResult.PASS; // Let other interactions proceed
    }

    private static void convertVillagerToNPC(VillagerEntity villager, ServerWorld world) {
        // Create a new instance of NPCEntity
        NPCEntity npcEntity = CiviliansMod.NPC_ENTITY.create(world);

        if (npcEntity != null) {

            npcEntity.refreshPositionAndAngles(villager.getX(), villager.getY(), villager.getZ(), villager.getYaw(), villager.getPitch());

            // Copy over relevant villager properties
            if (villager.hasCustomName()) {
                npcEntity.setCustomName(villager.getCustomName());
                npcEntity.setCustomNameVisible(villager.isCustomNameVisible());
            }

            // Add rainbow particle effects at the Villager's location
            spawnRainbowParticles(villager.getX(), villager.getY() + 1, villager.getZ(), world);

            // Play a custom sound effect for the transformation (replace with your own sound event)
            world.playSound(
                    null, // Null means all nearby players hear it
                    villager.getBlockPos(), // Location of the sound
                    ModSounds.NPC_CONVERSION_SOUND, // Example sound (replace as needed)
                    net.minecraft.sound.SoundCategory.PLAYERS, // Sound category
                    1.0f, // Volume
                    1.0f // Pitch
            );

            // Remove the villager from the world
            villager.discard();

            // Add the new NPC to the world
            world.spawnEntity(npcEntity);
        } else {
            // Provide feedback to the player if something goes wrong (e.g., spawning fails)
            System.err.println("Failed to convert Villager to NPCEntity!");
        }
    }

    // Spawn rainbow particles at the given position
    private static void spawnRainbowParticles(double x, double y, double z, ServerWorld world) {
        // Create rainbow particles using HSB (Hue, Saturation, Brightness)
        for (float hue = 0; hue <= 1; hue += 0.1f) {

            int color = java.awt.Color.HSBtoRGB(hue, 1.0F, 1.0F);
            float red = ((color >> 16) & 0xFF) / 255.0F;
            float green = ((color >> 8) & 0xFF) / 255.0F;
            float blue = (color & 0xFF) / 255.0F;


            Vector3f rgbColor = new Vector3f(red, green, blue);

            // Create the dust particle effect using the converted color
            DustParticleEffect rainbowParticle = new DustParticleEffect(
                    rgbColor,
                    1.0F // Particle scale
            );

            // Spawn multiple particles for the current hue
            world.spawnParticles(
                    rainbowParticle, // Particle effect
                    x, y, z,         // Position at Villager's location
                    10,              // Number of particles
                    0.5, 0.5, 0.5,   // Offset/spread (x, y, z)
                    0.01             // Speed
            );
        }
    }
}