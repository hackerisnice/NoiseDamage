package com.panda.noisedamage.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class NoiseConfig {
    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("noise_damage.json");
    public static Map<String, Float> soundLevels = new HashMap<>();
    public static int decayDelaySeconds = 30;
    public static float damageThreshold = 200f;
    public static float damagePerSecond = 2.0f;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void loadOrGenerate() {
        if (Files.notExists(CONFIG_PATH)) {
            generateDefault();
        }
        load();
    }

    private static void generateDefault() {
        Map<String, Float> defaults = new HashMap<>();
        for (Identifier id : Registries.SOUND_EVENT.getIds()) {
            defaults.put(id.toString(), 0.0f);
        }
        // 预设一些常用声音的分贝值
        defaults.put("entity.player.hurt", 5.0f);
        defaults.put("entity.player.death", 8.0f);
        defaults.put("entity.creeper.primed", 8.0f);
        defaults.put("entity.generic.explode", 10.0f);
        defaults.put("entity.tnt.primed", 7.0f);
        defaults.put("entity.lightning_bolt.thunder", 12.0f);
        defaults.put("block.anvil.land", 6.0f);
        defaults.put("entity.ender_dragon.death", 15.0f);
        // 可根据需要添加更多

        ConfigFile file = new ConfigFile();
        file.sound_levels = defaults;
        file.decay_delay_seconds = decayDelaySeconds;
        file.damage_threshold = damageThreshold;
        file.damage_per_second = damagePerSecond;

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(file, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            ConfigFile file = GSON.fromJson(reader, ConfigFile.class);
            if (file.sound_levels != null) {
                soundLevels = file.sound_levels;
            } else {
                soundLevels = new HashMap<>();
            }
            decayDelaySeconds = Math.max(1, file.decay_delay_seconds);
            damageThreshold = Math.max(1f, file.damage_threshold);
            damagePerSecond = Math.max(0.5f, file.damage_per_second);
        } catch (IOException e) {
            e.printStackTrace();
            soundLevels = new HashMap<>();
        }
    }

    public static float getSoundLevel(SoundEvent sound) {
        Identifier id = Registries.SOUND_EVENT.getId(sound);
        return soundLevels.getOrDefault(id.toString(), 0f);
    }

    private static class ConfigFile {
        Map<String, Float> sound_levels;
        int decay_delay_seconds;
        float damage_threshold;
        float damage_per_second;
    }
}
