package com.funalex.nuzlocke;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.funalex.nuzlocke.config.NuzlockeConfig;
import com.funalex.nuzlocke.state.NuzlockeRunState;
import com.funalex.nuzlocke.state.NuzlockeStateManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class NuzlockeManager {
    private static NuzlockeManager INSTANCE;

    private NuzlockeManager() {
    }

    public static NuzlockeManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NuzlockeManager();
        }
        return INSTANCE;
    }

    public boolean startRun(ServerPlayer player) {
        UUID playerId = player.getUUID();

        if (NuzlockeStateManager.getInstance().hasActiveRun(playerId)) {
            player.sendSystemMessage(Component.literal("§cYou already have an active Nuzlocke run!"));
            return false;
        }

        NuzlockeStateManager.getInstance().startRun(playerId);
        player.sendSystemMessage(Component.literal("§aNuzlocke Challenge Started!"));
        player.sendSystemMessage(Component.literal("§7Good luck, trainer. May your Pokémon survive..."));

        NuzlockeConfig config = NuzlockeConfig.getInstance();
        if (config.permaDeathRule) {
            player.sendSystemMessage(Component.literal("§7Perma-Death: §aEnabled"));
            player.sendSystemMessage(Component.literal("§7Death handling: §f" + config.deathHandling.name()));
        }

        return true;
    }

    public void startRunSilently(ServerPlayer player) {
        UUID playerId = player.getUUID();

        if (!NuzlockeStateManager.getInstance().hasActiveRun(playerId)) {
            NuzlockeStateManager.getInstance().startRun(playerId);
        }
    }

    public void endRun(ServerPlayer player, boolean voluntarily) {
        UUID playerId = player.getUUID();
        NuzlockeRunState state = NuzlockeStateManager.getInstance().getState(playerId);

        if (state == null || !state.isActive()) {
            player.sendSystemMessage(Component.literal("§cYou don't have an active Nuzlocke run!"));
            return;
        }

        NuzlockeStateManager.getInstance().endRun(playerId);

        if (voluntarily) {
            player.sendSystemMessage(Component.literal("§cNuzlocke run ended."));
        } else {
            player.sendSystemMessage(Component.literal("§4GAME OVER"));
            player.sendSystemMessage(Component.literal("§cAll your Pokémon have fallen..."));
        }

        // Show final stats
        player.sendSystemMessage(Component.literal("§eRun Statistics:"));
        player.sendSystemMessage(Component.literal("§7Pokémon Lost: §c" + state.getTotalDeaths()));
    }

    public boolean isNuzlockeActive(ServerPlayer player) {
        return NuzlockeStateManager.getInstance().hasActiveRun(player.getUUID());
    }
}
