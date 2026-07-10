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

public class ClientNoiseManager {
    private static float currentNoise = 0f;
    private static long lastIncreaseTick = 0;
    private static long clientTick = 0;

    public static void init() {
        currentNoise = 0f;
    }

    public static float getNoise() {
        return currentNoise;
    }

    /**
     * 由 SoundManagerMixin 调用，当客户端播放任意声音时触发
     */
    public static void onSoundPlayed(SoundInstance sound) {
        try {
            if (sound == null) return;

            // 忽略背景音乐和天气
            SoundCategory cat = sound.getCategory();
            if (cat == SoundCategory.MUSIC || cat == SoundCategory.RECORDS || cat == SoundCategory.WEATHER) {
                return;
            }

            Identifier id = sound.getId();
            if (id == null) return;  // 防止空声音 ID

            SoundEvent event = Registries.SOUND_EVENT.get(id);
            float baseDB = 0f;
            if (event != null) {
                baseDB = NoiseConfig.getSoundLevel(event);
            } else {
                // 兜底：直接用标识符字符串查找
                baseDB = NoiseConfig.soundLevels.getOrDefault(id.toString(), 0f);
            }

            if (baseDB <= 0) return;

            float volume = sound.getVolume();
            float added = baseDB * volume;
            if (added > 0) {
                currentNoise += added;
                lastIncreaseTick = clientTick;
            }
        } catch (Exception e) {
            // 忽略任何意外异常，避免游戏崩溃
        }
    }

    /**
     * 每客户端 tick 处理衰减并上报
     */
    public static void clientTick() {
        clientTick++;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // 衰减：超过 decayDelaySeconds 秒无增加则每秒降低 1 点
        long ticksSinceIncrease = clientTick - lastIncreaseTick;
        if (ticksSinceIncrease >= NoiseConfig.decayDelaySeconds * 20L) {
            currentNoise -= 0.05f; // 每 tick 减少 0.05，即每秒减少 1
            if (currentNoise < 0) currentNoise = 0;
        }

        // 每 20 tick (1秒) 向服务端上报一次当前噪声
        if (clientTick % 20 == 0) {
            ClientPlayNetworking.send(new NoiseReportPacket(currentNoise));
        }
    }
}
