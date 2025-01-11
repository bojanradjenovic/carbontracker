package dev.radjenovic.carbontracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

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

    private static final String CHANNEL_ID = "location_channel";
    private static final int NOTIFICATION_ID = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private SQLiteDatabase database;

    private List<Location> pathLocations = new ArrayList<>();
    private float distanceTraveled = 0f;  // Track distance
    private Location previousLocation = null;  // Track the last known location

    private LocationUpdateCallback locationUpdateCallback;  // Callback to send updates

    @Override
    public void onCreate() {
        super.onCreate();
        CarbonTrackerDatabase dbHelper = new CarbonTrackerDatabase(this);
        database = dbHelper.getWritableDatabase();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    pathLocations.add(location);  // Add to path

                    // Calculate distance if possible
                    if (previousLocation != null) {
                        distanceTraveled += previousLocation.distanceTo(location);
                    }

                    previousLocation = location;  // Update the previous location

                    saveLocationToDatabase(location);  // Store in database

                    if (locationUpdateCallback != null) {
                        locationUpdateCallback.onLocationUpdated(location, distanceTraveled);
                    }
                }
            }
        };

        startForegroundService();  // Start as foreground service
        requestLocationUpdates();  // Request location updates
    }

    // Save location data to the database
    private void saveLocationToDatabase(Location location) {
        ContentValues values = new ContentValues();
        values.put("latitude", location.getLatitude());
        values.put("longitude", location.getLongitude());
        values.put("timestamp", System.currentTimeMillis());
        database.insert("location_data", null, values);
        Log.d("LocationService", "Saved location to database");
    }

    private void startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tracking Location")
                .setContentText("Your location is being tracked")
                .setSmallIcon(R.drawable.ic_location)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);  // Update interval in milliseconds
        locationRequest.setFastestInterval(2000);  // Fastest interval
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        if (database != null) {
            database.close();  // Close the database
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    public class LocalBinder extends android.os.Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    // Setter for callback
    public void setLocationUpdateCallback(LocationUpdateCallback callback) {
        this.locationUpdateCallback = callback;
    }

    // Interface for location updates
    public interface LocationUpdateCallback {
        void onLocationUpdated(Location location, float distanceTraveled);
    }

    // Getters for the path and distance
    public List<Location> getPathLocations() {
        return pathLocations;
    }

    public float getDistanceTraveled() {
        return distanceTraveled;
    }
}
