package com.panda.noisedamage;

import com.panda.noisedamage.command.ReloadCommand;
import com.panda.noisedamage.config.NoiseConfig;
import com.panda.noisedamage.manager.NoiseManager;
import com.panda.noisedamage.network.NoiseSyncPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class NoiseDamageMod implements ModInitializer {
    public static final String MOD_ID = "noisedamage";

    @Override
    public void onInitialize() {
        // 注册自定义数据包
        PayloadTypeRegistry.playS2C().register(NoiseSyncPacket.ID, NoiseSyncPacket.CODEC);

        // 服务端启动时加载/生成配置文件
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            NoiseConfig.loadOrGenerate();
            NoiseManager.init();
        });

        // 服务端每tick处理噪声衰减和伤害
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            NoiseManager.tickAllPlayers(server);
        });

        // 玩家加入时加载噪声数据
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            NoiseManager.loadPlayerData(player);
        });

        // 玩家退出时保存噪声数据
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            NoiseManager.savePlayerData(player);
        });

        // 服务器关闭前最终保存一次（作为兜底）
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                NoiseManager.savePlayerData(player);
            }
        });

        // 注册管理员命令 /noisereload
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ReloadCommand.register(dispatcher);
        });
    }
}
