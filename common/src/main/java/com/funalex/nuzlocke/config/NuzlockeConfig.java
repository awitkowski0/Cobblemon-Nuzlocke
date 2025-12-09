package com.funalex.nuzlocke.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class NuzlockeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static NuzlockeConfig INSTANCE;
    private static Path configDirectory;


    /** Enable perma-death rule (fainted Pokemon are considered dead) */
    public boolean permaDeathRule = true;

    /** Announce deaths in chat */
    public boolean announceDeaths = true;

    /**
     * Message format for death announcements. Use %pokemon% for name, %species% for
     * species
     */
    public String deathMessage = "§7%player%'s §c%pokemon%§7 has fallen in battle...";

    /**
     * How to handle dead Pokemon.
     * RELEASE - Automatically release them
     * MARK_ONLY - Just mark as dead (can't revive)
     */
    public DeathHandlingMode deathHandling = DeathHandlingMode.RELEASE;

    public enum DeathHandlingMode {
        RELEASE,
        MARK_ONLY
    }

    // === Static Methods ===

    public static NuzlockeConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NuzlockeConfig();
        }
        return INSTANCE;
    }

    public static void load(Path configDir) {
        configDirectory = configDir;
        Path configFile = configDir.resolve("nuzlocke.json");

        if (Files.exists(configFile)) {
            try (Reader reader = Files.newBufferedReader(configFile)) {
                INSTANCE = GSON.fromJson(reader, NuzlockeConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new NuzlockeConfig();
                }
            } catch (IOException e) {
                INSTANCE = new NuzlockeConfig();
            }
        } else {
            INSTANCE = new NuzlockeConfig();
            save(configDir);
        }
    }

    public static void save(Path configDir) {
        try {
            Files.createDirectories(configDir);
            Path configFile = configDir.resolve("nuzlocke.json");

            try (Writer writer = Files.newBufferedWriter(configFile)) {
                GSON.toJson(getInstance(), writer);
            }
        } catch (IOException ignored) {
        }
    }

    public static void save() {
        if (configDirectory != null) {
            save(configDirectory);
        }
    }

    public static void reload(Path configDir) {
        INSTANCE = null;
        load(configDir);
    }

    public static void reload() {
        if (configDirectory != null) {
            reload(configDirectory);
        }
    }
}
