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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EloAdminCommand implements CommandExecutor, TabCompleter {

    private final SolarElo plugin;

    public EloAdminCommand(SolarElo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("solarelo.admin")) {
            plugin.getMessageManager().send(sender, "no-permission", "#ff3c3cBạn không có quyền làm điều này.");
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player player) {
                if (plugin.getGuiConfigManager().getAdminConfig().getBoolean("enabled", true)) {
                    dev.solar.solarelo.gui.EloGui.openEloAdmin(plugin, player);
                } else {
                    plugin.getMessageManager().send(player, "gui-disabled-admin", "&cTính năng Quản trị hiện đang bị tắt.");
                }
            } else {
                sendHelp(sender);
            }
            return true;
        }

        if (args.length == 1) {
            String targetName = args[0];
            if (!Arrays.asList("reload", "season", "reset", "set", "add", "remove", "search", "lock", "unlock").contains(targetName.toLowerCase())) {
                if (sender instanceof Player player) {
                    Player online = Bukkit.getPlayerExact(targetName);
                    UUID uuid;
                    String name;
                    if (online != null) {
                        uuid = online.getUniqueId();
                        name = online.getName();
                    } else {
                        PlayerData offline = plugin.getDatabaseManager().getPlayerByName(targetName);
                        if (offline != null) {
                            uuid = offline.getUuid();
                            name = offline.getName();
                        } else {
                            plugin.getMessageManager().send(sender, "player-not-found", "&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cKhông tìm thấy người chơi này.");
                            return true;
                        }
                    }

                    if (plugin.getGuiConfigManager().getAdminConfig().getBoolean("enabled", true)) {
                        dev.solar.solarelo.gui.EloGui.openEloAdminDetail(plugin, player, uuid, name);
                    } else {
                        plugin.getMessageManager().send(player, "gui-disabled-admin", "&cTính năng Quản trị hiện đang bị tắt.");
                    }
                    return true;
                }
            }
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("search")) {
            if (args.length < 2) {
                sender.sendMessage(colorize("&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cSai cú pháp! Sử dụng: &#ffffff/eloadmin search <player>"));
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(colorize("&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cLệnh này chỉ dành cho người chơi!"));
                return true;
            }
            String targetName = args[1];
            Player online = Bukkit.getPlayerExact(targetName);
            UUID uuid;
            String name;
            if (online != null) {
                uuid = online.getUniqueId();
                name = online.getName();
            } else {
                PlayerData offline = plugin.getDatabaseManager().getPlayerByName(targetName);
                if (offline == null) {
                    plugin.getMessageManager().send(sender, "player-not-found", "&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cKhông tìm thấy người chơi này.");
                    return true;
                }
                uuid = offline.getUuid();
                name = offline.getName();
            }

            if (plugin.getGuiConfigManager().getAdminConfig().getBoolean("enabled", true)) {
                dev.solar.solarelo.gui.EloGui.openEloAdminDetail(plugin, player, uuid, name);
            } else {
                plugin.getMessageManager().send(player, "gui-disabled-admin", "&cTính năng Quản trị hiện đang bị tắt.");
            }
            return true;
        }

        if (sub.equals("lock") || sub.equals("unlock")) {
            if (args.length < 2) {
                sender.sendMessage(colorize("&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cSai cú pháp! Sử dụng: &#ffffff/eloadmin " + sub + " <player>"));
                return true;
            }
            String target = args[1];
            Player online = Bukkit.getPlayerExact(target);
            UUID uuid;
            String name;
            PlayerData data;
            if (online != null) {
                uuid = online.getUniqueId();
                name = online.getName();
                data = plugin.getEloManager().getData(uuid, name);
            } else {
                PlayerData offline = plugin.getDatabaseManager().getPlayerByName(target);
                if (offline == null) {
                    plugin.getMessageManager().send(sender, "player-not-found", "&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cKhông tìm thấy người chơi này.");
                    return true;
                }
                uuid = offline.getUuid();
                name = offline.getName();
                data = offline;
            }

            boolean lockState = sub.equals("lock");
            data.setLocked(lockState);
            data.setLockExpiry(0L);
            plugin.getDatabaseManager().savePlayer(data);
            if (plugin.getEloManager().getCachedData(uuid) != null) {
                plugin.getEloManager().getCachedData(uuid).setLocked(lockState);
                plugin.getEloManager().getCachedData(uuid).setLockExpiry(0L);
            }

            if (lockState) {
                String msg = plugin.getMessageManager().get("admin-lock-success", "&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã khóa Elo của player &#ffffff{player} &fthành công.")
                        .replace("{player}", name);
                dev.solar.solarelo.managers.MessageManager.sendMessage(sender, msg);

                if (online != null && online.isOnline()) {
                    String userMsg = plugin.getMessageManager().get("elo-locked-by-admin", "&#ff3c3cElo của bạn đã bị khóa bởi Admin!");
                    dev.solar.solarelo.managers.MessageManager.sendMessage(online, userMsg);
                }
            } else {
                String msg = plugin.getMessageManager().get("admin-unlock-success", "&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã mở khóa Elo của player &#ffffff{player} &fthành công.")
                        .replace("{player}", name);
                dev.solar.solarelo.managers.MessageManager.sendMessage(sender, msg);

                if (online != null && online.isOnline()) {
                    String userMsg = plugin.getMessageManager().get("elo-unlocked-by-admin", "&#00ff3cElo của bạn đã được mở khóa bởi Admin!");
                    dev.solar.solarelo.managers.MessageManager.sendMessage(online, userMsg);
                }
            }
            return true;
        }

        if (sub.equals("reload")) {
            plugin.reloadConfig();
            plugin.getMessageManager().load();
            plugin.getRankManager().loadRankConfig();
            plugin.getGuiConfigManager().reloadConfigs();
            plugin.getEffectManager().load();
            plugin.getMessageManager().send(sender, "reload", "#00ff3cReload config thành công!");
            return true;
        }

        if (sub.equals("season")) {
            if (!plugin.getSeasonConfig().getBoolean("season.enabled", true)) {
                plugin.getMessageManager().send(sender, "season-disabled-error", "&#ff3c3cTính năng Mùa giải hiện đang bị tắt.");
                return true;
            }
            if (args.length < 2 || !args[1].equalsIgnoreCase("reset")) {
                sender.sendMessage(colorize("&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cSai cú pháp! Sử dụng: &#ffffff/eloadmin season reset"));
                return true;
            }
            plugin.getEloManager().resetSeason(sender);
            return true;
        }

        if (sub.equals("reset")) {
            if (args.length < 2) {
                sender.sendMessage(colorize("&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cSai cú pháp! Sử dụng: &#ffffff/eloadmin reset <player/*>"));
                return true;
            }
            String target = args[1];
            if (target.equals("*")) {
                plugin.getEloManager().resetEloAll();
                String msg = plugin.getMessageManager().get("admin-reset", "&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã reset ELO của &#ffffff{player} &fvề mặc định.")
                        .replace("{player}", "Tất cả người chơi");
                dev.solar.solarelo.managers.MessageManager.sendMessage(sender, msg);
            } else {
                Player online = Bukkit.getPlayerExact(target);
                UUID uuid;
                String name;
                if (online != null) {
                    uuid = online.getUniqueId();
                    name = online.getName();
                } else {
                    PlayerData offline = plugin.getDatabaseManager().getPlayerByName(target);
                    if (offline == null) {
                        plugin.getMessageManager().send(sender, "player-not-found", "&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cKhông tìm thấy người chơi này.");
                        return true;
                    }
                    uuid = offline.getUuid();
                    name = offline.getName();
                }
                plugin.getEloManager().resetElo(uuid, name);
                String msg = plugin.getMessageManager().get("admin-reset", "&#00ff3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &fĐã reset ELO của &#ffffff{player} &fvề mặc định.")
                        .replace("{player}", name);
                dev.solar.solarelo.managers.MessageManager.sendMessage(sender, msg);
            }
            return true;
        }

        if (sub.equals("set") || sub.equals("add") || sub.equals("remove")) {
            if (args.length < 3) {
                sender.sendMessage(colorize("&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cSai cú pháp! Sử dụng: &#ffffff/eloadmin " + sub + " <player/*> <amount>"));
                return true;
            }
            String target = args[1];
            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(colorize("&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cSố lượng ELO phải là số nguyên!"));
                return true;
            }

            if (target.equals("*")) {
                switch (sub) {
                    case "set" -> {
                        plugin.getEloManager().setEloAll(amount);
                        String msg = plugin.getMessageManager().get("admin-set", "#00ff3cĐã set Elo {player} thành {elo}")
                                .replace("{player}", "Tất cả người chơi").replace("{elo}", String.valueOf(amount));
                        dev.solar.solarelo.managers.MessageManager.sendMessage(sender, msg);
                    }
                    case "add" -> {
                        plugin.getEloManager().addEloAll(amount);
                        String msg = plugin.getMessageManager().get("admin-add", "#00ff3cĐã cộng {amount} Elo cho {player}")
                                .replace("{player}", "Tất cả người chơi").replace("{amount}", String.valueOf(amount));
                        dev.solar.solarelo.managers.MessageManager.sendMessage(sender, msg);
                    }
                    case "remove" -> {
                        plugin.getEloManager().removeEloAll(amount);
                        String msg = plugin.getMessageManager().get("admin-remove", "#ff3c3cĐã trừ {amount} Elo của {player}")
                                .replace("{player}", "Tất cả người chơi").replace("{amount}", String.valueOf(amount));
                        dev.solar.solarelo.managers.MessageManager.sendMessage(sender, msg);
                    }
                }
            } else {
                Player online = Bukkit.getPlayerExact(target);
                UUID uuid;
                String name;
                if (online != null) {
                    uuid = online.getUniqueId();
                    name = online.getName();
                } else {
                    PlayerData offline = plugin.getDatabaseManager().getPlayerByName(target);
                    if (offline == null) {
                        plugin.getMessageManager().send(sender, "player-not-found", "&#ff3c3cᴇʟᴏ ᴀᴅᴍɪɴ &8» &cKhông tìm thấy người chơi này.");
                        return true;
                    }
                    uuid = offline.getUuid();
                    name = offline.getName();
                }

                switch (sub) {
                    case "set" -> {
                        plugin.getEloManager().setElo(uuid, name, amount);
                        String msg = plugin.getMessageManager().get("admin-set", "#00ff3cĐã set Elo {player} thành {elo}")
                                .replace("{player}", name).replace("{elo}", String.valueOf(amount));
                        dev.solar.solarelo.managers.MessageManager.sendMessage(sender, msg);
                    }
                    case "add" -> {
                        plugin.getEloManager().addElo(uuid, name, amount);
                        String msg = plugin.getMessageManager().get("admin-add", "#00ff3cĐã cộng {amount} Elo cho {player}")
                                .replace("{player}", name).replace("{amount}", String.valueOf(amount));
                        dev.solar.solarelo.managers.MessageManager.sendMessage(sender, msg);
                    }
                    case "remove" -> {
                        plugin.getEloManager().removeElo(uuid, name, amount);
                        String msg = plugin.getMessageManager().get("admin-remove", "#ff3c3cĐã trừ {amount} Elo của {player}")
                                .replace("{player}", name).replace("{amount}", String.valueOf(amount));
                        dev.solar.solarelo.managers.MessageManager.sendMessage(sender, msg);
                    }
                }
            }
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(colorize("&#ffaa00&m────────────────────────────────────────────────────"));
        sender.sendMessage(colorize("&#ffaa00sᴏʟᴀʀᴇʟᴏ ᴀᴅᴍɪɴ ʜᴇʟᴘ"));
        sender.sendMessage(colorize("  &8▪ &#ffaa00/eloadmin search <player> &7- Tìm kiếm & xem chi tiết Elo"));
        sender.sendMessage(colorize("  &8▪ &#ffaa00/eloadmin lock <player> &7- Khóa Elo của người chơi"));
        sender.sendMessage(colorize("  &8▪ &#ffaa00/eloadmin unlock <player> &7- Mở khóa Elo của người chơi"));
        sender.sendMessage(colorize("  &8▪ &#ffaa00/eloadmin set <player/*> <amount> &7- Đặt Elo"));
        sender.sendMessage(colorize("  &8▪ &#ffaa00/eloadmin add <player/*> <amount> &7- Cộng Elo"));
        sender.sendMessage(colorize("  &8▪ &#ffaa00/eloadmin remove <player/*> <amount> &7- Trừ Elo"));
        sender.sendMessage(colorize("  &8▪ &#ffaa00/eloadmin reset <player/*> &7- Reset Elo"));
        sender.sendMessage(colorize("  &8▪ &#ffaa00/eloadmin season reset &7- Reset mùa giải & trao thưởng"));
        sender.sendMessage(colorize("  &8▪ &#ffaa00/eloadmin reload &7- Reload config"));
        sender.sendMessage(colorize("&#ffaa00&m────────────────────────────────────────────────────"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("solarelo.admin")) return new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("set", "add", "remove", "reset", "reload", "season", "search", "lock", "unlock"));
            subs.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            return subs.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("season")) {
                return Arrays.asList("reset").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (Arrays.asList("set", "add", "remove", "reset", "search", "lock", "unlock").contains(sub)) {
                List<String> list = new ArrayList<>();
                if (!sub.equals("search") && !sub.equals("lock") && !sub.equals("unlock")) {
                    list.add("*");
                }
                list.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                return list.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }

    private String colorize(String s) {
        return dev.solar.solarelo.managers.EloManager.colorize(s);
    }
}
