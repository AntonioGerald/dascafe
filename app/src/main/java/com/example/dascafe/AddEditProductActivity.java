package com.example.dascafe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.dascafe.model.Product;
import com.example.dascafe.model.ProductUpdateRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AddEditProductActivity extends AppCompatActivity {
    EditText etProductName, etDescription, etPrice, etImageUrl;
    Button btnSave, btnSelectImage;
    ImageButton btnBack;
    TextView tvTitle;
    ImageView ivProductImage;
    ApiService apiService;
    StorageService storageService;

    boolean isEditMode = false;
    int productId = -1;
    Uri selectedImageUri = null;
    String uploadedImagePath = null;
    
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        // Initialize views
        etProductName = findViewById(R.id.etProductName);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etImageUrl = findViewById(R.id.etImageUrl);
        btnSave = findViewById(R.id.btnSave);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        ivProductImage = findViewById(R.id.ivProductImage);

        apiService = ApiClient.getClient().create(ApiService.class);
        
        // Create storage service with storage base URL
        Retrofit storageRetrofit = new Retrofit.Builder()
                .baseUrl("https://bnfspphvunzpxlwrkelz.supabase.co/")
                .build();
        storageService = storageRetrofit.create(StorageService.class);

        setupImagePicker();
        btnBack.setOnClickListener(v -> finish());
        btnSelectImage.setOnClickListener(v -> checkPermissionAndPickImage());

        // Check if editing existing product
        if (getIntent().hasExtra("product_id")) {
            isEditMode = true;
            productId = getIntent().getIntExtra("product_id", -1);
            String name = getIntent().getStringExtra("product_name");
            String description = getIntent().getStringExtra("product_description");
            double price = getIntent().getDoubleExtra("product_price", 0);
            String image = getIntent().getStringExtra("product_image");

            tvTitle.setText("Edit Product");
            btnSave.setText("Update Product");

            etProductName.setText(name);
            etDescription.setText(description);
            etPrice.setText(String.valueOf(price));
            if (image != null) {
                etImageUrl.setText(image);
                uploadedImagePath = image;
                loadImagePreview(image);
            }
        }

        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void setupImagePicker() {
        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            ivProductImage.setImageURI(selectedImageUri);
                            uploadImageToStorage();
                        }
                    }
                }
        );

        // Permission launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(this, "Permission denied to read images", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void checkPermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadImageToStorage() {
        if (selectedImageUri == null) return;

        try {
            // Create temp file from URI
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            File tempFile = new File(getCacheDir(), "temp_image_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            // Create multipart body
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);

            String filename = "product_" + System.currentTimeMillis() + ".jpg";
            
            btnSelectImage.setEnabled(false);
            btnSelectImage.setText("Uploading...");

            storageService.uploadImage(filename, body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    btnSelectImage.setEnabled(true);
                    btnSelectImage.setText("Select Image from Gallery");
                    
                    if (response.isSuccessful()) {
                        uploadedImagePath = "product_images/" + filename;
                        Toast.makeText(AddEditProductActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        Log.d("IMAGE_UPLOAD", "Success: " + uploadedImagePath);
                    } else {
                        String errorBody = "";
                        try {
                            if (response.errorBody() != null) {
                                errorBody = response.errorBody().string();
                            }
                        } catch (Exception e) {
                            Log.e("IMAGE_UPLOAD", "Error reading error body", e);
                        }
                        Log.e("IMAGE_UPLOAD", "Failed: " + response.code() + " - " + response.message() + " | Body: " + errorBody);
                        Toast.makeText(AddEditProductActivity.this, "Upload failed. Using manual URL instead.", Toast.LENGTH_LONG).show();
                    }
                    tempFile.delete();
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    btnSelectImage.setEnabled(true);
                    btnSelectImage.setText("Select Image from Gallery");
                    Log.e("IMAGE_UPLOAD", "Error: " + t.getMessage(), t);
                    Toast.makeText(AddEditProductActivity.this, "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    tempFile.delete();
                }
            });

        } catch (Exception e) {
            Log.e("IMAGE_UPLOAD", "Exception: " + e.getMessage(), e);
            Toast.makeText(this, "Error preparing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImagePreview(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            String fullUrl;
            if (imagePath.startsWith("http")) {
                fullUrl = imagePath;
            } else {
                fullUrl = "https://bnfspphvunzpxlwrkelz.supabase.co/storage/v1/object/public/products/" + imagePath;
            }
            Glide.with(this).load(fullUrl).into(ivProductImage);
        }
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            etProductName.setError("Product name is required");
            etProductName.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            etPrice.setError("Price is required");
            etPrice.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price format");
            etPrice.requestFocus();
            return;
        }

        // Create product object (without id for new products)
        Product product = new Product();
        product.name = name;
        product.description = description.isEmpty() ? null : description;
        product.price = price;
        
        // Use uploaded image path if available, otherwise use manual URL
        if (uploadedImagePath != null && !uploadedImagePath.isEmpty()) {
            product.image = uploadedImagePath;
        } else if (!imageUrl.isEmpty()) {
            product.image = imageUrl;
        } else {
            product.image = null;
        }
        
        // Set default values for required fields
        product.stock_quantity = 100; // Default stock
        product.is_active = true; // Active by default

        btnSave.setEnabled(false);

        if (isEditMode) {
            updateProduct(product);
        } else {
            createProduct(product);
        }
    }

    private void createProduct(Product product) {
        Log.d("CREATE_PRODUCT", "Creating product: " + product.name + " | Price: " + product.price + " | Stock: " + product.stock_quantity + " | Image: " + product.image);
        
        apiService.createProduct(product).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Toast.makeText(AddEditProductActivity.this, "Product created successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("CREATE_PRODUCT", "Error reading error body", e);
                    }
                    Log.e("CREATE_PRODUCT", "Code: " + response.code() + " | Message: " + response.message() + " | Body: " + errorBody);
                    Toast.makeText(AddEditProductActivity.this, "Failed: " + errorBody, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                btnSave.setEnabled(true);
                Log.e("CREATE_PRODUCT", "Error: " + t.getMessage(), t);
                Toast.makeText(AddEditProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProduct(Product product) {
        // Create update request without id field
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        updateRequest.name = product.name;
        updateRequest.description = product.description;
        updateRequest.image = product.image;
        updateRequest.price = product.price;
        updateRequest.stock_quantity = product.stock_quantity;
        updateRequest.is_active = product.is_active;
        
        apiService.updateProduct("eq." + productId, updateRequest).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                btnSave.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(AddEditProductActivity.this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("UPDATE_PRODUCT", "Error reading error body", e);
                    }
                    Log.e("UPDATE_PRODUCT", "Code: " + response.code() + " | Message: " + response.message() + " | Body: " + errorBody);
                    Toast.makeText(AddEditProductActivity.this, "Failed to update: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                btnSave.setEnabled(true);
                Log.e("UPDATE_PRODUCT", "Error: " + t.getMessage(), t);
                Toast.makeText(AddEditProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
