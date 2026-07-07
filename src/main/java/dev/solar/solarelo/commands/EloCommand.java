package dev.solar.solarelo.commands;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EloCommand implements CommandExecutor, TabCompleter {

    private final SolarElo plugin;

    public EloCommand(SolarElo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("toggle")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(colorize("#ff3c3cLệnh này chỉ dùng được trong game."));
                return true;
            }
            if (args.length < 2) {
                player.sendMessage(colorize("#ffaa00[SolarElo] #ffffffSử dụng: #ffaa00/elo toggle <chat|title|bounty>"));
                return true;
            }
            String sub = args[1].toLowerCase();
            plugin.runAsync(() -> {
                PlayerData data = plugin.getEloManager().getData(player.getUniqueId(), player.getName());
                if (data == null) return;

                if (sub.equals("chat")) {
                    data.setSettingChat(!data.isSettingChat());
                    plugin.getDatabaseManager().savePlayer(data);
                    String status = data.isSettingChat() ? "&#00ff3cBật" : "&#ff3c3cTắt";
                    player.sendMessage(colorize("#ffaa00[SolarElo] #ffffffĐã " + status + " #ffffffthông báo chat."));
                } else if (sub.equals("title")) {
                    data.setSettingTitle(!data.isSettingTitle());
                    plugin.getDatabaseManager().savePlayer(data);
                    String status = data.isSettingTitle() ? "&#00ff3cBật" : "&#ff3c3cTắt";
                    player.sendMessage(colorize("#ffaa00[SolarElo] #ffffffĐã " + status + " #ffffffthông báo Title/ActionBar."));
                } else if (sub.equals("bounty")) {
                    data.setSettingWelcomeEffect(!data.isSettingWelcomeEffect());
                    plugin.getDatabaseManager().savePlayer(data);
                    String status = data.isSettingWelcomeEffect() ? "&#00ff3cBật" : "&#ff3c3cTắt";
                    player.sendMessage(colorize("#ffaa00[SolarElo] #ffffffĐã " + status + " #ffffffthông báo khi bị săn truy nã."));
                } else {
                    player.sendMessage(colorize("#ffaa00[SolarElo] #ffffffSử dụng: #ffaa00/elo toggle <chat|title|bounty>"));
                }
            });
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(colorize("#ff3c3cLệnh này chỉ dùng được trong game."));
                return true;
            }

            if (!plugin.getGuiConfigManager().getLeaderboardConfig().getBoolean("enabled", true)) {
                plugin.runAsync(() -> {
                    PlayerData cached = plugin.getEloManager().getCachedData(player.getUniqueId());
                    PlayerData data = cached != null ? cached : plugin.getDatabaseManager().loadPlayer(player.getUniqueId(), player.getName());
                    plugin.runSync(() -> {
                        if (data != null) {
                            sendStats(sender, data, true);
                        }
                    });
                });
                return true;
            }

            dev.solar.solarelo.gui.EloGui.openMainMenu(plugin, player);
            return true;
        }

        String targetName = args[0];
        plugin.runAsync(() -> {
            Player online = Bukkit.getPlayerExact(targetName);
            PlayerData data;
            if (online != null) {
                PlayerData cached = plugin.getEloManager().getCachedData(online.getUniqueId());
                data = cached != null ? cached : plugin.getDatabaseManager().loadPlayer(online.getUniqueId(), online.getName());
            } else {
                data = plugin.getDatabaseManager().getPlayerByName(targetName);
            }
            plugin.runSync(() -> {
                if (data == null) {
                    plugin.getMessageManager().send(sender, "player-not-found", "#ff3c3cKhông tìm thấy player.");
                    return;
                }
                if (sender instanceof Player player && plugin.getGuiConfigManager().getStatsConfig().getBoolean("enabled", true)) {
                    dev.solar.solarelo.gui.EloGui.openStats(plugin, player, data.getName(), 1, "HIGH_TO_LOW");
                } else {
                    sendStats(sender, data, false);
                }
            });
        });

        return true;
    }

    private void sendStats(CommandSender sender, PlayerData data, boolean isSelf) {
        String rankKey = plugin.getRankManager().getRank(data.getElo());
        String rankDisplay = plugin.getRankManager().getRankDisplay(rankKey);

        String msgKey = isSelf ? "elo-info-own" : "elo-info";
        String msg = plugin.getMessageManager().get(msgKey,
                        "#555555[#ffaa00SolarElo#555555] #ffaa00{player} #555555| #ffffffElo: #ffaa00{elo}")
                .replace("{player}", data.getName())
                .replace("{elo}", String.valueOf(data.getElo()))
                .replace("{rank}", rankDisplay)
                .replace("{kills}", String.valueOf(data.getKills()))
                .replace("{deaths}", String.valueOf(data.getDeaths()))
                .replace("{kd}", String.valueOf(data.getKDRatio()))
                .replace("{streak}", String.valueOf(data.getCurrentStreak()))
                .replace("{best_streak}", String.valueOf(data.getBestStreak()));

        dev.solar.solarelo.managers.MessageManager.sendMessage(sender, msg);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("toggle");
            list.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            return list.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("toggle")) {
            return java.util.Arrays.asList("chat", "title", "bounty").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private String colorize(String s) {
        return dev.solar.solarelo.managers.EloManager.colorize(s);
    }
}
