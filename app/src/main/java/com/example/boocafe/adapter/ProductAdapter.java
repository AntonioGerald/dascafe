package com.example.boocafe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.boocafe.R;
import com.example.boocafe.model.CartItem;
import com.example.boocafe.model.Product;
import com.example.boocafe.utils.CartManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText(product.name);
        holder.tvDesc.setText(product.description);
        holder.tvPrice.setText("Rp. " + product.price);
        holder.tvStock.setText("Stock: " + product.stock_quantity);
        
        if (product.image != null && !product.image.isEmpty()) {
            String fullUrl;
            if (product.image.startsWith("http")) {
                // Kalau image sudah full URL
                fullUrl = product.image;
            } else {
                // Kalau image hanya path (misal "product_images/xxx.jpg")
                fullUrl = "https://bnfspphvunzpxlwrkelz.supabase.co/storage/v1/object/public/products/" + product.image;
            }
            Glide.with(context).load(fullUrl).into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.new_logo);
        }

        // Get current quantity in cart
        int currentQty = getProductQuantityInCart(product.id);
        holder.tvQuantity.setText(String.valueOf(currentQty));
        
        // Disable add button if out of stock
        boolean outOfStock = product.stock_quantity <= 0;
        boolean maxReached = currentQty >= product.stock_quantity;
        
        holder.btnPlus.setEnabled(!outOfStock && !maxReached);
        if (outOfStock) {
            holder.tvStock.setTextColor(0xFFF44336); // Red for out of stock
        } else if (product.stock_quantity < 10) {
            holder.tvStock.setTextColor(0xFFFFC107); // Yellow for low stock
        } else {
            holder.tvStock.setTextColor(0xFFCCCCCC); // Gray for normal stock
        }

        // Plus button - add to cart
        holder.btnPlus.setOnClickListener(v -> {
            int qty = getProductQuantityInCart(product.id);
            if (qty >= product.stock_quantity) {
                Toast.makeText(context, "Maximum stock reached", Toast.LENGTH_SHORT).show();
                return;
            }
            CartManager.getInstance().addToCart(product);
            int newQty = getProductQuantityInCart(product.id);
            holder.tvQuantity.setText(String.valueOf(newQty));
            
            // Update button state
            holder.btnPlus.setEnabled(newQty < product.stock_quantity);
            
            Toast.makeText(context, "Added", Toast.LENGTH_SHORT).show();
        });

        // Minus button - remove from cart
        holder.btnMinus.setOnClickListener(v -> {
            int qty = getProductQuantityInCart(product.id);
            if (qty > 0) {
                removeOneFromCart(product.id);
                int newQty = getProductQuantityInCart(product.id);
                holder.tvQuantity.setText(String.valueOf(newQty));
                Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getProductQuantityInCart(int productId) {
        List<CartItem> cart = CartManager.getInstance().getCart();
        for (CartItem item : cart) {
            if (item.product.id == productId) {
                return item.quantity;
            }
        }
        return 0;
    }

    private void removeOneFromCart(int productId) {
        List<CartItem> cart = CartManager.getInstance().getCart();
        for (int i = 0; i < cart.size(); i++) {
            CartItem item = cart.get(i);
            if (item.product.id == productId) {
                if (item.quantity > 1) {
                    item.quantity--;
                } else {
                    cart.remove(i);
                }
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPrice, tvQuantity, tvStock;
        ImageView imgProduct;
        Button btnPlus, btnMinus;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvDesc = itemView.findViewById(R.id.tvProductDesc);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvStock = itemView.findViewById(R.id.tvStock);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }
}
