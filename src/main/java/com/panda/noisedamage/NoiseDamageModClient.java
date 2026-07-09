package com.panda.noisedamage;

import com.panda.noisedamage.hud.NoiseHudOverlay;
import com.panda.noisedamage.network.NoiseSyncPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class NoiseDamageModClient implements ClientModInitializer {
    public static float clientNoiseLevel = 0f;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NoiseSyncPacket.ID, (payload, context) -> {
            context.client().execute(() -> {
                clientNoiseLevel = payload.noise();
            });
        });

        NoiseHudOverlay.register();
    }
}
