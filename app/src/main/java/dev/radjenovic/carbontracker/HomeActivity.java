package dev.radjenovic.carbontracker;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private TextView locationTextView, carbonFootprintTextView;
    private Spinner transportModeSpinner;
    private Button statsButton, startTrackingButton;

    private boolean isTracking = false;
    private LocationService locationTrackingService;
    private boolean isBound = false;

    private MapView mapView;
    private GoogleMap googleMap;

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
        mapView = findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Initialize spinner with transport modes
        String[] transportModes = {"Car", "Bus", "Train", "Bicycle", "Walking"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, transportModes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transportModeSpinner.setAdapter(adapter);

        // Spinner item selection listener
        transportModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMode = (String) parent.getItemAtPosition(position);
                Toast.makeText(HomeActivity.this, "Selected mode: " + selectedMode, Toast.LENGTH_SHORT).show();
                // Add your logic based on the selected mode here
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle case when nothing is selected
            }
        });

        // Navigate to Stats Page
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

    // Check location permission
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Start location tracking
    private void startTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Intent serviceIntent = new Intent(this, LocationService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
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
        stopService(new Intent(this, LocationService.class));
        isTracking = false;
        startTrackingButton.setText("Start Tracking");
    }

    // Service connection
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationTrackingService = binder.getService();
            isBound = true;

            // Set callback for location updates
            locationTrackingService.setLocationUpdateCallback((location, distanceTraveled) -> {
                updateMap(locationTrackingService.getPathLocations());
                locationTextView.setText(String.format(Locale.getDefault(), "Lat: %.5f, Long: %.5f", location.getLatitude(), location.getLongitude()));
                carbonFootprintTextView.setText(String.format(Locale.getDefault(), "Distance: %.2f km", distanceTraveled / 1000));
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    // Update map path
    private void updateMap(List<Location> pathLocations) {
        if (googleMap == null || pathLocations.isEmpty()) return;

        googleMap.clear();  // Clear old map data

        PolylineOptions polylineOptions = new PolylineOptions();
        for (Location location : pathLocations) {
            polylineOptions.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }

        googleMap.addPolyline(polylineOptions);

        // Move the camera to the last location in the path
        Location lastLocation = pathLocations.get(pathLocations.size() - 1);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (isTracking) {
            stopTracking();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
