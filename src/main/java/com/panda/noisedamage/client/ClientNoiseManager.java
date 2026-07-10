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

    public static void init() {
        currentNoise = 0f;
        LOGGER.info("ClientNoiseManager initialized. Config loaded: {} sounds, threshold={}, decay={}s",
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

            Identifier id = sound.getId();
            if (id == null) return;

            SoundEvent event = Registries.SOUND_EVENT.get(id);
            float baseDB = 0f;
            String lookupType = "unknown";
            if (event != null) {
                baseDB = NoiseConfig.getSoundLevel(event);
                lookupType = "registered";
            } else {
                baseDB = NoiseConfig.soundLevels.getOrDefault(id.toString(), 0f);
                lookupType = "string";
            }

            // 调试输出：所有非零分贝的声音，或所有脚步声（step）
            if (baseDB > 0 || id.getPath().contains("step")) {
                LOGGER.info("[Sound] {} | dB: {} | vol: {} | cat: {} | lookup: {}",
                        id, baseDB, sound.getVolume(), cat, lookupType);
            }

            if (baseDB <= 0) return;

            float volume = sound.getVolume();
            float added = baseDB * volume;
            if (added > 0) {
                currentNoise += added;
                lastIncreaseTick = clientTick;
                LOGGER.info("[Noise] +{} dB (total: {})", added, currentNoise);
            }
        } catch (Exception e) {
            LOGGER.error("Error processing sound", e);
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
            // 每秒报告一次当前噪声值（方便观察）
            if (currentNoise > 0) {
                LOGGER.info("[Status] Noise: {:.1f} dB", currentNoise);
            }
        }
    }
}
