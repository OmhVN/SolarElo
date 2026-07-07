package dev.solar.solarelo.commands;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import dev.solar.solarelo.gui.EloGui;
import dev.solar.solarelo.managers.EloManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BountyCommand implements CommandExecutor {

    private final SolarElo plugin;

    public BountyCommand(SolarElo plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(EloManager.colorize("#ff3c3cLệnh này chỉ dùng được trong game."));
            return true;
        }

        if (!plugin.getBountyConfig().getBoolean("bounty.enabled", true) || !plugin.getGuiConfigManager().getBountyConfig().getBoolean("enabled", true)) {
            String msg = plugin.getMessageManager().get("gui-disabled-bounty", "&#ff3c3cTính năng Săn tiền thưởng hiện đang bị tắt.");
            player.sendMessage(EloManager.colorize(msg));
            return true;
        }

        PlayerData selfData = plugin.getEloManager().getData(player.getUniqueId(), player.getName());
        if (selfData != null && selfData.isLocked()) {
            String msg = plugin.getMessageManager().get("bounty-locked-error", "&cElo của bạn đang bị khóa, không thể sử dụng tính năng săn thưởng!");
            player.sendMessage(EloManager.colorize(msg));
            return true;
        }

        EloGui.openBounty(plugin, player);
        return true;
    }
}
