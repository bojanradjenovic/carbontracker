package dev.radjenovic.carbontracker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
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

    private List<Location> pathLocations = new ArrayList<>();
    private float distanceTraveled = 0f;  // Store the distance traveled

    // Callback interface to send updates to the activity
    private LocationUpdateCallback locationUpdateCallback;
    private Location previousLocation = null; // Store the previous location

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Location callback to receive updates
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    // Add location to path
                    pathLocations.add(location);

                    // Calculate distance if previousLocation is available
                    if (previousLocation != null) {
                        distanceTraveled += previousLocation.distanceTo(location);
                    }

                    // Update the previous location
                    previousLocation = location;

                    // Optionally, broadcast the location for updates in the activity
                    Intent intent = new Intent("LOCATION_UPDATE");
                    intent.putExtra("location", location);
                    sendBroadcast(intent);

                    // If the callback is set, call it with the new location and distance
                    if (locationUpdateCallback != null) {
                        locationUpdateCallback.onLocationUpdated(location, distanceTraveled);
                    }
                }
            }
        };

        // Start foreground service with a notification
        startForegroundService();
        requestLocationUpdates();
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
                .setContentText("Your location is being tracked.")
                .setSmallIcon(R.drawable.ic_location) // Replace with your app's icon
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // Update interval in milliseconds
        locationRequest.setFastestInterval(2000); // Fastest interval in milliseconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    // Add this inner class for binding the service
    public class LocalBinder extends android.os.Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    // Method to set the callback for location updates
    public void setLocationUpdateCallback(LocationUpdateCallback callback) {
        this.locationUpdateCallback = callback;
    }

    // Callback interface for location updates
    public interface LocationUpdateCallback {
        void onLocationUpdated(Location location, float distanceTraveled);
    }

    // Method to get the total distance traveled
    public float getDistanceTraveled() {
        return distanceTraveled;
    }

    // Method to get the list of locations
    public List<Location> getPathLocations() {
        return pathLocations;
    }
}
