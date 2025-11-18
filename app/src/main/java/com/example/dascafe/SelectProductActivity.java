package com.example.dascafe;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dascafe.adapter.ProductAdapter;
import com.example.dascafe.model.Product;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectProductActivity extends AppCompatActivity {
    RecyclerView rvProducts;
    ImageButton btnBack;
    SearchView searchView;
    ApiService apiService;
    ProductAdapter productAdapter;
    List<Product> allProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_product);

        rvProducts = findViewById(R.id.rvProducts);
        btnBack = findViewById(R.id.btnBack);
        searchView = findViewById(R.id.searchView);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        apiService = ApiClient.getClient().create(ApiService.class);

        btnBack.setOnClickListener(v -> finish());

        setupSearch();
        loadProducts();
    }

    private void loadProducts() {
        apiService.getProducts().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allProducts = response.body();
                    productAdapter = new ProductAdapter(SelectProductActivity.this, allProducts);
                    rvProducts.setAdapter(productAdapter);
                } else {
                    Log.e("API_RESPONSE", "Code: " + response.code() + " | Error: " + response.message());
                    Toast.makeText(SelectProductActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(SelectProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return true;
            }
        });
    }

    private void filterProducts(String query) {
        if (allProducts == null) return;

        if (query == null || query.trim().isEmpty()) {
            productAdapter = new ProductAdapter(this, allProducts);
            rvProducts.setAdapter(productAdapter);
        } else {
            List<Product> filteredList = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            
            for (Product product : allProducts) {
                if (product.name.toLowerCase().contains(lowerQuery) ||
                    (product.description != null && product.description.toLowerCase().contains(lowerQuery))) {
                    filteredList.add(product);
                }
            }
            
            productAdapter = new ProductAdapter(this, filteredList);
            rvProducts.setAdapter(productAdapter);
        }
    }
}
