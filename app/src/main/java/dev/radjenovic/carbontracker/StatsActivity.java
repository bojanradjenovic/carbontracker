package dev.radjenovic.carbontracker;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsActivity extends AppCompatActivity {

    private TextView totalDistanceTextView;
    private RecyclerView transportDataRecyclerView;
    private TransportDataAdapter adapter;

    private SQLiteDatabase database;

    private static final Map<String, Float> EMISSION_FACTORS;

    static {
        EMISSION_FACTORS = new HashMap<>();
        EMISSION_FACTORS.put("Car", 0.121f);     // kg CO2/km
        EMISSION_FACTORS.put("Bus", 0.089f);    // kg CO2/km
        EMISSION_FACTORS.put("Train", 0.041f);  // kg CO2/km
        EMISSION_FACTORS.put("Bicycle", 0.0f);  // kg CO2/km
        EMISSION_FACTORS.put("Walking", 0.0f);  // kg CO2/km
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // Initialize views
        totalDistanceTextView = findViewById(R.id.totalDistanceTextView);
        transportDataRecyclerView = findViewById(R.id.transportDataRecyclerView);

        Button exportButton = findViewById(R.id.exportButton);
        // Initialize RecyclerView
        transportDataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransportDataAdapter(new ArrayList<>());
        transportDataRecyclerView.setAdapter(adapter);

        // Open database
        CarbonTrackerDatabase dbHelper = new CarbonTrackerDatabase(this);
        database = dbHelper.getReadableDatabase();

        // Load data
        loadStats();

        // Set up export button click listener
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportDatabaseToDocuments();
            }
        });
    }

    // Method to export the database to Documents folder using MediaStore
    private void exportDatabaseToDocuments() {
        // Get the source database file
        File sourceFile = new File(getDatabasePath(CarbonTrackerDatabase.DATABASE_NAME).getAbsolutePath());

        // Define the content values for the MediaStore
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "carbon_tracker.db");  // File name
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream");  // MIME type
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);  // Documents folder

        // Get the content resolver
        ContentResolver resolver = getContentResolver();
        Uri contentUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        // Insert the file into MediaStore
        Uri uri = resolver.insert(contentUri, values);

        if (uri != null) {
            try (InputStream inputStream = new FileInputStream(sourceFile);
                 OutputStream outputStream = resolver.openOutputStream(uri)) {

                if (outputStream == null) {
                    throw new IOException("Failed to open output stream.");
                }

                // Copy the content of the source file into the output stream
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                // Inform the user that the database was exported successfully
                Toast.makeText(this, "Database exported to Documents", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error exporting database.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadStats() {
        // Query to calculate total distance
        Cursor distanceCursor = database.rawQuery(
                "SELECT SUM(" + CarbonTrackerDatabase.COLUMN_DISTANCE_TRAVELED + ") AS total_distance FROM " +
                        CarbonTrackerDatabase.TABLE_LOCATION_DATA, null);

        if (distanceCursor.moveToFirst()) {
            float totalDistance = distanceCursor.getFloat(distanceCursor.getColumnIndexOrThrow("total_distance"));
            totalDistanceTextView.setText(String.format("Total Distance Traveled: %.2f km", totalDistance));
        }
        distanceCursor.close();

        // Query to fetch transport data
        Cursor transportCursor = database.query(
                CarbonTrackerDatabase.TABLE_LOCATION_DATA,
                null,
                null,
                null,
                null,
                null,
                CarbonTrackerDatabase.COLUMN_TIMESTAMP + " DESC"
        );

        List<TransportData> transportDataList = new ArrayList<>();
        while (transportCursor.moveToNext()) {
            String transportMode = transportCursor.getString(transportCursor.getColumnIndexOrThrow(CarbonTrackerDatabase.COLUMN_TRANSPORT_MODE));
            float distanceTraveled = transportCursor.getFloat(transportCursor.getColumnIndexOrThrow(CarbonTrackerDatabase.COLUMN_DISTANCE_TRAVELED));
            long timestamp = transportCursor.getLong(transportCursor.getColumnIndexOrThrow(CarbonTrackerDatabase.COLUMN_TIMESTAMP));

            // Calculate CO2 emissions for the trip
            float emissionFactor = EMISSION_FACTORS.getOrDefault(transportMode, 0f);
            float co2Emissions = (distanceTraveled / 1000) * emissionFactor; // Convert distance to km for CO2 calculation

            // Add the trip data to the list
            transportDataList.add(new TransportData(transportMode, distanceTraveled, timestamp, co2Emissions));
        }
        transportCursor.close();

        // Update RecyclerView adapter
        adapter.updateData(transportDataList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}
