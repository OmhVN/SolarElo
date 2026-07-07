package dev.solar.solarelo.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;
import dev.solar.solarelo.api.model.EloHistoryEntry;
import dev.solar.solarelo.api.model.KillHistoryEntry;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class DatabaseManager {

    private final SolarElo plugin;
    private HikariDataSource dataSource;
    boolean isMySQL;
    private final PlayerDao playerDao;
    private final HistoryDao historyDao;

    public DatabaseManager(SolarElo plugin) {
        this.plugin = plugin;
        this.playerDao = new PlayerDao(plugin, this);
        this.historyDao = new HistoryDao(plugin, this);
    }

    public boolean initialize() {
        String type = plugin.getDatabaseConfig().getString("database.type", "SQLITE");
        if (type == null) type = "SQLITE";
        type = type.trim().toUpperCase();
        isMySQL = type.equals("MYSQL") || type.equals("MARIADB");

        HikariConfig config = new HikariConfig();

        if (isMySQL) {
            String host = plugin.getDatabaseConfig().getString("database.mysql.host", "localhost");
            int port = plugin.getDatabaseConfig().getInt("database.mysql.port", 3306);
            String database = plugin.getDatabaseConfig().getString("database.mysql.database", "solarelo");
            String username = plugin.getDatabaseConfig().getString("database.mysql.username", "root");
            String password = plugin.getDatabaseConfig().getString("database.mysql.password", "");
            int poolSize = plugin.getDatabaseConfig().getInt("database.mysql.pool-size", 10);

            if (type.equals("MARIADB")) {
                config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database +
                        "?useSSL=false&autoReconnect=true&characterEncoding=utf8");
                config.setDriverClassName("org.mariadb.jdbc.Driver");
            } else {
                config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database +
                        "?useSSL=false&autoReconnect=true&characterEncoding=utf8");
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            }
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(poolSize);
            config.setMinimumIdle(2);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        } else {
            File oldDbFile = new File(plugin.getDataFolder(), "solarelo.db");
            File dataFolder = new File(plugin.getDataFolder(), "data");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File dbFile = new File(dataFolder, "solarelo.db");

            if (oldDbFile.exists() && !dbFile.exists()) {
                plugin.getLogger().info("Found solarelo.db at old location. Migrating to data/solarelo.db...");
                try {
                    java.nio.file.Files.copy(oldDbFile.toPath(), dbFile.toPath());
                    plugin.getLogger().info("Migration successful.");
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to migrate database to new location!", e);
                }
            }

            config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(1);
            config.setConnectionTestQuery("SELECT 1");
        }

        config.setPoolName("SolarElo-Pool");
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        try {
            dataSource = new HikariDataSource(config);
            createTables();
            migrateSchema();
            plugin.getLogger().info("Database initialized successfully (" + type + ")");
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to database!", e);
            return false;
        }
    }

    private void createTables() throws SQLException {
        try (Connection conn = getConnection()) {
            createPlayersTable(conn);
            createKillHistoryTable(conn);
            createEloHistoryTable(conn);
        }
    }

    private void createPlayersTable(Connection conn) throws SQLException {
        String sql;
        if (isMySQL) {
            sql = "CREATE TABLE IF NOT EXISTS solarelo_players (" +
                    "uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                    "name VARCHAR(16) NOT NULL," +
                    "elo INT NOT NULL DEFAULT 1000," +
                    "kills INT NOT NULL DEFAULT 0," +
                    "deaths INT NOT NULL DEFAULT 0," +
                    "current_streak INT NOT NULL DEFAULT 0," +
                    "best_streak INT NOT NULL DEFAULT 0," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "setting_chat INT NOT NULL DEFAULT 1," +
                    "setting_welcome_effect INT NOT NULL DEFAULT 1," +
                    "setting_title INT NOT NULL DEFAULT 1," +
                    "last_ip VARCHAR(45) DEFAULT NULL," +
                    "locked TINYINT NOT NULL DEFAULT 0," +
                    "lock_expiry BIGINT NOT NULL DEFAULT 0" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        } else {
            sql = "CREATE TABLE IF NOT EXISTS solarelo_players (" +
                    "uuid TEXT NOT NULL PRIMARY KEY," +
                    "name TEXT NOT NULL," +
                    "elo INTEGER NOT NULL DEFAULT 1000," +
                    "kills INTEGER NOT NULL DEFAULT 0," +
                    "deaths INTEGER NOT NULL DEFAULT 0," +
                    "current_streak INTEGER NOT NULL DEFAULT 0," +
                    "best_streak INTEGER NOT NULL DEFAULT 0," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "setting_chat INTEGER NOT NULL DEFAULT 1," +
                    "setting_welcome_effect INTEGER NOT NULL DEFAULT 1," +
                    "setting_title INTEGER NOT NULL DEFAULT 1," +
                    "last_ip TEXT DEFAULT NULL," +
                    "locked INTEGER NOT NULL DEFAULT 0," +
                    "lock_expiry INTEGER NOT NULL DEFAULT 0" +
                    ");";
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void createKillHistoryTable(Connection conn) throws SQLException {
        String sql;
        if (isMySQL) {
            sql = "CREATE TABLE IF NOT EXISTS solarelo_kill_history (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "killer_uuid VARCHAR(36) NOT NULL," +
                    "victim_uuid VARCHAR(36) NOT NULL," +
                    "kill_time BIGINT NOT NULL" +
                    ") ENGINE=InnoDB;";
        } else {
            sql = "CREATE TABLE IF NOT EXISTS solarelo_kill_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "killer_uuid TEXT NOT NULL," +
                    "victim_uuid TEXT NOT NULL," +
                    "kill_time INTEGER NOT NULL" +
                    ");";
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void createEloHistoryTable(Connection conn) throws SQLException {
        String sql;
        if (isMySQL) {
            sql = "CREATE TABLE IF NOT EXISTS solarelo_elo_history (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "player_uuid VARCHAR(36) NOT NULL," +
                    "change_amount INT NOT NULL," +
                    "reason VARCHAR(255) NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB;";
        } else {
            sql = "CREATE TABLE IF NOT EXISTS solarelo_elo_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_uuid TEXT NOT NULL," +
                    "change_amount INTEGER NOT NULL," +
                    "reason TEXT NOT NULL," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");";
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void executeSilent(Statement stmt, String sql) {
        try {
            stmt.execute(sql);
        } catch (SQLException ignored) {}
    }

    private void migrateSchema() {
        try (Connection conn = getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                if (isMySQL) {
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN current_streak INT NOT NULL DEFAULT 0");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN best_streak INT NOT NULL DEFAULT 0");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN setting_chat INT NOT NULL DEFAULT 1");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN setting_welcome_effect INT NOT NULL DEFAULT 1");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN setting_title INT NOT NULL DEFAULT 1");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN last_ip VARCHAR(45) DEFAULT NULL");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN locked TINYINT NOT NULL DEFAULT 0");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN lock_expiry BIGINT NOT NULL DEFAULT 0");
                    executeSilent(stmt, "UPDATE solarelo_players SET setting_chat = 1 WHERE setting_chat IS NULL");
                    executeSilent(stmt, "UPDATE solarelo_players SET setting_welcome_effect = 1 WHERE setting_welcome_effect IS NULL");
                    executeSilent(stmt, "UPDATE solarelo_players SET setting_title = 1 WHERE setting_title IS NULL");
                } else {
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN current_streak INTEGER NOT NULL DEFAULT 0");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN best_streak INTEGER NOT NULL DEFAULT 0");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN created_at TIMESTAMP DEFAULT NULL");
                    executeSilent(stmt, "UPDATE solarelo_players SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN setting_chat INTEGER NOT NULL DEFAULT 1");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN setting_welcome_effect INTEGER NOT NULL DEFAULT 1");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN setting_title INTEGER NOT NULL DEFAULT 1");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN last_ip TEXT DEFAULT NULL");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN locked INTEGER NOT NULL DEFAULT 0");
                    executeSilent(stmt, "ALTER TABLE solarelo_players ADD COLUMN lock_expiry INTEGER NOT NULL DEFAULT 0");
                    executeSilent(stmt, "UPDATE solarelo_players SET setting_chat = 1 WHERE setting_chat IS NULL");
                    executeSilent(stmt, "UPDATE solarelo_players SET setting_welcome_effect = 1 WHERE setting_welcome_effect IS NULL");
                    executeSilent(stmt, "UPDATE solarelo_players SET setting_title = 1 WHERE setting_title IS NULL");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Schema migration warning: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public boolean isMySQL() {
        return isMySQL;
    }

    public PlayerData loadPlayer(UUID uuid, String name) {
        return playerDao.loadPlayer(uuid, name);
    }

    public void savePlayer(PlayerData data) {
        playerDao.savePlayer(data);
    }

    private void insertPlayer(PlayerData data) {
        playerDao.insertPlayer(data);
    }

    private void updatePlayerName(UUID uuid, String name) {
        playerDao.updatePlayerName(uuid, name);
    }

    public PlayerData getPlayerByName(String name) {
        return playerDao.getPlayerByName(name);
    }

    public List<PlayerData> getTopPlayers(int limit, int offset) {
        return playerDao.getTopPlayers(limit, offset, true);
    }

    public List<PlayerData> getTopPlayers(int limit, int offset, boolean descending) {
        return playerDao.getTopPlayers(limit, offset, descending);
    }

    public int getTotalPlayers() {
        return playerDao.getTotalPlayers();
    }

    public int getPlayerRank(UUID uuid) {
        return playerDao.getPlayerRank(uuid);
    }

    public void recordKill(UUID killer, UUID victim) {
        historyDao.recordKill(killer, victim);
    }

    public long getLastKillTime(UUID killer, UUID victim) {
        return historyDao.getLastKillTime(killer, victim);
    }

    public int getRecentKillCount(UUID killer, UUID victim, long since) {
        return historyDao.getRecentKillCount(killer, victim, since);
    }

    public void resetPlayer(UUID uuid) {
        playerDao.resetPlayer(uuid);
    }

    public void setEloAll(int elo) {
        playerDao.setEloAll(elo);
    }

    public void addEloAll(int amount, int maxElo) {
        playerDao.addEloAll(amount, maxElo);
    }

    public void removeEloAll(int amount, int minElo) {
        playerDao.removeEloAll(amount, minElo);
    }

    public void resetEloAll(int defaultElo) {
        playerDao.resetEloAll(defaultElo);
    }

    public void softResetEloAll(int defaultElo, double multiplier, boolean resetStats) {
        playerDao.softResetEloAll(defaultElo, multiplier, resetStats);
    }

    public void recordEloChange(UUID uuid, int changeAmount, String reason) {
        historyDao.recordEloChange(uuid, changeAmount, reason);
    }

    public long getLastEloChangeTime(UUID uuid) {
        return historyDao.getLastEloChangeTime(uuid);
    }

    public List<EloHistoryEntry> getEloHistory(UUID playerUuid) {
        return historyDao.getEloHistory(playerUuid);
    }

    public List<KillHistoryEntry> getKillHistory(UUID playerUuid) {
        return historyDao.getKillHistory(playerUuid);
    }

    public String getPlayerName(UUID uuid) {
        return playerDao.getPlayerName(uuid);
    }

    public List<PlayerData> getAlts(String ip) {
        return playerDao.getAlts(ip);
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
