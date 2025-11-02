package com.miplata.data;

public class Transaction {
    private double amount;
    private long dateMillis;
    private String type;        // DEBIT / CREDIT / UNKNOWN
    private String description; // resumen

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public long getDateMillis() { return dateMillis; }
    public void setDateMillis(long dateMillis) { this.dateMillis = dateMillis; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

