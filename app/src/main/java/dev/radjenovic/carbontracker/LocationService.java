package dev.radjenovic.carbontracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "LocationTrackingChannel";
    private final IBinder binder = new LocalBinder();
    private boolean isTracking = false;
    private List<Location> locations = new ArrayList<>();
    private float totalDistance = 0;
    private String transportMode = "Car"; // Default transport mode
    private LocationUpdateCallback locationUpdateCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private CarbonTrackerDatabase database;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder; // Return the binder to allow the activity to interact with the service
    }

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        database = new CarbonTrackerDatabase(this);
        createNotificationChannel();
        startForegroundService();
        initializeLocationRequest();
    }

    private void startForegroundService() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tracking Location")
                .setContentText("Tracking your carbon footprint...")
                .setSmallIcon(R.drawable.ic_location) // Use your own icon here
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Location Tracking", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void initializeLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public class LocalBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }

    // Start tracking method
    public void startTracking(String mode) {
        if (isTracking) {
            Log.d("LocationService", "Already tracking.");
            return;
        }
        isTracking = true;
        transportMode = mode; // Set the transport mode
        locations.clear(); // Clear previous locations
        totalDistance = 0;

        Log.d("LocationService", "Started tracking.");
        startLocationUpdates();
    }

    // Stop tracking method
    public void stopTracking() {
        if (!isTracking) {
            Log.d("LocationService", "Not currently tracking.");
            return;
        }
        isTracking = false;
        Log.d("LocationService", "Stopped tracking.");
        stopLocationUpdates();
        saveTrackingData(); // Save data when tracking stops
    }

    private void startLocationUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    onLocationUpdate(location);
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void onLocationUpdate(Location location) {
        if (locationUpdateCallback != null && isTracking) {
            if (!locations.isEmpty()) {
                Location lastLocation = locations.get(locations.size() - 1);
                totalDistance += lastLocation.distanceTo(location); // Calculate distance
            }
            locations.add(location);
            locationUpdateCallback.onLocationUpdate(location, totalDistance); // Update UI in HomeActivity
        }
    }

    // Set the callback to receive location updates
    public void setLocationUpdateCallback(LocationUpdateCallback callback) {
        this.locationUpdateCallback = callback;
    }

    // Save tracking data when the tracking is stopped
    private void saveTrackingData() {
        Log.d("LocationService", "Saving tracking data...");

        // Insert data into the database
        long timestamp = System.currentTimeMillis();
        float distanceTraveled = totalDistance / 1000; // Convert distance to kilometers

        SQLiteDatabase db = database.getWritableDatabase();
        String insertQuery = "INSERT INTO " + CarbonTrackerDatabase.TABLE_LOCATION_DATA +
                " (" + CarbonTrackerDatabase.COLUMN_TRANSPORT_MODE + ", " +
                CarbonTrackerDatabase.COLUMN_DISTANCE_TRAVELED + ", " +
                CarbonTrackerDatabase.COLUMN_TIMESTAMP + ") VALUES (?, ?, ?)";

        db.execSQL(insertQuery, new Object[]{transportMode, distanceTraveled, timestamp});
        db.close();
    }

    // Interface to handle location updates in HomeActivity
    public interface LocationUpdateCallback {
        void onLocationUpdate(Location location, float totalDistance);
    }
}
