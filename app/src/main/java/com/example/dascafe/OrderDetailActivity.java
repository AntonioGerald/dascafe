package com.example.dascafe;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dascafe.adapter.OrderDetailAdapter;
import com.example.dascafe.model.OrderItemRequest;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {
    TextView tvOrderId, tvStatus, tvPaymentMethod, tvTotalAmount;
    RecyclerView rvOrderItems;
    ImageButton btnBack;
    ApiService apiService;
    OrderDetailAdapter adapter;

    int orderId;
    double totalAmount;
    String paymentMethod;
    String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Initialize views
        tvOrderId = findViewById(R.id.tvOrderId);
        tvStatus = findViewById(R.id.tvStatus);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        btnBack = findViewById(R.id.btnBack);

        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));

        apiService = ApiClient.getClient().create(ApiService.class);

        // Get data from intent
        orderId = getIntent().getIntExtra("order_id", 0);
        totalAmount = getIntent().getDoubleExtra("total_amount", 0);
        paymentMethod = getIntent().getStringExtra("payment_method");
        status = getIntent().getStringExtra("status");

        // Set order info
        tvOrderId.setText("Order #" + orderId);
        tvTotalAmount.setText("Rp. " + String.format("%,d", (long) totalAmount));
        tvPaymentMethod.setText(paymentMethod != null ? paymentMethod : "cash");
        
        // Set status with color
        if (status != null) {
            tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
            int statusColor;
            switch (status.toLowerCase()) {
                case "completed":
                    statusColor = 0xFF4CAF50; // Green
                    break;
                case "cancelled":
                    statusColor = 0xFFF44336; // Red
                    break;
                default:
                    statusColor = 0xFFFFC107; // Yellow
                    break;
            }
            tvStatus.setTextColor(statusColor);
        }

        btnBack.setOnClickListener(v -> finish());

        // Load order items
        loadOrderItems();
    }

    private void loadOrderItems() {
        apiService.getOrderItems("eq." + orderId).enqueue(new Callback<List<OrderItemRequest>>() {
            @Override
            public void onResponse(Call<List<OrderItemRequest>> call, Response<List<OrderItemRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<OrderItemRequest> items = response.body();
                    adapter = new OrderDetailAdapter(OrderDetailActivity.this, items);
                    rvOrderItems.setAdapter(adapter);
                } else {
                    Log.e("ORDER_DETAIL", "Failed to load items: " + response.code());
                    Toast.makeText(OrderDetailActivity.this, "Failed to load order items", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderItemRequest>> call, Throwable t) {
                Log.e("ORDER_DETAIL", "Error: " + t.getMessage(), t);
                Toast.makeText(OrderDetailActivity.this, "Error loading items", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
