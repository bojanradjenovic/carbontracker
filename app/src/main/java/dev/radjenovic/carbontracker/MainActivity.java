package dev.radjenovic.carbontracker;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private TextView locationTextView, carbonFootprintTextView;
    private LocationManager locationManager;
    private final int LOCATION_REQUEST_CODE = 100;
    private Location lastLocation = null;
    private double totalDistance = 0.0; // in kilometers
    private double carbonFootprint = 0.0; // in kg CO2

    private static final String TAG = "CarbonTracker";

    private static final String KEY_TOTAL_DISTANCE = "total_distance";
    private static final String KEY_CARBON_FOOTPRINT = "carbon_footprint";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTextView = findViewById(R.id.locationTextView);
        carbonFootprintTextView = findViewById(R.id.carbonFootprintTextView);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

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

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    Log.d(TAG, "Location changed: " + location);
                    updateLocationData(location);
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

            // Update carbon footprint (assuming a car with an average of 0.12 kg CO2 per km)
            double carbonEmissionPerKm = 0.12; // kg CO2 per km
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
