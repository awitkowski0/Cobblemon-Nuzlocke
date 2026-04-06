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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import java.util.function.Consumer;

public class PermaDeathHandler {

    private static boolean registered = false;

    public static void register() {
        if (registered)
            return;
        registered = true;

        // Only use POKEMON_FAINTED to track deaths to avoid double-counting.
        // Cobblemon triggers POKEMON_FAINTED when health reaches 0, which happens in both battle and overworld.
        CobblemonEvents.POKEMON_FAINTED.subscribe(Priority.NORMAL, (Consumer<PokemonFaintedEvent>) PermaDeathHandler::onPokemonFaint);
        CobblemonEvents.POKEMON_HEALED.subscribe(Priority.HIGH, (Consumer<PokemonHealedEvent>) PermaDeathHandler::onPokemonHealed);
    }

    /**
     * Handle a Pokemon fainting (works for both in and out of battle)
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

            int lives = state.decreaseLife(pokemon.getUuid());

            if (lives > 0) {
                if (NuzlockeConfig.getInstance().announceDeaths) {
                    ServerPlayer player = pokemon.getOwnerPlayer();
                    if (player != null) {
                        String msg = NuzlockeConfig.getInstance().lifeLostMessage
                            .replace("%player%", player.getDisplayName().getString())
                            .replace("%pokemon%", nickname)
                            .replace("%lives%", String.valueOf(lives));
                        MutableComponent comp = Component.literal(msg);
                        for (Player p : player.getCommandSenderWorld().players()) {
                            p.sendSystemMessage(comp);
                        }
                    }
                }
                NuzlockeStateManager.getInstance().save();
                return Unit.INSTANCE;
            }

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

        ServerPlayer player = pokemon.getOwnerPlayer();

        if (player != null && config.announceDeaths) {
            String message = config.deathMessage;
            message = message.replace("%player%", player.getDisplayName().getString());
            message = message.replace("%pokemon%", nickname);

            MutableComponent announceMessage = Component.literal(message);

            if (!player.getCommandSenderWorld().players().isEmpty()) {
                for (Player player1 : player.getCommandSenderWorld().players()) {
                    player1.sendSystemMessage(announceMessage);
                }
            }
        }

        var storeCoords = pokemon.getStoreCoordinates().get();
        if (storeCoords != null) {
            var store = storeCoords.getStore();
            store.remove(pokemon);
        }
    }
}
