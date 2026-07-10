package com.panda.noisedamage;

import com.panda.noisedamage.client.ClientNoiseManager;
import com.panda.noisedamage.hud.NoiseHudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class NoiseDamageModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientNoiseManager.init();

        // 客户端每 tick 衰减并上报
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientNoiseManager.clientTick();
        });

        // 注册 HUD
        NoiseHudOverlay.register();
    }
}
