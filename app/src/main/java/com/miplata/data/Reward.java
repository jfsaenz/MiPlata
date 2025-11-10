package com.miplata.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rewards")
public class Reward {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "partner_name")
    private String partnerName;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "cost_in_points")
    private int costInPoints;

    @ColumnInfo(name = "category")
    private String category; // Ej: "Café", "Restaurantes", "Libros"

    @ColumnInfo(name = "qr_code_url")
    private String qrCodeUrl;

    @ColumnInfo(name = "logo_url")
    private String logoUrl;

    // Getters y Setters

    public int getId() {
        return id;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCostInPoints() {
        return costInPoints;
    }

    public void setCostInPoints(int costInPoints) {
        this.costInPoints = costInPoints;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }
}
