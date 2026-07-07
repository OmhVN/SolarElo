package dev.solar.solarelo.gui;

import dev.solar.solarelo.SolarElo;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class GuiConfigManager {

    private final SolarElo plugin;
    private FileConfiguration leaderboardConfig;
    private FileConfiguration rewardsConfig;
    private FileConfiguration statsConfig;
    private FileConfiguration bountyConfig;
    private FileConfiguration confirmationConfig;
    private FileConfiguration activeQuestConfig;
    private FileConfiguration mainConfig;
    private FileConfiguration settingsConfig;
    private FileConfiguration adminConfig;

    public GuiConfigManager(SolarElo plugin) {
        this.plugin = plugin;
        reloadConfigs();
    }

    public void reloadConfigs() {
        File guiFolder = new File(plugin.getDataFolder(), "gui");
        if (!guiFolder.exists()) {
            guiFolder.mkdirs();
        }

        File leaderboardFile = new File(guiFolder, "leaderboard.yml");
        if (!leaderboardFile.exists()) {
            plugin.saveResource("gui/leaderboard.yml", false);
        }
        leaderboardConfig = YamlConfiguration.loadConfiguration(leaderboardFile);

        File rewardsFile = new File(guiFolder, "rewards.yml");
        if (!rewardsFile.exists()) {
            plugin.saveResource("gui/rewards.yml", false);
        }
        rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);

        File statsFile = new File(guiFolder, "stats.yml");
        if (!statsFile.exists()) {
            plugin.saveResource("gui/stats.yml", false);
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);

        File bountyFile = new File(guiFolder, "bounty.yml");
        if (!bountyFile.exists()) {
            plugin.saveResource("gui/bounty.yml", false);
        }
        bountyConfig = YamlConfiguration.loadConfiguration(bountyFile);

        File confirmationFile = new File(guiFolder, "confirmation.yml");
        if (!confirmationFile.exists()) {
            plugin.saveResource("gui/confirmation.yml", false);
        }
        confirmationConfig = YamlConfiguration.loadConfiguration(confirmationFile);

        File activeQuestFile = new File(guiFolder, "active_quest.yml");
        if (!activeQuestFile.exists()) {
            plugin.saveResource("gui/active_quest.yml", false);
        }
        activeQuestConfig = YamlConfiguration.loadConfiguration(activeQuestFile);

        File mainFile = new File(guiFolder, "main.yml");
        if (!mainFile.exists()) {
            plugin.saveResource("gui/main.yml", false);
        }
        mainConfig = YamlConfiguration.loadConfiguration(mainFile);

        File settingsFile = new File(guiFolder, "settings.yml");
        if (!settingsFile.exists()) {
            plugin.saveResource("gui/settings.yml", false);
        }
        settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);

        File adminFile = new File(guiFolder, "admin.yml");
        if (!adminFile.exists()) {
            plugin.saveResource("gui/admin.yml", false);
        }
        adminConfig = YamlConfiguration.loadConfiguration(adminFile);
    }

    public FileConfiguration getLeaderboardConfig() {
        return leaderboardConfig;
    }

    public FileConfiguration getRewardsConfig() {
        return rewardsConfig;
    }

    public FileConfiguration getStatsConfig() {
        return statsConfig;
    }

    public FileConfiguration getBountyConfig() {
        return bountyConfig;
    }

    public FileConfiguration getConfirmationConfig() {
        return confirmationConfig;
    }

    public FileConfiguration getActiveQuestConfig() {
        return activeQuestConfig;
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getSettingsConfig() {
        return settingsConfig;
    }

    public FileConfiguration getAdminConfig() {
        return adminConfig;
    }
}
