package com.example.boocafe;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface StorageService {
    @Headers({
            "apikey: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow",
            "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuZnNwcGh2dW56cHhsd3JrZWx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTYyMTg0MTQsImV4cCI6MjA3MTc5NDQxNH0.GEfgK-ksnIFLq02OEeydyozG2l8FtqAGy7kZ8BE1zow"
    })
    @Multipart
    @POST("storage/v1/object/products/product_images/{filename}")
    Call<ResponseBody> uploadImage(
            @Path("filename") String filename,
            @Part MultipartBody.Part file
    );
}
