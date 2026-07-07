package dev.solar.solarelo.data;

import dev.solar.solarelo.SolarElo;
import dev.solar.solarelo.api.model.EloHistoryEntry;
import dev.solar.solarelo.api.model.KillHistoryEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class HistoryDao {
    private final SolarElo plugin;
    private final DatabaseManager dbManager;

    public HistoryDao(SolarElo plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
    }

    private Connection getConnection() throws SQLException {
        return dbManager.getConnection();
    }

    public void recordKill(UUID killer, UUID victim) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO solarelo_kill_history (killer_uuid, victim_uuid, kill_time) VALUES (?, ?, ?)")) {
            ps.setString(1, killer.toString());
            ps.setString(2, victim.toString());
            ps.setLong(3, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to record kill", e);
        }
    }

    public long getLastKillTime(UUID killer, UUID victim) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT kill_time FROM solarelo_kill_history WHERE killer_uuid = ? AND victim_uuid = ? " +
                             "ORDER BY kill_time DESC LIMIT 1")) {
            ps.setString(1, killer.toString());
            ps.setString(2, victim.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get last kill time", e);
        }
        return 0;
    }

    public int getRecentKillCount(UUID killer, UUID victim, long since) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT COUNT(*) FROM solarelo_kill_history WHERE killer_uuid = ? AND victim_uuid = ? " +
                             "AND kill_time > ?")) {
            ps.setString(1, killer.toString());
            ps.setString(2, victim.toString());
            ps.setLong(3, since);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get recent kill count", e);
        }
        return 0;
    }

    public void recordEloChange(UUID uuid, int changeAmount, String reason) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO solarelo_elo_history (player_uuid, change_amount, reason, created_at) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, changeAmount);
            ps.setString(3, reason);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to record Elo change", e);
        }
    }

    public long getLastEloChangeTime(UUID uuid) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT created_at FROM solarelo_elo_history WHERE player_uuid = ? ORDER BY id DESC LIMIT 1")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp(1);
                    if (ts != null) {
                        return ts.getTime();
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get last Elo change time for " + uuid, e);
        }
        return 0L;
    }

    public List<EloHistoryEntry> getEloHistory(UUID playerUuid) {
        List<EloHistoryEntry> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM solarelo_elo_history WHERE player_uuid = ? ORDER BY created_at DESC LIMIT 50")) {
            ps.setString(1, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new EloHistoryEntry(
                            rs.getInt("change_amount"),
                            rs.getString("reason"),
                            rs.getTimestamp("created_at").getTime()
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get Elo history", e);
        }
        return list;
    }

    public List<KillHistoryEntry> getKillHistory(UUID playerUuid) {
        List<KillHistoryEntry> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM solarelo_kill_history WHERE killer_uuid = ? OR victim_uuid = ? ORDER BY kill_time DESC LIMIT 50")) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, playerUuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new KillHistoryEntry(
                            UUID.fromString(rs.getString("killer_uuid")),
                            UUID.fromString(rs.getString("victim_uuid")),
                            rs.getLong("kill_time")
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get kill history", e);
        }
        return list;
    }
}
