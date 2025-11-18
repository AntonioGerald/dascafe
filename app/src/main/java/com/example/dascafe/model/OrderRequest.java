package com.example.dascafe.model;

public class OrderRequest {
    public int user_id;
    public double total_amount;
    public String payment_method;
    public String status;

    public OrderRequest(int user_id, double total_amount, String payment_method, String status) {
        this.user_id = user_id;
        this.total_amount = total_amount;
        this.payment_method = payment_method;
        this.status = status;
    }


}