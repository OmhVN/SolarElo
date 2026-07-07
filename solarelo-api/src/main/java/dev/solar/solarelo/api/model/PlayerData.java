package dev.solar.solarelo.api.model;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private String name;
    private int elo;
    private int kills;
    private int deaths;
    private int currentStreak;
    private int bestStreak;
    private final long createdAt;
    private boolean settingChat = true;
    private boolean settingWelcomeEffect = true;
    private boolean settingTitle = true;
    private String lastIp = null;
    private boolean locked = false;
    private long lockExpiry = 0L;

    public PlayerData(UUID uuid, String name, int elo, int kills, int deaths, int currentStreak, int bestStreak, long createdAt, boolean settingChat, boolean settingWelcomeEffect, boolean settingTitle) {
        this.uuid = uuid;
        this.name = name;
        this.elo = elo;
        this.kills = kills;
        this.deaths = deaths;
        this.currentStreak = currentStreak;
        this.bestStreak = bestStreak;
        this.createdAt = createdAt;
        this.settingChat = settingChat;
        this.settingWelcomeEffect = settingWelcomeEffect;
        this.settingTitle = settingTitle;
    }

    public PlayerData(UUID uuid, String name, int elo, int kills, int deaths, int currentStreak, int bestStreak, long createdAt) {
        this(uuid, name, elo, kills, deaths, currentStreak, bestStreak, createdAt, true, true, true);
    }

    public PlayerData(UUID uuid, String name, int elo, int kills, int deaths, int currentStreak, int bestStreak) {
        this(uuid, name, elo, kills, deaths, currentStreak, bestStreak, System.currentTimeMillis(), true, true, true);
    }

    public PlayerData(UUID uuid, String name, int elo, int kills, int deaths) {
        this(uuid, name, elo, kills, deaths, 0, 0, System.currentTimeMillis(), true, true, true);
    }

    public UUID getUuid() { return uuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getElo() { return elo; }
    public void setElo(int elo) { this.elo = elo; }

    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }

    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int streak) { this.currentStreak = streak; }

    public int getBestStreak() { return bestStreak; }
    public void setBestStreak(int bestStreak) { this.bestStreak = bestStreak; }

    public long getCreatedAt() { return createdAt; }

    public void addKill() {
        this.kills++;
        this.currentStreak++;
        if (this.currentStreak > this.bestStreak) {
            this.bestStreak = this.currentStreak;
        }
    }

    public void addDeath() {
        this.deaths++;
        this.currentStreak = 0;
    }

    public double getKDRatio() {
        if (deaths == 0) return kills;
        return Math.round((double) kills / deaths * 100.0) / 100.0;
    }

    public boolean isSettingChat() { return settingChat; }
    public void setSettingChat(boolean settingChat) { this.settingChat = settingChat; }

    public boolean isSettingWelcomeEffect() { return settingWelcomeEffect; }
    public void setSettingWelcomeEffect(boolean settingWelcomeEffect) { this.settingWelcomeEffect = settingWelcomeEffect; }

    public boolean isSettingTitle() { return settingTitle; }
    public void setSettingTitle(boolean settingTitle) { this.settingTitle = settingTitle; }

    public String getLastIp() { return lastIp; }
    public void setLastIp(String lastIp) { this.lastIp = lastIp; }

    public boolean isLocked() { return locked || (lockExpiry > System.currentTimeMillis()); }
    public void setLocked(boolean locked) { this.locked = locked; }

    public long getLockExpiry() { return lockExpiry; }
    public void setLockExpiry(long lockExpiry) { this.lockExpiry = lockExpiry; }
}
