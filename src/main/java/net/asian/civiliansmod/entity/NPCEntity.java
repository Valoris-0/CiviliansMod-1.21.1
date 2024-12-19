package net.asian.civiliansmod.entity;

import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class NPCEntity extends PathAwareEntity {
    private float targetYaw = 0.0F; // The yaw to smoothly rotate towards
    private boolean isTurning = false; // Whether the NPC is currently in the process of turning

    public NPCEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);

        // Assign a random name when the NPC is spawned
        String[] names = {
                "Charles", "Evelyn", "Sarah", "James", "Henry",
                "Olivia", "Emma", "Liam", "Mia", "Noah"
        };

        String randomName = names[this.random.nextInt(names.length)];
        this.setCustomName(Text.literal(randomName)); // CustomName gets persisted
        this.setCustomNameVisible(true); // Makes the name always visible above the NPC
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
                        "Ouch! That hurt!", "Hey, watch it!",
                        "Why would you do that?!", "Stop hitting me!",
                        "What’s wrong with you?", "Please, don’t hurt me!"
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
        if (!this.getWorld().isClient() && hand == Hand.MAIN_HAND) {
            // Stop wandering when interacting
            this.getNavigation().stop();

            // Calculate the target yaw (angle toward the player)
            double dx = player.getX() - this.getX();
            double dz = player.getZ() - this.getZ();
            targetYaw = (float) (Math.atan2(dz, dx) * (180F / Math.PI)) - 90F; // Store target yaw
            isTurning = true; // Start the turning process

            // Handle interaction dialogue
            Text nameText = this.getCustomName();
            String npcName = nameText != null ? nameText.getString() : "NPC"; // Retrieve the NPC's name

            String[] dialogues = {
                    "Hello there, traveler! How can I help you?",
                    "I hope you're enjoying the day.",
                    "Stay safe—the world is dangerous.",
                    "There's treasure hidden nearby... or so I've heard.",
                    "Don't forget to stay out of trouble!"
            };

            String dialogue = dialogues[this.random.nextInt(dialogues.length)]; // Random dialogue
            player.sendMessage(Text.literal(npcName + ": " + dialogue)); // Send message to the player

            return ActionResult.SUCCESS; // Successful interaction
        }

        return super.interactMob(player, hand); // Let other interactions occur
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

    // Helper method to normalize yaw angle differences
    private float wrapDegrees(float degrees) {
        // Ensure the yaw stays within the range [-180, 180]
        while (degrees >= 180.0F) {
            degrees -= 360.0F;
        }
        while (degrees < -180.0F) {
            degrees += 360.0F;
        }
        return degrees;
    }
}