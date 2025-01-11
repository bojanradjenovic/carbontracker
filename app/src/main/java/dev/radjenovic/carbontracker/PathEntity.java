package dev.radjenovic.carbontracker;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

@Entity(tableName = "paths")
public class PathEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String transportMode;

    @TypeConverters(LatLngConverter.class)
    public List<LatLng> pathPoints;

    public long timestamp;
}
