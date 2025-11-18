package com.example.dascafe;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dascafe.adapter.BestSellingAdapter;
import com.example.dascafe.model.OrderItemRequest;
import com.example.dascafe.model.OrderResponse;
import com.example.dascafe.model.ProductSales;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends AppCompatActivity {
    ImageButton btnBack;
    TextView tvTotalSales, tvTotalTransactions, tvTotalProductsSold;
    RecyclerView rvBestSelling;
    View emptyLayout, layoutDateRange;
    ChipGroup chipGroupTimeframe;
    Chip chipToday, chipWeek, chipMonth, chipAll, chipCustom;
    Button btnStartDate, btnEndDate;
    
    ApiService apiService;
    SessionManager sessionManager;
    BestSellingAdapter bestSellingAdapter;
    
    List<OrderResponse> allOrders;
    List<OrderItemRequest> allOrderItems;
    Calendar startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        tvTotalSales = findViewById(R.id.tvTotalSales);
        tvTotalTransactions = findViewById(R.id.tvTotalTransactions);
        tvTotalProductsSold = findViewById(R.id.tvTotalProductsSold);
        rvBestSelling = findViewById(R.id.rvBestSelling);
        emptyLayout = findViewById(R.id.emptyLayout);
        layoutDateRange = findViewById(R.id.layoutDateRange);
        chipGroupTimeframe = findViewById(R.id.chipGroupTimeframe);
        chipToday = findViewById(R.id.chipToday);
        chipWeek = findViewById(R.id.chipWeek);
        chipMonth = findViewById(R.id.chipMonth);
        chipAll = findViewById(R.id.chipAll);
        chipCustom = findViewById(R.id.chipCustom);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);

        rvBestSelling.setLayoutManager(new LinearLayoutManager(this));
        
        // Initialize date range to today
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();

        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(this);

        btnBack.setOnClickListener(v -> finish());
        setupTimeframeFilter();
        setupDatePickers();

        loadData();
    }

    private void setupTimeframeFilter() {
        chipGroupTimeframe.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipToday) {
                layoutDateRange.setVisibility(View.GONE);
                calculateReports("today");
            } else if (checkedId == R.id.chipWeek) {
                layoutDateRange.setVisibility(View.GONE);
                calculateReports("week");
            } else if (checkedId == R.id.chipMonth) {
                layoutDateRange.setVisibility(View.GONE);
                calculateReports("month");
            } else if (checkedId == R.id.chipAll) {
                layoutDateRange.setVisibility(View.GONE);
                calculateReports("all");
            } else if (checkedId == R.id.chipCustom) {
                layoutDateRange.setVisibility(View.VISIBLE);
                calculateReportsByCustomRange();
            }
        });
    }

    private void setupDatePickers() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        
        btnStartDate.setOnClickListener(v -> new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    startDate.set(year, month, dayOfMonth, 0, 0, 0);
                    startDate.set(Calendar.MILLISECOND, 0);
                    btnStartDate.setText(dateFormat.format(startDate.getTime()));
                    if (chipCustom.isChecked()) calculateReportsByCustomRange();
                },
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH)
        ).show());

        btnEndDate.setOnClickListener(v -> new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    endDate.set(year, month, dayOfMonth, 23, 59, 59);
                    endDate.set(Calendar.MILLISECOND, 999);
                    btnEndDate.setText(dateFormat.format(endDate.getTime()));
                    if (chipCustom.isChecked()) calculateReportsByCustomRange();
                },
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH)
        ).show());
        
        btnStartDate.setText(dateFormat.format(startDate.getTime()));
        btnEndDate.setText(dateFormat.format(endDate.getTime()));
    }

    private void loadData() {
        int userId = sessionManager.getUserId();
        if (userId <= 0) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load orders
        apiService.getOrdersByUser("eq." + userId).enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(Call<List<OrderResponse>> call, Response<List<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allOrders = response.body();
                    loadOrderItems();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("REPORTS", "Error reading error body", e);
                    }
                    Log.e("REPORTS", "Failed to load orders: " + response.code() + " | " + response.message() + " | Body: " + errorBody);
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<OrderResponse>> call, Throwable t) {
                Log.e("REPORTS", "Error loading orders: " + t.getMessage());
                showEmptyState();
            }
        });
    }

    private void loadOrderItems() {
        // Load all order items
        apiService.getOrderItems(null).enqueue(new Callback<List<OrderItemRequest>>() {
            @Override
            public void onResponse(Call<List<OrderItemRequest>> call, Response<List<OrderItemRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allOrderItems = response.body();
                    calculateReports("all");
                } else {
                    Log.e("REPORTS", "Failed to load order items: " + response.code());
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<OrderItemRequest>> call, Throwable t) {
                Log.e("REPORTS", "Error loading order items: " + t.getMessage());
                showEmptyState();
            }
        });
    }

    private void calculateReports(String timeframe) {
        if (allOrders == null || allOrderItems == null) {
            showEmptyState();
            return;
        }

        // Filter orders by timeframe
        List<OrderResponse> filteredOrders = filterOrdersByTimeframe(timeframe);

        if (filteredOrders.isEmpty()) {
            showEmptyState();
            return;
        }

        // Calculate statistics
        double totalSales = 0;
        int totalTransactions = filteredOrders.size();
        int totalProductsSold = 0;

        // Get order IDs
        List<Integer> orderIds = new ArrayList<>();
        for (OrderResponse order : filteredOrders) {
            orderIds.add(order.id);
            totalSales += order.total_amount;
        }

        // Calculate product sales
        Map<Integer, ProductSales> productSalesMap = new HashMap<>();

        for (OrderItemRequest item : allOrderItems) {
            if (orderIds.contains(item.order_id)) {
                totalProductsSold += item.quantity;

                // Aggregate by product
                int productId = item.product_id;
                if (productSalesMap.containsKey(productId)) {
                    ProductSales existing = productSalesMap.get(productId);
                    existing.quantity_sold += item.quantity;
                    existing.total_revenue += item.subtotal;
                } else {
                    String productName = item.products != null ? item.products.name : "Product #" + productId;
                    String productImage = item.products != null ? item.products.image : null;
                    productSalesMap.put(productId, new ProductSales(
                            productId,
                            productName,
                            productImage,
                            item.quantity,
                            item.subtotal
                    ));
                }
            }
        }

        // Update UI
        tvTotalSales.setText("Rp. " + String.format("%,d", (long) totalSales));
        tvTotalTransactions.setText(String.valueOf(totalTransactions));
        tvTotalProductsSold.setText(totalProductsSold + " items");

        // Sort products by quantity sold
        List<ProductSales> productSalesList = new ArrayList<>(productSalesMap.values());
        Collections.sort(productSalesList, new Comparator<ProductSales>() {
            @Override
            public int compare(ProductSales p1, ProductSales p2) {
                return Integer.compare(p2.quantity_sold, p1.quantity_sold);
            }
        });

        // Show best selling products
        if (productSalesList.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            rvBestSelling.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            rvBestSelling.setVisibility(View.VISIBLE);
            bestSellingAdapter = new BestSellingAdapter(this, productSalesList);
            rvBestSelling.setAdapter(bestSellingAdapter);
        }
    }

    private List<OrderResponse> filterOrdersByTimeframe(String timeframe) {
        List<OrderResponse> filteredOrders = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        for (OrderResponse order : allOrders) {
            if (order.created_at == null || order.created_at.isEmpty()) {
                if (timeframe.equals("all")) {
                    filteredOrders.add(order);
                }
                continue;
            }

            try {
                Date orderDate = sdf.parse(order.created_at);
                if (orderDate == null) continue;

                boolean include = false;
                switch (timeframe) {
                    case "today":
                        calendar.setTime(now);
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        Date startOfDay = calendar.getTime();
                        include = orderDate.after(startOfDay) || orderDate.equals(startOfDay);
                        break;

                    case "week":
                        calendar.setTime(now);
                        calendar.add(Calendar.DAY_OF_YEAR, -7);
                        Date weekAgo = calendar.getTime();
                        include = orderDate.after(weekAgo);
                        break;

                    case "month":
                        calendar.setTime(now);
                        calendar.add(Calendar.MONTH, -1);
                        Date monthAgo = calendar.getTime();
                        include = orderDate.after(monthAgo);
                        break;

                    case "all":
                        include = true;
                        break;
                }

                if (include) {
                    filteredOrders.add(order);
                }
            } catch (ParseException e) {
                Log.e("DATE_PARSE", "Error parsing date: " + order.created_at, e);
                if (timeframe.equals("all")) {
                    filteredOrders.add(order);
                }
            }
        }

        return filteredOrders;
    }

    private void showEmptyState() {
        tvTotalSales.setText("Rp. 0");
        tvTotalTransactions.setText("0");
        tvTotalProductsSold.setText("0 items");
        emptyLayout.setVisibility(View.VISIBLE);
        rvBestSelling.setVisibility(View.GONE);
    }

    private void calculateReportsByCustomRange() {
        if (allOrders == null || allOrderItems == null) {
            showEmptyState();
            return;
        }

        List<OrderResponse> filteredOrders = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Date start = startDate.getTime();
        Date end = endDate.getTime();

        for (OrderResponse order : allOrders) {
            if (order.created_at == null || order.created_at.isEmpty()) continue;

            try {
                Date orderDate = sdf.parse(order.created_at);
                if (orderDate != null && !orderDate.before(start) && !orderDate.after(end)) {
                    filteredOrders.add(order);
                }
            } catch (ParseException e) {
                Log.e("REPORTS", "Date parse error: " + e.getMessage());
            }
        }

        if (filteredOrders.isEmpty()) {
            showEmptyState();
            return;
        }

        // Calculate statistics
        double totalSales = 0;
        int totalTransactions = filteredOrders.size();
        int totalProductsSold = 0;

        // Get order IDs
        List<Integer> orderIds = new ArrayList<>();
        for (OrderResponse order : filteredOrders) {
            orderIds.add(order.id);
            totalSales += order.total_amount;
        }

        // Calculate product sales
        Map<Integer, ProductSales> productSalesMap = new HashMap<>();

        for (OrderItemRequest item : allOrderItems) {
            if (orderIds.contains(item.order_id)) {
                totalProductsSold += item.quantity;

                // Aggregate by product
                int productId = item.product_id;
                if (productSalesMap.containsKey(productId)) {
                    ProductSales existing = productSalesMap.get(productId);
                    existing.quantity_sold += item.quantity;
                    existing.total_revenue += item.subtotal;
                } else {
                    String productName = item.products != null ? item.products.name : "Product #" + productId;
                    String productImage = item.products != null ? item.products.image : null;
                    productSalesMap.put(productId, new ProductSales(
                            productId,
                            productName,
                            productImage,
                            item.quantity,
                            item.subtotal
                    ));
                }
            }
        }

        // Update UI
        tvTotalSales.setText("Rp. " + String.format("%,d", (long) totalSales));
        tvTotalTransactions.setText(String.valueOf(totalTransactions));
        tvTotalProductsSold.setText(totalProductsSold + " items");

        // Sort products by quantity sold
        List<ProductSales> productSalesList = new ArrayList<>(productSalesMap.values());
        Collections.sort(productSalesList, new Comparator<ProductSales>() {
            @Override
            public int compare(ProductSales p1, ProductSales p2) {
                return Integer.compare(p2.quantity_sold, p1.quantity_sold);
            }
        });

        // Show best selling products
        if (productSalesList.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            rvBestSelling.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            rvBestSelling.setVisibility(View.VISIBLE);
            bestSellingAdapter = new BestSellingAdapter(this, productSalesList);
            rvBestSelling.setAdapter(bestSellingAdapter);
        }
    }
}
