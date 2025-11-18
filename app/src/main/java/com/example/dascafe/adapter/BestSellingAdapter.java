package com.example.dascafe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.dascafe.R;
import com.example.dascafe.model.ProductSales;

import java.util.List;

public class BestSellingAdapter extends RecyclerView.Adapter<BestSellingAdapter.ViewHolder> {

    private Context context;
    private List<ProductSales> productSalesList;

    public BestSellingAdapter(Context context, List<ProductSales> productSalesList) {
        this.context = context;
        this.productSalesList = productSalesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_best_selling, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductSales sales = productSalesList.get(position);

        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvProductName.setText(sales.product_name);
        holder.tvQuantitySold.setText(sales.quantity_sold + " sold");
        holder.tvRevenue.setText("Rp. " + String.format("%,d", (long) sales.total_revenue));

        // Load product image
        if (sales.product_image != null && !sales.product_image.isEmpty()) {
            String fullUrl;
            if (sales.product_image.startsWith("http")) {
                fullUrl = sales.product_image;
            } else {
                fullUrl = "https://bnfspphvunzpxlwrkelz.supabase.co/storage/v1/object/public/products/" + sales.product_image;
            }
            Glide.with(context).load(fullUrl).into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.logo_das_cafe);
        }

        // Color rank badge based on position
        int rankColor;
        if (position == 0) {
            rankColor = 0xFFFFD700; // Gold
        } else if (position == 1) {
            rankColor = 0xFFC0C0C0; // Silver
        } else if (position == 2) {
            rankColor = 0xFFCD7F32; // Bronze
        } else {
            rankColor = 0xFFFF6F00; // Orange
        }
        holder.tvRank.setBackgroundColor(rankColor);
    }

    @Override
    public int getItemCount() {
        return productSalesList != null ? productSalesList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvProductName, tvQuantitySold, tvRevenue;
        ImageView imgProduct;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantitySold = itemView.findViewById(R.id.tvQuantitySold);
            tvRevenue = itemView.findViewById(R.id.tvRevenue);
            imgProduct = itemView.findViewById(R.id.imgProduct);
        }
    }
}
