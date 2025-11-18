package com.example.dascafe.model;

public class ProductSales {
    public int product_id;
    public String product_name;
    public String product_image;
    public int quantity_sold;
    public double total_revenue;

    public ProductSales(int product_id, String product_name, String product_image, int quantity_sold, double total_revenue) {
        this.product_id = product_id;
        this.product_name = product_name;
        this.product_image = product_image;
        this.quantity_sold = quantity_sold;
        this.total_revenue = total_revenue;
    }
}
