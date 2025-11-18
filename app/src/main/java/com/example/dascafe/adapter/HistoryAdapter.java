package com.example.dascafe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dascafe.R;
import com.example.dascafe.model.OrderResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<OrderResponse> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(OrderResponse order);
    }

    public HistoryAdapter(Context context, List<OrderResponse> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        OrderResponse order = orderList.get(position);

        holder.tvOrderId.setText("Order #" + order.id);
        holder.tvTotal.setText("Rp. " + (long) order.total_amount);
        holder.tvPaymentMethod.setText(order.payment_method != null ? order.payment_method : "Cash");
        
        // Format status
        String status = order.status != null ? order.status : "pending";
        holder.tvStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
        
        // Set status color
        int statusColor;
        switch (status.toLowerCase()) {
            case "completed":
                statusColor = 0xFF4CAF50; // Green
                break;
            case "cancelled":
                statusColor = 0xFFF44336; // Red
                break;
            default:
                statusColor = 0xFFFFC107; // Yellow
                break;
        }
        holder.tvStatus.setTextColor(statusColor);

        // Format date
        if (order.created_at != null && !order.created_at.isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(order.created_at);
                if (date != null) {
                    holder.tvDate.setText(outputFormat.format(date));
                } else {
                    holder.tvDate.setText("Recent");
                }
            } catch (Exception e) {
                holder.tvDate.setText("Recent");
            }
        } else {
            holder.tvDate.setText("Recent");
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvTotal, tvPaymentMethod, tvStatus, tvDate;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvPaymentMethod = itemView.findViewById(R.id.tvPaymentMethod);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
