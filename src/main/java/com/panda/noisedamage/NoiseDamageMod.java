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

public class NoiseDamageMod implements ModInitializer {
    public static final String MOD_ID = "noisedamage";

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playS2C().register(NoiseSyncPacket.ID, NoiseSyncPacket.CODEC);

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            NoiseConfig.loadOrGenerate();
            NoiseManager.init();
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            NoiseManager.tickAllPlayers(server);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ReloadCommand.register(dispatcher);
        });
    }
}
