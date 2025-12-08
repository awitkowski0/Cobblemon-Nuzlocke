package com.funalex.nuzlocke;

import com.funalex.nuzlocke.config.NuzlockeConfig;
import com.funalex.nuzlocke.handlers.PermaDeathHandler;
import com.funalex.nuzlocke.state.NuzlockeStateManager;

import java.nio.file.Path;

public class NuzlockeMod {

    public static final String MOD_ID = "nuzlocke";
    public static final String MOD_NAME = "Cobblemon: Nuzlocke";

    private static boolean initialized = false;
    private static Path configDir;
    private static Path dataDir;

    /**
     * Initialize the Nuzlocke mod.
     * 
     * @param configDirectory Path to the config directory (e.g.,
     *                        .minecraft/config/nuzlocke)
     * @param dataDirectory   Path to the data directory (e.g.,
     *                        .minecraft/saves/world/data/nuzlocke)
     */
    public static void initialize(Path configDirectory, Path dataDirectory) {
        if (initialized) {
            System.out.println("[Nuzlocke] Already initialized, skipping...");
            return;
        }

        System.out.println("[Nuzlocke] Initializing " + MOD_NAME + "...");

        configDir = configDirectory;
        dataDir = dataDirectory;

        NuzlockeConfig.load(configDir);
        NuzlockeStateManager.getInstance().initialize(dataDir);

        registerEventHandlers();

        initialized = true;
    }

    public static void initialize(Path configDirectory) {
        initialize(configDirectory, configDirectory);
    }

    private static void registerEventHandlers() {
        PermaDeathHandler.register();
    }

    public static void shutdown() {
        NuzlockeStateManager.getInstance().save();
        NuzlockeConfig.save(configDir);
    }
}
