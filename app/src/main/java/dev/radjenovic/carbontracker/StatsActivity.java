package dev.radjenovic.carbontracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StatsActivity extends AppCompatActivity {

    private TextView statsTextView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // Initialize views
        statsTextView = findViewById(R.id.statsTextView);

        // Shared preferences to get stored data
        sharedPreferences = getSharedPreferences("CarbonTracker", MODE_PRIVATE);

        // Retrieve total distance and carbon footprint
        float totalDistance = sharedPreferences.getFloat("totalDistance", 0);
        float totalCarbonFootprint = sharedPreferences.getFloat("carbonFootprint", 0);

        // Display the stats
        statsTextView.setText("Total Distance: " + totalDistance + " km\nTotal Carbon Footprint: " + totalCarbonFootprint + " kg CO2");
    }
}
