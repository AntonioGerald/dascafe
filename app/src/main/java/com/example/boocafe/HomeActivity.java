package com.example.boocafe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HomeActivity extends AppCompatActivity {
    TextView tvWelcome;
    CardView btnOrder, btnProducts, btnHistory, btnReports;
    Button btnLogout;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        tvWelcome = findViewById(R.id.tvWelcome);
        btnOrder = findViewById(R.id.btnOrder);
        btnProducts = findViewById(R.id.btnProducts);
        btnHistory = findViewById(R.id.btnHistory);
        btnReports = findViewById(R.id.btnReports);
        btnLogout = findViewById(R.id.btnLogout);

        String username = sessionManager.getUsername();

        if (username != null && !username.isEmpty()) {
            tvWelcome.setText("Welcome, " + username);
        } else {
            tvWelcome.setText("Welcome to Das Cafe!");
        }

        btnProducts.setOnClickListener(v -> startActivity(new Intent(this, ProductsActivity.class)));
        btnOrder.setOnClickListener(v -> startActivity(new Intent(this, OrderActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        btnReports.setOnClickListener(v -> startActivity(new Intent(this, ReportsActivity.class)));
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    sessionManager.logoutUser();
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }
        String username = sessionManager.getUsername();
        if (username != null && !username.isEmpty()) {
            tvWelcome.setText("Welcome, " + username);
        }
    }
}