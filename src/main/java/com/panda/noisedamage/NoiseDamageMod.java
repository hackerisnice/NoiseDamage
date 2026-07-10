package com.panda.noisedamage;

import com.panda.noisedamage.command.ReloadCommand;
import com.panda.noisedamage.config.NoiseConfig;
import com.panda.noisedamage.network.NoiseReportPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NoiseDamageMod implements ModInitializer {
    public static final String MOD_ID = "noisedamage";
    private static final Map<UUID, Float> playerNoise = new ConcurrentHashMap<>();

    @Override
    public void onInitialize() {
        // 注册 C2S 数据包
        PayloadTypeRegistry.playC2S().register(NoiseReportPacket.ID, NoiseReportPacket.CODEC);

        // 接收客户端上报的噪声值
        ServerPlayNetworking.registerGlobalReceiver(NoiseReportPacket.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            playerNoise.put(player.getUuid(), payload.noise());
        });

        // 每 tick 对超过阈值的玩家造成伤害
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                Float noise = playerNoise.get(player.getUuid());
                if (noise != null && noise >= NoiseConfig.damageThreshold && server.getTicks() % 20 == 0) {
                    player.damage(player.getDamageSources().generic(), NoiseConfig.damagePerSecond);
                }
            }
        });

        // 玩家退出时清除其记录
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            playerNoise.remove(handler.getPlayer().getUuid());
        });

        // 首次启动时加载配置
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (server.getTicks() == 1) {
                NoiseConfig.loadOrGenerate();
            }
        });

        // 注册重载命令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ReloadCommand.register(dispatcher);
        });
    }
}
