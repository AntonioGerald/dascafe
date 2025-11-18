package com.example.dascafe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dascafe.R;
import com.example.dascafe.model.Product;

import java.util.List;

public class ProductManageAdapter extends RecyclerView.Adapter<ProductManageAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnProductActionListener listener;
    private boolean isAdmin;

    public interface OnProductActionListener {
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    public ProductManageAdapter(Context context, List<Product> productList, OnProductActionListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
        // Check if user is admin
        com.example.dascafe.SessionManager sessionManager = new com.example.dascafe.SessionManager(context);
        this.isAdmin = sessionManager.isAdmin();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_manage, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText(product.name);
        holder.tvDesc.setText(product.description);
        holder.tvPrice.setText("Rp. " + product.price);
        holder.tvStock.setText("Stock: " + product.stock_quantity);
        
        // Color-code stock levels
        if (product.stock_quantity <= 0) {
            holder.tvStock.setTextColor(0xFFF44336); // Red for out of stock
        } else if (product.stock_quantity < 10) {
            holder.tvStock.setTextColor(0xFFFFC107); // Yellow for low stock
        } else {
            holder.tvStock.setTextColor(0xFFCCCCCC); // Gray for normal stock
        }

        if (product.image != null && !product.image.isEmpty()) {
            String fullUrl;
            if (product.image.startsWith("http")) {
                fullUrl = product.image;
            } else {
                fullUrl = "https://bnfspphvunzpxlwrkelz.supabase.co/storage/v1/object/public/products/" + product.image;
            }
            Glide.with(context).load(fullUrl).into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.pumpkin);
        }

        // Hide buttons if not admin
        if (!isAdmin) {
            holder.btnEdit.setVisibility(android.view.View.GONE);
            holder.btnDelete.setVisibility(android.view.View.GONE);
        } else {
            holder.btnEdit.setVisibility(android.view.View.VISIBLE);
            holder.btnDelete.setVisibility(android.view.View.VISIBLE);
            
            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(product);
                }
            });

            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(product);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPrice, tvStock;
        ImageView imgProduct;
        Button btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvDesc = itemView.findViewById(R.id.tvProductDesc);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvStock = itemView.findViewById(R.id.tvStock);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
