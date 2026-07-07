package dev.solar.solarelo.api.model;

public class EloHistoryEntry {
    private final int changeAmount;
    private final String reason;
    private final long timestamp;

    public EloHistoryEntry(int changeAmount, String reason, long timestamp) {
        this.changeAmount = changeAmount;
        this.reason = reason;
        this.timestamp = timestamp;
    }

    public int getChangeAmount() { return changeAmount; }
    public String getReason() { return reason; }
    public long getTimestamp() { return timestamp; }
}
