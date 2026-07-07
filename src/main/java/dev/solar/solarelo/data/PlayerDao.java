package dev.solar.solarelo.data;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.PlayerData;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class PlayerDao {
    private final SolarElo plugin;
    private final DatabaseManager dbManager;

    public PlayerDao(SolarElo plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
    }

    private Connection getConnection() throws SQLException {
        return dbManager.getConnection();
    }

    private boolean isMySQL() {
        return dbManager.isMySQL();
    }

    public PlayerData loadPlayer(UUID uuid, String name) {
        try (Connection conn = getConnection()) {
            PlayerData data = fetchPlayerData(conn, uuid);
            if (data != null) {
                if (!data.getName().equals(name)) {
                    data.setName(name);
                    updatePlayerNameInDb(conn, uuid, name);
                }
                return data;
            } else {
                return insertNewPlayer(conn, uuid, name);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load player data for " + uuid, e);
            int defaultElo = plugin.getConfig().getInt("default-elo", 1000);
            return new PlayerData(uuid, name, defaultElo, 0, 0, 0, 0, System.currentTimeMillis());
        }
    }

    private PlayerData fetchPlayerData(Connection conn, UUID uuid) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM solarelo_players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    long createdAt = ts != null ? ts.getTime() : System.currentTimeMillis();
                    int chatVal = rs.getInt("setting_chat");
                    boolean settingChat = rs.wasNull() || chatVal != 0;
                    int welcomeVal = rs.getInt("setting_welcome_effect");
                    boolean settingWelcomeEffect = rs.wasNull() || welcomeVal != 0;
                    int titleVal = rs.getInt("setting_title");
                    boolean settingTitle = rs.wasNull() || titleVal != 0;
                    PlayerData data = new PlayerData(
                            uuid,
                            rs.getString("name"),
                            rs.getInt("elo"),
                            rs.getInt("kills"),
                            rs.getInt("deaths"),
                            rs.getInt("current_streak"),
                            rs.getInt("best_streak"),
                            createdAt,
                            settingChat,
                            settingWelcomeEffect,
                            settingTitle
                    );
                    data.setLastIp(rs.getString("last_ip"));
                    data.setLocked(rs.getInt("locked") != 0);
                    data.setLockExpiry(rs.getLong("lock_expiry"));
                    return data;
                }
            }
        }
        return null;
    }

    private void updatePlayerNameInDb(Connection conn, UUID uuid, String name) throws SQLException {
        try (PreparedStatement psUpdate = conn.prepareStatement("UPDATE solarelo_players SET name = ? WHERE uuid = ?")) {
            psUpdate.setString(1, name);
            psUpdate.setString(2, uuid.toString());
            psUpdate.executeUpdate();
        }
    }

    private PlayerData insertNewPlayer(Connection conn, UUID uuid, String name) throws SQLException {
        int defaultElo = plugin.getConfig().getInt("default-elo", 1000);
        long now = System.currentTimeMillis();
        PlayerData data = new PlayerData(uuid, name, defaultElo, 0, 0, 0, 0, now, true, true, true);
        try (PreparedStatement psInsert = conn.prepareStatement(
                "INSERT INTO solarelo_players (uuid, name, elo, kills, deaths, current_streak, best_streak, created_at, setting_chat, setting_welcome_effect, setting_title, last_ip, locked, lock_expiry) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            psInsert.setString(1, data.getUuid().toString());
            psInsert.setString(2, data.getName());
            psInsert.setInt(3, data.getElo());
            psInsert.setInt(4, data.getKills());
            psInsert.setInt(5, data.getDeaths());
            psInsert.setInt(6, data.getCurrentStreak());
            psInsert.setInt(7, data.getBestStreak());
            psInsert.setTimestamp(8, new Timestamp(now));
            psInsert.setInt(9, data.isSettingChat() ? 1 : 0);
            psInsert.setInt(10, data.isSettingWelcomeEffect() ? 1 : 0);
            psInsert.setInt(11, data.isSettingTitle() ? 1 : 0);
            psInsert.setString(12, data.getLastIp());
            psInsert.setInt(13, data.isLocked() ? 1 : 0);
            psInsert.setLong(14, data.getLockExpiry());
            psInsert.executeUpdate();
        }
        return data;
    }

    public void savePlayer(PlayerData data) {
        try (Connection conn = getConnection()) {
            String sql;
            if (isMySQL()) {
                sql = "INSERT INTO solarelo_players (uuid, name, elo, kills, deaths, current_streak, best_streak, created_at, setting_chat, setting_welcome_effect, setting_title, last_ip, locked, lock_expiry) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE name=VALUES(name), elo=VALUES(elo), kills=VALUES(kills), deaths=VALUES(deaths), current_streak=VALUES(current_streak), best_streak=VALUES(best_streak), setting_chat=VALUES(setting_chat), setting_welcome_effect=VALUES(setting_welcome_effect), setting_title=VALUES(setting_title), last_ip=VALUES(last_ip), locked=VALUES(locked), lock_expiry=VALUES(lock_expiry)";
            } else {
                sql = "INSERT OR REPLACE INTO solarelo_players (uuid, name, elo, kills, deaths, current_streak, best_streak, created_at, setting_chat, setting_welcome_effect, setting_title, last_ip, locked, lock_expiry) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, data.getUuid().toString());
                ps.setString(2, data.getName());
                ps.setInt(3, data.getElo());
                ps.setInt(4, data.getKills());
                ps.setInt(5, data.getDeaths());
                ps.setInt(6, data.getCurrentStreak());
                ps.setInt(7, data.getBestStreak());
                ps.setTimestamp(8, new Timestamp(data.getCreatedAt()));
                ps.setInt(9, data.isSettingChat() ? 1 : 0);
                ps.setInt(10, data.isSettingWelcomeEffect() ? 1 : 0);
                ps.setInt(11, data.isSettingTitle() ? 1 : 0);
                ps.setString(12, data.getLastIp());
                ps.setInt(13, data.isLocked() ? 1 : 0);
                ps.setLong(14, data.getLockExpiry());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save player data for " + data.getUuid(), e);
        }
    }

    public void insertPlayer(PlayerData data) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO solarelo_players (uuid, name, elo, kills, deaths, current_streak, best_streak, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, data.getUuid().toString());
            ps.setString(2, data.getName());
            ps.setInt(3, data.getElo());
            ps.setInt(4, data.getKills());
            ps.setInt(5, data.getDeaths());
            ps.setInt(6, data.getCurrentStreak());
            ps.setInt(7, data.getBestStreak());
            ps.setTimestamp(8, new Timestamp(data.getCreatedAt()));
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to insert player data", e);
        }
    }

    public void updatePlayerName(UUID uuid, String name) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE solarelo_players SET name = ? WHERE uuid = ?")) {
            ps.setString(1, name);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to update player name", e);
        }
    }

    public PlayerData getPlayerByName(String name) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM solarelo_players WHERE LOWER(name) = LOWER(?)")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    long createdAt = ts != null ? ts.getTime() : System.currentTimeMillis();
                    PlayerData pData = new PlayerData(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("name"),
                            rs.getInt("elo"),
                            rs.getInt("kills"),
                            rs.getInt("deaths"),
                            rs.getInt("current_streak"),
                            rs.getInt("best_streak"),
                            createdAt
                    );
                    pData.setLocked(rs.getInt("locked") != 0);
                    pData.setLockExpiry(rs.getLong("lock_expiry"));
                    pData.setLastIp(rs.getString("last_ip"));
                    return pData;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get player by name: " + name, e);
        }
        return null;
    }

    public List<PlayerData> getTopPlayers(int limit, int offset, boolean descending) {
        List<PlayerData> list = new ArrayList<>();
        String order = descending ? "DESC" : "ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM solarelo_players ORDER BY elo " + order + " LIMIT ? OFFSET ?")) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    long createdAt = ts != null ? ts.getTime() : System.currentTimeMillis();
                    PlayerData pData = new PlayerData(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("name"),
                            rs.getInt("elo"),
                            rs.getInt("kills"),
                            rs.getInt("deaths"),
                            rs.getInt("current_streak"),
                            rs.getInt("best_streak"),
                            createdAt
                    );
                    pData.setLocked(rs.getInt("locked") != 0);
                    pData.setLockExpiry(rs.getLong("lock_expiry"));
                    pData.setLastIp(rs.getString("last_ip"));
                    list.add(pData);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get top players", e);
        }
        return list;
    }

    public int getTotalPlayers() {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM solarelo_players")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get total player count", e);
        }
        return 0;
    }

    public int getPlayerRank(UUID uuid) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) + 1 FROM solarelo_players WHERE elo > " +
                             "(SELECT elo FROM solarelo_players WHERE uuid = ?)")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get player rank", e);
        }
        return -1;
    }

    public void resetPlayer(UUID uuid) {
        int defaultElo = plugin.getConfig().getInt("default-elo", 1000);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE solarelo_players SET elo = ?, kills = 0, deaths = 0, current_streak = 0, best_streak = 0 WHERE uuid = ?")) {
            ps.setInt(1, defaultElo);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to reset player", e);
        }
    }

    public void setEloAll(int elo) {
        int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE solarelo_players SET elo = ?")) {
                ps.setInt(1, elo);
                ps.executeUpdate();
            }
            if (elo > minElo) {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE solarelo_players SET locked = 0, lock_expiry = 0")) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to set ELO for all players", e);
        }
    }

    public void addEloAll(int amount, int maxElo) {
        int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE solarelo_players SET elo = CASE WHEN elo + ? > ? THEN ? ELSE elo + ? END")) {
                ps.setInt(1, amount);
                ps.setInt(2, maxElo);
                ps.setInt(3, maxElo);
                ps.setInt(4, amount);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE solarelo_players SET locked = 0, lock_expiry = 0 WHERE elo > ?")) {
                ps.setInt(1, minElo);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to add ELO for all players", e);
        }
    }

    public void removeEloAll(int amount, int minElo) {
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("UPDATE solarelo_players SET elo = CASE WHEN elo - ? < ? THEN ? ELSE elo - ? END")) {
                ps.setInt(1, amount);
                ps.setInt(2, minElo);
                ps.setInt(3, minElo);
                ps.setInt(4, amount);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE solarelo_players SET locked = 0, lock_expiry = 0 WHERE elo > ?")) {
                ps.setInt(1, minElo);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to remove ELO for all players", e);
        }
    }

    public void resetEloAll(int defaultElo) {
        int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                     "UPDATE solarelo_players SET elo = ?, kills = 0, deaths = 0, current_streak = 0, best_streak = 0")) {
                ps.setInt(1, defaultElo);
                ps.executeUpdate();
            }
            if (defaultElo > minElo) {
                try (PreparedStatement ps = conn.prepareStatement("UPDATE solarelo_players SET locked = 0, lock_expiry = 0")) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to reset ELO for all players", e);
        }
    }

    public void softResetEloAll(int defaultElo, double multiplier, boolean resetStats) {
        int minElo = plugin.getConfig().getInt("elo.minimum-elo", -500);
        String sql;
        if (resetStats) {
            sql = "UPDATE solarelo_players SET elo = ? + ROUND((elo - ?) * ?), kills = 0, deaths = 0, current_streak = 0, best_streak = 0";
        } else {
            sql = "UPDATE solarelo_players SET elo = ? + ROUND((elo - ?) * ?)";
        }
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, defaultElo);
                ps.setInt(2, defaultElo);
                ps.setDouble(3, multiplier);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE solarelo_players SET locked = 0, lock_expiry = 0 WHERE elo > ?")) {
                ps.setInt(1, minElo);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to soft reset ELO for all players", e);
        }
    }

    public String getPlayerName(UUID uuid) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name FROM solarelo_players WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("name");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get player name by UUID", e);
        }
        return "Unknown";
    }

    public List<PlayerData> getAlts(String ip) {
        List<PlayerData> list = new ArrayList<>();
        if (ip == null || ip.isEmpty()) return list;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM solarelo_players WHERE last_ip = ?")) {
            ps.setString(1, ip);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("created_at");
                    long createdAt = ts != null ? ts.getTime() : System.currentTimeMillis();
                    boolean settingChat = rs.getInt("setting_chat") != 0;
                    boolean settingWelcomeEffect = rs.getInt("setting_welcome_effect") != 0;
                    boolean settingTitle = rs.getInt("setting_title") != 0;
                    PlayerData data = new PlayerData(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("name"),
                            rs.getInt("elo"),
                            rs.getInt("kills"),
                            rs.getInt("deaths"),
                            rs.getInt("current_streak"),
                            rs.getInt("best_streak"),
                            createdAt,
                            settingChat,
                            settingWelcomeEffect,
                            settingTitle
                    );
                    data.setLastIp(rs.getString("last_ip"));
                    data.setLocked(rs.getInt("locked") != 0);
                    data.setLockExpiry(rs.getLong("lock_expiry"));
                    list.add(data);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get alts by IP", e);
        }
        return list;
    }
}
