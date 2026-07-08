package dev.solar.solarelo.managers;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import dev.solar.solarelo.api.model.ScoringMode;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class EloCalculator {

    private final SolarElo plugin;
    private final EloManager eloManager;
    private final Random random = new Random();

    public EloCalculator(SolarElo plugin, EloManager eloManager) {
        this.plugin = plugin;
        this.eloManager = eloManager;
    }

    private ScoringMode getScoringMode() {
        String modeStr = plugin.getConfig().getString("scoring-mode", "FORMULA");
        if (modeStr == null) modeStr = "FORMULA";
        modeStr = modeStr.trim().toUpperCase();
        switch (modeStr) {
            case "RANDOM":
                return ScoringMode.RANDOM;
            case "KD":
                return ScoringMode.KD;
            case "CUSTOM":
                return ScoringMode.CUSTOM;
            case "FORMULA":
                return ScoringMode.FORMULA;
            default:
                plugin.getLogger().warning("scoring-mode không hợp lệ: " + modeStr + " — dùng FORMULA");
                return ScoringMode.FORMULA;
        }
    }

    public int[] calculateEloChange(int killerElo, int victimElo,
                                     PlayerData killerData, PlayerData victimData) {
        ScoringMode mode = getScoringMode();
        return switch (mode) {
            case RANDOM -> calculateRandom();
            case KD -> calculateKD(killerData, victimData);
            case CUSTOM -> calculateCustom(killerElo, victimElo, killerData, victimData);
            default -> calculateFormula(killerElo, victimElo);
        };
    }

    private int[] calculateCustom(int killerElo, int victimElo, PlayerData killerData, PlayerData victimData) {
        String formulaGain = plugin.getConfig().getString("elo.custom.formula-gain", "32 * (1 - 1 / (1 + 10^((victim_elo - killer_elo) / 400)))");
        String formulaLoss = plugin.getConfig().getString("elo.custom.formula-loss", "32 * (1 - (1 - 1 / (1 + 10^((victim_elo - killer_elo) / 400))))");

        double killerKD = killerData.getKDRatio();
        double victimKD = victimData.getKDRatio();
        int killerStreak = killerData.getCurrentStreak();
        int victimStreak = victimData.getCurrentStreak();
        int kFactor = plugin.getConfig().getInt("elo.k-factor", 32);

        int gain = (int) Math.round(evaluateFormula(formulaGain, killerElo, victimElo, killerKD, victimKD, killerStreak, victimStreak, kFactor));
        int loss = (int) Math.round(evaluateFormula(formulaLoss, killerElo, victimElo, killerKD, victimKD, killerStreak, victimStreak, kFactor));

        gain = Math.max(1, gain);
        loss = Math.max(1, loss);

        return new int[]{gain, loss};
    }

    private double evaluateFormula(String formula, int killerElo, int victimElo, double killerKD, double victimKD, int killerStreak, int victimStreak, int kFactor) {
        return dev.solar.solarelo.utils.FormulaEvaluator.evaluateFormula(plugin, formula, killerElo, victimElo, killerKD, victimKD, killerStreak, victimStreak, kFactor);
    }

    private int[] calculateFormula(int killerElo, int victimElo) {
        int k = plugin.getConfig().getInt("elo.k-factor", 32);
        double expectedKiller = 1.0 / (1.0 + Math.pow(10, (victimElo - killerElo) / 400.0));
        double expectedVictim = 1.0 - expectedKiller;

        int gain = (int) Math.round(k * (1.0 - expectedKiller));
        int loss = (int) Math.round(k * (1.0 - expectedVictim));

        gain = Math.max(1, gain);
        loss = Math.max(1, loss);
        return new int[]{gain, loss};
    }

    private int[] calculateRandom() {
        int minGain = plugin.getConfig().getInt("elo.random.min-gain", 5);
        int maxGain = plugin.getConfig().getInt("elo.random.max-gain", 25);
        int minLoss = plugin.getConfig().getInt("elo.random.min-loss", 5);
        int maxLoss = plugin.getConfig().getInt("elo.random.max-loss", 25);

        int gain = minGain + random.nextInt(Math.max(1, maxGain - minGain + 1));
        int loss = minLoss + random.nextInt(Math.max(1, maxLoss - minLoss + 1));
        return new int[]{gain, loss};
    }

    private int[] calculateKD(PlayerData killer, PlayerData victim) {
        int base = plugin.getConfig().getInt("elo.kd.base", 20);
        int minGain = plugin.getConfig().getInt("elo.kd.min-gain", 3);
        int maxGain = plugin.getConfig().getInt("elo.kd.max-gain", 50);
        int minLoss = plugin.getConfig().getInt("elo.kd.min-loss", 3);
        int maxLoss = plugin.getConfig().getInt("elo.kd.max-loss", 50);

        double killerKD = killer.getKDRatio();
        double victimKD = victim.getKDRatio();

        if (killerKD <= 0) killerKD = 0.1;
        if (victimKD <= 0) victimKD = 0.1;

        double gainMultiplier = victimKD / killerKD;
        int gain = (int) Math.round(base * gainMultiplier);
        gain = Math.max(minGain, Math.min(maxGain, gain));

        double lossMultiplier = killerKD / victimKD;
        int loss = (int) Math.round(base * lossMultiplier);
        loss = Math.max(minLoss, Math.min(maxLoss, loss));

        return new int[]{gain, loss};
    }

    public AntiFarmResult checkAntiFarm(Player killer, Player victim, PlayerData victimData, org.bukkit.Location victimLoc) {
        if (!plugin.getConfig().getBoolean("anti-farm.enabled", true)) {
            return AntiFarmResult.ALLOWED;
        }

        UUID killerId = killer.getUniqueId();
        UUID victimId = victim.getUniqueId();

        if (plugin.getConfig().getBoolean("anti-farm.ip-check.enabled", true)) {
            if (isSameIPOrSubnet(killer, victim)) {
                return AntiFarmResult.BLOCKED_IP;
            }
        }

        if (plugin.getConfig().getBoolean("anti-farm.activity-check.enabled", true)) {
            if (plugin.getConfig().getBoolean("anti-farm.activity-check.spawn-camping.enabled", true)) {
                long spawnTime = eloManager.getLastSpawnTimes().getOrDefault(victimId, 0L);
                long elapsedSecs = (System.currentTimeMillis() - spawnTime) / 1000L;
                long protSecs = plugin.getConfig().getInt("anti-farm.activity-check.spawn-camping.protection-seconds", 10);

                boolean isNearSpawn = false;
                org.bukkit.Location spawnLoc = eloManager.getLastSpawnLocations().get(victimId);
                if (spawnLoc != null && spawnLoc.getWorld() != null && victimLoc != null && victimLoc.getWorld() != null) {
                    if (spawnLoc.getWorld().equals(victimLoc.getWorld())) {
                        double radius = plugin.getConfig().getDouble("anti-farm.activity-check.spawn-camping.protection-radius", 15);
                        if (spawnLoc.distanceSquared(victimLoc) <= radius * radius) {
                            isNearSpawn = true;
                        }
                    }
                }

                if (elapsedSecs < protSecs && isNearSpawn) {
                    String actionStr = plugin.getConfig().getString("anti-farm.activity-check.spawn-camping.action", "BLOCK");
                    if (actionStr != null) actionStr = actionStr.trim();
                    if ("BLOCK".equalsIgnoreCase(actionStr)) {
                        return AntiFarmResult.BLOCKED_SPAWN;
                    } else if ("DIMINISH".equalsIgnoreCase(actionStr)) {
                        return AntiFarmResult.DIMINISHED;
                    }
                }
            }

            int noMoveSecs = plugin.getConfig().getInt("anti-farm.activity-check.no-move-seconds", 15);
            if (noMoveSecs > 0 && (victimData == null || victimData.getDeaths() == 0)) {
                long lastMove = eloManager.getLastMoveTimes().getOrDefault(victimId, 0L);
                long elapsed = (System.currentTimeMillis() - lastMove) / 1000L;
                if (elapsed >= noMoveSecs) {
                    return AntiFarmResult.BLOCKED_AFK;
                }
            }

            int noAttackSecs = plugin.getConfig().getInt("anti-farm.activity-check.no-attack-seconds", 15);
            if (noAttackSecs > 0) {
                long lastAttack = eloManager.getLastAttackTimes().getOrDefault(victimId, 0L);
                long elapsed = (System.currentTimeMillis() - lastAttack) / 1000L;
                if (elapsed >= noAttackSecs) {
                    return AntiFarmResult.BLOCKED_AFK;
                }
            }
        }

        int cooldownSecs = plugin.getConfig().getInt("anti-farm.same-player-cooldown", 300);
        if (cooldownSecs > 0) {
            long lastKill = plugin.getDatabaseManager().getLastKillTime(killerId, victimId);
            if (lastKill > 0 && System.currentTimeMillis() - lastKill < cooldownSecs * 1000L) {
                return AntiFarmResult.BLOCKED_COOLDOWN;
            }
        }

        int threshold = plugin.getConfig().getInt("anti-farm.repeat-kill-threshold", 3);
        if (threshold > 0) {
            long oneHourAgo = System.currentTimeMillis() - 3600000L;
            int recentKills = plugin.getDatabaseManager().getRecentKillCount(killerId, victimId, oneHourAgo);
            if (recentKills >= threshold) {
                return AntiFarmResult.DIMINISHED;
            }
        }

        return AntiFarmResult.ALLOWED;
    }

    private boolean isSameIPOrSubnet(Player killer, Player victim) {
        if (killer.getAddress() == null || victim.getAddress() == null) {
            return false;
        }

        String killerIP = killer.getAddress().getAddress().getHostAddress();
        String victimIP = victim.getAddress().getAddress().getHostAddress();

        if (plugin.getConfig().getBoolean("anti-farm.ip-check.prevent-same-ip", true)) {
            if (killerIP.equals(victimIP)) {
                return true;
            }
        }

        if (plugin.getConfig().getBoolean("anti-farm.ip-check.prevent-same-subnet", true)) {
            if (isSameSubnet(killerIP, victimIP)) {
                return true;
            }
        }

        return false;
    }

    private boolean isSameSubnet(String ip1, String ip2) {
        if (ip1 == null || ip2 == null) return false;
        if (ip1.equals(ip2)) return true;

        if (ip1.contains(".") && ip2.contains(".")) {
            String[] parts1 = ip1.split("\\.");
            String[] parts2 = ip2.split("\\.");
            if (parts1.length >= 3 && parts2.length >= 3) {
                return parts1[0].equals(parts2[0]) &&
                       parts1[1].equals(parts2[1]) &&
                       parts1[2].equals(parts2[2]);
            }
        }

        if (ip1.contains(":") && ip2.contains(":")) {
            String[] parts1 = ip1.split(":");
            String[] parts2 = ip2.split(":");
            if (parts1.length >= 4 && parts2.length >= 4) {
                return parts1[0].equalsIgnoreCase(parts2[0]) &&
                       parts1[1].equalsIgnoreCase(parts2[1]) &&
                       parts1[2].equalsIgnoreCase(parts2[2]) &&
                       parts1[3].equalsIgnoreCase(parts2[3]);
            }
        }

        return false;
    }
}
