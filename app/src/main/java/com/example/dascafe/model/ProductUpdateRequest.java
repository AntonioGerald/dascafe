package com.example.dascafe.model;

public class ProductUpdateRequest {
    public String name;
    public String description;
    public String image;
    public double price;
    public int stock_quantity;
    public boolean is_active;
    // Note: No id field - it's auto-generated and shouldn't be updated
}
