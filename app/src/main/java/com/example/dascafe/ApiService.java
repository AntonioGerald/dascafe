package com.example.dascafe;

import com.example.dascafe.model.OrderItemRequest;
import com.example.dascafe.model.OrderRequest;
import com.example.dascafe.model.OrderResponse;
import com.example.dascafe.model.Product;
import com.example.dascafe.model.ProductUpdateRequest;
import com.example.dascafe.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;
import java.util.List;

public interface ApiService {
    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Content-Type: application/json"
    })
    @GET("users")
    Call<List<User>> login(@Query("email") String email);

    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Content-Type: application/json"
    })
    @GET("products?select=*")
    Call<List<Product>> getProducts();

    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @POST("orders")
    Call<List<OrderResponse>> createOrder(@Body OrderRequest order);

    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @POST("order_items")
    Call<List<OrderItemRequest>> createOrderItem(@Body OrderItemRequest orderItem);

    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Content-Type: application/json"
    })
    @GET("orders?select=*&order=id.desc")
    Call<List<OrderResponse>> getOrdersByUser(@Query("user_id") String userIdFilter);

    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Content-Type: application/json"
    })
    @GET("order_items?select=*,products(name,price,image)")
    Call<List<OrderItemRequest>> getOrderItems(@Query("order_id") String orderIdFilter);

    // Product CRUD operations
    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @POST("products")
    Call<List<Product>> createProduct(@Body Product product);

    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Content-Type: application/json",
            "Prefer: return=representation"
    })
    @PATCH("products")
    Call<List<Product>> updateProduct(@Query("id") String productId, @Body ProductUpdateRequest product);

    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Content-Type: application/json"
    })
    @DELETE("products")
    Call<Void> deleteProduct(@Query("id") String productId);

    // Update product stock only
    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Content-Type: application/json"
    })
    @GET("products?select=stock_quantity")
    Call<List<Product>> getProductStock(@Query("id") String productId);
}


