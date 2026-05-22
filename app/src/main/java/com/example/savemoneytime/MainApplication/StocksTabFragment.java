package com.example.savemoneytime.MainApplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savemoneytime.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.List;

public class StocksTabFragment extends Fragment {

    private static final String[] SYMBOLS  = { "VIC", "VCB", "FPT", "HPG", "MWG", "TCB" };
    // Bổ sung thêm mảng Tên công ty để fill vào thiết kế mới
    private static final String[] NAMES    = { "Vingroup JSC", "Vietcombank", "FPT Corp", "Hoa Phat Group", "Mobile World", "Techcombank" };
    private static final String[] PRICES   = { "45,200", "87,500", "98,700", "25,100", "43,600", "32,400" };
    private static final String[] CHANGES  = { "+1.2%", "-0.5%", "+2.1%", "+0.8%", "-1.3%", "+0.4%" };
    private static final float[][] SPARK   = {
            {43f,44.2f,43.8f,44.5f,44f,45f,45.2f}, {88.5f,88f,87.8f,88.2f,87.9f,87.6f,87.5f},
            {94f,95.5f,96f,97.2f,97.8f,98f,98.7f}, {24.5f,24.8f,25f,25.2f,25f,25.1f,25.1f},
            {45f,44.5f,44f,43.8f,43.5f,43.7f,43.6f}, {32f,32.1f,32f,32.3f,32.2f,32.4f,32.4f}
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stocks_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.rv_stocks);
        if (rv != null) {
            rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
            rv.setAdapter(new StockAdapter());
        }
    }

    private class StockAdapter extends RecyclerView.Adapter<StockAdapter.VH> {
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_stock_card, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            h.tvSym.setText(SYMBOLS[pos]);
            h.tvName.setText(NAMES[pos]);
            h.tvPrice.setText(PRICES[pos]);

            boolean isUp = CHANGES[pos].startsWith("+");
            h.tvChange.setText(CHANGES[pos]);

            // 🔥 Logic tự động đổi màu Nền (Background) và Chữ (Text) dựa vào Xu hướng Tăng/Giảm
            if (isUp) {
                h.tvChange.setTextColor(Color.parseColor("#34D399")); // bm_income
                h.tvChange.setBackgroundResource(R.drawable.bg_stock_trend_up);
            } else {
                h.tvChange.setTextColor(Color.parseColor("#F87171")); // bm_expense
                h.tvChange.setBackgroundResource(R.drawable.bg_stock_trend_down);
            }

            if (h.chart != null) {
                h.chart.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                drawSparkline(h.chart, SPARK[pos], isUp);
            }

            h.itemView.setOnClickListener(v -> {
                if (!isAdded()) return;
                StockDetailFragment detail = StockDetailFragment.newInstance(SYMBOLS[pos], NAMES[pos], PRICES[pos], CHANGES[pos]);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        .replace(R.id.fragment_container, detail).addToBackStack("stock_detail").commit();
            });
        }
        @Override public int getItemCount() { return SYMBOLS.length; }

        class VH extends RecyclerView.ViewHolder {
            TextView tvSym, tvName, tvPrice, tvChange;
            LineChart chart;

            VH(View v) {
                super(v);
                // 🔥 Đã khớp lệnh 100% với các ID chuẩn của thiết kế Premium
                tvSym = v.findViewById(R.id.tv_stock_ticker);
                tvName = v.findViewById(R.id.tv_stock_name);
                tvPrice = v.findViewById(R.id.tv_stock_price);
                tvChange = v.findViewById(R.id.tv_stock_change);
                chart = v.findViewById(R.id.chart_sparkline);
            }
        }
    }

    private void drawSparkline(LineChart chart, float[] data, boolean isUp) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < data.length; i++) entries.add(new Entry(i, data[i]));
        int color = isUp ? Color.parseColor("#34D399") : Color.parseColor("#F87171");
        LineDataSet ds = new LineDataSet(entries, "");
        ds.setColor(color); ds.setLineWidth(1.5f); ds.setDrawCircles(false); ds.setDrawValues(false);
        ds.setDrawFilled(true); ds.setFillColor(color); ds.setFillAlpha(25); ds.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        chart.setData(new LineData(ds));
        chart.getDescription().setEnabled(false); chart.getLegend().setEnabled(false);
        chart.getAxisLeft().setEnabled(false); chart.getAxisRight().setEnabled(false); chart.getXAxis().setEnabled(false);
        chart.setTouchEnabled(false); chart.setDrawGridBackground(false); chart.setBackgroundColor(Color.TRANSPARENT);
        chart.invalidate();
    }
}