package dev.solar.solarelo.api;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SolarEloAPIImpl implements SolarEloAPI {

    private final SolarElo plugin;

    public SolarEloAPIImpl(SolarElo plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Integer> getElo(UUID uuid) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        plugin.runAsync(() -> {
            try {
                PlayerData data = plugin.getEloManager().getData(uuid, "");
                future.complete(data != null ? data.getElo() : plugin.getConfig().getInt("default-elo", 1000));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public int getEloSync(UUID uuid) {
        PlayerData data = plugin.getEloManager().getData(uuid, "");
        return data != null ? data.getElo() : plugin.getConfig().getInt("default-elo", 1000);
    }

    @Override
    public void setElo(UUID uuid, int amount) {
        plugin.runAsync(() -> {
            PlayerData data = plugin.getEloManager().getData(uuid, "");
            if (data != null) {
                int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
                int maxElo = plugin.getConfig().getInt("elo.maximum-elo", 50000);
                int oldElo = data.getElo();
                int newElo = Math.min(maxElo, Math.max(minElo, amount));

                data.setElo(newElo);
                plugin.getDatabaseManager().savePlayer(data);

                String reason = "Thay đổi qua API (Set " + amount + " Elo)";
                plugin.getDatabaseManager().recordEloChange(uuid, newElo - oldElo, reason);
                plugin.getEloManager().invalidateRankCache();

                org.bukkit.entity.Player online = org.bukkit.Bukkit.getPlayer(uuid);
                if (online != null && online.isOnline()) {
                    plugin.getEloManager().handleEloChangeEffectsAndRank(online, oldElo, newElo);
                }
            }
        });
    }

    @Override
    public void addElo(UUID uuid, int amount) {
        plugin.runAsync(() -> {
            PlayerData data = plugin.getEloManager().getData(uuid, "");
            if (data != null) {
                int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
                int maxElo = plugin.getConfig().getInt("elo.maximum-elo", 50000);
                int oldElo = data.getElo();
                int newElo = Math.min(maxElo, Math.max(minElo, oldElo + amount));

                data.setElo(newElo);
                plugin.getDatabaseManager().savePlayer(data);

                String reason = "Thay đổi qua API (Cộng " + amount + " Elo)";
                plugin.getDatabaseManager().recordEloChange(uuid, newElo - oldElo, reason);
                plugin.getEloManager().invalidateRankCache();

                org.bukkit.entity.Player online = org.bukkit.Bukkit.getPlayer(uuid);
                if (online != null && online.isOnline()) {
                    plugin.getEloManager().handleEloChangeEffectsAndRank(online, oldElo, newElo);
                }
            }
        });
    }

    @Override
    public void removeElo(UUID uuid, int amount) {
        plugin.runAsync(() -> {
            PlayerData data = plugin.getEloManager().getData(uuid, "");
            if (data != null) {
                int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
                int maxElo = plugin.getConfig().getInt("elo.maximum-elo", 50000);
                int oldElo = data.getElo();
                int newElo = Math.min(maxElo, Math.max(minElo, oldElo - amount));

                data.setElo(newElo);
                plugin.getDatabaseManager().savePlayer(data);

                String reason = "Thay đổi qua API (Trừ " + amount + " Elo)";
                plugin.getDatabaseManager().recordEloChange(uuid, newElo - oldElo, reason);
                plugin.getEloManager().invalidateRankCache();

                org.bukkit.entity.Player online = org.bukkit.Bukkit.getPlayer(uuid);
                if (online != null && online.isOnline()) {
                    plugin.getEloManager().handleEloChangeEffectsAndRank(online, oldElo, newElo);
                }
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> isLocked(UUID uuid) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        plugin.runAsync(() -> {
            try {
                PlayerData data = plugin.getEloManager().getData(uuid, "");
                future.complete(data != null && data.isLocked());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public boolean isLockedSync(UUID uuid) {
        PlayerData data = plugin.getEloManager().getData(uuid, "");
        return data != null && data.isLocked();
    }

    @Override
    public void setLocked(UUID uuid, boolean locked) {
        plugin.runAsync(() -> {
            PlayerData data = plugin.getEloManager().getData(uuid, "");
            if (data != null) {
                data.setLocked(locked);
                plugin.getDatabaseManager().savePlayer(data);

                org.bukkit.entity.Player online = org.bukkit.Bukkit.getPlayer(uuid);
                if (online != null && online.isOnline()) {
                    plugin.runForEntity(online, () -> {
                        String msgKey = locked ? "elo-locked-by-admin" : "elo-unlocked-by-admin";
                        String fallback = locked ? "&cElo của bạn đã bị khóa bởi Admin!" : "&aElo của bạn đã được mở khóa bởi Admin!";
                        String msg = plugin.getMessageManager().get(msgKey, fallback);
                        dev.solar.solarelo.managers.MessageManager.sendMessage(online, msg);
                    });
                }
            }
        });
    }

    @Override
    public String getRankName(UUID uuid) {
        PlayerData data = plugin.getEloManager().getData(uuid, "");
        if (data == null) return "";
        String rankKey = plugin.getRankManager().getRank(data.getElo());
        return plugin.getRankManager().getRankDisplay(rankKey);
    }

    @Override
    public String getRankPrefix(UUID uuid) {
        PlayerData data = plugin.getEloManager().getData(uuid, "");
        if (data == null) return "";
        String rankKey = plugin.getRankManager().getRank(data.getElo());
        return plugin.getRankManager().getRankPrefix(rankKey);
    }

    @Override
    public int getKills(UUID uuid) {
        PlayerData data = plugin.getEloManager().getData(uuid, "");
        return data != null ? data.getKills() : 0;
    }

    @Override
    public int getDeaths(UUID uuid) {
        PlayerData data = plugin.getEloManager().getData(uuid, "");
        return data != null ? data.getDeaths() : 0;
    }

    @Override
    public int getCurrentStreak(UUID uuid) {
        PlayerData data = plugin.getEloManager().getData(uuid, "");
        return data != null ? data.getCurrentStreak() : 0;
    }

    @Override
    public int getBestStreak(UUID uuid) {
        PlayerData data = plugin.getEloManager().getData(uuid, "");
        return data != null ? data.getBestStreak() : 0;
    }

    @Override
    public void resetStats(UUID uuid) {
        plugin.runAsync(() -> {
            PlayerData data = plugin.getEloManager().getData(uuid, "");
            if (data != null) {
                int defaultElo = plugin.getConfig().getInt("default-elo", 1000);
                data.setElo(defaultElo);
                data.setKills(0);
                data.setDeaths(0);
                data.setCurrentStreak(0);
                data.setBestStreak(0);
                plugin.getDatabaseManager().savePlayer(data);

                String reason = "Reset stats qua API";
                plugin.getDatabaseManager().recordEloChange(uuid, 0, reason);
                plugin.getEloManager().invalidateRankCache();
            }
        });
    }
}
