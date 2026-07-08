package dev.solar.solarelo.managers;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PvpKillProcessor {

    private final SolarElo plugin;
    private final EloManager eloManager;

    public PvpKillProcessor(SolarElo plugin, EloManager eloManager) {
        this.plugin = plugin;
        this.eloManager = eloManager;
    }

    public void processKill(Player killer, Player victim) {
        org.bukkit.Location victimLoc = victim.getLocation();
        plugin.runAsync(() -> {
            PlayerData killerData = eloManager.getCachedData(killer.getUniqueId());
            if (killerData == null) {
                killerData = plugin.getDatabaseManager().loadPlayer(killer.getUniqueId(), killer.getName());
                eloManager.cachePlayer(killer.getUniqueId(), killerData);
                eloManager.initializeActivityData(killer);
            }
            PlayerData victimData = eloManager.getCachedData(victim.getUniqueId());
            if (victimData == null) {
                victimData = plugin.getDatabaseManager().loadPlayer(victim.getUniqueId(), victim.getName());
                eloManager.cachePlayer(victim.getUniqueId(), victimData);
                eloManager.initializeActivityData(victim);
            }

            if (killerData == null || victimData == null) return;

            AntiFarmResult antiFarm = handleKillAntiFarm(killer, victim, victimData, victimLoc);
            if (antiFarm != AntiFarmResult.ALLOWED && antiFarm != AntiFarmResult.DIMINISHED) return;

            int killerElo = killerData.getElo();
            int victimElo = victimData.getElo();

            boolean diffCheckEnabled = plugin.getConfig().getBoolean("anti-farm.elo-difference.enabled", true);
            int maxDiff = plugin.getConfig().getInt("anti-farm.elo-difference.max-difference", 200);
            int diff = Math.abs(killerElo - victimElo);
            if (diffCheckEnabled && diff > maxDiff) {
                plugin.getLogger().info("[Anti-Farm] Bỏ qua cộng/trừ Elo cho " + killer.getName() + " và " + victim.getName() + ". Lý do: ELO_DIFFERENCE_TOO_HIGH (Chênh lệnh: " + diff + " > " + maxDiff + ")");
                plugin.runForEntity(killer, () -> {
                    String msg = plugin.getMessageManager().get("anti-farm-elo-difference",
                                    "&cKhông thể nhận Elo do chênh lệch Elo giữa hai bên quá lớn ({difference} Elo).")
                            .replace("{difference}", String.valueOf(diff));
                    dev.solar.solarelo.managers.MessageManager.sendMessage(killer, msg);
                });
                return;
            }

            int[] changes = eloManager.getEloCalculator().calculateEloChange(killerElo, victimElo, killerData, victimData);
            int gain = changes[0];
            int loss = changes[1];

            if (antiFarm == AntiFarmResult.DIMINISHED) {
                int percent = plugin.getConfig().getInt("anti-farm.diminished-return-percent", 50);
                gain = Math.max(1, gain * percent / 100);
            }

            boolean streakEnabled = plugin.getConfig().getBoolean("kill-streak.enabled", true);
            int streakBeforeKill = killerData.getCurrentStreak();
            int bonusPercent = 0;

            if (streakEnabled && streakBeforeKill >= 1) {
                int bonusPerKill = plugin.getConfig().getInt("kill-streak.bonus-per-kill-percent", 10);
                int maxBonus = plugin.getConfig().getInt("kill-streak.max-bonus-streak", 10);
                int effectiveStreak = Math.min(streakBeforeKill, maxBonus);
                bonusPercent = effectiveStreak * bonusPerKill;
                gain = gain + (gain * bonusPercent / 100);
            }

            boolean top1BonusEnabled = plugin.getConfig().getBoolean("top-1-bonus.enabled", true);
            int top1ExtraTotal = 0;
            boolean isTop1Killed = false;

            if (top1BonusEnabled) {
                if (plugin.getDatabaseManager().getPlayerRank(victim.getUniqueId()) == 1) {
                    isTop1Killed = true;
                    int top1ExtraPercent = plugin.getConfig().getInt("top-1-bonus.extra-percent", 20);
                    int top1ExtraFlat = plugin.getConfig().getInt("top-1-bonus.extra-flat", 10);
                    top1ExtraTotal = (gain * top1ExtraPercent / 100) + top1ExtraFlat;
                    gain += top1ExtraTotal;
                }
            }

            int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
            int maxElo = plugin.getConfig().getInt("elo.maximum-elo", 50000);

            String killerOldRank = plugin.getRankManager().getRank(killerElo);
            String victimOldRank = plugin.getRankManager().getRank(victimElo);

            int victimStreakPenalty = 0;
            if (streakEnabled && victimData.getCurrentStreak() >= 1) {
                int penaltyPerStreak = plugin.getConfig().getInt("kill-streak.death-streak-penalty", 5);
                victimStreakPenalty = victimData.getCurrentStreak() * penaltyPerStreak;
                loss += victimStreakPenalty;
            }

            int victimStreakSnapshot = victimData.getCurrentStreak();
            int[] gainRef = new int[]{gain};
            List<String> bountyCommandsToRun = processBountyRewards(killer, victim, killerData, victimData, victimStreakSnapshot, gainRef);
            gain = gainRef[0];

            int finalGain = killerData.isLocked() ? 0 : gain;
            int finalLoss = victimData.isLocked() ? 0 : loss;

            int newKillerElo = Math.min(maxElo, Math.max(minElo, killerElo + finalGain));
            killerData.setElo(newKillerElo);
            killerData.addKill();

            if (killerData.isLocked()) {
                plugin.runForEntity(killer, () -> {
                    String msg = plugin.getMessageManager().get("bounty-locked-error", "&cElo của bạn đang bị khóa, không thể thay đổi Elo!");
                    dev.solar.solarelo.managers.MessageManager.sendMessage(killer, msg);
                });
            }

            if (plugin.getBountyConfig().getBoolean("bounty.enabled", true) && !killerData.isLocked()) {
                int newKillerStreak = killerData.getCurrentStreak();
                org.bukkit.configuration.ConfigurationSection streakSec = plugin.getBountyConfig().getConfigurationSection("bounty.streak-bounties");
                if (streakSec != null && streakSec.contains(String.valueOf(newKillerStreak))) {
                    int bountyElo = plugin.getBountyConfig().getInt("bounty.streak-bounties." + newKillerStreak + ".reward-elo", 0);
                    String broadcastMsg = plugin.getMessageManager().get("bounty-broadcast-streak", "")
                            .replace("{player}", killer.getName())
                            .replace("{streak}", String.valueOf(newKillerStreak))
                            .replace("{elo}", String.valueOf(bountyElo));
                    if (!broadcastMsg.isEmpty()) {
                        plugin.runSync(() -> Bukkit.broadcastMessage(EloManager.colorize(broadcastMsg)));
                    }
                }
            }

            if (!bountyCommandsToRun.isEmpty()) {
                plugin.runSync(() -> {
                    for (String cmd : bountyCommandsToRun) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), EloManager.colorize(cmd));
                    }
                });
            }

            int newVictimElo = Math.min(maxElo, Math.max(minElo, victimElo - finalLoss));
            boolean justLocked = false;
            if (newVictimElo <= minElo && !victimData.isLocked()) {
                String autoLockDurationStr = plugin.getConfig().getString("elo-lock.auto-lock-duration", "1d");
                long durationMillis = EloManager.parseTimeStringToMillis(autoLockDurationStr);
                victimData.setLockExpiry(System.currentTimeMillis() + durationMillis);
                justLocked = true;
            }
            victimData.setElo(newVictimElo);
            victimData.addDeath();

            if (justLocked) {
                plugin.runForEntity(victim, () -> {
                    String lockMsg = plugin.getMessageManager().get("elo-locked-auto", "&cElo của bạn đã bị tự động khóa do đạt mức tối thiểu!");
                    dev.solar.solarelo.managers.MessageManager.sendMessage(victim, lockMsg);
                });
            }

            org.bukkit.configuration.file.FileConfiguration adminConfig = plugin.getGuiConfigManager().getAdminConfig();
            String killReason = adminConfig.getString("reasons.kill", "⚔ Hạ gục {player}").replace("{player}", victim.getName());
            String deathReason = adminConfig.getString("reasons.death", "☠ Bị hạ gục bởi {player}").replace("{player}", killer.getName());

            applyEloChangesAndSave(killer, victim, killerData, victimData, finalGain, finalLoss, killReason, deathReason);

            broadcastKillMessage(killer, victim, killerData, victimData);

            String killerNewRank = plugin.getRankManager().getRank(killerData.getElo());
            String victimNewRank = plugin.getRankManager().getRank(victimData.getElo());

            triggerKillEffects(killer, victim, killerData, victimData, finalGain, finalLoss, killerData.getCurrentStreak(), bonusPercent, antiFarm == AntiFarmResult.DIMINISHED, isTop1Killed, top1ExtraTotal, killerOldRank, killerNewRank, victimOldRank, victimNewRank, victimStreakSnapshot);

            if (!killerOldRank.equals(killerNewRank)) {
                plugin.getRankManager().executeRankCommands(killer, killerNewRank);
            }
        });
    }

    private AntiFarmResult handleKillAntiFarm(Player killer, Player victim, PlayerData victimData, org.bukkit.Location victimLoc) {
        AntiFarmResult antiFarm = eloManager.getEloCalculator().checkAntiFarm(killer, victim, victimData, victimLoc);
        if (antiFarm != AntiFarmResult.ALLOWED && antiFarm != AntiFarmResult.DIMINISHED) {
            plugin.getLogger().info("[Anti-Farm] Bỏ qua cộng/trừ Elo cho " + killer.getName() + " và " + victim.getName() + ". Lý do: " + antiFarm.name());
        }

        if (antiFarm == AntiFarmResult.BLOCKED_COOLDOWN) {
            long lastKill = plugin.getDatabaseManager().getLastKillTime(killer.getUniqueId(), victim.getUniqueId());
            int cooldownSecs = plugin.getConfig().getInt("anti-farm.same-player-cooldown", 300);
            long remaining = (cooldownSecs * 1000L - (System.currentTimeMillis() - lastKill)) / 1000;

            plugin.runForEntity(killer, () -> {
                String msg = plugin.getMessageManager().get("anti-farm-cooldown",
                                "&cChờ {seconds}s trước khi giết {player} lại.")
                        .replace("{seconds}", String.valueOf(remaining))
                        .replace("{player}", victim.getName());
                dev.solar.solarelo.managers.MessageManager.sendMessage(killer, msg);
            });
        } else if (antiFarm == AntiFarmResult.BLOCKED_IP) {
            plugin.runForEntity(killer, () -> {
                String msg = plugin.getMessageManager().get("anti-farm-ip",
                        "&cKhông thể nhận Elo do trùng IP hoặc cùng dải IP subnet.");
                dev.solar.solarelo.managers.MessageManager.sendMessage(killer, msg);
            });
        } else if (antiFarm == AntiFarmResult.BLOCKED_AFK) {
            plugin.runForEntity(killer, () -> {
                String msg = plugin.getMessageManager().get("anti-farm-afk",
                        "&cKhông thể nhận Elo do nạn nhân đang AFK hoặc không hoạt động.");
                dev.solar.solarelo.managers.MessageManager.sendMessage(killer, msg);
            });
        } else if (antiFarm == AntiFarmResult.BLOCKED_SPAWN) {
            plugin.runForEntity(killer, () -> {
                String msg = plugin.getMessageManager().get("anti-farm-spawn",
                        "&cKhông thể nhận Elo do nạn nhân vừa hồi sinh hoặc ở gần điểm spawn.");
                dev.solar.solarelo.managers.MessageManager.sendMessage(killer, msg);
            });
        }
        return antiFarm;
    }

    private List<String> processBountyRewards(Player killer, Player victim, PlayerData killerData, PlayerData victimData, int victimStreakSnapshot, int[] gainRef) {
        List<String> bountyCommandsToRun = new ArrayList<>();
        if (!plugin.getBountyConfig().getBoolean("bounty.enabled", true) || killerData.isLocked()) {
            return bountyCommandsToRun;
        }

        if (victimStreakSnapshot > 0) {
            int highestStreakMilestone = -1;
            org.bukkit.configuration.ConfigurationSection streakSec = plugin.getBountyConfig().getConfigurationSection("bounty.streak-bounties");
            if (streakSec != null) {
                for (String key : streakSec.getKeys(false)) {
                    try {
                        int milestone = Integer.parseInt(key);
                        if (milestone <= victimStreakSnapshot && milestone > highestStreakMilestone) {
                            highestStreakMilestone = milestone;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            if (highestStreakMilestone != -1) {
                int streakBountyElo = plugin.getBountyConfig().getInt("bounty.streak-bounties." + highestStreakMilestone + ".reward-elo", 0);
                List<String> streakBountyCmds = plugin.getBountyConfig().getStringList("bounty.streak-bounties." + highestStreakMilestone + ".commands");

                gainRef[0] += streakBountyElo;

                for (String cmd : streakBountyCmds) {
                    bountyCommandsToRun.add(cmd.replace("{killer}", killer.getName())
                                              .replace("{victim}", victim.getName())
                                              .replace("{streak}", String.valueOf(victimStreakSnapshot))
                                              .replace("{milestone}", String.valueOf(highestStreakMilestone)));
                }

                String claimMsg = plugin.getMessageManager().get("bounty-claim-streak", "")
                        .replace("{killer}", killer.getName())
                        .replace("{victim}", victim.getName())
                        .replace("{streak}", String.valueOf(victimStreakSnapshot))
                        .replace("{elo}", String.valueOf(streakBountyElo));
                if (!claimMsg.isEmpty()) {
                    plugin.runSync(() -> Bukkit.broadcastMessage(EloManager.colorize(claimMsg)));
                }
            }
        }

        int victimRank = plugin.getDatabaseManager().getPlayerRank(victim.getUniqueId());
        if (victimRank > 0) {
            org.bukkit.configuration.ConfigurationSection topBountySec = plugin.getBountyConfig().getConfigurationSection("bounty.top-player-bounties");
            if (topBountySec != null && topBountySec.contains(String.valueOf(victimRank))) {
                int topBountyElo = plugin.getBountyConfig().getInt("bounty.top-player-bounties." + victimRank + ".reward-elo", 0);
                List<String> topBountyCmds = plugin.getBountyConfig().getStringList("bounty.top-player-bounties." + victimRank + ".commands");

                gainRef[0] += topBountyElo;

                for (String cmd : topBountyCmds) {
                    bountyCommandsToRun.add(cmd.replace("{killer}", killer.getName())
                                              .replace("{victim}", victim.getName())
                                              .replace("{rank}", String.valueOf(victimRank)));
                }

                String topClaimMsg = plugin.getMessageManager().get("bounty-claim-top", "")
                        .replace("{killer}", killer.getName())
                        .replace("{victim}", victim.getName())
                        .replace("{rank}", String.valueOf(victimRank))
                        .replace("{elo}", String.valueOf(topBountyElo));
                if (!topClaimMsg.isEmpty()) {
                    plugin.runSync(() -> Bukkit.broadcastMessage(EloManager.colorize(topClaimMsg)));
                }
            }
        }

        UUID activeTargetUuid = eloManager.getActiveBountyTarget(killer.getUniqueId());
        if (activeTargetUuid != null && activeTargetUuid.equals(victim.getUniqueId())) {
            eloManager.clearActiveBountyTarget(killer.getUniqueId());

            int bountyQuestElo = plugin.getBountyConfig().getInt("bounty-quest.reward-elo", 20);
            gainRef[0] += bountyQuestElo;

            long cooldownEnd = System.currentTimeMillis() + (plugin.getBountyConfig().getInt("bounty-quest.cooldown-seconds", 5400) * 1000L);
            eloManager.setBountyCooldown(killer.getUniqueId(), cooldownEnd);

            List<String> bountyQuestCmds = plugin.getBountyConfig().getStringList("bounty-quest.commands");
            if (bountyQuestCmds != null) {
                for (String cmd : bountyQuestCmds) {
                    bountyCommandsToRun.add(cmd.replace("{killer}", killer.getName())
                                             .replace("{victim}", victim.getName())
                                             .replace("{reward_elo}", String.valueOf(bountyQuestElo)));
                }
            }

            String questBroadcast = plugin.getMessageManager().get("bounty-quest-broadcast", "#00ff3c{killer} #ffffffđã hoàn thành nhiệm vụ và tiêu diệt #ff3c3c{victim}#ffffff!");
            String questCompletedMsg = plugin.getMessageManager().get("bounty-quest-completed", "#00ff3c[Nhiệm Vụ] Bạn đã tiêu diệt mục tiêu {victim} và nhận thưởng +{reward_elo} Elo!");

            plugin.runSync(() -> {
                if (questBroadcast != null && !questBroadcast.isEmpty()) {
                    Bukkit.broadcastMessage(EloManager.colorize(questBroadcast.replace("{killer}", killer.getName()).replace("{victim}", victim.getName()).replace("{reward_elo}", String.valueOf(bountyQuestElo))));
                }
                killer.sendMessage(EloManager.colorize(questCompletedMsg.replace("{victim}", victim.getName()).replace("{reward_elo}", String.valueOf(bountyQuestElo))));
            });
        }
        return bountyCommandsToRun;
    }

    private void applyEloChangesAndSave(Player killer, Player victim, PlayerData killerData, PlayerData victimData, int finalGain, int finalLoss, String killReason, String deathReason) {
        plugin.getDatabaseManager().savePlayer(killerData);
        plugin.getDatabaseManager().savePlayer(victimData);
        plugin.getDatabaseManager().recordKill(killer.getUniqueId(), victim.getUniqueId());
        plugin.getDatabaseManager().recordEloChange(killer.getUniqueId(), finalGain, killReason);
        plugin.getDatabaseManager().recordEloChange(victim.getUniqueId(), -finalLoss, deathReason);
    }

    private void triggerKillEffects(Player killer, Player victim, PlayerData killerData, PlayerData victimData, int finalGain, int finalLoss, int killerStreak, int bonusPercent, boolean diminished, boolean isTop1Killed, int top1ExtraTotal, String killerOldRank, String killerNewRank, String victimOldRank, String victimNewRank, int victimStreakSnapshot) {
        boolean streakEnabled = plugin.getConfig().getBoolean("kill-streak.enabled", true);

        if (isTop1Killed) {
            plugin.getWebhookManager().sendTop1Defeat(killer.getName(), victim.getName());
        } else {
            plugin.getWebhookManager().sendKill(killer.getName(), killerData.getElo(), victim.getName(), victimData.getElo());
        }

        if (isTop1Killed || bonusPercent > 0) {
            eloManager.playEffects(killer, "bonus");
        } else {
            eloManager.playEffects(killer, "plus");
        }
        eloManager.playEffects(victim, "minus");

        plugin.runForEntity(killer, () -> {
            String msgKey = diminished ? "anti-farm-diminished" : "kill-gain";
            String msg = plugin.getMessageManager().get(msgKey,
                            "&a+{gained} Elo | Total: {elo}")
                    .replace("{gained}", String.valueOf(finalGain))
                    .replace("{victim}", victim.getName())
                    .replace("{elo}", String.valueOf(killerData.getElo()))
                    .replace("{streak}", String.valueOf(killerStreak))
                    .replace("{bonus}", String.valueOf(bonusPercent));

            if (killerData.isSettingChat()) {
                dev.solar.solarelo.managers.MessageManager.sendMessage(killer, msg);

                if (isTop1Killed) {
                    String topMsg = plugin.getMessageManager().get("top-1-bonus",
                            "&6[TOP 1 BONUS] Bạn đã nhận thêm &e{extra} Elo &6từ việc hạ gục người đứng đầu Server!")
                            .replace("{extra}", String.valueOf(top1ExtraTotal));
                    dev.solar.solarelo.managers.MessageManager.sendMessage(killer, topMsg);
                }
            }

            if (plugin.getConfig().getBoolean("display.actionbar.enabled", true)) {
                String abFormat = plugin.getConfig().getString("display.actionbar.kill-format",
                                "&a+{gained} Elo &7| &fElo: &e{elo} &7| &fStreak: &c{streak}")
                        .replace("{gained}", String.valueOf(finalGain))
                        .replace("{elo}", String.valueOf(killerData.getElo()))
                        .replace("{streak}", String.valueOf(killerStreak))
                        .replace("{bonus}", String.valueOf(bonusPercent));
                sendActionBar(killer, abFormat);
            }

            if (killerData.isSettingTitle() && plugin.getConfig().getBoolean("display.title.enabled", true)) {
                List<Integer> announceStreaks = plugin.getConfig().getIntegerList("kill-streak.announce-streaks");
                int fadeIn = plugin.getConfig().getInt("display.title.fade-in", 5);
                int stay = plugin.getConfig().getInt("display.title.stay", 40);
                int fadeOut = plugin.getConfig().getInt("display.title.fade-out", 10);

                if (streakEnabled && announceStreaks.contains(killerStreak)) {
                    String titleStr = plugin.getConfig().getString("display.title.streak-title", "#ff3c3c{streak} KILL STREAK!");
                    String subStr = plugin.getConfig().getString("display.title.streak-subtitle", "#ffaa00+{bonus}% Elo Bonus");
                    String formattedTitle = titleStr.replace("{streak}", String.valueOf(killerStreak)).replace("{bonus}", String.valueOf(bonusPercent));
                    String formattedSub = subStr.replace("{streak}", String.valueOf(killerStreak)).replace("{bonus}", String.valueOf(bonusPercent));
                    killer.sendTitle(EloManager.colorize(formattedTitle), EloManager.colorize(formattedSub), fadeIn, stay, fadeOut);
                } else {
                    String titleStr = plugin.getConfig().getString("display.title.kill-title", "");
                    String subStr = plugin.getConfig().getString("display.title.kill-subtitle", "");
                    if ((titleStr != null && !titleStr.isEmpty()) || (subStr != null && !subStr.isEmpty())) {
                        String formattedTitle = titleStr == null ? "" : titleStr
                                .replace("{gained}", String.valueOf(finalGain))
                                .replace("{victim}", victim.getName())
                                .replace("{elo}", String.valueOf(killerData.getElo()))
                                .replace("{streak}", String.valueOf(killerStreak))
                                .replace("{bonus}", String.valueOf(bonusPercent));
                        String formattedSub = subStr == null ? "" : subStr
                                .replace("{gained}", String.valueOf(finalGain))
                                .replace("{victim}", victim.getName())
                                .replace("{elo}", String.valueOf(killerData.getElo()))
                                .replace("{streak}", String.valueOf(killerStreak))
                                .replace("{bonus}", String.valueOf(bonusPercent));
                        killer.sendTitle(EloManager.colorize(formattedTitle), EloManager.colorize(formattedSub), fadeIn, stay, fadeOut);
                    }
                }
            }
        });
    }

    private void sendActionBar(Player player, String message) {
        try {
            player.sendActionBar(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacyAmpersand().deserialize(message.replace("§", "&")));
        } catch (NoClassDefFoundError | Exception e) {
            try {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(EloManager.colorize(message)));
            } catch (Exception ignored) {}
        }
    }

    private void broadcastKillMessage(Player killer, Player victim, PlayerData killerData, PlayerData victimData) {
        dev.solar.solarelo.managers.KillMessageBroadcaster.broadcast(plugin, killer, victim, killerData, victimData);
    }
}
