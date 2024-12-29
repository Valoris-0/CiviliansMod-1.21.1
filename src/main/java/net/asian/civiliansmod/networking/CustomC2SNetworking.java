package net.asian.civiliansmod.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class CustomC2SNetworking {

    static {
        ServerPlayNetworking.registerGlobalReceiver(NPCDataPayload.ID, NPCDataPayload::handlePacket);
    }
    public static void intialize() {

    }

}
