package com.example.dascafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dascafe.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    EditText editEmail, editPassword;
    Button btnLogin;
    ProgressBar progressBar;
    ApiService apiService;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            redirectToHome();
            return;
        }

        editEmail = findViewById(R.id.Email);
        editPassword = findViewById(R.id.Password);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        apiService = ApiClient.getClient().create(ApiService.class);

        btnLogin.setOnClickListener(v -> performLogin());

        setupBackPressHandler();
    }


    private void setupPasswordToggle() {

    }

    private void performLogin() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Show loading
        showLoading(true);

        apiService.login("eq." + email).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                showLoading(false); // ✅ Hide loading

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    User user = response.body().get(0);
                    if (user.password.equals(password)) {
                        Toast.makeText(LoginActivity.this, "Login Berhasil", Toast.LENGTH_SHORT).show();

                        // ✅ Gunakan SessionManager untuk menyimpan session
                        sessionManager.createLoginSession(
                                user.id,           // user_id
                                user.name,         // username
                                user.email,        // email
                                user.role != null ? user.role : "user" // role (default: user)
                        );

                        redirectToHome();
                    } else {
                        Toast.makeText(LoginActivity.this, "Password salah", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User tidak ditemukan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                showLoading(false); // ✅ Hide loading
                Toast.makeText(LoginActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Method untuk redirect ke HomeActivity
    private void redirectToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        // ✅ Gunakan proper intent flags
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // ✅ Data username bisa diambil dari SessionManager, tidak perlu putExtra
        // String username = sessionManager.getUsername();
        // intent.putExtra("username", username);

        startActivity(intent);
        finish();
    }

    // ✅ Method untuk show/hide loading
    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        btnLogin.setEnabled(!isLoading);

        // ✅ Ubah text button saat loading
        btnLogin.setText(isLoading ? "Logging in..." : "Login");
    }

    // ✅ Setup back press handler (exit app dari login screen)
    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Exit app jika di login screen
                finishAffinity();
            }
        });
    }
}