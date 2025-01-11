package dev.radjenovic.carbontracker;

public class TransportData {
    private String transportMode;
    private float distanceTraveled;
    private long timestamp;

    public TransportData(String transportMode, float distanceTraveled, long timestamp) {
        this.transportMode = transportMode;
        this.distanceTraveled = distanceTraveled;
        this.timestamp = timestamp;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public float getDistanceTraveled() {
        return distanceTraveled;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
