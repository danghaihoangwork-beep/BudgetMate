package com.example.savemoneytime.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.savemoneytime.model.ExpenseEntity;
import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insert(ExpenseEntity expense);

    @Delete
    void delete(ExpenseEntity expense);

    @Query("DELETE FROM expenses WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    List<ExpenseEntity> getAll();

    @Query("SELECT * FROM expenses " +
            "WHERE strftime('%m', date/1000, 'unixepoch') = :month " +
            "AND strftime('%Y', date/1000, 'unixepoch') = :year " +
            "ORDER BY date DESC")
    List<ExpenseEntity> getByMonth(String month, String year);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses " +
            "WHERE strftime('%m', date/1000, 'unixepoch') = :month " +
            "AND strftime('%Y', date/1000, 'unixepoch') = :year")
    long getTotalByMonth(String month, String year);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses")
    long getTotalAll();
}