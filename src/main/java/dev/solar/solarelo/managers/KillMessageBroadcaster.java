package dev.solar.solarelo.managers;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class KillMessageBroadcaster {

    public static void broadcast(SolarElo plugin, Player killer, Player victim, PlayerData killerData, PlayerData victimData) {
        if (!plugin.getConfig().getBoolean("broadcast.enabled", true)) return;

        String format = plugin.getConfig().getString("broadcast.format", "#555555[#ffaa00SolarElo#555555] #ffaa00{killer} #ffffffđã hạ gục #ff3c3c{victim}#ffffff!");

        String hoverFormat = plugin.getMessageManager().getRaw("hover-stats-format",
            "#ffaa00{player} #ffffffStats:\n#aaaaaaRank: &r{rank}\n#aaaaaaElo: #ffaa00{elo}\n#aaaaaaK/D: #ffaa00{kd} #aaaaaa({kills}/{deaths})\n#aaaaaaStreak: #ff3c3c{streak}");

        TextComponent message = new TextComponent();

        int killerIndex = format.indexOf("{killer}");
        int victimIndex = format.indexOf("{victim}");

        if (killerIndex == -1 || victimIndex == -1) {
            String simple = format.replace("{killer}", killer.getName()).replace("{victim}", victim.getName());
            Bukkit.broadcastMessage(EloManager.colorize(simple));
            return;
        }

        boolean killerFirst = killerIndex < victimIndex;

        int firstIndex = killerFirst ? killerIndex : victimIndex;
        int secondIndex = killerFirst ? victimIndex : killerIndex;

        String firstPlaceholder = killerFirst ? "{killer}" : "{victim}";
        String secondPlaceholder = killerFirst ? "{victim}" : "{killer}";

        Player firstPlayer = killerFirst ? killer : victim;
        Player secondPlayer = killerFirst ? victim : killer;

        PlayerData firstData = killerFirst ? killerData : victimData;
        PlayerData secondData = killerFirst ? victimData : killerData;

        String part1 = format.substring(0, firstIndex);
        String part2 = format.substring(firstIndex + firstPlaceholder.length(), secondIndex);
        String part3 = format.substring(secondIndex + secondPlaceholder.length());

        BaseComponent[] components1 = TextComponent.fromLegacyText(EloManager.colorize(part1));
        BaseComponent[] components2 = TextComponent.fromLegacyText(EloManager.colorize(part2));

        TextComponent comp1 = buildInteractivePlayerComponent(plugin, firstPlayer, firstData, hoverFormat, components1);
        TextComponent comp2 = buildInteractivePlayerComponent(plugin, secondPlayer, secondData, hoverFormat, components2);

        for (BaseComponent c : components1) {
            message.addExtra(c);
        }
        message.addExtra(comp1);
        for (BaseComponent c : components2) {
            message.addExtra(c);
        }
        message.addExtra(comp2);
        for (BaseComponent c : TextComponent.fromLegacyText(EloManager.colorize(part3))) {
            message.addExtra(c);
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(message);
        }
        Bukkit.getConsoleSender().sendMessage(EloManager.colorize(format.replace("{killer}", killer.getName()).replace("{victim}", victim.getName())));
    }

    private static TextComponent buildInteractivePlayerComponent(SolarElo plugin, Player player, PlayerData data, String hoverFormat, BaseComponent[] precedingComponents) {
        String rankKey = plugin.getRankManager().getRank(data.getElo());
        String rankDisplay = plugin.getRankManager().getRankDisplay(rankKey);

        String hoverText = EloManager.colorize(hoverFormat
            .replace("{player}", player.getName())
            .replace("{rank}", rankDisplay)
            .replace("{elo}", String.valueOf(data.getElo()))
            .replace("{kills}", String.valueOf(data.getKills()))
            .replace("{deaths}", String.valueOf(data.getDeaths()))
            .replace("{kd}", String.valueOf(data.getKDRatio()))
            .replace("{streak}", String.valueOf(data.getCurrentStreak()))
        );

        net.md_5.bungee.api.ChatColor lastColor = net.md_5.bungee.api.ChatColor.WHITE;
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strikethrough = false;
        boolean obfuscated = false;

        if (precedingComponents != null && precedingComponents.length > 0) {
            BaseComponent last = precedingComponents[precedingComponents.length - 1];
            if (last.getColor() != null) {
                lastColor = last.getColor();
            }
            bold = last.isBold();
            italic = last.isItalic();
            underline = last.isUnderlined();
            strikethrough = last.isStrikethrough();
            obfuscated = last.isObfuscated();
        }

        TextComponent comp = new TextComponent(player.getName());
        comp.setColor(lastColor);
        comp.setBold(bold);
        comp.setItalic(italic);
        comp.setUnderlined(underline);
        comp.setStrikethrough(strikethrough);
        comp.setObfuscated(obfuscated);

        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new BaseComponent[] { new TextComponent(hoverText) }
        ));
        comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
            "/elo " + player.getName()
        ));
        return comp;
    }
}
