package com.example.savemoneytime.MainApplication;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.savemoneytime.R;
import com.example.savemoneytime.network.StockResponse;
import com.example.savemoneytime.network.StockRetrofitClient;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockDetailFragment extends Fragment {

    private static final String ARG_SYMBOL = "stock_symbol";
    private static final String ARG_NAME   = "stock_name";
    private static final String ARG_PRICE  = "stock_price";
    private static final String ARG_CHANGE = "stock_change";

    private static final String AV_API_KEY = "MEPVO0E9IMSB4VTA";

    private CombinedChart combinedChart;
    private BarChart volumeChart;

    private TextView tvSymbol, tvName, tvCurrentPrice, tvPriceChange, tvCompanyDesc;
    private ImageView btnBack;
    private String symbol, name, price, change;

    public static StockDetailFragment newInstance(String symbol, String name, String price, String change) {
        StockDetailFragment f = new StockDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SYMBOL, symbol);
        args.putString(ARG_NAME,   name);
        args.putString(ARG_PRICE,  price);
        args.putString(ARG_CHANGE, change);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            symbol = getArguments().getString(ARG_SYMBOL, "AAPL");
            name   = getArguments().getString(ARG_NAME,   "Apple Inc.");
            price  = getArguments().getString(ARG_PRICE,  "$189.50");
            change = getArguments().getString(ARG_CHANGE, "+1.2%");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_detail, container, false);

        combinedChart  = view.findViewById(R.id.combined_stock_chart);
        volumeChart    = view.findViewById(R.id.volume_stock_chart);

        tvSymbol       = view.findViewById(R.id.tv_stock_symbol);
        tvName         = view.findViewById(R.id.tv_stock_name);
        tvCurrentPrice = view.findViewById(R.id.tv_current_price);
        tvPriceChange  = view.findViewById(R.id.tv_price_change);
        btnBack        = view.findViewById(R.id.btn_back_stock);
        tvCompanyDesc  = view.findViewById(R.id.tv_company_desc);

        tvSymbol.setText(symbol);
        tvName.setText(name);
        tvCurrentPrice.setText(price);
        tvPriceChange.setText(change + (change.startsWith("+") ? " ▲" : " ▼"));
        tvPriceChange.setTextColor(change.startsWith("+") ? Color.parseColor("#34D399") : Color.parseColor("#F87171"));

        TextView tvMatrixCap = view.findViewById(R.id.tv_matrix_cap);
        TextView tvMatrixHigh = view.findViewById(R.id.tv_matrix_high);
        TextView tvMatrixLow = view.findViewById(R.id.tv_matrix_low);
        if(tvMatrixCap != null) tvMatrixCap.setText("2.98T USD");
        if(tvMatrixHigh != null) tvMatrixHigh.setText("$199.62");
        if(tvMatrixLow != null) tvMatrixLow.setText("$164.08");

        // 🔥 FIX ĐÓNG ĐINH: Gán text hồ sơ trực tiếp loại bỏ chữ Loading rác
        if(tvCompanyDesc != null) {
            tvCompanyDesc.setText(getCompanyProfile(symbol));
        }

        setupCombinedChart();
        setupVolumeChart();
        loadLiveStockData();

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) getParentFragmentManager().popBackStack();
        });

        return view;
    }

    private String getCompanyProfile(String symbol) {
        switch (symbol) {
            case "AAPL": return "Apple Inc. designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories worldwide. It is one of the Big Five American information technology companies.";
            case "MSFT": return "Microsoft Corporation develops, licenses, and supports software, services, devices, and solutions worldwide. Known for its Windows OS, Office suite, and Azure cloud platform.";
            case "NVDA": return "NVIDIA Corporation provides graphics, and compute and networking solutions in the United States, Taiwan, China, and internationally. It is a dominant force in the AI hardware market.";
            case "TSLA": return "Tesla, Inc. designs, develops, manufactures, leases, and sells electric vehicles, and energy generation and storage systems in the United States, China, and internationally.";
            case "GOOGL": return "Alphabet Inc. offers various products and platforms globally. It operates Google Search, YouTube, and Android.";
            case "AMZN": return "Amazon.com, Inc. engages in the retail sale of consumer products and subscriptions in North America and internationally. It also provides AWS, the leading cloud computing platform.";
            case "META": return "Meta Platforms builds technologies that help people connect, find communities, and grow businesses. It operates Facebook, Instagram, WhatsApp, and VR products.";
            case "NFLX": return "Netflix, Inc. provides entertainment services. It offers TV series, documentaries, feature films, and mobile games across various genres and languages.";
            case "AMD": return "Advanced Micro Devices, Inc. operates as a semiconductor company worldwide. It offers x86 microprocessors, GPUs, and related technologies.";
            case "INTC": return "Intel Corporation designs, manufactures, and sells essential technologies for the cloud, smart, and connected devices for retail, industrial, and consumer uses worldwide.";
            case "DIS": return "The Walt Disney Company operates as an entertainment company worldwide. It encompasses film studios, theme parks, and streaming services like Disney+.";
            case "V": return "Visa Inc. operates as a payments technology company worldwide. It facilitates digital payments among consumers, merchants, financial institutions, and government entities.";
            default: return name + " is a publicly traded company on the major stock exchanges. It engages in various business activities within its sector to generate revenue and provide value to its shareholders.";
        }
    }

    private void setupCombinedChart() {
        combinedChart.setBackgroundColor(Color.TRANSPARENT);
        combinedChart.getDescription().setEnabled(false);
        combinedChart.setDrawGridBackground(false);
        combinedChart.setTouchEnabled(true);
        combinedChart.setDragEnabled(true);
        combinedChart.setScaleEnabled(true);
        combinedChart.setPinchZoom(true);
        combinedChart.setAutoScaleMinMaxEnabled(true);
        combinedChart.getAxisRight().setEnabled(false);

        XAxis xAxis = combinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#3D4F6B"));
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(9f);

        YAxis leftAxis = combinedChart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#3D4F6B"));
        leftAxis.setGridColor(Color.parseColor("#1A233D"));
        leftAxis.setTextSize(9f);

        combinedChart.getLegend().setTextColor(Color.WHITE);
        combinedChart.getLegend().setTextSize(9f);
    }

    private void setupVolumeChart() {
        volumeChart.setBackgroundColor(Color.TRANSPARENT);
        volumeChart.getDescription().setEnabled(false);
        volumeChart.setTouchEnabled(false);
        volumeChart.getAxisRight().setEnabled(false);
        volumeChart.getLegend().setEnabled(false);

        volumeChart.getXAxis().setEnabled(false);
        volumeChart.getAxisLeft().setTextColor(Color.parseColor("#3D4F6B"));
        volumeChart.getAxisLeft().setTextSize(8f);
        volumeChart.getAxisLeft().setDrawGridLines(false);
    }

    private void loadLiveStockData() {
        StockRetrofitClient.getStockService()
                .getDailyOHLC("TIME_SERIES_DAILY", symbol, "compact", AV_API_KEY)
                .enqueue(new Callback<StockResponse>() {
                    @Override
                    public void onResponse(Call<StockResponse> call, Response<StockResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<com.example.savemoneytime.model.CandleData> candles = StockRetrofitClient.parseOHLC(response.body());
                            if (!candles.isEmpty() && getActivity() != null) {
                                getActivity().runOnUiThread(() -> bindAdvancedCharts(candles));
                                return;
                            }
                        }
                        generatePremiumMockCandles();
                    }
                    @Override
                    public void onFailure(Call<StockResponse> call, Throwable t) {
                        generatePremiumMockCandles();
                    }
                });
    }

    private void generatePremiumMockCandles() {
        List<com.example.savemoneytime.model.CandleData> candles = new ArrayList<>();
        float basePrice = 180f;
        java.util.Random random = new java.util.Random(42);
        for (int i = 0; i < 40; i++) {
            float open  = basePrice + (random.nextFloat() - 0.5f) * 10f;
            float close = open + (random.nextFloat() - 0.47f) * 8f;
            float high  = Math.max(open, close) + random.nextFloat() * 4f;
            float low   = Math.min(open, close) - random.nextFloat() * 4f;
            long volume = 20000000 + random.nextInt(50000000);
            candles.add(new com.example.savemoneytime.model.CandleData("2026-05-" + String.format(Locale.US, "%02d", i + 1), open, high, low, close, volume));
            basePrice = close;
        }
        bindAdvancedCharts(candles);
    }

    private void bindAdvancedCharts(List<com.example.savemoneytime.model.CandleData> candles) {
        List<CandleEntry> candleEntries = new ArrayList<>();
        List<BarEntry> volumeEntries = new ArrayList<>();
        List<Entry> ma5Entries = new ArrayList<>();
        List<Entry> ma20Entries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();
        List<Integer> volumeColors = new ArrayList<>();

        int startIndex = Math.max(0, candles.size() - 30);
        int chartIndex = 0;

        for (int i = startIndex; i < candles.size(); i++) {
            com.example.savemoneytime.model.CandleData c = candles.get(i);
            candleEntries.add(new CandleEntry(chartIndex, c.getHigh(), c.getLow(), c.getOpen(), c.getClose()));
            volumeEntries.add(new BarEntry(chartIndex, (float) c.getVolume()));

            volumeColors.add(c.isBullish() ? Color.parseColor("#34D399") : Color.parseColor("#F87171"));
            xLabels.add(c.getDate().length() >= 5 ? c.getDate().substring(5) : c.getDate());

            if (i >= 5) {
                float sum = 0;
                for (int k = i - 4; k <= i; k++) sum += candles.get(k).getClose();
                ma5Entries.add(new Entry(chartIndex, sum / 5f));
            }
            if (i >= 20) {
                float sum = 0;
                for (int k = i - 19; k <= i; k++) sum += candles.get(k).getClose();
                ma20Entries.add(new Entry(chartIndex, sum / 20f));
            }

            chartIndex++;
        }

        combinedChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xLabels));

        CandleDataSet candleSet = new CandleDataSet(candleEntries, "Candle");
        candleSet.setIncreasingColor(Color.parseColor("#34D399"));
        candleSet.setIncreasingPaintStyle(Paint.Style.FILL);
        candleSet.setDecreasingColor(Color.parseColor("#F87171"));
        candleSet.setDecreasingPaintStyle(Paint.Style.FILL);
        candleSet.setShadowColor(Color.parseColor("#9CA3AF"));
        candleSet.setShadowWidth(1.2f);
        candleSet.setDrawValues(false);
        com.github.mikephil.charting.data.CandleData finalCandleData = new com.github.mikephil.charting.data.CandleData(candleSet);

        LineDataSet ma5Set = new LineDataSet(ma5Entries, "MA5 Short");
        ma5Set.setColor(Color.parseColor("#FBBF24"));
        ma5Set.setLineWidth(1.8f);
        ma5Set.setDrawCircles(false);
        ma5Set.setDrawValues(false);

        LineDataSet ma20Set = new LineDataSet(ma20Entries, "MA20 Mid");
        ma20Set.setColor(Color.parseColor("#60A5FA"));
        ma20Set.setLineWidth(1.8f);
        ma20Set.setDrawCircles(false);
        ma20Set.setDrawValues(false);

        List<com.github.mikephil.charting.interfaces.datasets.ILineDataSet> lines = new ArrayList<>();
        lines.add(ma5Set);
        lines.add(ma20Set);
        com.github.mikephil.charting.data.LineData finalLineData = new com.github.mikephil.charting.data.LineData(lines);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(finalCandleData);
        combinedData.setData(finalLineData);

        combinedChart.setData(combinedData);
        combinedChart.animateX(600);
        combinedChart.invalidate();

        BarDataSet volSet = new BarDataSet(volumeEntries, "Volume");
        volSet.setColors(volumeColors);
        volSet.setDrawValues(false);
        BarData volData = new BarData(volSet);
        volData.setBarWidth(0.6f);

        volumeChart.setData(volData);
        volumeChart.animateY(500);
        volumeChart.invalidate();
    }
}