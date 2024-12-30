package net.asian.civiliansmod.entity.goal;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomDoorGoal extends Goal {
    private final PathAwareEntity npc;
    private BlockPos targetDoorPos;
    private final Random random = new Random();
    private int cooldownTimer = random.nextInt(500) + 200; // Initial cooldown randomized per NPC
    private int stayIndoorsTimer = 0;

    public CustomDoorGoal(PathAwareEntity npc) {
        this.npc = npc;
    }

    @Override
    public boolean canStart() {
        if (stayIndoorsTimer > 0 || cooldownTimer > 0) {
            return false;
        }

        // Ensure that the NPC has a chance not to start the goal
        if (random.nextFloat() < 0.3) { // 30% chance to even attempt to start
            return false;
        }

        targetDoorPos = findUnclaimedNearbyDoor();
        if (targetDoorPos != null && DoorCoordinator.claimDoor(targetDoorPos)) {
            return true;
        }

        targetDoorPos = null;
        return false;
    }

    @Override
    public boolean shouldContinue() {
        return targetDoorPos != null || stayIndoorsTimer > 0;
    }

    @Override
    public void start() {
        if (targetDoorPos != null) {
            this.npc.getNavigation().startMovingTo(
                    targetDoorPos.getX(),
                    targetDoorPos.getY(),
                    targetDoorPos.getZ(),
                    0.7
            );
        }
    }

    @Override
    public void stop() {
        if (targetDoorPos != null) {
            DoorCoordinator.releaseDoor(targetDoorPos);
        }
        targetDoorPos = null;
    }

    @Override
    public void tick() {
        if (targetDoorPos != null) {
            if (this.npc.getBlockPos().isWithinDistance(targetDoorPos, 1.5)) {
                setDoorOpen(true);
                stayIndoorsTimer = 100 + random.nextInt(400); // Stay indoors for a random duration (100-500 ticks)
                DoorCoordinator.setDoorCooldown(targetDoorPos, 600 + random.nextInt(400)); // Add a longer random door cooldown (600-1000 ticks)
                DoorCoordinator.releaseDoor(targetDoorPos);
                targetDoorPos = null;
            }
        } else if (stayIndoorsTimer > 0) {
            stayIndoorsTimer--;
            if (stayIndoorsTimer <= 0) {
                setDoorOpen(false);
                cooldownTimer = 400 + random.nextInt(800); // Random cooldown between 400-1200 ticks
            }
        } else {
            cooldownTimer--;
        }
    }

    private BlockPos findUnclaimedNearbyDoor() {
        BlockPos currentPos = this.npc.getBlockPos();
        List<BlockPos> validDoors = new ArrayList<>();
        int range = 10;

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    BlockPos pos = currentPos.add(dx, dy, dz);
                    BlockState blockState = this.npc.getWorld().getBlockState(pos);

                    if (isValidDoor(blockState) && !DoorCoordinator.isDoorOnCooldown(pos)) {
                        validDoors.add(pos);
                    }
                }
            }
        }

        return validDoors.isEmpty() ? null : validDoors.get(random.nextInt(validDoors.size()));
    }

    private boolean isValidDoor(BlockState blockState) {
        Block block = blockState.getBlock();
        return block instanceof DoorBlock && !blockState.get(DoorBlock.OPEN);
    }

    private void setDoorOpen(boolean open) {
        if (targetDoorPos != null) {
            BlockState blockState = this.npc.getWorld().getBlockState(targetDoorPos);
            if (blockState.getBlock() instanceof DoorBlock) {
                this.npc.getWorld().setBlockState(targetDoorPos, blockState.with(DoorBlock.OPEN, open));
            }
        }
    }
}