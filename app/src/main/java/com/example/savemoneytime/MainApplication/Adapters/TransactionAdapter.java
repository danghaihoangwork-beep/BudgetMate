package com.example.savemoneytime.MainApplication.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savemoneytime.MainApplication.DisplayItem;
import com.example.savemoneytime.R;
import com.example.savemoneytime.model.TransactionItem;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionItem item, int position);
    }

    private final List<DisplayItem> items;
    private final OnTransactionClickListener clickListener;

    // Giao diện Premium Claude ưa chuộng hiển thị giờ phút hơn (12:42 PM) vì ngày đã có ở Header
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

    public TransactionAdapter(List<DisplayItem> items, OnTransactionClickListener clickListener) {
        this.items = items;
        this.clickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == DisplayItem.TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_month_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayItem displayItem = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvMonthHeader.setText(displayItem.getHeaderTitle());
        } else if (holder instanceof TransactionViewHolder) {
            TransactionViewHolder tHolder = (TransactionViewHolder) holder;
            TransactionItem item = displayItem.getTransaction();

            tHolder.tvTitle.setText(item.getTitle());
            tHolder.tvCategory.setText(item.getCategoryName());
            tHolder.tvDate.setText(timeFormat.format(new Date(item.getDate())));

            // 🔥 Tự động gán Emoji xịn sò dựa theo tên danh mục
            tHolder.tvIcon.setText(getEmojiForCategory(item.getCategoryName()));

            String amountStr = String.format(Locale.US, "%,d", item.getAmount());
            if (item.isExpense()) {
                tHolder.tvAmount.setText("− $" + amountStr);
                tHolder.tvAmount.setTextColor(Color.parseColor("#F87171")); // Đỏ Expense
            } else {
                tHolder.tvAmount.setText("+ $" + amountStr);
                tHolder.tvAmount.setTextColor(Color.parseColor("#34D399")); // Xanh Income
            }

            if (item.getNote() != null && !item.getNote().isEmpty()) {
                tHolder.tvNote.setVisibility(View.VISIBLE);
                tHolder.tvNote.setText("“" + item.getNote() + "”");
            } else {
                tHolder.tvNote.setVisibility(View.GONE);
            }

            tHolder.itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onTransactionClick(item, holder.getAdapterPosition());
                }
            });
        }
    }

    // Hàm nhận diện từ khóa để tự động nhả Emoji chuẩn chỉ
    private String getEmojiForCategory(String categoryName) {
        if (categoryName == null) return "🧾";
        String lower = categoryName.toLowerCase();
        if (lower.contains("food") || lower.contains("eat") || lower.contains("lunch")) return "🍜";
        if (lower.contains("transport") || lower.contains("taxi") || lower.contains("uber")) return "🚗";
        if (lower.contains("health") || lower.contains("medical") || lower.contains("pill")) return "💊";
        if (lower.contains("shop") || lower.contains("buy")) return "🛍️";
        if (lower.contains("house") || lower.contains("rent") || lower.contains("home")) return "🏠";
        if (lower.contains("salary") || lower.contains("income") || lower.contains("wage")) return "💰";
        return "🧾";
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonthHeader;
        HeaderViewHolder(View v) {
            super(v);
            tvMonthHeader = v.findViewById(R.id.tv_date_header);
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        // 🔥 FIXED: Đã xóa cái tvTypeIndicator gây lỗi, thêm biến tvIcon cho khung Emoji
        TextView tvIcon;
        TextView tvTitle;
        TextView tvCategory;
        TextView tvDate;
        TextView tvAmount;
        TextView tvNote;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon          = itemView.findViewById(R.id.tv_trans_icon);
            tvTitle         = itemView.findViewById(R.id.tv_trans_title);
            tvCategory      = itemView.findViewById(R.id.tv_trans_category);
            tvDate          = itemView.findViewById(R.id.tv_trans_date);
            tvAmount        = itemView.findViewById(R.id.tv_trans_amount);
            tvNote          = itemView.findViewById(R.id.tv_trans_note);
        }
    }
}