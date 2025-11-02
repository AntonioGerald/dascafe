package com.example.boocafe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boocafe.model.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<CartItem> cartList;

    public CartAdapter(Context context, List<CartItem> cartList) {
        this.context = context;
        this.cartList = cartList;
    }

    @Override
    public CartViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);
        holder.tvLine1.setText(item.product.name + " x" + item.quantity);
        holder.tvLine2.setText("Rp. " + item.getSubtotal());
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvLine1, tvLine2;
        CartViewHolder(View itemView) {
            super(itemView);
            tvLine1 = itemView.findViewById(android.R.id.text1);
            tvLine2 = itemView.findViewById(android.R.id.text2);
        }
    }
}

