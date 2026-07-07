package dev.solar.solarelo.managers;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SeasonResetProcessor {

    private final SolarElo plugin;
    private final EloManager eloManager;

    public SeasonResetProcessor(SolarElo plugin, EloManager eloManager) {
        this.plugin = plugin;
        this.eloManager = eloManager;
    }

    public void resetSeason(org.bukkit.command.CommandSender sender) {
        plugin.getMessageManager().send(sender, "season-resetting", "#ffaa00Đang tiến hành kết thúc mùa giải và trao thưởng...");

        int defaultElo = plugin.getConfig().getInt("default-elo", 1000);
        double multiplier = plugin.getSeasonConfig().getDouble("season.soft-reset.multiplier", 0.4);
        boolean resetStats = plugin.getSeasonConfig().getBoolean("season.soft-reset.reset-stats", true);

        plugin.runAsync(() -> {
            int maxPos = 1;
            org.bukkit.configuration.ConfigurationSection ranksSec = plugin.getSeasonConfig().getConfigurationSection("season.rewards.ranks");
            org.bukkit.configuration.ConfigurationSection bracketsSec = plugin.getSeasonConfig().getConfigurationSection("season.rewards.brackets");

            if (ranksSec != null) {
                for (String key : ranksSec.getKeys(false)) {
                    try {
                        int val = Integer.parseInt(key);
                        if (val > maxPos) maxPos = val;
                    } catch (NumberFormatException ignored) {}
                }
            }
            if (bracketsSec != null) {
                for (String key : bracketsSec.getKeys(false)) {
                    if (key.contains("-")) {
                        String[] parts = key.split("-");
                        if (parts.length == 2) {
                            try {
                                int val = Integer.parseInt(parts[1].trim());
                                if (val > maxPos) maxPos = val;
                            } catch (NumberFormatException ignored) {}
                        }
                    } else {
                        try {
                            int val = Integer.parseInt(key.trim());
                            if (val > maxPos) maxPos = val;
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            List<PlayerData> topList = plugin.getDatabaseManager().getTopPlayers(maxPos, 0, true);
            List<String> commandsToDispatch = buildSeasonRewardCommands(topList, ranksSec, bracketsSec);

            plugin.getDatabaseManager().softResetEloAll(defaultElo, multiplier, resetStats);
            eloManager.invalidateRankCache();

            plugin.runSync(() -> {
                applySeasonResetPostLogic(sender, commandsToDispatch, defaultElo, multiplier, resetStats);
            });
        });
    }

    private List<String> buildSeasonRewardCommands(List<PlayerData> topList, org.bukkit.configuration.ConfigurationSection ranksSec, org.bukkit.configuration.ConfigurationSection bracketsSec) {
        List<String> commandsToDispatch = new ArrayList<>();
        for (int i = 0; i < topList.size(); i++) {
            PlayerData pData = topList.get(i);
            int pos = i + 1;

            List<String> rawCommands = new ArrayList<>();
            if (ranksSec != null && ranksSec.contains(String.valueOf(pos))) {
                rawCommands.addAll(ranksSec.getStringList(String.valueOf(pos)));
            }
            if (bracketsSec != null) {
                for (String key : bracketsSec.getKeys(false)) {
                    if (key.contains("-")) {
                        String[] parts = key.split("-");
                        if (parts.length == 2) {
                            try {
                                int start = Integer.parseInt(parts[0].trim());
                                int end = Integer.parseInt(parts[1].trim());
                                if (pos >= start && pos <= end) {
                                    rawCommands.addAll(bracketsSec.getStringList(key));
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    } else {
                        try {
                            int val = Integer.parseInt(key.trim());
                            if (pos == val) {
                                rawCommands.addAll(bracketsSec.getStringList(key));
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            String msgKey = null;
            if (pos == 1) msgKey = "season-reward-top1";
            else if (pos == 2) msgKey = "season-reward-top2";
            else if (pos == 3) msgKey = "season-reward-top3";
            else if (pos >= 4 && pos <= 10) msgKey = "season-reward-top10";

            if (msgKey != null) {
                String broadcastMsg = plugin.getMessageManager().get(msgKey, "");
                if (broadcastMsg != null && !broadcastMsg.isEmpty()) {
                    String formattedMsg = broadcastMsg.replace("{player}", pData.getName())
                                                       .replace("{elo}", String.valueOf(pData.getElo()))
                                                       .replace("{pos}", String.valueOf(pos));
                    commandsToDispatch.add("broadcast " + formattedMsg);
                }
            }

            for (String cmd : rawCommands) {
                String formatted = cmd.replace("{player}", pData.getName())
                                     .replace("{elo}", String.valueOf(pData.getElo()))
                                     .replace("{kills}", String.valueOf(pData.getKills()))
                                     .replace("{deaths}", String.valueOf(pData.getDeaths()))
                                     .replace("{pos}", String.valueOf(pos));
                commandsToDispatch.add(formatted);
            }
        }
        return commandsToDispatch;
    }

    private void applySeasonResetPostLogic(org.bukkit.command.CommandSender sender, List<String> commandsToDispatch, int defaultElo, double multiplier, boolean resetStats) {
        for (String cmd : commandsToDispatch) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), EloManager.colorize(cmd));
        }

        int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
        for (PlayerData data : eloManager.getCachedPlayers()) {
            int oldElo = data.getElo();
            int newElo = (int) Math.round(defaultElo + (oldElo - defaultElo) * multiplier);
            data.setElo(newElo);
            if (newElo > minElo) {
                data.setLocked(false);
                data.setLockExpiry(0L);
            }
            if (resetStats) {
                data.setKills(0);
                data.setDeaths(0);
                data.setCurrentStreak(0);
                data.setBestStreak(0);
            }

            Player p = Bukkit.getPlayer(data.getUuid());
            if (p != null && p.isOnline()) {
                eloManager.handleEloChangeEffectsAndRank(p, oldElo, newElo, EloChangeReason.ADMIN_RESET, 0);
            }
        }

        plugin.getMessageManager().send(sender, "season-reset-success",
            "#00ff3cMùa giải đã được reset thành công! Đã trao thưởng cho các người chơi hàng đầu và thực hiện soft-reset Elo.");
    }
}
