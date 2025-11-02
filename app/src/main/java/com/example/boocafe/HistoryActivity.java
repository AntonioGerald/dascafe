package com.example.boocafe;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.boocafe.adapter.HistoryAdapter;
import com.example.boocafe.model.OrderResponse;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {
    RecyclerView rvHistory;
    ImageButton btnBack;
    View emptyLayout, layoutDateRange;
    ChipGroup chipGroupTimeframe;
    Chip chipToday, chipWeek, chipMonth, chipAll, chipCustom;
    Button btnStartDate, btnEndDate;
    ApiService apiService;
    HistoryAdapter historyAdapter;
    SessionManager sessionManager;
    List<OrderResponse> allOrders;
    Calendar startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rvHistory = findViewById(R.id.rvHistory);
        btnBack = findViewById(R.id.btnBack);
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
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        
        // Initialize date range to today
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();

        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(this);

        btnBack.setOnClickListener(v -> finish());
        setupTimeframeFilter();
        setupDatePickers();

        loadHistory();
    }

    private void loadHistory() {
        int userId = sessionManager.getUserId();
        if (userId <= 0) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService.getOrdersByUser("eq." + userId).enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(Call<List<OrderResponse>> call, Response<List<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allOrders = response.body();
                    filterOrdersByTimeframe("all");
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("HISTORY", "Error reading error body", e);
                    }
                    Log.e("HISTORY", "Code: " + response.code() + " | Error: " + response.message() + " | Body: " + errorBody);
                    Toast.makeText(HistoryActivity.this, "Failed to load history: " + response.code(), Toast.LENGTH_SHORT).show();
                    emptyLayout.setVisibility(View.VISIBLE);
                    rvHistory.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<OrderResponse>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(HistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                emptyLayout.setVisibility(View.VISIBLE);
                rvHistory.setVisibility(View.GONE);
            }
        });
    }

    private void setupTimeframeFilter() {
        chipGroupTimeframe.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipToday) {
                layoutDateRange.setVisibility(View.GONE);
                filterOrdersByTimeframe("today");
            } else if (checkedId == R.id.chipWeek) {
                layoutDateRange.setVisibility(View.GONE);
                filterOrdersByTimeframe("week");
            } else if (checkedId == R.id.chipMonth) {
                layoutDateRange.setVisibility(View.GONE);
                filterOrdersByTimeframe("month");
            } else if (checkedId == R.id.chipAll) {
                layoutDateRange.setVisibility(View.GONE);
                filterOrdersByTimeframe("all");
            } else if (checkedId == R.id.chipCustom) {
                layoutDateRange.setVisibility(View.VISIBLE);
                filterOrdersByCustomRange();
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
                    if (chipCustom.isChecked()) filterOrdersByCustomRange();
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
                    if (chipCustom.isChecked()) filterOrdersByCustomRange();
                },
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH)
        ).show());
        
        btnStartDate.setText(dateFormat.format(startDate.getTime()));
        btnEndDate.setText(dateFormat.format(endDate.getTime()));
    }

    private void filterOrdersByTimeframe(String timeframe) {
        if (allOrders == null) return;

        List<OrderResponse> filteredOrders = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

        for (OrderResponse order : allOrders) {
            if (order.created_at == null || order.created_at.isEmpty()) {
                // If no date, include in "all" only
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
                // Include in "all" if date parsing fails
                if (timeframe.equals("all")) {
                    filteredOrders.add(order);
                }
            }
        }

        // Update UI
        if (filteredOrders.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            historyAdapter = new HistoryAdapter(HistoryActivity.this, filteredOrders, order -> {
                Intent intent = new Intent(HistoryActivity.this, OrderDetailActivity.class);
                intent.putExtra("order_id", order.id);
                intent.putExtra("total_amount", order.total_amount);
                intent.putExtra("payment_method", order.payment_method);
                intent.putExtra("status", order.status);
                startActivity(intent);
            });
            rvHistory.setAdapter(historyAdapter);
        }
    }

    private void filterOrdersByCustomRange() {
        if (allOrders == null) return;

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
                Log.e("HISTORY", "Date parse error: " + e.getMessage());
            }
        }

        // Update UI
        if (filteredOrders.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            historyAdapter = new HistoryAdapter(HistoryActivity.this, filteredOrders, order -> {
                Intent intent = new Intent(HistoryActivity.this, OrderDetailActivity.class);
                intent.putExtra("order_id", order.id);
                intent.putExtra("total_amount", order.total_amount);
                intent.putExtra("payment_method", order.payment_method);
                intent.putExtra("status", order.status);
                startActivity(intent);
            });
            rvHistory.setAdapter(historyAdapter);
        }
    }
}
