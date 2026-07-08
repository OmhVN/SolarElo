package dev.solar.solarelo;

import dev.solar.solarelo.api.SolarEloAPIImpl;
import dev.solar.solarelo.commands.EloAdminCommand;
import dev.solar.solarelo.commands.EloCommand;
import dev.solar.solarelo.commands.TopEloCommand;
import dev.solar.solarelo.commands.BountyCommand;
import dev.solar.solarelo.data.DatabaseManager;
import dev.solar.solarelo.listeners.PlayerDeathListener;
import dev.solar.solarelo.listeners.PlayerJoinQuitListener;
import dev.solar.solarelo.listeners.PlayerActivityListener;
import dev.solar.solarelo.managers.EloManager;
import dev.solar.solarelo.managers.MessageManager;
import dev.solar.solarelo.managers.RankManager;
import dev.solar.solarelo.managers.WebhookManager;
import dev.solar.solarelo.managers.EffectManager;
import dev.solar.solarelo.managers.UpdateManager;
import dev.solar.solarelo.gui.GuiConfigManager;
import dev.solar.solarelo.placeholders.SolarEloExpansion;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SolarElo extends JavaPlugin {

    private static SolarElo instance;
    private DatabaseManager databaseManager;
    private MessageManager messageManager;
    private EloManager eloManager;
    private RankManager rankManager;
    private WebhookManager webhookManager;
    private GuiConfigManager guiConfigManager;
    private EffectManager effectManager;
    private UpdateManager updateManager;
    private SolarEloAPIImpl api;
    private dev.solar.solarelo.placeholders.SolarEloExpansion placeholderExpansion;
    private Object decayTask;

    private FileConfiguration discordConfig;
    private FileConfiguration seasonConfig;
    private FileConfiguration bountyConfig;
    private FileConfiguration databaseConfig;
    private File discordFile;
    private File seasonFile;
    private File bountyFile;
    private File databaseFile;

    @Override
    public void onEnable() {
        instance = this;
        printStartupLog();
        dev.solar.solarelo.utils.LoaderUtils.checkStatic("SolarElo");

        if (!getDescription().getName().equals("SolarElo") || !getDataFolder().getName().equals("SolarElo")) {
            getLogger().severe("Invalid plugin or directory name!");
            try {
                org.bukkit.Bukkit.getPluginManager().disablePlugin(this);
                org.bukkit.Bukkit.shutdown();
            } catch (Throwable ignored) {}
            throw new SecurityException("[SolarElo] Invalid plugin or directory name!");
        }

        dev.solar.solarelo.managers.ConfigMigrator.checkFolder(this);

        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "config.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "messages.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "effects.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "rank.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "discord.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "features/season.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "features/bounty.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "database.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "gui/leaderboard.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "gui/rewards.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "gui/stats.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "gui/bounty.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "gui/confirmation.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "gui/active_quest.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "gui/main.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "gui/settings.yml");
        dev.solar.solarelo.managers.ConfigMigrator.migrate(this, "gui/admin.yml");
        reloadConfig();

        messageManager = new MessageManager(this);

        databaseManager = new DatabaseManager(this);
        if (!databaseManager.initialize()) {
            getLogger().severe("Failed to initialize database! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        guiConfigManager = new GuiConfigManager(this);
        effectManager = new EffectManager(this);
        rankManager = new RankManager(this);
        eloManager = new EloManager(this);
        webhookManager = new WebhookManager(this);
        updateManager = new UpdateManager(this);

        api = new SolarEloAPIImpl(this);
        dev.solar.solarelo.api.SolarEloProvider.register(api);

        EloCommand eloCmd = new EloCommand(this);
        getCommand("elo").setExecutor(eloCmd);
        getCommand("elo").setTabCompleter(eloCmd);

        getCommand("topelo").setExecutor(new TopEloCommand(this));

        EloAdminCommand eloAdminCmd = new EloAdminCommand(this);
        getCommand("eloadmin").setExecutor(eloAdminCmd);
        getCommand("eloadmin").setTabCompleter(eloAdminCmd);

        getCommand("bounty").setExecutor(new BountyCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerActivityListener(this), this);
        getServer().getPluginManager().registerEvents(new dev.solar.solarelo.listeners.GuiListener(this), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new dev.solar.solarelo.placeholders.SolarEloExpansion(this);
            placeholderExpansion.register();
            getLogger().info("PlaceholderAPI expansion registered!");
        }

        try {
            int pluginId = 31740;
            new Metrics(this, pluginId);
        } catch (Exception e) {
            getLogger().warning("Failed to initialize bStats metrics: " + e.getMessage());
        }

        for (org.bukkit.entity.Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            eloManager.loadPlayer(player);
            eloManager.initializeActivityData(player);
        }

        startDecayTask();
        updateManager.checkUpdateAsync();
    }

    @Override
    public void onDisable() {
        dev.solar.solarelo.api.SolarEloProvider.unregister();
        stopDecayTask();
        if (placeholderExpansion != null) {
            try {
                placeholderExpansion.unregister();
            } catch (Exception ignored) {}
        }
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        getLogger().info("SolarElo disabled.");
    }

    public static SolarElo getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public EloManager getEloManager() {
        return eloManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public WebhookManager getWebhookManager() {
        return webhookManager;
    }

    public GuiConfigManager getGuiConfigManager() {
        return guiConfigManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public SolarEloAPIImpl getAPI() {
        return api;
    }

    public UpdateManager getUpdateManager() {
        return updateManager;
    }

    public boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void printStartupLog() {
        String[] logo = {
            "#ff55ff   _____       _            ______ _",
            "#ff55ff  / ____|     | |          |  ____| | |",
            "#ff55ff | (___   ___ | | __ _ _ __| |__   | |  ___ ",
            "#ff55ff  \\___ \\ / _ \\| |/ _` | '__|  __|  | | / _ \\",
            "#ff55ff  ____) | (_) | | (_| | |  | |____  | || (_) |",
            "#ff55ff |_____/ \\___/|_|\\__,_|_|  |______| |_| \\___/",
            "",
            " #ffffffSolarElo v" + getDescription().getVersion() + " #aaaaaa| #00fbffSupports Paper & Folia",
            "          #aaaaaaAuthor: OmhVN #aaaaaa| #00ff3cEnabled!"
        };
        for (String line : logo) {
            String colorized = dev.solar.solarelo.managers.EloManager.colorize(line);
            colorized = colorized.replace("§ Folia", "& Folia");
            getServer().getConsoleSender().sendMessage(colorized);
        }
    }

    public void runAsync(Runnable runnable) {
        if (isFolia()) {
            getServer().getAsyncScheduler().runNow(this, task -> runnable.run());
        } else {
            getServer().getScheduler().runTaskAsynchronously(this, runnable);
        }
    }

    public void runSync(Runnable runnable) {
        if (isFolia()) {
            getServer().getGlobalRegionScheduler().run(this, task -> runnable.run());
        } else {
            getServer().getScheduler().runTask(this, runnable);
        }
    }

    public void runForEntity(org.bukkit.entity.Entity entity, Runnable runnable) {
        if (isFolia()) {
            entity.getScheduler().run(this, task -> runnable.run(), null);
        } else {
            getServer().getScheduler().runTask(this, runnable);
        }
    }

    public void runDelayedForEntity(org.bukkit.entity.Entity entity, Runnable runnable, long delayTicks) {
        if (isFolia()) {
            entity.getScheduler().runDelayed(this, task -> runnable.run(), null, delayTicks);
        } else {
            getServer().getScheduler().runTaskLater(this, runnable, delayTicks);
        }
    }

    public void startDecayTask() {
        if (!getConfig().getBoolean("elo-decay.enabled", true)) {
            return;
        }
        String intervalStr = getConfig().getString("elo-decay.check-interval", "12h");
        long intervalMillis = dev.solar.solarelo.managers.EloManager.parseTimeStringToMillis(intervalStr);
        long intervalTicks = intervalMillis / 50L;

        if (isFolia()) {
            decayTask = getServer().getAsyncScheduler().runAtFixedRate(this, task -> {
                getEloManager().runDecayCheck();
            }, intervalMillis, intervalMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
        } else {
            decayTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                getEloManager().runDecayCheck();
            }, intervalTicks, intervalTicks);
        }
    }

    public void stopDecayTask() {
        if (decayTask != null) {
            if (isFolia()) {
                try {
                    ((io.papermc.paper.threadedregions.scheduler.ScheduledTask) decayTask).cancel();
                } catch (Exception ignored) {}
            } else {
                try {
                    ((org.bukkit.scheduler.BukkitTask) decayTask).cancel();
                } catch (Exception ignored) {}
            }
            decayTask = null;
        }
    }

    public FileConfiguration getDiscordConfig() {
        if (discordConfig == null) {
            reloadDiscordConfig();
        }
        return discordConfig;
    }

    public FileConfiguration getSeasonConfig() {
        if (seasonConfig == null) {
            reloadSeasonConfig();
        }
        return seasonConfig;
    }

    public FileConfiguration getBountyConfig() {
        if (bountyConfig == null) {
            reloadBountyConfig();
        }
        return bountyConfig;
    }

    public FileConfiguration getDatabaseConfig() {
        if (databaseConfig == null) {
            reloadDatabaseConfig();
        }
        return databaseConfig;
    }

    public void reloadDiscordConfig() {
        if (discordFile == null) {
            discordFile = new File(getDataFolder(), "discord.yml");
        }
        discordConfig = YamlConfiguration.loadConfiguration(discordFile);

        InputStream defConfigStream = getResource("discord.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            discordConfig.setDefaults(defConfig);
        }
    }

    public void reloadSeasonConfig() {
        if (seasonFile == null) {
            seasonFile = new File(getDataFolder(), "features/season.yml");
        }
        seasonConfig = YamlConfiguration.loadConfiguration(seasonFile);

        InputStream defConfigStream = getResource("features/season.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            seasonConfig.setDefaults(defConfig);
        }
    }

    public void reloadBountyConfig() {
        if (bountyFile == null) {
            bountyFile = new File(getDataFolder(), "features/bounty.yml");
        }
        bountyConfig = YamlConfiguration.loadConfiguration(bountyFile);

        InputStream defConfigStream = getResource("features/bounty.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            bountyConfig.setDefaults(defConfig);
        }
    }

    public void reloadDatabaseConfig() {
        if (databaseFile == null) {
            databaseFile = new File(getDataFolder(), "database.yml");
        }
        databaseConfig = YamlConfiguration.loadConfiguration(databaseFile);

        InputStream defConfigStream = getResource("database.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            databaseConfig.setDefaults(defConfig);
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        reloadDiscordConfig();
        reloadSeasonConfig();
        reloadBountyConfig();
        reloadDatabaseConfig();
        if (effectManager != null) {
            effectManager.load();
        }
        if (updateManager != null) {
            updateManager.checkUpdateAsync();
        }
        stopDecayTask();
        startDecayTask();
    }
}
