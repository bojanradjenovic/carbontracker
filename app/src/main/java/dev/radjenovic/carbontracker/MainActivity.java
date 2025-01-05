package dev.radjenovic.carbontracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private TextView locationTextView, carbonFootprintTextView;
    private Spinner transportModeSpinner;
    private LocationManager locationManager;
    private final int LOCATION_REQUEST_CODE = 100;
    private Location lastLocation = null;
    private double totalDistance = 0.0; // in kilometers
    private double carbonFootprint = 0.0; // in kg CO2

    private static final String TAG = "CarbonTracker";

    private static final String KEY_TOTAL_DISTANCE = "total_distance";
    private static final String KEY_CARBON_FOOTPRINT = "carbon_footprint";

    private double carbonEmissionPerKm = 0.12; // Default to car

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTextView = findViewById(R.id.locationTextView);
        carbonFootprintTextView = findViewById(R.id.carbonFootprintTextView);
        transportModeSpinner = findViewById(R.id.transportModeSpinner);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Initialize the Spinner with transportation options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.transport_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transportModeSpinner.setAdapter(adapter);

        // Handle user selection from the Spinner
        transportModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                switch (position) {
                    case 0: // Car
                        carbonEmissionPerKm = 0.12;
                        break;
                    case 1: // Bike
                        carbonEmissionPerKm = 0.05;
                        break;
                    case 2: // Bus
                        carbonEmissionPerKm = 0.02;
                        break;
                }
                // Update the UI with the new carbon emission value
                updateUI();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Default to car if no selection
                carbonEmissionPerKm = 0.12;
            }
        });

        // Restore state if available
        if (savedInstanceState != null) {
            totalDistance = savedInstanceState.getDouble(KEY_TOTAL_DISTANCE, 0.0);
            carbonFootprint = savedInstanceState.getDouble(KEY_CARBON_FOOTPRINT, 0.0);
            updateUI();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDouble(KEY_TOTAL_DISTANCE, totalDistance);
        outState.putDouble(KEY_CARBON_FOOTPRINT, carbonFootprint);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Starting location updates...", Toast.LENGTH_SHORT).show();

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, // Use GPS provider
                    1000, // Minimum time interval between updates (1 second)
                    1, // Minimum distance between updates (1 meter)
                    new LocationListener() {
                        @Override
                        public void onLocationChanged(@NonNull Location location) {
                            // Filter out less accurate location data
                            if (location.getAccuracy() <= 50) {  // Only use accurate data (within 50 meters)
                                Log.d(TAG, "Location changed: " + location);
                                updateLocationData(location);
                            }
                        }

                        @Override
                        public void onProviderEnabled(@NonNull String provider) {
                            Log.d(TAG, "Provider enabled: " + provider);
                        }

                        @Override
                        public void onProviderDisabled(@NonNull String provider) {
                            Log.d(TAG, "Provider disabled: " + provider);
                            Toast.makeText(MainActivity.this, "Please enable GPS!", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLocationData(Location location) {
        // Calculate distance and update total distance
        if (lastLocation != null) {
            double distance = lastLocation.distanceTo(location) / 1000.0; // Convert meters to kilometers
            totalDistance += distance;

            // Update carbon footprint based on selected transportation mode
            carbonFootprint += distance * carbonEmissionPerKm;
        }

        lastLocation = location;

        // Update UI
        updateUI();

        // Log data
        Log.d(TAG, "Total Distance: " + totalDistance + " km");
        Log.d(TAG, "Carbon Footprint: " + carbonFootprint + " kg CO2");
    }

    private void updateUI() {
        locationTextView.setText(String.format("Latitude: %.6f\nLongitude: %.6f",
                lastLocation != null ? lastLocation.getLatitude() : 0.0,
                lastLocation != null ? lastLocation.getLongitude() : 0.0));
        carbonFootprintTextView.setText(String.format("Distance Traveled: %.2f km\nCarbon Footprint: %.2f kg CO2",
                totalDistance, carbonFootprint));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                locationTextView.setText("Permission denied. Cannot access location.");
            }
        }
    }
}
