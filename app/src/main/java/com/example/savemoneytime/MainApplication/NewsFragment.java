package com.example.savemoneytime.MainApplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.savemoneytime.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class NewsFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayoutMediator mediator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 🔥 ĐÃ FIX: Nắn ID Java theo đúng file XML mà sếp đang lưu trong máy
        viewPager = view.findViewById(R.id.view_pager_market);
        TabLayout tabLayout = view.findViewById(R.id.tab_layout_market);

        if (viewPager != null && tabLayout != null) {
            viewPager.setSaveEnabled(false); // Chống kẹt bộ nhớ đệm trạng thái cũ

            NewsTabAdapter adapter = new NewsTabAdapter(getChildFragmentManager(), getViewLifecycleOwner().getLifecycle());
            viewPager.setAdapter(adapter);

            mediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                tab.setText(position == 0 ? "STOCKS" : "FINANCIAL NEWS");
            });
            mediator.attach();
        }
    }

    @Override
    public void onDestroyView() {
        if (mediator != null) { mediator.detach(); mediator = null; }
        if (viewPager != null) { viewPager.setAdapter(null); viewPager = null; }
        super.onDestroyView();
    }

    private static class NewsTabAdapter extends FragmentStateAdapter {
        public NewsTabAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? new StocksTabFragment() : new FinancialNewsTabFragment();
        }
        @Override public int getItemCount() { return 2; }
    }
}