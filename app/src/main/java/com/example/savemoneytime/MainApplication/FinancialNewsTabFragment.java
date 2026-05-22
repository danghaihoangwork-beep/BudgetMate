package com.example.savemoneytime.MainApplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.savemoneytime.MainApplication.Adapters.NewsAdapter;
import com.example.savemoneytime.MainApplication.ViewModels.BudgetViewModel;
import com.example.savemoneytime.R;
import com.example.savemoneytime.network.NewsArticle;
import java.util.ArrayList;
import java.util.List;

public class FinancialNewsTabFragment extends Fragment {

    private static final String GNEWS_API_KEY = "bf3abc779ced0262fe84fc18599353b0";

    private BudgetViewModel    viewModel;
    private RecyclerView       rvNews;
    private NewsAdapter        adapter;
    private ProgressBar        progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private TextView           tvError;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_financial_news_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);

        rvNews       = view.findViewById(R.id.rv_news);
        progressBar  = view.findViewById(R.id.progress_bar);
        swipeRefresh = view.findViewById(R.id.swipe_refresh_news);
        tvError      = view.findViewById(R.id.tv_error);

        adapter = new NewsAdapter((url, title) -> {
            if (!isAdded() || url == null || url.isEmpty()) return;
            ArticleDetailFragment detail = ArticleDetailFragment.newInstance(url, title);

            // 🔥 FIXED CHÍ MẠNG: Đổi getParentFragmentManager() thành requireActivity().getSupportFragmentManager()
            // Ép hệ thống dùng Manager của Activity chính mới định vị được R.id.fragment_container tầng gốc!
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    .replace(R.id.fragment_container, detail)
                    .addToBackStack("article")
                    .commit();
        });

        if (rvNews != null) {
            rvNews.setLayoutManager(new LinearLayoutManager(view.getContext()));
            rvNews.setAdapter(adapter);
        }

        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeColors(0xFFD4AF37);
            swipeRefresh.setProgressBackgroundColorSchemeColor(0xFF1A233D);
            swipeRefresh.setOnRefreshListener(() -> viewModel.loadNews(GNEWS_API_KEY));
        }

        observeViewModel();

        viewModel.loadNews(GNEWS_API_KEY);
    }

    private void observeViewModel() {
        viewModel.getNews().observe(getViewLifecycleOwner(), articles -> {
            if (!isAdded()) return;
            if (articles != null && !articles.isEmpty()) {
                List<NewsArticle> limited = new ArrayList<>();
                int max = Math.min(articles.size(), 6);
                for (int i = 0; i < max; i++) limited.add(articles.get(i));
                if (adapter != null) adapter.updateArticles(limited);
            } else {
                loadMockNews();
            }
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            if (tvError != null) tvError.setVisibility(View.GONE);
        });

        viewModel.getNewsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (!isAdded() || progressBar == null) return;
            progressBar.setVisibility(loading != null && loading ? View.VISIBLE : View.GONE);
        });

        viewModel.getNewsError().observe(getViewLifecycleOwner(), error -> {
            if (!isAdded() || error == null) return;
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            loadMockNews();
        });
    }

    private void loadMockNews() {
        List<NewsArticle> mock = new ArrayList<>();
        mock.add(mk("VN-Index Rises 12 Points Led by Banking Stocks", "Vietnam's benchmark VN-Index gained nearly 12 points in morning session, driven by strong performance.", "CafeF", "2026-05-18", "https://cafef.vn"));
        mock.add(mk("Fed Holds Rates Steady, Signals Cautious Outlook", "The Federal Reserve kept interest rates unchanged, citing ongoing uncertainty.", "Reuters", "2026-05-17", "https://reuters.com"));
        if (adapter != null) adapter.updateArticles(mock);
    }

    private NewsArticle mk(String t, String d, String s, String dt, String u) {
        return new MockA(t, d, s, dt, u);
    }

    private static class MockA extends NewsArticle {
        private final String t, d, s, dt, u;
        MockA(String t, String d, String s, String dt, String u) { this.t=t; this.d=d; this.s=s; this.dt=dt; this.u=u; }
        @Override public String getTitle() { return t; } @Override public String getDescription() { return d; } @Override public String getSourceName() { return s; } @Override public String getPublishedAt() { return dt; } @Override public String getUrl() { return u; } @Override public String getImageUrl() { return ""; }
    }
}