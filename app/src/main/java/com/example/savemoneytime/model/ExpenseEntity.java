package com.example.savemoneytime.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses")
public class ExpenseEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "amount")
    private long amount;

    @ColumnInfo(name = "category_name")
    private String categoryName;

    @ColumnInfo(name = "date")
    private long date; // Unix timestamp milliseconds

    @ColumnInfo(name = "note")
    private String note;

    public ExpenseEntity(String title, long amount, String categoryName,
                         long date, String note) {
        this.title        = title;
        this.amount       = amount;
        this.categoryName = categoryName;
        this.date         = date;
        this.note         = note;
    }

    public int    getId()           { return id; }
    public String getTitle()        { return title; }
    public long   getAmount()       { return amount; }
    public String getCategoryName() { return categoryName; }
    public long   getDate()         { return date; }
    public String getNote()         { return note; }
    public void   setId(int id)     { this.id = id; }
}