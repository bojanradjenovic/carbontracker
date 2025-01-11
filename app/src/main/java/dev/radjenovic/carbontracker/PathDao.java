package dev.radjenovic.carbontracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PathDao {
    @Insert
    void insertPath(PathEntity pathEntity);

    @Query("SELECT * FROM paths ORDER BY timestamp DESC")
    List<PathEntity> getAllPaths();
}
