package com.example.savemoneytime.MainApplication.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savemoneytime.R;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface OnCategoryClick {
        void onClick(String categoryName);
    }

    private final List<String>    categories;
    private final OnCategoryClick listener;
    private int selectedPosition = -1;

    public CategoryAdapter(List<String> categories, OnCategoryClick listener) {
        this.categories = categories;
        this.listener   = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String cat = categories.get(position);
        holder.tvCategory.setText(cat);

        if (position == selectedPosition) {
            holder.tvCategory.setBackgroundColor(Color.parseColor("#D4AF37"));
            holder.tvCategory.setTextColor(Color.parseColor("#0A1128"));
        } else {
            holder.tvCategory.setBackgroundColor(Color.parseColor("#1A233D"));
            holder.tvCategory.setTextColor(Color.parseColor("#E0E6ED"));
        }

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            listener.onClick(cat);
        });
    }

    @Override
    public int getItemCount() { return categories.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory;
        ViewHolder(@NonNull View v) {
            super(v);
            tvCategory = v.findViewById(R.id.tv_category_name);
        }
    }
}