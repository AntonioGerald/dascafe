package com.example.boocafe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boocafe.R;
import com.example.boocafe.model.OrderItemRequest;

import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {

    private Context context;
    private List<OrderItemRequest> itemList;

    public OrderDetailAdapter(Context context, List<OrderItemRequest> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new OrderDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        OrderItemRequest item = itemList.get(position);

        // Show product name from joined products table
        if (item.products != null && item.products.name != null) {
            holder.tvProductName.setText(item.products.name);
        } else {
            holder.tvProductName.setText("Product #" + item.product_id);
        }
        
        holder.tvQuantity.setText(String.valueOf(item.quantity));
        holder.tvPrice.setText("Rp. " + String.format("%,d", (long) item.price));
        holder.tvSubtotal.setText("Rp. " + String.format("%,d", (long) item.subtotal));
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public static class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvQuantity, tvPrice, tvSubtotal;

        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
        }
    }
}
