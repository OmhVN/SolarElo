package dev.solar.solarelo.placeholders;

import dev.solar.solarelo.SolarElo;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SolarEloExpansion extends PlaceholderExpansion {

    private final SolarElo plugin;

    public SolarEloExpansion(SolarElo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "solarelo";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        String lowerParam = params.toLowerCase();
        if (lowerParam.equals("leaderboard_rank") || lowerParam.equals("leaderboard_position") || lowerParam.equals("rank_position")) {
            int rank = plugin.getEloManager().getCachedRank(player.getUniqueId());
            return rank <= 0 ? "N/A" : String.valueOf(rank);
        }
        if (lowerParam.equals("rank_color")) {
            int pos = plugin.getEloManager().getCachedRank(player.getUniqueId());
            return dev.solar.solarelo.gui.EloGui.getPositionColor(plugin, pos);
        }

        dev.solar.solarelo.api.model.PlayerData data = plugin.getEloManager().getCachedData(player.getUniqueId());
        if (data == null) {
            if (params.equalsIgnoreCase("elo")) {
                return String.valueOf(plugin.getConfig().getInt("default-elo", 1000));
            }
            if (params.equalsIgnoreCase("elo_formatted")) {
                return formatNumber(plugin.getConfig().getInt("default-elo", 1000));
            }
            if (params.equalsIgnoreCase("rank")) {
                String defaultRank = plugin.getRankManager().getRank(plugin.getConfig().getInt("default-elo", 1000));
                return plugin.getRankManager().getRankDisplay(defaultRank);
            }
            if (params.equalsIgnoreCase("rank_prefix")) {
                String defaultRank = plugin.getRankManager().getRank(plugin.getConfig().getInt("default-elo", 1000));
                return plugin.getRankManager().getRankPrefix(defaultRank);
            }
            return "0";
        }

        switch (params.toLowerCase()) {
            case "elo":
                return String.valueOf(data.getElo());
            case "elo_formatted":
                return formatNumber(data.getElo());
            case "kills":
                return String.valueOf(data.getKills());
            case "kills_formatted":
                return formatNumber(data.getKills());
            case "deaths":
                return String.valueOf(data.getDeaths());
            case "deaths_formatted":
                return formatNumber(data.getDeaths());
            case "kd":
                return String.valueOf(data.getKDRatio());
            case "streak":
                return String.valueOf(data.getCurrentStreak());
            case "streak_formatted":
                return formatNumber(data.getCurrentStreak());
            case "best_streak":
                return String.valueOf(data.getBestStreak());
            case "best_streak_formatted":
                return formatNumber(data.getBestStreak());
            case "rank":
                String rankKey = plugin.getRankManager().getRank(data.getElo());
                return plugin.getRankManager().getRankDisplay(rankKey);
            case "rank_prefix":
                String rKey = plugin.getRankManager().getRank(data.getElo());
                return plugin.getRankManager().getRankPrefix(rKey);
            default:
                return null;
        }
    }

    private String formatNumber(double value) {
        boolean isNegative = value < 0;
        double absValue = Math.abs(value);

        if (absValue < 1000.0) {
            if (absValue == (long) absValue) {
                return (isNegative ? "-" : "") + (long) absValue;
            }
            return (isNegative ? "-" : "") + String.format(java.util.Locale.US, "%.2f", absValue);
        }

        String[] suffixes = new String[]{"", "K", "M", "B", "T"};
        int index = 0;
        double val = absValue;
        while (val >= 1000.0 && index < suffixes.length - 1) {
            val /= 1000.0;
            index++;
        }

        String formatted;
        if (val == (long) val) {
            formatted = (long) val + suffixes[index];
        } else {
            formatted = String.format(java.util.Locale.US, "%.1f", val) + suffixes[index];
        }

        return (isNegative ? "-" : "") + formatted;
    }
}
