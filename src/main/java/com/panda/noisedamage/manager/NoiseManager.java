package com.panda.noisedamage.manager;

import com.panda.noisedamage.config.NoiseConfig;
import com.panda.noisedamage.network.NoiseSyncPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NoiseManager {
    public static final double MAX_AUDIBLE_DISTANCE = 16.0;
    private static final Map<UUID, PlayerNoiseData> dataMap = new ConcurrentHashMap<>();

    public static void init() {
        dataMap.clear();
    }

    public static void addNoise(ServerPlayerEntity player, SoundEvent sound, float volume, Vec3d soundPos) {
        float baseDB = NoiseConfig.getSoundLevel(sound);
        if (baseDB <= 0) return;

        double distance = player.getPos().distanceTo(soundPos);
        double effectiveRadius = MAX_AUDIBLE_DISTANCE * volume;
        if (distance > effectiveRadius) return;

        double attenuation = 1.0 - (distance / effectiveRadius);
        if (attenuation < 0) attenuation = 0;
        float actualDB = (float) (baseDB * attenuation);
        if (actualDB <= 0) return;

        PlayerNoiseData data = dataMap.computeIfAbsent(player.getUuid(), id -> new PlayerNoiseData());
        data.noiseLevel += actualDB;
        data.lastIncreaseTick = player.getServerWorld().getTime();
    }

    public static void tickAllPlayers(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerNoiseData data = dataMap.get(player.getUuid());
            if (data == null) continue;

            long serverTime = player.getServerWorld().getTime();
            long ticksSinceIncrease = serverTime - data.lastIncreaseTick;
            if (ticksSinceIncrease >= NoiseConfig.decayDelaySeconds * 20L) {
                data.noiseLevel -= 0.05f;
                if (data.noiseLevel < 0) data.noiseLevel = 0;
            }

            if (data.noiseLevel >= NoiseConfig.damageThreshold && serverTime % 20 == 0) {
                player.damage(player.getDamageSources().generic(), NoiseConfig.damagePerSecond);
            }

            ServerPlayNetworking.send(player, new NoiseSyncPacket(data.noiseLevel));
        }
    }

    // 从 NBT 加载玩家噪声数据（由 Mixin 调用）
    public static void readPlayerDataFromNbt(ServerPlayerEntity player, NbtCompound nbt) {
        if (nbt.contains("NoiseDamage")) {
            NbtCompound tag = nbt.getCompound("NoiseDamage");
            PlayerNoiseData data = new PlayerNoiseData();
            data.noiseLevel = tag.getFloat("NoiseLevel");
            data.lastIncreaseTick = tag.getLong("LastIncreaseTick");
            dataMap.put(player.getUuid(), data);
        }
    }

    // 保存玩家噪声数据到 NBT（由 Mixin 调用）
    public static void writePlayerDataToNbt(ServerPlayerEntity player, NbtCompound nbt) {
        PlayerNoiseData data = dataMap.get(player.getUuid());
        if (data != null) {
            NbtCompound tag = new NbtCompound();
            tag.putFloat("NoiseLevel", data.noiseLevel);
            tag.putLong("LastIncreaseTick", data.lastIncreaseTick);
            nbt.put("NoiseDamage", tag);
        }
    }

    // 保留原有的加载/保存方法供其他事件调用（已不需要，但可做清理用）
    public static void loadPlayerData(ServerPlayerEntity player) {
        // 数据现在由 Mixin 自动加载，无需手动调用
    }

    public static void savePlayerData(ServerPlayerEntity player) {
        // 数据现在由 Mixin 自动保存，无需手动调用
    }

    private static class PlayerNoiseData {
        float noiseLevel;
        long lastIncreaseTick;
    }
}
