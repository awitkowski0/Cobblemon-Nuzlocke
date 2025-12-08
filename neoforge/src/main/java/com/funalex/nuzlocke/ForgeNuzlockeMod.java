package com.funalex.nuzlocke;

import com.funalex.nuzlocke.commands.NuzlockeCommands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.nio.file.Path;

@Mod("nuzlocke")
public class ForgeNuzlockeMod {
    public ForgeNuzlockeMod() {
        Path configDir = FMLPaths.CONFIGDIR.get().resolve("nuzlocke");
        NuzlockeMod.initialize(configDir);

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onCommandRegistration(RegisterCommandsEvent event) {
        NuzlockeCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            NuzlockeManager.getInstance().startRunSilently(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        NuzlockeMod.shutdown();
    }
}
