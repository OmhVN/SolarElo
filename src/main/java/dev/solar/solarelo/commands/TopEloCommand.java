package dev.solar.solarelo.commands;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class TopEloCommand implements CommandExecutor {

    private final SolarElo plugin;

    public TopEloCommand(SolarElo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!plugin.getGuiConfigManager().getLeaderboardConfig().getBoolean("enabled", true)) {
            plugin.runAsync(() -> {
                java.util.List<PlayerData> dbPlayers = plugin.getDatabaseManager().getTopPlayers(60, 0, true);
                java.util.List<PlayerData> top = new java.util.ArrayList<>();
                for (PlayerData pd : dbPlayers) {
                    org.bukkit.entity.Player onlinePlayer = org.bukkit.Bukkit.getPlayer(pd.getUuid());
                    if (onlinePlayer != null && plugin.getEloManager().isIpBlocked(onlinePlayer)) {
                        continue;
                    }
                    top.add(pd);
                    if (top.size() >= 10) {
                        break;
                    }
                }
                plugin.runSync(() -> {
                    sender.sendMessage(dev.solar.solarelo.managers.EloManager.colorize("#555555&m         &r #ffaa00Top 10 Elo #555555&m         "));
                    int rank = 1;
                    for (PlayerData pd : top) {
                        sender.sendMessage(dev.solar.solarelo.managers.EloManager.colorize(
                            "#ffaa00#" + rank + " #ffffff" + pd.getName() + " #aaaaaa- #ffaa00" + pd.getElo() + " ELO"
                        ));
                        rank++;
                    }
                    sender.sendMessage(dev.solar.solarelo.managers.EloManager.colorize("#555555&m                                           "));
                });
            });
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(dev.solar.solarelo.managers.EloManager.colorize("#ff3c3cLệnh này chỉ dùng được trong game."));
            return true;
        }

        dev.solar.solarelo.gui.EloGui.openLeaderboard(plugin, player, 1, "HIGH_TO_LOW");
        return true;
    }
}
