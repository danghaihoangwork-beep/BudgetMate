package com.example.savemoneytime.Intro.IntroViewPager;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savemoneytime.R;
import java.util.List;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.SlideViewHolder> {

    private final List<OnBoardingItem> items;

    public OnboardingAdapter(List<OnBoardingItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding_slide, parent, false);
        return new SlideViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        private final View      slideRoot;
        private final ImageView ivIllustration;
        private final TextView  tvAccentLabel;
        private final TextView  tvTitle;
        private final TextView  tvDescription;

        SlideViewHolder(@NonNull View v) {
            super(v);
            slideRoot      = v.findViewById(R.id.slide_root);
            ivIllustration = v.findViewById(R.id.iv_illustration);
            tvAccentLabel  = v.findViewById(R.id.tv_accent_label);
            tvTitle        = v.findViewById(R.id.tv_title);
            tvDescription  = v.findViewById(R.id.tv_description);
        }

        void bind(OnBoardingItem item) {
            GradientDrawable gradient = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{ item.getBgColorStart(), item.getBgColorEnd() }
            );
            slideRoot.setBackground(gradient);

            ivIllustration.setImageResource(item.getImageRes());

            tvAccentLabel.setText(item.getAccentLabel());
            tvAccentLabel.setTextColor(Color.parseColor("#D4AF37"));

            tvTitle.setText(item.getTitle());
            tvTitle.setTextColor(Color.parseColor("#FFFFFF"));

            tvDescription.setText(item.getDescription());
            tvDescription.setTextColor(Color.parseColor("#E0E6ED"));
        }
    }
}