package net.asian.civiliansmod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.asian.civiliansmod.gui.CustomNPCScreen;
import net.minecraft.client.MinecraftClient;

public class NPCEntity extends PathAwareEntity {
    // DataTracker key for the variant
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(
            NPCEntity.class,
            TrackedDataHandlerRegistry.INTEGER
    );

    private float targetYaw = 0.0F; // The yaw to smoothly rotate towards
    private boolean isTurning = false; // Whether the NPC is currently in the process of turning

    public NPCEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);

        if (!this.getWorld().isClient) { // Server-side variant assignment
            // Randomly assign the variant (0–2 = default, 3–5 = slim)
            int variant = this.random.nextInt(53);
            this.setVariant(variant); // Update DataTracker value with assigned variant

            // Assign default model and slim model names to the entity
            String[] defaultModelNames = { "Charles", "Cade", "Henry", "Liam", "Rodney", "Nathaniel", "Elliot", "Julian", "Malcolm", "Tobias", "Wesley", "Felix", "Desmond", "Simon", "Miles", "Everett", "Dorian", "Quentin", "Cedric", "Adrian", "Roman", "Marcus", "Gideon", "Levi", "Jasper" };
            String[] slimModelNames = { "Evelyn", "Sarah", "Olivia", "Emma", "Alexia", "Amelia", "Celeste", "Lillian", "Joleen", "Rosalie", "Clara", "Vivienne", "Elena", "Margot", "Nora", "Daphne", "Fiona", "Genevieve", "Juliette", "Lucille", "Naomi", "Ivy", "Serena", "Vera", "Adelaide" };

            if (variant >= 0 && variant <= 25) {  // Default models: Variants 0, 1, 2
                String randomName = defaultModelNames[this.random.nextInt(defaultModelNames.length)];
                this.setCustomName(Text.literal(randomName));
                System.out.println("Assigned 'default' name: " + randomName + " to variant: " + variant);
            } else if (variant >= 26 && variant <= 51) {  // Slim models: Variants 3, 4, 5
                String randomName = slimModelNames[this.random.nextInt(slimModelNames.length)];
                this.setCustomName(Text.literal(randomName));
                System.out.println("Assigned 'slim' name: " + randomName + " to variant: " + variant);
            }

            this.setCustomNameVisible(true);  // Ensure name is visible
        }
    }


    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, 0); // Default initialized to variant 0
    }

    // Getter for the variant
    public int getVariant() {
        return this.dataTracker.get(VARIANT);
    }

    // Setter for the variant
    public void setVariant(int variant) {
        this.dataTracker.set(VARIANT, variant);
    }

    public void setCustomName(Text name) {
        super.setCustomName(name); // Call super to update name
        // This ensures the name change is tracked and saved
    }

    // Helper method to determine if the current variant is slim
    public boolean isSlim() {
        return this.getVariant() >= 26 && this.getVariant() <= 51;
    }

    @Override
    public boolean cannotDespawn() {
        return true; // Prevents the entity from despawning
    }
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        // Save the variant to NBT
        nbt.putInt("Variant", this.getVariant());
        // Save the custom name to NBT
        if (this.hasCustomName()) {
            nbt.putString("CustomName", this.getCustomName().getString());
        } else {
            nbt.remove("CustomName"); // Clear old data if no name exists
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt); // Call parent to load standard entity data
        if (nbt.contains("Variant")) {
            this.setVariant(nbt.getInt("Variant")); // Load custom variant from NBT

        }

        // Load the custom name from NBT, if exists
        if (nbt.contains("CustomName")) {
            this.setCustomName(Text.literal(nbt.getString("CustomName")));
        }
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return PathAwareEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0) // 20HP
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3); // Normal speed
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new WanderAroundFarGoal(this, 0.6)); // Wander behavior
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean hurt = super.damage(source, amount);

        if (hurt && source.getAttacker() != null) {
            if (!this.getWorld().isClient()) {
                Text nameText = this.getCustomName();
                String npcName = nameText != null ? nameText.getString() : "NPC";

                String[] hitDialogues = {
                        "Ouch! That hurt!",
                        "Hey, watch it!",
                        "Why would you do that?!",
                        "Stop hitting me!",
                        "What’s wrong with you?",
                        "Please, don’t hurt me!",
                        "What have I done to deserve this?!",
                        "Fight me fair and square!",
                        "Watch it pal, you don't know who you're messing with.",
                        "Ow!",
                        "GET AWAY FROM ME!",
                        ";)",
                        "Why must this world cast unfortunate events upon me!",
                        "Hey...please stop I have already had a long day.",
                        "IF ONLY THERE WAS A HERO WHO COULD SAVE ME!",
                        "Lash your anger out on the sheep, not me!",
                        "I'm so sorry, I'm so sorry!",
                        "The prophecy foretold you would do this.",
                        "Friends shouldn't hurt other friends!"
                };

                String hitDialogue = hitDialogues[this.random.nextInt(hitDialogues.length)];

                if (source.getAttacker() instanceof PlayerEntity player) {
                    player.sendMessage(Text.literal(npcName + ": " + hitDialogue));
                }

                double dx = this.getX() - source.getAttacker().getX();
                double dz = this.getZ() - source.getAttacker().getZ();
                double fleeDistance = 12.0;

                this.getNavigation().startMovingTo(
                        this.getX() + dx * fleeDistance,
                        this.getY(),
                        this.getZ() + dz * fleeDistance,
                        1.2
                );
            }
        }

        return hurt;
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        // Ensure the interaction is in the main hand
        if (hand == Hand.MAIN_HAND) {
            // Check if the player is sneaking
            if (player.isSneaking()) {
                // Only proceed on the client side
                if (this.getWorld().isClient) {
                    MinecraftClient.getInstance().setScreen(new CustomNPCScreen(this));
                }
                return ActionResult.SUCCESS; // Indicate the interaction was handled
            }

            // (Optional) Normal interaction behavior if not sneaking
            if (!this.getWorld().isClient()) {
                this.getNavigation().stop();

                double dx = player.getX() - this.getX();
                double dz = player.getZ() - this.getZ();
                targetYaw = (float) (Math.atan2(dz, dx) * (180F / Math.PI)) - 90F;
                isTurning = true;

                Text nameText = this.getCustomName();
                String npcName = nameText != null ? nameText.getString() : "NPC";

                String[] dialogues = {
                        "Hello there, traveler! How can I help you?",
                        "I hope you're enjoying the day.",
                        "Stay safe—the world is dangerous.",
                        "There's treasure hidden nearby... or so I've heard.",
                        "Don't forget to stay out of trouble!",
                        "I'm here to help you, traveler.",
                        "What can I do for you?",
                        "I'm so hungry... Got any spare food?",
                        "I need to get my eyes checked, everything looks pixelated!",
                        "Sometimes it feels like I'm in a dream. I'm not sure what to do.",
                        "Hey! Can I help you something traveler?",
                        "Some would say the world is flat... can you believe that?",
                        "I don't have time to talk right now, I'm sorry!",
                        "Wow you look totally awesome, I might copy your look!",
                        "I need to find the hidden treasure, rumors have it that it's somewhere around here.",
                        "I love this place, it's a lot of fun to be here!",
                        "I hope someone got rid of that scary dragon... I'm sure it's not here anymore.",
                        "Want to go hunting with me?",
                        "Have you seen my friend? He's a little bit of a troublemaker.",
                        "Hopefully this place doesn't get too crowded...",
                        "I am surprised to see there are not more people here...",
                        "I'm so happy to see you, traveler!",
                        "When the birds sing, I can't help but sing along too.",
                        "I feel this unforgiving anger built up in my body! MUST... MUST... STOP!",
                        "Oop! Excuse me, let me just squeeze past ya",
                        "Darkness consumes me...",
                        "I AM SO HAPPY TO SEE YOU AGAIN! I LOVE YOU!",
                        "Hey, you're doing great"
                };

                String dialogue = dialogues[this.random.nextInt(dialogues.length)];
                player.sendMessage(Text.literal(npcName + ": " + dialogue));
            }
            return ActionResult.SUCCESS;
        }

        // Delegate to superclass for other interactions
        return super.interactMob(player, hand);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        // Handle smooth turning (called every tick)
        if (isTurning) {
            smoothTurnToTargetYaw();
        }
    }

    private void smoothTurnToTargetYaw() {
        float turnRate = 7.5F; // Amount to rotate per tick (increase for faster turning)
        float yawDifference = wrapDegrees(targetYaw - this.getYaw()); // Calculate the difference to the target yaw

        // Stop turning if we're very close to the target yaw
        if (Math.abs(yawDifference) < 1.0F) {
            this.setYaw(targetYaw); // Snap to the target
            this.bodyYaw = targetYaw;
            this.headYaw = targetYaw;
            isTurning = false; // We’re done turning
        } else {
            // Apply only part of the rotation to smooth out the turn
            float yawAdjustment = Math.min(turnRate, Math.max(-turnRate, yawDifference));
            this.setYaw(this.getYaw() + yawAdjustment);
            this.bodyYaw = this.getYaw();
            this.headYaw = this.getYaw();
        }
    }

    private float wrapDegrees(float degrees) {
        while (degrees >= 180.0F) {
            degrees -= 360.0F;
        }
        while (degrees < -180.0F) {
            degrees += 360.0F;
        }
        return degrees;
    }
}