package com.example.dascafe.model;

public class OrderItemRequest {
    public int order_id;
    public int product_id;
    public int quantity;
    public double price;
    public double subtotal;
    
    // For joined queries with product details
    public Product products;

    public OrderItemRequest(int order_id, int product_id, int quantity, double price, double subtotal) {
        this.order_id = order_id;
        this.product_id = product_id;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }
}
