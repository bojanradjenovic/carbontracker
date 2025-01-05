package dev.radjenovic.carbontracker;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private TextView locationTextView, carbonFootprintTextView;
    private Spinner transportModeSpinner;
    private Button statsButton, startTrackingButton;

    private SharedPreferences sharedPreferences;

    private boolean isTracking = false; // Track the state of tracking
    private LocationService locationTrackingService;
    private boolean isBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        locationTextView = findViewById(R.id.locationTextView);
        carbonFootprintTextView = findViewById(R.id.carbonFootprintTextView);
        transportModeSpinner = findViewById(R.id.transportModeSpinner);
        statsButton = findViewById(R.id.statsButton);
        startTrackingButton = findViewById(R.id.startTrackingButton);

        // Shared preferences to store data locally
        sharedPreferences = getSharedPreferences("CarbonTracker", MODE_PRIVATE);

        // Button to navigate to Stats Page
        statsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StatsActivity.class);
            startActivity(intent);
        });

        // Request permissions
        checkLocationPermission();

        // Start/Stop Tracking Button
        startTrackingButton.setOnClickListener(v -> {
            if (isTracking) {
                stopTracking();
            } else {
                startTracking();
            }
        });
    }

    // Check and request location permissions if not granted
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Start location tracking
    private void startTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Intent serviceIntent = new Intent(this, LocationService.class);
            serviceIntent.putExtra("transportMode", getTransportMode());
            ContextCompat.startForegroundService(this, serviceIntent);

            // Bind to the service to get updates
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

            isTracking = true;
            startTrackingButton.setText("Stop Tracking");
        } else {
            checkLocationPermission();
        }
    }

    // Stop location tracking
    private void stopTracking() {
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }

        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);

        isTracking = false;
        startTrackingButton.setText("Start Tracking");
    }

    // Get the selected transport mode from the spinner
    private String getTransportMode() {
        if (transportModeSpinner.getSelectedItem() != null) {
            return transportModeSpinner.getSelectedItem().toString();
        }
        return "Car"; // Default value
    }

    // Service connection to communicate with LocationTrackingService
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationTrackingService = binder.getService();
            isBound = true;

            // Set a callback to receive location updates
            locationTrackingService.setLocationUpdateCallback((location, distanceTraveled) -> {
                // Update location TextView with real-time location
                String locationText = String.format(Locale.getDefault(),
                        "Latitude: %.5f\nLongitude: %.5f", location.getLatitude(), location.getLongitude());
                locationTextView.setText(locationText);

                // Update distance TextView
                String distanceText = String.format(Locale.getDefault(), "Distance traveled: %.2f km", distanceTraveled / 1000);
                carbonFootprintTextView.setText(distanceText);
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTracking) {
            stopTracking();
        }
    }
}
