package dev.radjenovic.carbontracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CarbonTrackerDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "carbon_tracker.db";
    public static final int DATABASE_VERSION = 1;

    // Table name and columns
    public static final String TABLE_LOCATION_DATA = "location_data";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TRANSPORT_MODE = "transport_mode";
    public static final String COLUMN_DISTANCE_TRAVELED = "distance_traveled";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // SQL query to create the table
    private static final String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE_LOCATION_DATA + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TRANSPORT_MODE + " TEXT NOT NULL, " +
            COLUMN_DISTANCE_TRAVELED + " REAL NOT NULL, " +
            COLUMN_TIMESTAMP + " INTEGER NOT NULL" +
            ");";

    public CarbonTrackerDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION_DATA);
        onCreate(db);
    }
}
