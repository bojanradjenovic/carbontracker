package dev.radjenovic.carbontracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CarbonTrackerDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "carbon_tracker.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TRANSPORT_DATA = "transport_data";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TRANSPORT_MODE = "transport_mode";
    public static final String COLUMN_DISTANCE_TRAVELED = "distance_traveled";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public static final String TABLE_LOCATION_DATA = "location_data";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";

    public CarbonTrackerDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create tables
        db.execSQL("CREATE TABLE " + TABLE_TRANSPORT_DATA + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TRANSPORT_MODE + " TEXT, " +
                COLUMN_DISTANCE_TRAVELED + " REAL, " +
                COLUMN_TIMESTAMP + " INTEGER)");

        db.execSQL("CREATE TABLE " + TABLE_LOCATION_DATA + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_DISTANCE_TRAVELED + " REAL, " +
                COLUMN_TRANSPORT_MODE + " TEXT, " +
                COLUMN_TIMESTAMP + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSPORT_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION_DATA);
        onCreate(db);
    }
}
