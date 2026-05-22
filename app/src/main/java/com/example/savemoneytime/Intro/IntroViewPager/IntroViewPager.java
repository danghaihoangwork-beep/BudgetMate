package com.example.savemoneytime.Intro.IntroViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.savemoneytime.MainApplication.MainActivity;
import com.example.savemoneytime.R;
import java.util.ArrayList;
import java.util.List;

public class IntroViewPager extends AppCompatActivity {

    private ViewPager2        viewPager;
    private OnboardingAdapter adapter;
    private LinearLayout      dotsContainer;
    private Button            btnNext;
    private TextView          tvSkip;

    private static final int    TOTAL_SLIDES = 4;
    private static final String PREFS_NAME   = "BudgetMatePrefs";
    private static final String KEY_INTRO    = "introSeen";

    private static final int NAVY_DEEP  = 0xFF0A1128;
    private static final int NAVY_MID   = 0xFF0D1B3E;
    private static final int NAVY_SLIDE = 0xFF111D35;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_intro_viewpager);

        viewPager     = findViewById(R.id.view_pager_intro);
        dotsContainer = findViewById(R.id.dots_container);
        btnNext       = findViewById(R.id.btn_next);
        tvSkip        = findViewById(R.id.tv_skip);

        setupSlides();
        setupDots(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                if (position == TOTAL_SLIDES - 1) {
                    btnNext.setText("Get Started");
                    tvSkip.setVisibility(View.INVISIBLE);
                } else {
                    btnNext.setText("Next");
                    tvSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < TOTAL_SLIDES - 1) {
                viewPager.setCurrentItem(current + 1, true);
            } else {
                finishOnboarding();
            }
        });

        tvSkip.setOnClickListener(v -> finishOnboarding());
    }

    private void setupSlides() {
        List<OnBoardingItem> items = new ArrayList<>();

        items.add(new OnBoardingItem(
                R.drawable.ic_onboard_track,
                "EXPENSES",
                "Track Spending",
                "Log every purchase in seconds.\nSee exactly where your money goes.",
                NAVY_DEEP,
                NAVY_MID
        ));

        items.add(new OnBoardingItem(
                R.drawable.ic_onboard_budget,
                "BUDGETING",
                "Save More",
                "Set monthly budgets per category.\nGet alerts before you overspend.",
                0xFF0B1829,
                0xFF152238
        ));

        items.add(new OnBoardingItem(
                R.drawable.ic_onboard_chart,
                "ANALYTICS",
                "See Insights",
                "Visual charts reveal your financial\nhealth at a glance, every month.",
                0xFF0C1220,
                NAVY_SLIDE
        ));

        items.add(new OnBoardingItem(
                R.drawable.ic_onboard_news,
                "MARKET NEWS",
                "Stay Informed",
                "Live stock prices and curated\nfinancial news, all in one place.",
                0xFF080E1A,
                0xFF101C30
        ));

        adapter = new OnboardingAdapter(items);
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(new CardStackTransformer());
    }

    private void setupDots(int activeIndex) {
        dotsContainer.removeAllViews();

        for (int i = 0; i < TOTAL_SLIDES; i++) {
            ImageView dot = new ImageView(this);
            int widthDp  = (i == activeIndex) ? 28 : 10;
            int heightDp = 10;

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(dpToPx(widthDp), dpToPx(heightDp));
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dot.setLayoutParams(params);

            dot.setImageResource(
                    i == activeIndex ? R.drawable.dot_active : R.drawable.dot_inactive
            );
            dotsContainer.addView(dot);
        }
    }

    private void finishOnboarding() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit().putBoolean(KEY_INTRO, true).apply();
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    static class CardStackTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        @Override
        public void transformPage(@NonNull View page, float position) {
            int pageWidth  = page.getWidth();
            int pageHeight = page.getHeight();

            if (position < -1) {
                page.setAlpha(MIN_ALPHA);
            } else if (position <= 1) {
                float scale      = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scale) / 2;
                float horzMargin = pageWidth  * (1 - scale) / 2;

                page.setTranslationX(
                        position < 0
                                ? horzMargin - vertMargin / 2
                                : -horzMargin + vertMargin / 2
                );
                page.setScaleX(scale);
                page.setScaleY(scale);
                page.setAlpha(
                        MIN_ALPHA + (scale - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA)
                );
            } else {
                page.setAlpha(MIN_ALPHA);
            }
        }
    }
}