package com.funalex.nuzlocke.handlers;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonFaintedEvent;
import com.cobblemon.mod.common.api.events.pokemon.healing.PokemonHealedEvent;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.funalex.nuzlocke.config.NuzlockeConfig;
import com.funalex.nuzlocke.state.NuzlockeRunState;
import com.funalex.nuzlocke.state.NuzlockeStateManager;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.function.Consumer;

public class PermaDeathHandler {

    private static boolean registered = false;

    public static void register() {
        if (registered)
            return;
        registered = true;

        CobblemonEvents.BATTLE_FAINTED.subscribe(Priority.NORMAL, (Consumer<BattleFaintedEvent>) PermaDeathHandler::onBattleFaint);
        CobblemonEvents.POKEMON_FAINTED.subscribe(Priority.NORMAL, (Consumer<PokemonFaintedEvent>) PermaDeathHandler::onPokemonFaint);
        CobblemonEvents.POKEMON_HEALED.subscribe(Priority.HIGH, (Consumer<PokemonHealedEvent>) PermaDeathHandler::onPokemonHealed);
    }

    /**
     * Handle a Pokemon fainting during battle
     */
    private static Unit onBattleFaint(BattleFaintedEvent event) {
        if (!NuzlockeConfig.getInstance().permaDeathRule) {
            return Unit.INSTANCE;
        }

        BattlePokemon battlePokemon = event.getKilled();
        Pokemon pokemon = battlePokemon.getEffectedPokemon();

        UUID ownerUuid = getOwnerUuid(pokemon);
        if (ownerUuid == null)
            return Unit.INSTANCE;

        if (!NuzlockeStateManager.getInstance().hasActiveRun(ownerUuid))
            return Unit.INSTANCE;

        NuzlockeRunState state = NuzlockeStateManager.getInstance().getState(ownerUuid);
        if (state != null && !state.isPokemonDead(pokemon.getUuid())) {
            String nickname = pokemon.getNickname() != null ? pokemon.getNickname().getString()
                    : pokemon.getSpecies().getName();
            String species = pokemon.getSpecies().getName();
            int level = pokemon.getLevel();

            state.markPokemonDead(pokemon.getUuid(), nickname, species, level);
            NuzlockeStateManager.getInstance().save();

            // Auto-release if configured
            releasePokemon(pokemon, nickname);
        }

        return Unit.INSTANCE;
    }

    /**
     * Handle a Pokemon fainting outside of battle
     */
    private static Unit onPokemonFaint(PokemonFaintedEvent event) {
        if (!NuzlockeConfig.getInstance().permaDeathRule) {
            return Unit.INSTANCE;
        }

        Pokemon pokemon = event.getPokemon();

        UUID ownerUuid = getOwnerUuid(pokemon);
        if (ownerUuid == null)
            return Unit.INSTANCE;

        if (!NuzlockeStateManager.getInstance().hasActiveRun(ownerUuid))
            return Unit.INSTANCE;

        NuzlockeRunState state = NuzlockeStateManager.getInstance().getState(ownerUuid);
        if (state != null && !state.isPokemonDead(pokemon.getUuid())) {
            String nickname = pokemon.getNickname() != null ? pokemon.getNickname().getString()
                    : pokemon.getSpecies().getName();
            String species = pokemon.getSpecies().getName();
            int level = pokemon.getLevel();

            state.markPokemonDead(pokemon.getUuid(), nickname, species, level);
            NuzlockeStateManager.getInstance().save();

            // Auto-release if configured
            releasePokemon(pokemon, nickname);
        }

        return Unit.INSTANCE;
    }

    private static Unit onPokemonHealed(PokemonHealedEvent event) {
        if (!NuzlockeConfig.getInstance().permaDeathRule) {
            return Unit.INSTANCE;
        }

        Pokemon pokemon = event.getPokemon();

        UUID ownerUuid = getOwnerUuid(pokemon);
        if (ownerUuid == null)
            return Unit.INSTANCE;

        NuzlockeRunState state = NuzlockeStateManager.getInstance().getState(ownerUuid);
        if (state != null && state.isPokemonDead(pokemon.getUuid())) {
            event.cancel();
        }

        return Unit.INSTANCE;
    }

    private static UUID getOwnerUuid(Pokemon pokemon) {
        var storeCoordinatesObservable = pokemon.getStoreCoordinates();
        var storeCoordinates = storeCoordinatesObservable.get();
        if (storeCoordinates == null)
            return null;

        var store = storeCoordinates.getStore();

        return store.getUuid();
    }

    private static void releasePokemon(Pokemon pokemon, String nickname) {
        NuzlockeConfig config = NuzlockeConfig.getInstance();

        // Only auto-release if configured to do so
        if (config.deathHandling != NuzlockeConfig.DeathHandlingMode.RELEASE) {
            return;
        }

        var storeCoords = pokemon.getStoreCoordinates().get();
        if (storeCoords != null) {
            var store = storeCoords.getStore();
            store.remove(pokemon);

            // Notify the player
            ServerPlayer player = pokemon.getOwnerPlayer();
            if (player != null) {
                player.sendSystemMessage(Component.literal("§c" + nickname + " §7has been released from your party..."));
            }
        }
    }
}
