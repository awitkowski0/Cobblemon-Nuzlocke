package com.funalex.nuzlocke.state;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NuzlockeStateManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type STATE_MAP_TYPE = new TypeToken<Map<UUID, NuzlockeRunState>>() {
    }.getType();

    private static NuzlockeStateManager INSTANCE;

    private final Map<UUID, NuzlockeRunState> playerStates = new ConcurrentHashMap<>();
    private Path dataDir;

    private NuzlockeStateManager() {
    }

    public static NuzlockeStateManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NuzlockeStateManager();
        }
        return INSTANCE;
    }

    public void initialize(Path dataDir) {
        this.dataDir = dataDir;
        load();
    }

    /**
     * Get the run state for a player, creating a new one if needed
     */
    public NuzlockeRunState getOrCreateState(UUID playerId) {
        return playerStates.computeIfAbsent(playerId, NuzlockeRunState::new);
    }

    /**
     * Get the run state for a player, or null if none exists
     */
    public NuzlockeRunState getState(UUID playerId) {
        return playerStates.get(playerId);
    }

    public boolean hasActiveRun(UUID playerId) {
        NuzlockeRunState state = playerStates.get(playerId);
        return state != null && state.isActive();
    }

    public NuzlockeRunState startRun(UUID playerId) {
        NuzlockeRunState state = getOrCreateState(playerId);
        state.startRun();
        save();
        return state;
    }

    /**
     * End a player's current Nuzlocke run
     */
    public void endRun(UUID playerId) {
        NuzlockeRunState state = playerStates.get(playerId);
        if (state != null) {
            state.endRun();
            save();
        }
    }

    public void load() {
        if (dataDir == null)
            return;

        Path stateFile = dataDir.resolve("nuzlocke_states.json");

        if (Files.exists(stateFile)) {
            try (Reader reader = Files.newBufferedReader(stateFile)) {
                Map<UUID, NuzlockeRunState> loaded = GSON.fromJson(reader, STATE_MAP_TYPE);
                if (loaded != null) {
                    playerStates.clear();
                    playerStates.putAll(loaded);
                }
            } catch (IOException ignored) {
            }
        }
    }

    public void save() {
        if (dataDir == null)
            return;

        try {
            Files.createDirectories(dataDir);
            Path stateFile = dataDir.resolve("nuzlocke_states.json");

            try (Writer writer = Files.newBufferedWriter(stateFile)) {
                GSON.toJson(playerStates, STATE_MAP_TYPE, writer);
            }
        } catch (IOException ignored) {
        }
    }
}
