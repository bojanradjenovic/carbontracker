package dev.radjenovic.carbontracker;

public class TransportData {
    private String transportMode;
    private float distanceTraveled;
    private long timestamp;
    private float co2Emissions;

    public TransportData(String transportMode, float distanceTraveled, long timestamp, float co2Emissions) {
        this.transportMode = transportMode;
        this.distanceTraveled = distanceTraveled;
        this.timestamp = timestamp;
        this.co2Emissions = co2Emissions;
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

    public float getCo2Emissions() {
        return co2Emissions;
    }
}
