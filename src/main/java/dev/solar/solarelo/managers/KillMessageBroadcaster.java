package dev.solar.solarelo.managers;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class KillMessageBroadcaster {

    public static void broadcast(SolarElo plugin, Player killer, Player victim, PlayerData killerData, PlayerData victimData) {
        if (!plugin.getConfig().getBoolean("broadcast.enabled", true)) return;

        String format = plugin.getConfig().getString("broadcast.format", "#555555[#ffaa00SolarElo#555555] #ffaa00{killer} #ffffffđã hạ gục #ff3c3c{victim}#ffffff!");
        String hoverFormat = plugin.getMessageManager().getRaw("hover-stats-format",
            "#ffaa00{player} #ffffffStats:\n#aaaaaaRank: &r{rank}\n#aaaaaaElo: #ffaa00{elo}\n#aaaaaaK/D: #ffaa00{kd} #aaaaaa({kills}/{deaths})\n#aaaaaaStreak: #ff3c3c{streak}");

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

        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

        Component comp1 = buildInteractivePlayerComponent(plugin, firstPlayer, firstData, hoverFormat);
        Component comp2 = buildInteractivePlayerComponent(plugin, secondPlayer, secondData, hoverFormat);

        Component finalMessage = serializer.deserialize(EloManager.colorize(part1))
                .append(comp1)
                .append(serializer.deserialize(EloManager.colorize(part2)))
                .append(comp2)
                .append(serializer.deserialize(EloManager.colorize(part3)));

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(finalMessage);
        }
        Bukkit.getConsoleSender().sendMessage(EloManager.colorize(format.replace("{killer}", killer.getName()).replace("{victim}", victim.getName())));
    }

    private static Component buildInteractivePlayerComponent(SolarElo plugin, Player player, PlayerData data, String hoverFormat) {
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

        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

        return Component.text(player.getName())
                .hoverEvent(HoverEvent.showText(serializer.deserialize(hoverText)))
                .clickEvent(ClickEvent.runCommand("/elo " + player.getName()));
    }
}
