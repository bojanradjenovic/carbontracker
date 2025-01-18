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
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private String selectedMode = "Car"; // Default mode
    private TextView carbonFootprintTextView;
    private Spinner transportModeSpinner;
    private Button statsButton, startTrackingButton;

    private boolean isTracking = false;
    private LocationService locationTrackingService;
    private boolean isBound = false;

    private GoogleMap googleMap;
    private List<Location> pathLocations = new ArrayList<>();
    private float totalDistance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        carbonFootprintTextView = findViewById(R.id.carbonFootprintTextView);
        transportModeSpinner = findViewById(R.id.transportModeSpinner);
        statsButton = findViewById(R.id.statsButton);
        startTrackingButton = findViewById(R.id.startTrackingButton);

        // Initialize map fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        SupportMapFragment mapFragment = new SupportMapFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.mapFragmentContainer, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);

        String[] transportModes = {"Car", "Bus", "Train", "Bicycle", "Walking"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, transportModes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transportModeSpinner.setAdapter(adapter);

        transportModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMode = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        statsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StatsActivity.class);
            startActivity(intent);
        });

        checkLocationPermission();

        startTrackingButton.setOnClickListener(v -> {
            if (isTracking) {
                stopTracking();
            } else {
                startTracking();
            }
        });

        startLocationService();
    }

    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void startTracking() {
        if (isBound && locationTrackingService != null) {
            locationTrackingService.startTracking(selectedMode);
            startTrackingButton.setText("Stop Tracking");
            isTracking = true;
        }
    }

    private void stopTracking() {
        if (isBound && locationTrackingService != null) {
            locationTrackingService.stopTracking();
            startTrackingButton.setText("Start Tracking");
            isTracking = false;
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            locationTrackingService = binder.getService();
            isBound = true;

            locationTrackingService.setLocationUpdateCallback((location, distance) -> {
                pathLocations.add(location);
                totalDistance = distance;
                updateMap();
                carbonFootprintTextView.setText(String.format(Locale.getDefault(), "Distance: %.2f km", totalDistance / 1000));
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            locationTrackingService = null;
        }
    };

    private void updateMap() {
        if (googleMap != null && !pathLocations.isEmpty()) {
            googleMap.clear();
            PolylineOptions polylineOptions = new PolylineOptions();
            for (Location location : pathLocations) {
                polylineOptions.add(new LatLng(location.getLatitude(), location.getLongitude()));
            }
            googleMap.addPolyline(polylineOptions);

            Location lastLocation = pathLocations.get(pathLocations.size() - 1);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }
}
