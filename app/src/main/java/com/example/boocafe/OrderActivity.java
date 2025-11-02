package com.example.boocafe;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.boocafe.adapter.CartAdapter;
import com.example.boocafe.model.CartItem;
import com.example.boocafe.model.OrderItemRequest;
import com.example.boocafe.model.OrderRequest;
import com.example.boocafe.model.OrderResponse;
import com.example.boocafe.model.Product;
import com.example.boocafe.model.ProductUpdateRequest;
import com.example.boocafe.utils.CartManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderActivity extends AppCompatActivity {
    RecyclerView rvCart;
    TextView tvTotal;
    Spinner spPaymentMethod;
    Button btnCheckout;
    Button btnChooseProduct;
    FloatingActionButton fabAddProduct;
    android.widget.ImageButton btnBack;
    CartAdapter cartAdapter;
    List<CartItem> cartList;
    View emptyLayout;

    ApiService apiService;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        rvCart = findViewById(R.id.rvCart);
        tvTotal = findViewById(R.id.tvTotal);
        spPaymentMethod = findViewById(R.id.spPaymentMethod);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnChooseProduct = findViewById(R.id.btnChooseProduct);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        btnBack = findViewById(R.id.btnBack);
        emptyLayout = findViewById(R.id.emptyLayout);

        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(this);

        cartList = CartManager.getInstance().getCart();
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, cartList, this::updateTotal);
        rvCart.setAdapter(cartAdapter);

        updateTotal();
        updateEmptyState();

        btnCheckout.setOnClickListener(v -> checkout());
        fabAddProduct.setOnClickListener(v -> openProductsActivity());
        btnBack.setOnClickListener(v -> finish());
        if (btnChooseProduct != null) {
            btnChooseProduct.setOnClickListener(v -> openProductsActivity());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ORDER_ACTIVITY", "User ID: " + sessionManager.getUserId());
        cartAdapter.notifyDataSetChanged();
        updateTotal();
    }

    private void updateTotal() {
        double total = CartManager.getInstance().getTotal();
        tvTotal.setText("Rp. " + String.format("%,d", (long) total));
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean isEmpty = cartList == null || cartList.isEmpty();
        if (emptyLayout != null) emptyLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvCart.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        btnCheckout.setEnabled(!isEmpty);
    }

    private void checkout() {
        if (cartList == null || cartList.isEmpty()) {
            Toast.makeText(this, "Keranjang kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = sessionManager != null ? sessionManager.getUserId() : -1;
        if (userId <= 0) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = spPaymentMethod.getSelectedItem() != null ? spPaymentMethod.getSelectedItem().toString() : "cash";
        double total = CartManager.getInstance().getTotal();

        // 1) Create order
        // Try common enum values: completed, processing, or leave status empty
        OrderRequest orderRequest = new OrderRequest(userId, total, paymentMethod, "completed");
        btnCheckout.setEnabled(false);
        
        Log.d("ORDER_REQUEST", "Creating order - UserID: " + userId + ", Total: " + total + ", Payment: " + paymentMethod);

        apiService.createOrder(orderRequest).enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(Call<List<OrderResponse>> call, Response<List<OrderResponse>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        int orderId = response.body().get(0).id;
                        Log.d("ORDER_SUCCESS", "Order created with ID: " + orderId);
                        postOrderItems(orderId);
                    } else {
                        btnCheckout.setEnabled(true);
                        Log.e("ORDER_ERROR", "Empty response body");
                        Toast.makeText(OrderActivity.this, "Order created but no ID returned", Toast.LENGTH_LONG).show();
                    }
                } else {
                    btnCheckout.setEnabled(true);
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("ORDER_ERROR", "Error reading error body", e);
                    }
                    Log.e("ORDER_ERROR", "Code: " + response.code() + " | Message: " + response.message() + " | Body: " + errorBody);
                    Toast.makeText(OrderActivity.this, "Failed: " + response.message() + " (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderResponse>> call, Throwable t) {
                btnCheckout.setEnabled(true);
                Log.e("ORDER_ERROR", "Error: " + t.getMessage(), t);
                Toast.makeText(OrderActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void postOrderItems(int orderId) {
        if (cartList == null || cartList.isEmpty()) {
            finishCheckoutSuccess();
            return;
        }

        // Copy list to avoid concurrent modification if UI changes
        List<CartItem> itemsToPost = new ArrayList<>(cartList);

        final int[] remaining = {itemsToPost.size()};
        final boolean[] failed = {false};

        for (CartItem ci : itemsToPost) {
            OrderItemRequest itemReq = new OrderItemRequest(
                    orderId,
                    ci.product.id,
                    ci.quantity,
                    ci.product.price,
                    ci.getSubtotal()
            );

            apiService.createOrderItem(itemReq).enqueue(new Callback<List<OrderItemRequest>>() {
                @Override
                public void onResponse(Call<List<OrderItemRequest>> call, Response<List<OrderItemRequest>> response) {
                    if (!response.isSuccessful()) {
                        failed[0] = true;
                    }
                    checkAndFinalize(remaining, failed);
                }

                @Override
                public void onFailure(Call<List<OrderItemRequest>> call, Throwable t) {
                    failed[0] = true;
                    checkAndFinalize(remaining, failed);
                }
            });
        }
    }

    private void checkAndFinalize(int[] remaining, boolean[] failed) {
        remaining[0] -= 1;
        if (remaining[0] <= 0) {
            if (failed[0]) {
                btnCheckout.setEnabled(true);
                Toast.makeText(this, "Sebagian item gagal tersimpan", Toast.LENGTH_SHORT).show();
            } else {
                finishCheckoutSuccess();
            }
        }
    }

    private void finishCheckoutSuccess() {
        Toast.makeText(this, "Order berhasil dibuat!", Toast.LENGTH_SHORT).show();
        
        // Deduct stock for each product
        deductStock();
        
        CartManager.getInstance().clearCart();
        cartAdapter.notifyDataSetChanged();
        updateTotal();
        btnCheckout.setEnabled(true);
    }
    
    private void deductStock() {
        for (CartItem item : cartList) {
            int newStock = item.product.stock_quantity - item.quantity;
            if (newStock < 0) newStock = 0;
            
            // Create update request with only stock_quantity
            ProductUpdateRequest updateRequest = new ProductUpdateRequest();
            updateRequest.name = item.product.name;
            updateRequest.description = item.product.description;
            updateRequest.image = item.product.image;
            updateRequest.price = item.product.price;
            updateRequest.stock_quantity = newStock;
            updateRequest.is_active = item.product.is_active;
            
            // Update stock in database
            apiService.updateProduct("eq." + item.product.id, updateRequest).enqueue(new Callback<List<Product>>() {
                @Override
                public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                    if (response.isSuccessful()) {
                        Log.d("STOCK_UPDATE", "Stock updated for product: " + item.product.name);
                    } else {
                        Log.e("STOCK_UPDATE", "Failed to update stock: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<List<Product>> call, Throwable t) {
                    Log.e("STOCK_UPDATE", "Error updating stock: " + t.getMessage());
                }
            });
        }
    }

    private void openProductsActivity() {
        Intent intent = new Intent(this, SelectProductActivity.class);
        startActivity(intent);
    }
}

