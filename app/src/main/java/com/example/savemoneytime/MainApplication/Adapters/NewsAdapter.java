package com.example.savemoneytime.MainApplication.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.savemoneytime.R;
import com.example.savemoneytime.network.NewsArticle;
import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_FEATURED = 0;
    private static final int TYPE_LIST     = 1;

    public interface OnArticleClick { void onArticleClick(String url, String title); }

    private final List<NewsArticle> articles = new ArrayList<>();
    private final OnArticleClick    clickCallback;

    public NewsAdapter(OnArticleClick clickCallback) {
        this.clickCallback = clickCallback;
    }

    @Override
    public int getItemViewType(int position) {
        return (articles.size() > 0 && position == 0) ? TYPE_FEATURED : TYPE_LIST;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_FEATURED) {
            return new FeaturedVH(inf.inflate(R.layout.item_news_featured, parent, false));
        } else {
            return new ListVH(inf.inflate(R.layout.item_news_list, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position >= articles.size()) return;
        try {
            NewsArticle a = articles.get(position);
            if (a == null) return;
            String url = a.getUrl() != null ? a.getUrl() : "";
            String title = a.getTitle() != null ? a.getTitle() : "";
            String date = a.getPublishedAt() != null ? a.getPublishedAt() : "";
            String dateS = date.length() >= 10 ? date.substring(0, 10) : date;

            if (holder instanceof FeaturedVH) {
                FeaturedVH fh = (FeaturedVH) holder;
                fh.tvTitle.setText(title);
                fh.tvSource.setText(a.getSourceName() != null ? a.getSourceName().toUpperCase() : "");
                // Đã xóa dòng set text cho tvDesc để khớp với giao diện gọn gàng mới
                fh.tvDate.setText(dateS);
                loadImg(fh.ivImage, a.getImageUrl());
                fh.itemView.setOnClickListener(v -> clickCallback.onArticleClick(url, title));
            } else if (holder instanceof ListVH) {
                ListVH lh = (ListVH) holder;
                lh.tvTitle.setText(title);
                lh.tvSource.setText(a.getSourceName() != null ? a.getSourceName().toUpperCase() : "");
                lh.tvDate.setText(dateS);
                loadImg(lh.ivThumb, a.getImageUrl());
                lh.itemView.setOnClickListener(v -> clickCallback.onArticleClick(url, title));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override public int getItemCount() { return articles.size(); }

    public void updateArticles(List<NewsArticle> newList) {
        if (newList == null) return;
        this.articles.clear();
        this.articles.addAll(newList);
        notifyDataSetChanged();
    }

    private void loadImg(ImageView iv, String imgUrl) {
        if (iv == null) return;
        try {
            if (imgUrl != null && !imgUrl.isEmpty()) {
                Glide.with(iv.getContext()).load(imgUrl).placeholder(R.drawable.ic_nav_news)
                        .transition(DrawableTransitionOptions.withCrossFade(250)).centerCrop().into(iv);
            } else { iv.setImageResource(R.drawable.ic_nav_news); }
        } catch (Exception e) { iv.setImageResource(R.drawable.ic_nav_news); }
    }

    // 🔥 ĐÃ FIX CHUẨN ID CỦA BÀI BÁO NỔI BẬT (FEATURED)
    static class FeaturedVH extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvSource, tvDate, tvTitle;

        FeaturedVH(View v) {
            super(v);
            ivImage = v.findViewById(R.id.iv_featured_img);
            tvSource = v.findViewById(R.id.tv_featured_source);
            tvDate = v.findViewById(R.id.tv_featured_time);
            tvTitle = v.findViewById(R.id.tv_featured_title);
        }
    }

    // 🔥 ĐÃ FIX CHUẨN ID CỦA BÀI BÁO DANH SÁCH (LIST)
    static class ListVH extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvSource, tvDate, tvTitle;

        ListVH(View v) {
            super(v);
            ivThumb = v.findViewById(R.id.iv_news_thumbnail);
            tvSource = v.findViewById(R.id.tv_news_source);
            tvDate = v.findViewById(R.id.tv_news_time);
            tvTitle = v.findViewById(R.id.tv_news_title);
        }
    }
}