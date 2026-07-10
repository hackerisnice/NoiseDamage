package com.panda.noisedamage;

import com.panda.noisedamage.client.ClientNoiseManager;
import com.panda.noisedamage.config.NoiseConfig;
import com.panda.noisedamage.hud.NoiseHudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class NoiseDamageModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // ✅ 客户端加载配置（重要！）
        NoiseConfig.loadOrGenerate();

        // 初始化噪声管理器
        ClientNoiseManager.init();

        // 每客户端 tick 处理衰减和上报
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientNoiseManager.clientTick();
        });

        // 注册 HUD
        NoiseHudOverlay.register();
    }
}
