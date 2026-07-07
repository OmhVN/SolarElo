package dev.solar.solarelo.managers;

import dev.solar.solarelo.SolarElo;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class RankManager {

    private final SolarElo plugin;
    private FileConfiguration rankConfig;
    private File rankFile;

    private final LinkedHashMap<String, Integer> rankMinElo = new LinkedHashMap<>();

    public RankManager(SolarElo plugin) {
        this.plugin = plugin;
        loadRankConfig();
    }

    public void loadRankConfig() {
        rankFile = new File(plugin.getDataFolder(), "rank.yml");
        if (!rankFile.exists()) {
            plugin.saveResource("rank.yml", false);
        }
        rankConfig = YamlConfiguration.loadConfiguration(rankFile);
        rankMinElo.clear();

        if (rankConfig.getConfigurationSection("ranks") == null) return;

        Set<String> keys = rankConfig.getConfigurationSection("ranks").getKeys(false);
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();

        for (String key : keys) {
            int minElo = rankConfig.getInt("ranks." + key + ".min-elo", 0);
            entries.add(new AbstractMap.SimpleEntry<>(key, minElo));
        }

        entries.sort(Map.Entry.comparingByValue());
        for (Map.Entry<String, Integer> e : entries) {
            rankMinElo.put(e.getKey(), e.getValue());
        }
    }

    public String getRank(int elo) {
        String current = rankMinElo.isEmpty() ? "bronze" : rankMinElo.keySet().iterator().next();
        for (Map.Entry<String, Integer> entry : rankMinElo.entrySet()) {
            if (elo >= entry.getValue()) {
                current = entry.getKey();
            }
        }
        return current;
    }

    public String getRankDisplay(String rankKey) {
        String display = rankConfig.getString("ranks." + rankKey + ".display", rankKey);
        return colorize(display);
    }

    public String getRankPrefix(String rankKey) {
        String prefix = rankConfig.getString("ranks." + rankKey + ".prefix", "");
        return colorize(prefix);
    }

    public int getRankOrdinal(String rankKey) {
        int i = 0;
        for (String key : rankMinElo.keySet()) {
            if (key.equals(rankKey)) return i;
            i++;
        }
        return 0;
    }

    public void executeRankCommands(Player player, String rankKey) {
        List<String> commands = rankConfig.getStringList("ranks." + rankKey + ".rankup-commands");
        if (commands.isEmpty()) return;

        plugin.runSync(() -> {
            for (String cmd : commands) {
                String parsed = cmd.replace("{player}", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
            }
        });
    }

    public String getNextRank(int elo) {
        String current = getRank(elo);
        boolean found = false;
        for (String key : rankMinElo.keySet()) {
            if (found) {
                return key;
            }
            if (key.equals(current)) {
                found = true;
            }
        }
        return null;
    }

    public int getEloNeededForNextRank(int elo) {
        String next = getNextRank(elo);
        if (next == null) return 0;
        int minElo = rankMinElo.getOrDefault(next, 0);
        return Math.max(0, minElo - elo);
    }

    public FileConfiguration getRankConfig() {
        return rankConfig;
    }

    public LinkedHashMap<String, Integer> getRankMinElo() {
        return rankMinElo;
    }

    private String colorize(String s) {
        return EloManager.colorize(s);
    }
}
