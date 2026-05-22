package com.example.savemoneytime.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.savemoneytime.model.RevenueEntity;
import java.util.List;

@Dao
public interface RevenueDao {

    @Insert
    void insert(RevenueEntity revenue);

    @Delete
    void delete(RevenueEntity revenue);

    @Query("DELETE FROM revenues WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM revenues ORDER BY date DESC")
    List<RevenueEntity> getAll();

    @Query("SELECT * FROM revenues " +
            "WHERE strftime('%m', date/1000, 'unixepoch') = :month " +
            "AND strftime('%Y', date/1000, 'unixepoch') = :year " +
            "ORDER BY date DESC")
    List<RevenueEntity> getByMonth(String month, String year);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM revenues " +
            "WHERE strftime('%m', date/1000, 'unixepoch') = :month " +
            "AND strftime('%Y', date/1000, 'unixepoch') = :year")
    long getTotalByMonth(String month, String year);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM revenues")
    long getTotalAll();
}