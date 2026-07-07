package dev.solar.solarelo.managers;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EloManager {

    private static Object miniMessageInstance = null;
    private static java.lang.reflect.Method deserializeMethod = null;

    static {
        try {
            Class<?> clazz = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            try {
                java.lang.reflect.Method mmMethod = clazz.getMethod("miniMessage");
                miniMessageInstance = mmMethod.invoke(null);
            } catch (NoSuchMethodException e) {
                try {
                    java.lang.reflect.Method getMethod = clazz.getMethod("get");
                    miniMessageInstance = getMethod.invoke(null);
                } catch (NoSuchMethodException ex) {
                    try {
                        java.lang.reflect.Method builderMethod = clazz.getMethod("builder");
                        Object builder = builderMethod.invoke(null);
                        java.lang.reflect.Method buildMethod = builder.getClass().getMethod("build");
                        miniMessageInstance = buildMethod.invoke(builder);
                    } catch (Exception ignored) {}
                }
            }
            if (miniMessageInstance != null) {
                deserializeMethod = miniMessageInstance.getClass().getMethod("deserialize", String.class);
            }
        } catch (Exception ignored) {}
    }

    private final SolarElo plugin;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> cachedRanks = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastRankCacheTimes = new ConcurrentHashMap<>();

    private final Map<UUID, Long> lastMoveTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastAttackTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastSpawnTimes = new ConcurrentHashMap<>();
    private final Map<UUID, org.bukkit.Location> lastSpawnLocations = new ConcurrentHashMap<>();

    private final Map<UUID, UUID> activeBountyTargets = new ConcurrentHashMap<>();
    private final Map<UUID, Long> activeBountyEndTimes = new ConcurrentHashMap<>();
    private final Map<UUID, Long> bountyCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, PendingDeathNotification> pendingDeathNotifications = new ConcurrentHashMap<>();

    private final EloCalculator eloCalculator;
    private final PvpKillProcessor pvpKillProcessor;
    private final SeasonResetProcessor seasonResetProcessor;

    public static class PendingDeathNotification {
        public final String killerName;
        public final int lost;
        public final int newElo;
        public final int streak;
        public final String oldRank;
        public final String newRank;

        public PendingDeathNotification(String killerName, int lost, int newElo, int streak, String oldRank, String newRank) {
            this.killerName = killerName;
            this.lost = lost;
            this.newElo = newElo;
            this.streak = streak;
            this.oldRank = oldRank;
            this.newRank = newRank;
        }
    }

    public EloManager(SolarElo plugin) {
        this.plugin = plugin;
        this.eloCalculator = new EloCalculator(plugin, this);
        this.pvpKillProcessor = new PvpKillProcessor(plugin, this);
        this.seasonResetProcessor = new SeasonResetProcessor(plugin, this);
    }

    public EloCalculator getEloCalculator() {
        return eloCalculator;
    }

    public PvpKillProcessor getPvpKillProcessor() {
        return pvpKillProcessor;
    }

    public SeasonResetProcessor getSeasonResetProcessor() {
        return seasonResetProcessor;
    }

    public Map<UUID, Long> getLastMoveTimes() {
        return lastMoveTimes;
    }

    public Map<UUID, Long> getLastAttackTimes() {
        return lastAttackTimes;
    }

    public Map<UUID, Long> getLastSpawnTimes() {
        return lastSpawnTimes;
    }

    public Map<UUID, org.bukkit.Location> getLastSpawnLocations() {
        return lastSpawnLocations;
    }

    public java.util.Collection<PlayerData> getCachedPlayers() {
        return cache.values();
    }

    public void cachePlayer(UUID uuid, PlayerData data) {
        cache.put(uuid, data);
    }

    public UUID getActiveBountyTarget(UUID playerUuid) {
        Long endTime = activeBountyEndTimes.get(playerUuid);
        if (endTime != null && System.currentTimeMillis() > endTime) {
            activeBountyTargets.remove(playerUuid);
            activeBountyEndTimes.remove(playerUuid);
            return null;
        }
        return activeBountyTargets.get(playerUuid);
    }

    public Long getActiveBountyEndTime(UUID playerUuid) {
        return activeBountyEndTimes.get(playerUuid);
    }

    public void setActiveBountyTarget(UUID playerUuid, UUID targetUuid) {
        long duration = 5400;
        try {
            duration = plugin.getBountyConfig().getInt("bounty-quest.contract-duration-seconds", 5400);
        } catch (Exception ignored) {}
        setActiveBountyTarget(playerUuid, targetUuid, System.currentTimeMillis() + (duration * 1000L));
    }

    public void setActiveBountyTarget(UUID playerUuid, UUID targetUuid, long endTime) {
        activeBountyTargets.put(playerUuid, targetUuid);
        activeBountyEndTimes.put(playerUuid, endTime);
    }

    public void clearActiveBountyTarget(UUID playerUuid) {
        activeBountyTargets.remove(playerUuid);
        activeBountyEndTimes.remove(playerUuid);
    }

    public boolean isBountyTargetActive(UUID targetUuid) {
        if (targetUuid == null) return false;
        for (UUID hunterUuid : new java.util.ArrayList<>(activeBountyTargets.keySet())) {
            UUID activeTarget = getActiveBountyTarget(hunterUuid);
            if (targetUuid.equals(activeTarget)) {
                return true;
            }
        }
        return false;
    }

    public void cleanUpExpiredBounties() {
        long now = System.currentTimeMillis();
        for (UUID key : new java.util.ArrayList<>(activeBountyEndTimes.keySet())) {
            Long endTime = activeBountyEndTimes.get(key);
            if (endTime != null && now > endTime) {
                activeBountyTargets.remove(key);
                activeBountyEndTimes.remove(key);
            }
        }
        for (UUID key : new java.util.ArrayList<>(bountyCooldowns.keySet())) {
            Long endTime = bountyCooldowns.get(key);
            if (endTime != null && now > endTime) {
                bountyCooldowns.remove(key);
            }
        }
    }

    public long getBountyCooldown(UUID playerUuid) {
        cleanUpExpiredBounties();
        return bountyCooldowns.getOrDefault(playerUuid, 0L);
    }

    public void setBountyCooldown(UUID playerUuid, long endTime) {
        bountyCooldowns.put(playerUuid, endTime);
    }

    public int getCachedRank(UUID uuid) {
        long now = System.currentTimeMillis();
        Long lastTime = lastRankCacheTimes.get(uuid);
        if (lastTime == null || now - lastTime > 10000L) {
            plugin.runAsync(() -> {
                int rank = plugin.getDatabaseManager().getPlayerRank(uuid);
                cachedRanks.put(uuid, rank);
                lastRankCacheTimes.put(uuid, System.currentTimeMillis());
            });
        }
        return cachedRanks.getOrDefault(uuid, -1);
    }

    public void updateRankCache(UUID uuid, int rank) {
        cachedRanks.put(uuid, rank);
        lastRankCacheTimes.put(uuid, System.currentTimeMillis());
    }

    public void invalidateRankCache() {
        cachedRanks.clear();
        lastRankCacheTimes.clear();
    }

    public static long parseTimeStringToMillis(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return 86400000L;
        }
        timeStr = timeStr.trim().toLowerCase();
        try {
            char unit = timeStr.charAt(timeStr.length() - 1);
            if (Character.isDigit(unit)) {
                return Long.parseLong(timeStr) * 3600000L;
            }
            long value = Long.parseLong(timeStr.substring(0, timeStr.length() - 1));
            switch (unit) {
                case 's': return value * 1000L;
                case 'm': return value * 60000L;
                case 'h': return value * 3600000L;
                case 'd': return value * 86400000L;
                default: return value * 3600000L;
            }
        } catch (Exception e) {
            return 86400000L;
        }
    }

    public void runDecayCheck() {
        if (!plugin.getConfig().getBoolean("elo-decay.enabled", true)) {
            return;
        }
        plugin.runAsync(() -> {
            int decayAmount = plugin.getConfig().getInt("elo-decay.decay-amount", 20);
            String thresholdStr = plugin.getConfig().getString("elo-decay.inactive-threshold", "24h");
            long thresholdMillis = parseTimeStringToMillis(thresholdStr);

            List<PlayerData> topPlayers = plugin.getDatabaseManager().getTopPlayers(10, 0, true);
            for (PlayerData player : topPlayers) {
                if (player.isLocked()) {
                    continue;
                }
                long lastChange = plugin.getDatabaseManager().getLastEloChangeTime(player.getUuid());
                if (lastChange == 0L) {
                    lastChange = player.getCreatedAt();
                }

                if (System.currentTimeMillis() - lastChange > thresholdMillis) {
                    applyEloDecay(player.getUuid(), player.getName(), decayAmount);
                }
            }
        });
    }

    public void applyEloDecay(UUID uuid, String name, int amount) {
        int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
        int maxElo = plugin.getConfig().getInt("elo.maximum-elo", 50000);

        PlayerData data = cache.containsKey(uuid) ? cache.get(uuid)
                : plugin.getDatabaseManager().loadPlayer(uuid, name);
        if (data.isLocked()) {
            return;
        }
        int oldElo = data.getElo();
        int newElo = Math.min(maxElo, Math.max(minElo, oldElo - amount));

        data.setElo(newElo);
        if (cache.containsKey(uuid)) cache.put(uuid, data);
        plugin.getDatabaseManager().savePlayer(data);

        String reason = "Hao hụt Elo do không hoạt động (-" + amount + " Elo)";
        plugin.getDatabaseManager().recordEloChange(uuid, newElo - oldElo, reason);
        invalidateRankCache();

        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            plugin.runForEntity(player, () -> {
                handleEloChangeEffectsAndRank(player, oldElo, newElo);
                String msg = plugin.getMessageManager().get("elo-decay-notice",
                        "&cBạn bị trừ {amount} Elo do không hoạt động tích cực gần đây!")
                        .replace("{amount}", String.valueOf(amount));
                dev.solar.solarelo.managers.MessageManager.sendMessage(player, msg);
            });
        }
    }

    public void loadPlayer(Player player) {
        cleanUpExpiredBounties();
        plugin.runAsync(() -> {
            PlayerData data = plugin.getDatabaseManager().loadPlayer(player.getUniqueId(), player.getName());
            if (player.getAddress() != null) {
                data.setLastIp(player.getAddress().getAddress().getHostAddress());
                plugin.getDatabaseManager().savePlayer(data);
            }
            cache.put(player.getUniqueId(), data);
        });
    }

    public void unloadPlayer(UUID uuid) {
        PlayerData data = cache.remove(uuid);
        if (data != null) {
            plugin.runAsync(() -> plugin.getDatabaseManager().savePlayer(data));
        }
        activeBountyTargets.remove(uuid);
        activeBountyEndTimes.remove(uuid);
        pendingDeathNotifications.remove(uuid);
    }

    public void triggerPendingDeathNotifications(Player player) {
        PendingDeathNotification pending = pendingDeathNotifications.remove(player.getUniqueId());
        if (pending == null) return;

        PlayerData data = getCachedData(player.getUniqueId());
        if (data == null) {
            data = plugin.getDatabaseManager().loadPlayer(player.getUniqueId(), player.getName());
            cache.put(player.getUniqueId(), data);
        }

        final PlayerData finalData = data;
        final boolean streakEnabled = plugin.getConfig().getBoolean("kill-streak.enabled", true);

        if (finalData.isSettingChat()) {
            if (streakEnabled && pending.streak >= 2) {
                String streakEndMsg = plugin.getMessageManager().get("streak-end",
                                "&cStreak &e{streak} &ckill đã kết thúc!")
                        .replace("{streak}", String.valueOf(pending.streak));
                dev.solar.solarelo.managers.MessageManager.sendMessage(player, streakEndMsg);
            }

            String msg = plugin.getMessageManager().get("death-loss",
                            "&c-{lost} Elo | Total: {elo}")
                    .replace("{lost}", String.valueOf(pending.lost))
                    .replace("{killer}", pending.killerName)
                    .replace("{elo}", String.valueOf(pending.newElo));
            dev.solar.solarelo.managers.MessageManager.sendMessage(player, msg);
        }

        if (plugin.getConfig().getBoolean("display.actionbar.enabled", true)) {
            String abFormat = plugin.getConfig().getString("display.actionbar.death-format",
                            "&c-{lost} Elo &7| &fElo: &e{elo}")
                    .replace("{lost}", String.valueOf(pending.lost))
                    .replace("{elo}", String.valueOf(pending.newElo));
            sendActionBar(player, abFormat);
        }

        if (finalData.isSettingTitle() && plugin.getConfig().getBoolean("display.title.enabled", true)) {
            String titleStr = plugin.getConfig().getString("display.title.death-title", "");
            String subStr = plugin.getConfig().getString("display.title.death-subtitle", "");
            if ((titleStr != null && !titleStr.isEmpty()) || (subStr != null && !subStr.isEmpty())) {
                String formattedTitle = titleStr == null ? "" : titleStr
                        .replace("{lost}", String.valueOf(pending.lost))
                        .replace("{killer}", pending.killerName)
                        .replace("{elo}", String.valueOf(pending.newElo));
                String formattedSub = subStr == null ? "" : subStr
                        .replace("{lost}", String.valueOf(pending.lost))
                        .replace("{killer}", pending.killerName)
                        .replace("{elo}", String.valueOf(pending.newElo));
                int fadeIn = plugin.getConfig().getInt("display.title.fade-in", 5);
                int stay = plugin.getConfig().getInt("display.title.stay", 40);
                int fadeOut = plugin.getConfig().getInt("display.title.fade-out", 10);
                player.sendTitle(colorize(formattedTitle), colorize(formattedSub), fadeIn, stay, fadeOut);
            }
        }

        if (!pending.oldRank.equals(pending.newRank)) {
            notifyRankChange(player, pending.oldRank, pending.newRank);
        }
    }

    public PlayerData getCachedData(UUID uuid) {
        return cache.get(uuid);
    }

    public PlayerData getData(UUID uuid, String name) {
        PlayerData cached = cache.get(uuid);
        if (cached != null) return cached;
        PlayerData data = plugin.getDatabaseManager().loadPlayer(uuid, name);
        if (org.bukkit.Bukkit.getPlayer(uuid) != null) {
            cache.put(uuid, data);
        }
        return data;
    }

    public void initializeActivityData(Player player) {
        UUID uuid = player.getUniqueId();
        lastMoveTimes.put(uuid, System.currentTimeMillis());
        lastAttackTimes.put(uuid, System.currentTimeMillis());
        lastSpawnTimes.put(uuid, System.currentTimeMillis());
        lastSpawnLocations.put(uuid, player.getLocation());
    }

    public void updateLastMovement(Player player) {
        lastMoveTimes.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void updateLastAttack(Player player) {
        lastAttackTimes.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void recordSpawn(Player player) {
        lastSpawnTimes.put(player.getUniqueId(), System.currentTimeMillis());
        lastSpawnLocations.put(player.getUniqueId(), player.getLocation());
    }

    public void recordSpawn(Player player, org.bukkit.Location location) {
        lastSpawnTimes.put(player.getUniqueId(), System.currentTimeMillis());
        lastSpawnLocations.put(player.getUniqueId(), location != null ? location : player.getLocation());
    }

    public void removeActivityData(UUID uuid) {
        lastMoveTimes.remove(uuid);
        lastAttackTimes.remove(uuid);
        lastSpawnTimes.remove(uuid);
        lastSpawnLocations.remove(uuid);
    }

    public void processKill(Player killer, Player victim) {
        pvpKillProcessor.processKill(killer, victim);
    }

    private void notifyRankChange(Player player, String oldRank, String newRank) {
        int oldOrdinal = plugin.getRankManager().getRankOrdinal(oldRank);
        int newOrdinal = plugin.getRankManager().getRankOrdinal(newRank);
        PlayerData data = getData(player.getUniqueId(), player.getName());

        String msgKey = newOrdinal > oldOrdinal ? "rank-up" : "rank-down";
        String msg = plugin.getMessageManager().get(msgKey, "&6RANK UP! Bạn đã lên {rank}!")
                .replace("{rank}", plugin.getRankManager().getRankDisplay(newRank));
        if (data == null || data.isSettingChat()) {
            dev.solar.solarelo.managers.MessageManager.sendMessage(player, msg);
        }

        if ((data == null || data.isSettingTitle()) && plugin.getConfig().getBoolean("display.title.enabled", true)) {
            String titleKey = newOrdinal > oldOrdinal ? "display.title.rank-up-title" : "display.title.rank-down-title";
            String subKey = newOrdinal > oldOrdinal ? "display.title.rank-up-subtitle" : "display.title.rank-down-subtitle";

            String defaultTitle = newOrdinal > oldOrdinal ? "#ffaa00★ RANK UP ★" : "#ff3c3c▼ RANK DOWN ▼";
            String defaultSub = newOrdinal > oldOrdinal ? "#ffaa00Bạn đã thăng hạng lên {rank}" : "#aaaaaaBạn đã xuống hạng {rank}";

            String titleStr = plugin.getConfig().getString(titleKey, defaultTitle);
            String subStr = plugin.getConfig().getString(subKey, defaultSub)
                    .replace("{rank}", plugin.getRankManager().getRankDisplay(newRank));

            int fadeIn = plugin.getConfig().getInt("display.title.fade-in", 5);
            int stay = plugin.getConfig().getInt("display.title.stay", 40);
            int fadeOut = plugin.getConfig().getInt("display.title.fade-out", 10);

            player.sendTitle(colorize(titleStr), colorize(subStr), fadeIn, stay, fadeOut);
        }

        if (newOrdinal > oldOrdinal) {
            playEffects(player, "rank-up");
            plugin.getWebhookManager().sendRankUp(player.getName(), plugin.getRankManager().getRankDisplay(newRank));
        } else {
            playEffects(player, "rank-down");
        }
    }

    public void playEffects(Player player, String configPath) {
        if (player == null || !player.isOnline()) return;

        String soundStr = plugin.getEffectManager().getEffectSoundString("sound", configPath, "");
        if (soundStr != null && !soundStr.isEmpty()) {
            soundStr = soundStr.trim();
            double volume = plugin.getEffectManager().getEffectSoundDouble("volume", configPath, 1.0);
            double pitch = plugin.getEffectManager().getEffectSoundDouble("pitch", configPath, 1.0);
            try {
                org.bukkit.Sound sound = EffectManager.matchSound(soundStr);
                plugin.runForEntity(player, () -> player.playSound(player.getLocation(), sound, (float) volume, (float) pitch));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound config for effects." + configPath + ": " + soundStr);
            }
        }

        String particleStr = plugin.getEffectManager().getEffectParticleString("particle", configPath, "");
        if (particleStr != null && !particleStr.isEmpty()) {
            particleStr = particleStr.trim();
            int count = plugin.getEffectManager().getEffectParticleInt("particle-count", configPath, 10);
            try {
                org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleStr.toUpperCase());
                plugin.runForEntity(player, () -> {
                    player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), count, 0.5, 0.5, 0.5, 0.05);
                });
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid particle config for effects." + configPath + ": " + particleStr);
            }
        }
    }

    public void handleEloChangeEffectsAndRank(Player player, int oldElo, int newElo) {
        handleEloChangeEffectsAndRank(player, oldElo, newElo, null, 0);
    }

    public void handleEloChangeEffectsAndRank(Player player, int oldElo, int newElo, EloChangeReason reason, int parameter) {
        invalidateRankCache();
        PlayerData data = getData(player.getUniqueId(), player.getName());
        if (newElo > oldElo) {
            playEffects(player, "plus");
        } else if (newElo < oldElo) {
            playEffects(player, "minus");
        }

        if (reason != null && (data == null || data.isSettingTitle()) && plugin.getConfig().getBoolean("display.title.enabled", true)) {
            String titleKey = null;
            String subKey = null;
            String defaultTitle = "";
            String defaultSub = "";

            switch (reason) {
                case ADMIN_SET -> {
                    titleKey = "display.title.admin-set-title";
                    subKey = "display.title.admin-set-subtitle";
                    defaultTitle = "&#00ffcc⚙ ELO SET ⚙";
                    defaultSub = "&#e0e0e0Elo của bạn được đặt thành &#00ffcc{elo}";
                }
                case ADMIN_ADD -> {
                    titleKey = "display.title.admin-add-title";
                    subKey = "display.title.admin-add-subtitle";
                    defaultTitle = "&#00ff3c▲ ELO ADDED ▲";
                    defaultSub = "&#e0e0e0Bạn được cộng &#00ff3c+{amount} ELO";
                }
                case ADMIN_REMOVE -> {
                    titleKey = "display.title.admin-remove-title";
                    subKey = "display.title.admin-remove-subtitle";
                    defaultTitle = "&#ff3c3c▼ ELO REMOVED ▼";
                    defaultSub = "&#e0e0e0Bạn bị trừ &#ff3c3c-{amount} ELO";
                }
                case ADMIN_RESET -> {
                    titleKey = "display.title.admin-reset-title";
                    subKey = "display.title.admin-reset-subtitle";
                    defaultTitle = "&#e0e0e0↺ ELO RESET ↺";
                    defaultSub = "&#e0e0e0Elo của bạn đã được reset về mặc định";
                }
            }

            if (titleKey != null) {
                String titleStr = plugin.getConfig().getString(titleKey, defaultTitle);
                String subStr = plugin.getConfig().getString(subKey, defaultSub);
                if ((titleStr != null && !titleStr.isEmpty()) || (subStr != null && !subStr.isEmpty())) {
                    String formattedTitle = titleStr == null ? "" : titleStr
                            .replace("{elo}", String.valueOf(newElo))
                            .replace("{amount}", String.valueOf(parameter));
                    String formattedSub = subStr == null ? "" : subStr
                            .replace("{elo}", String.valueOf(newElo))
                            .replace("{amount}", String.valueOf(parameter));
                    int fadeIn = plugin.getConfig().getInt("display.title.fade-in", 5);
                    int stay = plugin.getConfig().getInt("display.title.stay", 40);
                    int fadeOut = plugin.getConfig().getInt("display.title.fade-out", 10);
                    plugin.runForEntity(player, () -> player.sendTitle(colorize(formattedTitle), colorize(formattedSub), fadeIn, stay, fadeOut));
                }
            }
        }

        String oldRank = plugin.getRankManager().getRank(oldElo);
        String newRank = plugin.getRankManager().getRank(newElo);
        if (!oldRank.equals(newRank)) {
            plugin.runForEntity(player, () -> {
                notifyRankChange(player, oldRank, newRank);
            });
            plugin.getRankManager().executeRankCommands(player, newRank);
        }
    }

    public void setElo(UUID uuid, String name, int amount) {
        plugin.runAsync(() -> {
            PlayerData data = cache.containsKey(uuid) ? cache.get(uuid)
                    : plugin.getDatabaseManager().loadPlayer(uuid, name);
            int oldElo = data.getElo();
            data.setElo(amount);
            int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
            if (amount > minElo) {
                data.setLocked(false);
                data.setLockExpiry(0L);
            }
            if (cache.containsKey(uuid)) cache.put(uuid, data);
            plugin.getDatabaseManager().savePlayer(data);

            org.bukkit.configuration.file.FileConfiguration adminConfig = plugin.getGuiConfigManager().getAdminConfig();
            String reason = adminConfig.getString("reasons.admin-set", "⚙ Thay đổi bởi Admin ({elo} Elo)").replace("{elo}", String.valueOf(amount));
            plugin.getDatabaseManager().recordEloChange(uuid, amount - oldElo, reason);

            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                handleEloChangeEffectsAndRank(player, oldElo, amount, EloChangeReason.ADMIN_SET, amount);
            }
        });
    }

    public void addElo(UUID uuid, String name, int amount) {
        int maxElo = plugin.getConfig().getInt("elo.maximum-elo", 50000);
        plugin.runAsync(() -> {
            PlayerData data = cache.containsKey(uuid) ? cache.get(uuid)
                    : plugin.getDatabaseManager().loadPlayer(uuid, name);
            int oldElo = data.getElo();
            int newElo = Math.min(maxElo, oldElo + amount);
            data.setElo(newElo);
            int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
            if (newElo > minElo) {
                data.setLocked(false);
                data.setLockExpiry(0L);
            }
            if (cache.containsKey(uuid)) cache.put(uuid, data);
            plugin.getDatabaseManager().savePlayer(data);

            org.bukkit.configuration.file.FileConfiguration adminConfig = plugin.getGuiConfigManager().getAdminConfig();
            String reason = adminConfig.getString("reasons.admin-add", "✚ Cộng bởi Admin (+{amount} Elo)").replace("{amount}", String.valueOf(amount));
            plugin.getDatabaseManager().recordEloChange(uuid, newElo - oldElo, reason);

            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                handleEloChangeEffectsAndRank(player, oldElo, newElo, EloChangeReason.ADMIN_ADD, amount);
            }
        });
    }

    public void removeElo(UUID uuid, String name, int amount) {
        int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
        int maxElo = plugin.getConfig().getInt("elo.maximum-elo", 50000);
        plugin.runAsync(() -> {
            PlayerData data = cache.containsKey(uuid) ? cache.get(uuid)
                    : plugin.getDatabaseManager().loadPlayer(uuid, name);
            int oldElo = data.getElo();
            int newElo = Math.min(maxElo, Math.max(minElo, oldElo - amount));
            data.setElo(newElo);
            if (newElo > minElo) {
                data.setLocked(false);
                data.setLockExpiry(0L);
            }
            if (cache.containsKey(uuid)) cache.put(uuid, data);
            plugin.getDatabaseManager().savePlayer(data);

            org.bukkit.configuration.file.FileConfiguration adminConfig = plugin.getGuiConfigManager().getAdminConfig();
            String reason = adminConfig.getString("reasons.admin-remove", "➖ Trừ bởi Admin (-{amount} Elo)").replace("{amount}", String.valueOf(amount));
            plugin.getDatabaseManager().recordEloChange(uuid, newElo - oldElo, reason);

            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                handleEloChangeEffectsAndRank(player, oldElo, newElo, EloChangeReason.ADMIN_REMOVE, amount);
            }
        });
    }

    public void resetElo(UUID uuid, String name) {
        int defaultElo = plugin.getConfig().getInt("default-elo", 1000);
        plugin.runAsync(() -> {
            PlayerData data = cache.containsKey(uuid) ? cache.get(uuid)
                    : plugin.getDatabaseManager().loadPlayer(uuid, name);
            int oldElo = data.getElo();
            data.setElo(defaultElo);
            data.setKills(0);
            data.setDeaths(0);
            data.setCurrentStreak(0);
            data.setBestStreak(0);
            int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
            if (defaultElo > minElo) {
                data.setLocked(false);
                data.setLockExpiry(0L);
            }
            if (cache.containsKey(uuid)) cache.put(uuid, data);
            plugin.getDatabaseManager().savePlayer(data);

            org.bukkit.configuration.file.FileConfiguration adminConfig = plugin.getGuiConfigManager().getAdminConfig();
            String reason = adminConfig.getString("reasons.admin-reset", "⟳ Reset bởi Admin");
            plugin.getDatabaseManager().recordEloChange(uuid, defaultElo - oldElo, reason);

            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                handleEloChangeEffectsAndRank(player, oldElo, defaultElo, EloChangeReason.ADMIN_RESET, 0);
            }
        });
    }

    public void setEloAll(int amount) {
        int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
        plugin.runAsync(() -> {
            for (PlayerData data : cache.values()) {
                int oldElo = data.getElo();
                data.setElo(amount);
                if (amount > minElo) {
                    data.setLocked(false);
                    data.setLockExpiry(0L);
                }
                Player player = Bukkit.getPlayer(data.getUuid());
                if (player != null && player.isOnline()) {
                    handleEloChangeEffectsAndRank(player, oldElo, amount, EloChangeReason.ADMIN_SET, amount);
                }
            }
            plugin.getDatabaseManager().setEloAll(amount);
        });
    }

    public void addEloAll(int amount) {
        int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
        int maxElo = plugin.getConfig().getInt("elo.maximum-elo", 50000);
        plugin.runAsync(() -> {
            for (PlayerData data : cache.values()) {
                int oldElo = data.getElo();
                int newElo = Math.min(maxElo, oldElo + amount);
                data.setElo(newElo);
                if (newElo > minElo) {
                    data.setLocked(false);
                    data.setLockExpiry(0L);
                }
                Player player = Bukkit.getPlayer(data.getUuid());
                if (player != null && player.isOnline()) {
                    handleEloChangeEffectsAndRank(player, oldElo, newElo, EloChangeReason.ADMIN_ADD, amount);
                }
            }
            plugin.getDatabaseManager().addEloAll(amount, maxElo);
        });
    }

    public void removeEloAll(int amount) {
        int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
        int maxElo = plugin.getConfig().getInt("elo.maximum-elo", 50000);
        plugin.runAsync(() -> {
            for (PlayerData data : cache.values()) {
                int oldElo = data.getElo();
                int newElo = Math.min(maxElo, Math.max(minElo, oldElo - amount));
                data.setElo(newElo);
                if (newElo > minElo) {
                    data.setLocked(false);
                    data.setLockExpiry(0L);
                }
                Player player = Bukkit.getPlayer(data.getUuid());
                if (player != null && player.isOnline()) {
                    handleEloChangeEffectsAndRank(player, oldElo, newElo, EloChangeReason.ADMIN_REMOVE, amount);
                }
            }
            plugin.getDatabaseManager().removeEloAll(amount, minElo);
        });
    }

    public void resetEloAll() {
        int defaultElo = plugin.getConfig().getInt("default-elo", 1000);
        int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
        plugin.runAsync(() -> {
            for (PlayerData data : cache.values()) {
                int oldElo = data.getElo();
                data.setElo(defaultElo);
                data.setKills(0);
                data.setDeaths(0);
                data.setCurrentStreak(0);
                data.setBestStreak(0);
                if (defaultElo > minElo) {
                    data.setLocked(false);
                    data.setLockExpiry(0L);
                }
                Player player = Bukkit.getPlayer(data.getUuid());
                if (player != null && player.isOnline()) {
                    handleEloChangeEffectsAndRank(player, oldElo, defaultElo, EloChangeReason.ADMIN_RESET, 0);
                }
            }
            plugin.getDatabaseManager().resetEloAll(defaultElo);
        });
    }

    public static String colorize(String msg) {
        if (msg == null) return "";

        if (msg.contains("<") && msg.contains(">") && deserializeMethod != null && miniMessageInstance != null) {
            try {
                net.kyori.adventure.text.Component component = (net.kyori.adventure.text.Component) deserializeMethod.invoke(miniMessageInstance, msg);
                msg = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(component);
            } catch (Exception ignored) {}
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("&?#([A-Fa-f0-9]{6})").matcher(msg);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder builder = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                builder.append('§').append(c);
            }
            matcher.appendReplacement(sb, builder.toString());
        }
        matcher.appendTail(sb);
        msg = sb.toString();

        return msg.replace("&", "§");
    }

    private void sendActionBar(Player player, String message) {
        try {
            player.sendActionBar(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                    .legacyAmpersand().deserialize(message.replace("§", "&")));
        } catch (NoClassDefFoundError | Exception e) {
            try {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(colorize(message)));
            } catch (Exception ignored) {}
        }
    }

    public void resetSeason(org.bukkit.command.CommandSender sender) {
        seasonResetProcessor.resetSeason(sender);
    }

    public boolean isIpBlocked(Player player) {
        if (!plugin.getConfig().getBoolean("ip-check.enabled", true)) {
            return false;
        }
        if (player.getAddress() == null) {
            return false;
        }
        String ip = player.getAddress().getAddress().getHostAddress();

        List<Player> sameIpPlayers = new ArrayList<>();
        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (p.getAddress() != null && p.getAddress().getAddress().getHostAddress().equals(ip)) {
                sameIpPlayers.add(p);
            }
        }
        if (sameIpPlayers.size() <= 1) {
            return false;
        }

        Player oldestPlayer = null;
        long oldestTime = Long.MAX_VALUE;
        for (Player p : sameIpPlayers) {
            PlayerData data = getCachedData(p.getUniqueId());
            if (data == null) {
                data = plugin.getDatabaseManager().loadPlayer(p.getUniqueId(), p.getName());
            }
            long createdAt = data != null ? data.getCreatedAt() : System.currentTimeMillis();
            if (createdAt < oldestTime) {
                oldestTime = createdAt;
                oldestPlayer = p;
            } else if (createdAt == oldestTime) {
                if (oldestPlayer == null || p.getUniqueId().toString().compareTo(oldestPlayer.getUniqueId().toString()) < 0) {
                    oldestPlayer = p;
                }
            }
        }

        return oldestPlayer != null && !oldestPlayer.getUniqueId().equals(player.getUniqueId());
    }

    public String getSeasonTimeRemaining() {
        if (!plugin.getSeasonConfig().getBoolean("season.enabled", true)) {
            return plugin.getMessageManager().get("season-disabled-display", "Đã tắt");
        }
        String dateStr = plugin.getSeasonConfig().getString("season.end-date", "");
        if (dateStr == null || dateStr.isEmpty()) {
            return "N/A";
        }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date endDate = sdf.parse(dateStr);
            long diff = endDate.getTime() - System.currentTimeMillis();
            if (diff <= 0) {
                return "Kết thúc";
            }
            long seconds = diff / 1000;
            long days = seconds / 86400;
            long hours = (seconds % 86400) / 3600;
            long minutes = (seconds % 3600) / 60;
            if (days > 0) {
                return days + "d " + hours + "h";
            } else if (hours > 0) {
                return hours + "h " + minutes + "m";
            } else {
                return minutes + "m";
            }
        } catch (Exception e) {
            return "N/A";
        }
    }

}
