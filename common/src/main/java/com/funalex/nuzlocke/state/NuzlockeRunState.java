package com.funalex.nuzlocke.state;

import java.util.*;

public class NuzlockeRunState {
    private UUID playerId;
    private boolean active = false;
    private long startTimestamp = 0;

    private final Set<UUID> deadPokemon = new HashSet<>();

    private final Map<UUID, DeadPokemonInfo> graveyard = new HashMap<>();

    private int totalDeaths = 0;

    private final Map<UUID, Integer> pokemonLives = new HashMap<>();

    public NuzlockeRunState(UUID playerId) {
        this.playerId = playerId;
    }

    public int getLives(UUID pokemonId) {
        return pokemonLives.getOrDefault(pokemonId, com.funalex.nuzlocke.config.NuzlockeConfig.getInstance().maxLives);
    }

    public int decreaseLife(UUID pokemonId) {
        int lives = getLives(pokemonId) - 1;
        pokemonLives.put(pokemonId, lives);
        return lives;
    }

    public void startRun() {
        this.active = true;
        this.startTimestamp = System.currentTimeMillis();
        this.deadPokemon.clear();
        this.graveyard.clear();
        this.totalDeaths = 0;
        this.pokemonLives.clear();
    }

    public void endRun() {
        this.active = false;
    }

    public boolean isPokemonDead(UUID pokemonId) {
        return deadPokemon.contains(pokemonId);
    }

    public void markPokemonDead(UUID pokemonId, String nickname, String species, int level) {
        deadPokemon.add(pokemonId);
        graveyard.put(pokemonId, new DeadPokemonInfo(nickname, species, level, System.currentTimeMillis()));
        totalDeaths++;
    }

    public Map<UUID, DeadPokemonInfo> getGraveyard() {
        return Collections.unmodifiableMap(graveyard);
    }

    public boolean isActive() {
        return active;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    public record DeadPokemonInfo(String nickname, String species, int level, long deathTimestamp) {
    }
}
