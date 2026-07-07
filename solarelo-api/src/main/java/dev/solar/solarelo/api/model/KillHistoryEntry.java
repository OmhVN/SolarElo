package dev.solar.solarelo.api.model;

import java.util.UUID;

public class KillHistoryEntry {
    private final UUID killerUuid;
    private final UUID victimUuid;
    private final long timestamp;

    public KillHistoryEntry(UUID killerUuid, UUID victimUuid, long timestamp) {
        this.killerUuid = killerUuid;
        this.victimUuid = victimUuid;
        this.timestamp = timestamp;
    }

    public UUID getKillerUuid() { return killerUuid; }
    public UUID getVictimUuid() { return victimUuid; }
    public long getTimestamp() { return timestamp; }
}
