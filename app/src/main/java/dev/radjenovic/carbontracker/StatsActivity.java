package dev.radjenovic.carbontracker;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StatsActivity extends AppCompatActivity {

    private TextView totalDistanceTextView;
    private RecyclerView transportDataRecyclerView;
    private TransportDataAdapter adapter;

    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // Initialize views
        totalDistanceTextView = findViewById(R.id.totalDistanceTextView);
        transportDataRecyclerView = findViewById(R.id.transportDataRecyclerView);

        // Initialize RecyclerView
        transportDataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransportDataAdapter(new ArrayList<>());
        transportDataRecyclerView.setAdapter(adapter);

        // Open database
        CarbonTrackerDatabase dbHelper = new CarbonTrackerDatabase(this);
        database = dbHelper.getReadableDatabase();

        // Load data
        loadStats();
    }

    private void loadStats() {
        // Query to calculate total distance
        Cursor distanceCursor = database.rawQuery(
                "SELECT SUM(" + CarbonTrackerDatabase.COLUMN_DISTANCE_TRAVELED + ") AS total_distance FROM " +
                        CarbonTrackerDatabase.TABLE_LOCATION_DATA, null);

        if (distanceCursor.moveToFirst()) {
            float totalDistance = distanceCursor.getFloat(distanceCursor.getColumnIndexOrThrow("total_distance"));
            totalDistanceTextView.setText(String.format("Total Distance Traveled: %.2f km", totalDistance / 1000));
        }
        distanceCursor.close();

        // Query to fetch transport data
        Cursor transportCursor = database.query(
                CarbonTrackerDatabase.TABLE_TRANSPORT_DATA,
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

            transportDataList.add(new TransportData(transportMode, distanceTraveled, timestamp));
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
