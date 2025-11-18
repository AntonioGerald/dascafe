package com.example.dascafe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dascafe.adapter.ProductManageAdapter;
import com.example.dascafe.model.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductsActivity extends AppCompatActivity {
    RecyclerView rvProducts;
    ImageButton btnBack;
    SearchView searchView;
    FloatingActionButton fabAddProduct;
    ApiService apiService;
    ProductManageAdapter productAdapter;
    List<Product> allProducts;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        rvProducts = findViewById(R.id.rvProducts);
        btnBack = findViewById(R.id.btnBack);
        searchView = findViewById(R.id.searchView);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        apiService = ApiClient.getClient().create(ApiService.class);
        sessionManager = new SessionManager(this);

        // Hide FAB if not admin
        if (!sessionManager.isAdmin()) {
            fabAddProduct.setVisibility(android.view.View.GONE);
        }

        btnBack.setOnClickListener(v -> finish());
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(ProductsActivity.this, AddEditProductActivity.class);
            startActivityForResult(intent, 100);
        });

        setupSearch();
        loadProducts();
    }

    private void loadProducts() {
        apiService.getProducts().enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allProducts = response.body();
                    setupAdapter(allProducts);
                } else {
                    Log.e("API_RESPONSE", "Code: " + response.code() + " | Error: " + response.message());
                    Toast.makeText(ProductsActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }



            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(ProductsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void setupAdapter(List<Product> products) {
        productAdapter = new ProductManageAdapter(this, products, new ProductManageAdapter.OnProductActionListener() {
            @Override
            public void onEditClick(Product product) {
                if (!sessionManager.isAdmin()) {
                    Toast.makeText(ProductsActivity.this, "Only admin can edit products", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(ProductsActivity.this, AddEditProductActivity.class);
                intent.putExtra("product_id", product.id);
                intent.putExtra("product_name", product.name);
                intent.putExtra("product_description", product.description);
                intent.putExtra("product_price", product.price);
                intent.putExtra("product_image", product.image);
                startActivityForResult(intent, 100);
            }

            @Override
            public void onDeleteClick(Product product) {
                if (!sessionManager.isAdmin()) {
                    Toast.makeText(ProductsActivity.this, "Only admin can delete products", Toast.LENGTH_SHORT).show();
                    return;
                }
                showDeleteConfirmation(product);
            }
        });
        rvProducts.setAdapter(productAdapter);
    }

    private void showDeleteConfirmation(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.name + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProduct(Product product) {
        apiService.deleteProduct("eq." + product.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProductsActivity.this, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                    loadProducts(); // Reload the list
                } else {
                    Log.e("DELETE_PRODUCT", "Code: " + response.code() + " | Message: " + response.message());
                    Toast.makeText(ProductsActivity.this, "Failed to delete product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("DELETE_PRODUCT", "Error: " + t.getMessage(), t);
                Toast.makeText(ProductsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProducts(String query) {
        if (allProducts == null) return;

        if (query == null || query.trim().isEmpty()) {
            setupAdapter(allProducts);
        } else {
            List<Product> filteredList = new ArrayList<>();
            String lowerQuery = query.toLowerCase();
            
            for (Product product : allProducts) {
                if (product.name.toLowerCase().contains(lowerQuery) ||
                    (product.description != null && product.description.toLowerCase().contains(lowerQuery))) {
                    filteredList.add(product);
                }
            }
            
            setupAdapter(filteredList);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            // Reload products after add/edit
            loadProducts();
        }
    }
}
