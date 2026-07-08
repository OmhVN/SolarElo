package dev.solar.solarelo.api;

import dev.solar.solarelo.api.model.PlayerData;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SolarEloAPI {

    CompletableFuture<Integer> getElo(UUID uuid);

    int getEloSync(UUID uuid);

    void setElo(UUID uuid, int amount);

    void addElo(UUID uuid, int amount);

    void removeElo(UUID uuid, int amount);

    CompletableFuture<Boolean> isLocked(UUID uuid);

    boolean isLockedSync(UUID uuid);

    void setLocked(UUID uuid, boolean locked);

    String getRankName(UUID uuid);

    String getRankPrefix(UUID uuid);

    int getKills(UUID uuid);

    int getDeaths(UUID uuid);

    int getCurrentStreak(UUID uuid);

    int getBestStreak(UUID uuid);

    void resetStats(UUID uuid);
}
