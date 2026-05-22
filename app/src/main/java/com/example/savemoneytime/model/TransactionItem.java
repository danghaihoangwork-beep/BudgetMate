package com.example.savemoneytime.model;

public class TransactionItem {

    public enum Type { EXPENSE, REVENUE }

    private final int    id;
    private final String title;
    private final long   amount;
    private final String categoryName;
    private final long   date;
    private final String note;
    private final Type   type;

    public TransactionItem(int id, String title, long amount,
                           String categoryName, long date,
                           String note, Type type) {
        this.id           = id;
        this.title        = title;
        this.amount       = amount;
        this.categoryName = categoryName;
        this.date         = date;
        this.note         = note;
        this.type         = type;
    }

    public static TransactionItem fromExpense(ExpenseEntity e) {
        return new TransactionItem(
                e.getId(), e.getTitle(), e.getAmount(),
                e.getCategoryName(), e.getDate(), e.getNote(),
                Type.EXPENSE
        );
    }

    public static TransactionItem fromRevenue(RevenueEntity r) {
        return new TransactionItem(
                r.getId(), r.getTitle(), r.getAmount(),
                r.getCategoryName(), r.getDate(), r.getNote(),
                Type.REVENUE
        );
    }

    public int     getId()           { return id; }
    public String  getTitle()        { return title; }
    public long    getAmount()       { return amount; }
    public String  getCategoryName() { return categoryName; }
    public long    getDate()         { return date; }
    public String  getNote()         { return note; }
    public Type    getType()         { return type; }
    public boolean isExpense()       { return type == Type.EXPENSE; }
}