package com.example.savemoneytime.MainApplication;

import com.example.savemoneytime.model.TransactionItem;

public class DisplayItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TRANSACTION = 1;

    private final int type;
    private String headerTitle;
    private TransactionItem transaction;

    public DisplayItem(String headerTitle) {
        this.type = TYPE_HEADER;
        this.headerTitle = headerTitle;
    }

    public DisplayItem(TransactionItem transaction) {
        this.type = TYPE_TRANSACTION;
        this.transaction = transaction;
    }

    public int getType() { return type; }
    public String getHeaderTitle() { return headerTitle; }
    public TransactionItem getTransaction() { return transaction; }
}