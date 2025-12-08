package com.funalex.nuzlocke;

import com.funalex.nuzlocke.commands.NuzlockeCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class FabricNuzlockeMod implements ModInitializer {

    @Override
    public void onInitialize() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve("nuzlocke");
        NuzlockeMod.initialize(configDir);
        CommandRegistrationCallback.EVENT.register(NuzlockeCommands::register);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            NuzlockeManager.getInstance().startRunSilently(handler.getPlayer());
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            NuzlockeMod.shutdown();
        });
    }
}
