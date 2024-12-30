package net.asian.civiliansmod.entity.goal;

import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DoorCoordinator {
    private static final Set<BlockPos> claimedDoors = Collections.synchronizedSet(new HashSet<>());
    private static final Map<BlockPos, Integer> doorCooldowns = Collections.synchronizedMap(new HashMap<>());

    public static synchronized boolean claimDoor(BlockPos doorPos) {
        if (!claimedDoors.contains(doorPos) && !isDoorOnCooldown(doorPos)) {
            claimedDoors.add(doorPos);
            return true;
        }
        return false;
    }

    public static synchronized void releaseDoor(BlockPos doorPos) {
        claimedDoors.remove(doorPos);
    }

    public static synchronized void setDoorCooldown(BlockPos doorPos, int cooldownTicks) {
        doorCooldowns.put(doorPos, cooldownTicks);
    }

    public static synchronized void tickCooldowns() {
        doorCooldowns.entrySet().removeIf(entry -> {
            int remainingTime = entry.getValue() - 1;
            if (remainingTime <= 0) {
                return true;
            }
            entry.setValue(remainingTime);
            return false;
        });
    }

    public static boolean isDoorOnCooldown(BlockPos doorPos) {
        return doorCooldowns.containsKey(doorPos);
    }
}