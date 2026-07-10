package com.panda.noisedamage.client;

import com.panda.noisedamage.config.NoiseConfig;
import com.panda.noisedamage.network.NoiseReportPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientNoiseManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("NoiseDamage");
    private static float currentNoise = 0f;
    private static long lastIncreaseTick = 0;
    private static long clientTick = 0;
    // 调试开关：改成 false 可关闭日志刷屏
    private static final boolean DEBUG_LOG_ALL = true;

    public static void init() {
        currentNoise = 0f;
        LOGGER.info("Initialized. Sounds in config: {}, Threshold: {} dB, Decay: {}s",
                NoiseConfig.soundLevels.size(), NoiseConfig.damageThreshold, NoiseConfig.decayDelaySeconds);
    }

    public static float getNoise() {
        return currentNoise;
    }

    public static void onSoundPlayed(SoundInstance sound) {
        try {
            if (sound == null) return;

            SoundCategory cat = sound.getCategory();
            if (cat == SoundCategory.MUSIC || cat == SoundCategory.RECORDS || cat == SoundCategory.WEATHER) {
                return;
            }

            Identifier id = sound.getId();  // 此处可能抛出异常，由外层 try 捕获
            if (id == null) return;

            float volume = sound.getVolume();
            float baseDB = 0f;
            SoundEvent event = Registries.SOUND_EVENT.get(id);
            if (event != null) {
                baseDB = NoiseConfig.getSoundLevel(event);
            } else {
                baseDB = NoiseConfig.soundLevels.getOrDefault(id.toString(), 0f);
            }

            // 调试：输出所有声音（如果太多可关闭 DEBUG_LOG_ALL）
            if (DEBUG_LOG_ALL) {
                LOGGER.info("[ALL] {} | dB: {} | vol: {} | cat: {}", id, baseDB, volume, cat);
            }

            if (baseDB <= 0) return;

            float added = baseDB * volume;
            if (added > 0) {
                currentNoise += added;
                lastIncreaseTick = clientTick;
                LOGGER.info("[Noise+] {} (+{} dB)", id, added);
            }
        } catch (Exception e) {
            // 静默忽略，不做任何日志（异常声音非常多，避免刷屏卡顿）
        }
    }

    public static void clientTick() {
        clientTick++;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        long ticksSinceIncrease = clientTick - lastIncreaseTick;
        if (ticksSinceIncrease >= NoiseConfig.decayDelaySeconds * 20L) {
            currentNoise -= 0.05f;
            if (currentNoise < 0) currentNoise = 0;
        }

        if (clientTick % 20 == 0) {
            ClientPlayNetworking.send(new NoiseReportPacket(currentNoise));
        }
    }
}
