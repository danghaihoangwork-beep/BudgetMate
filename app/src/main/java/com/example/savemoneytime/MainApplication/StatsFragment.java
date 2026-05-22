package com.example.savemoneytime.MainApplication;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.savemoneytime.R;
import com.example.savemoneytime.database.AppDatabase;
import com.example.savemoneytime.model.ExpenseEntity;
import com.example.savemoneytime.model.RevenueEntity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;

public class StatsFragment extends Fragment {

    private Spinner spinnerMonth, spinnerYear;
    private MaterialButtonToggleGroup togglePeriod;
    private TextView tvTotalIncome, tvTotalExpense, tvAiInsight;
    private MaterialButton btnExport;

    private BarChart barChart;
    private LineChart lineChart;
    private PieChart pieChartExpense, pieChartIncome;
    private RadarChart radarChart;

    private AppDatabase db;
    private boolean isMonthly = true;
    private int selectedMonth, selectedYear;

    private List<ExpenseEntity> currentExpenses = new ArrayList<>();
    private List<RevenueEntity> currentRevenues = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        db = AppDatabase.getInstance(requireContext());

        spinnerMonth    = view.findViewById(R.id.spinner_month_stats);
        spinnerYear     = view.findViewById(R.id.spinner_year_stats);
        togglePeriod    = view.findViewById(R.id.toggle_group_period);
        tvTotalIncome   = view.findViewById(R.id.tv_total_income_stats);
        tvTotalExpense  = view.findViewById(R.id.tv_total_expense_stats);
        btnExport       = view.findViewById(R.id.btn_export_report);
        tvAiInsight     = view.findViewById(R.id.tv_ai_insight);

        barChart        = view.findViewById(R.id.bar_chart_compare);
        lineChart       = view.findViewById(R.id.line_chart_trend);
        pieChartExpense = view.findViewById(R.id.pie_chart_expense);
        pieChartIncome  = view.findViewById(R.id.pie_chart_income);
        radarChart      = view.findViewById(R.id.radar_chart_habits);

        initFilters();
        setupCharts();
        loadData();

        btnExport.setOnClickListener(v -> triggerExportFlow());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void initFilters() {
        Calendar cal = Calendar.getInstance();
        selectedMonth = cal.get(Calendar.MONTH) + 1;
        selectedYear = cal.get(Calendar.YEAR);

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, months) {
            @NonNull @Override public View getView(int pos, @Nullable View conv, @NonNull ViewGroup par) {
                View v = super.getView(pos, conv, par);
                if (v instanceof TextView) { ((TextView) v).setTextColor(Color.parseColor("#FFD4AF37")); ((TextView) v).setTextSize(14f); }
                return v;
            }
            @Override public View getDropDownView(int pos, @Nullable View conv, @NonNull ViewGroup par) {
                View v = super.getDropDownView(pos, conv, par);
                if (v instanceof TextView) { ((TextView) v).setTextColor(Color.parseColor("#FFD4AF37")); ((TextView) v).setBackgroundColor(Color.parseColor("#FF111827")); }
                return v;
            }
        };
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);
        spinnerMonth.setSelection(selectedMonth - 1);

        String[] years = {"2024", "2025", "2026"};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_item, years) {
            @NonNull @Override public View getView(int pos, @Nullable View conv, @NonNull ViewGroup par) {
                View v = super.getView(pos, conv, par);
                if (v instanceof TextView) { ((TextView) v).setTextColor(Color.parseColor("#FFD4AF37")); ((TextView) v).setTextSize(14f); }
                return v;
            }
            @Override public View getDropDownView(int pos, @Nullable View conv, @NonNull ViewGroup par) {
                View v = super.getDropDownView(pos, conv, par);
                if (v instanceof TextView) { ((TextView) v).setTextColor(Color.parseColor("#FFD4AF37")); ((TextView) v).setBackgroundColor(Color.parseColor("#FF111827")); }
                return v;
            }
        };
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
        spinnerYear.setSelection(2);

        togglePeriod.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isMonthly = (checkedId == R.id.btn_monthly);
                spinnerMonth.setVisibility(isMonthly ? View.VISIBLE : View.GONE);
                loadData();
            }
        });

        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedMonth = spinnerMonth.getSelectedItemPosition() + 1;
                selectedYear = Integer.parseInt(spinnerYear.getSelectedItem().toString());
                loadData();
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };

        spinnerMonth.setOnItemSelectedListener(itemSelectedListener);
        spinnerYear.setOnItemSelectedListener(itemSelectedListener);
    }

    private void setupCharts() {
        barChart.setBackgroundColor(Color.TRANSPARENT);
        barChart.getDescription().setEnabled(false);
        barChart.setScaleEnabled(false);
        barChart.setDragEnabled(false);
        barChart.getXAxis().setTextColor(Color.parseColor("#9CA3AF"));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisLeft().setTextColor(Color.parseColor("#9CA3AF"));
        barChart.getAxisLeft().setGridColor(Color.parseColor("#1F2A44"));
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setTextColor(Color.WHITE);

        // 🔥 FIX CHÍ MẠNG (1): Đẩy biên dưới lên cao để nhãn tháng không bao giờ bị cắt/che khuất
        lineChart.setBackgroundColor(Color.TRANSPARENT);
        lineChart.getDescription().setEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setDragEnabled(false);
        lineChart.setExtraBottomOffset(18f); // Tạo vùng đệm an toàn dưới đáy cho text hiển thị công khai
        lineChart.setExtraLeftOffset(6f);
        lineChart.setExtraRightOffset(12f);

        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setTextColor(Color.parseColor("#9CA3AF"));
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisLeft().setTextColor(Color.parseColor("#9CA3AF"));
        lineChart.getAxisLeft().setGridColor(Color.parseColor("#1F2A44"));
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setTextColor(Color.WHITE);

        configurePieChartStructure(pieChartExpense);
        configurePieChartStructure(pieChartIncome);

        radarChart.setBackgroundColor(Color.TRANSPARENT);
        radarChart.getDescription().setEnabled(false);
        radarChart.setWebLineWidth(1f);
        radarChart.setWebColor(Color.parseColor("#3D4F6B"));
        radarChart.setWebLineWidthInner(1f);
        radarChart.setWebColorInner(Color.parseColor("#1F2A44"));
        radarChart.setWebAlpha(150);
        radarChart.getXAxis().setTextSize(9f);
        radarChart.getXAxis().setTextColor(Color.parseColor("#E0E6ED"));
        radarChart.getYAxis().setDrawLabels(false);
        radarChart.getLegend().setTextColor(Color.WHITE);
    }

    private void configurePieChartStructure(PieChart chart) {
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(62f);
        chart.setHoleColor(Color.parseColor("#111827"));
        chart.setDrawEntryLabels(false); // Tắt nhãn đè trực tiếp lên bánh để tránh rối mắt

        Legend pLegend = chart.getLegend();
        pLegend.setEnabled(true);
        pLegend.setTextColor(Color.WHITE);
        pLegend.setTextSize(10f);
        pLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        pLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        pLegend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        pLegend.setWordWrapEnabled(true);
        pLegend.setXEntrySpace(8f);
        pLegend.setYEntrySpace(4f);
    }

    private void loadData() {
        String monthStr = String.format(Locale.US, "%02d", selectedMonth);
        String yearStr = String.valueOf(selectedYear);

        AppDatabase.dbExecutor.execute(() -> {
            if (isMonthly) {
                currentExpenses = db.expenseDao().getByMonth(monthStr, yearStr);
                currentRevenues = db.revenueDao().getByMonth(monthStr, yearStr);
            } else {
                List<ExpenseEntity> allExp = db.expenseDao().getAll();
                List<RevenueEntity> allRev = db.revenueDao().getAll();
                currentExpenses = new ArrayList<>();
                currentRevenues = new ArrayList<>();
                SimpleDateFormat yf = new SimpleDateFormat("yyyy", Locale.US);
                for (ExpenseEntity e : allExp) {
                    if (yf.format(new Date(e.getDate())).equals(yearStr)) currentExpenses.add(e);
                }
                for (RevenueEntity r : allRev) {
                    if (yf.format(new Date(r.getDate())).equals(yearStr)) currentRevenues.add(r);
                }
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> updateDashboardUI(currentExpenses, currentRevenues));
            }
        });
    }

    private void updateDashboardUI(List<ExpenseEntity> expenses, List<RevenueEntity> revenues) {
        long totalExp = 0;
        long totalRev = 0;
        Map<String, Long> categoryTotals = new HashMap<>();
        Map<String, Float> expenseCats = new HashMap<>();
        Map<String, Float> revenueCats = new HashMap<>();

        for (ExpenseEntity e : expenses) {
            totalExp += e.getAmount();
            categoryTotals.put(e.getCategoryName(), categoryTotals.getOrDefault(e.getCategoryName(), 0L) + e.getAmount());
            expenseCats.put(e.getCategoryName(), expenseCats.getOrDefault(e.getCategoryName(), 0f) + e.getAmount());
        }
        for (RevenueEntity r : revenues) {
            totalRev += r.getAmount();
            revenueCats.put(r.getCategoryName(), revenueCats.getOrDefault(r.getCategoryName(), 0f) + r.getAmount());
        }

        tvTotalIncome.setText(String.format(Locale.US, "$%,d", totalRev));
        tvTotalExpense.setText(String.format(Locale.US, "$%,d", totalExp));

        if (tvAiInsight != null) {
            if (totalExp > 0) {
                String topCat = "";
                long maxAmt = 0;
                for (Map.Entry<String, Long> entry : categoryTotals.entrySet()) {
                    if (entry.getValue() > maxAmt) { maxAmt = entry.getValue(); topCat = entry.getKey(); }
                }
                float percent = (maxAmt * 100f) / totalExp;

                String recommendation = "";
                if (totalRev > 0) {
                    float savingsRate = ((totalRev - totalExp) * 100f) / totalRev;
                    if (savingsRate > 20) {
                        recommendation = "\n\n💡 Recommendation: Great job! Your savings rate is " + String.format(Locale.US, "%.1f%%", savingsRate) + ". Consider investing the surplus into low-risk Index Funds (like S&P 500) to combat inflation.";
                    } else if (savingsRate > 0) {
                        recommendation = "\n\n💡 Recommendation: You are saving " + String.format(Locale.US, "%.1f%%", savingsRate) + " of your income. Try the 50/30/20 rule to boost your savings rate up to 20%.";
                    } else {
                        recommendation = "\n\n💡 Recommendation: Warning! You are operating at a deficit. Review your discretionary spending immediately to balance your cash flow.";
                    }
                }

                tvAiInsight.setText(String.format(Locale.US,
                        "Your highest expense is '%s', taking up %.1f%% of your total outflow. " +
                                "Consider setting a stricter limit on this category next month to optimize your cash flow.%s",
                        topCat, percent, recommendation));
            } else {
                tvAiInsight.setText("Not enough expense data to generate insights yet. Keep logging your transactions!");
            }
        }

        updateBarChartGrouped(expenses, revenues);
        updateLineChartTrend(expenses, revenues);

        // 🔥 FIX CHÍ MẠNG (2): Cấu hình bảng màu PREMIUM tương phản cao độc lập (Tuyệt đối không dùng gradient đơn sắc)
        int[] distinctExpenseColors = new int[]{
                Color.parseColor("#FFF87171"), // Đỏ San Hô (Transport)
                Color.parseColor("#FFD4AF37"), // Vàng Gold (Travel)
                Color.parseColor("#FF60A5FA"), // Xanh Dương (Food)
                Color.parseColor("#FFA78BFA"), // Tím Bách Hợp (Shopping)
                Color.parseColor("#FF14B8A6"), // Xanh Ngọc Teal (Health)
                Color.parseColor("#FFFB923C"), // Cam Vibrant
                Color.parseColor("#FF34D399")  // Xanh Lá Thạch Anh
        };

        int[] distinctIncomeColors = new int[]{
                Color.parseColor("#FF34D399"), // Xanh Lá Mint (Salary)
                Color.parseColor("#FF14B8A6"), // Xanh Teal (Transport)
                Color.parseColor("#FF60A5FA"), // Xanh Sky (Food)
                Color.parseColor("#FFD4AF37"), // Vàng Gold
                Color.parseColor("#FFA78BFA")  // Tím Lavender
        };

        renderPieChartData(pieChartExpense, expenseCats, distinctExpenseColors, "No Expenses");
        renderPieChartData(pieChartIncome, revenueCats, distinctIncomeColors, "No Income");

        updateRadarChartDesign(expenses);
    }

    private void renderPieChartData(PieChart chart, Map<String, Float> dataMap, int[] palette, String blankText) {
        if (dataMap.isEmpty()) {
            chart.clear();
            chart.setNoDataText(blankText);
            chart.setNoDataTextColor(Color.parseColor("#9CA3AF"));
            return;
        }
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : dataMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }
        PieDataSet set = new PieDataSet(entries, "");
        set.setSliceSpace(3f);
        set.setSelectionShift(6f);
        set.setColors(palette);

        PieData data = new PieData(set);
        data.setValueFormatter(new PercentFormatter(chart));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);

        chart.setData(data);
        chart.animateY(800);
        chart.invalidate();
    }

    private ValueFormatter getCompactFormatter() {
        return new ValueFormatter() {
            @Override public String getFormattedValue(float value) {
                if (value == 0) return "";
                if (value >= 1_000_000) return String.format(Locale.US, "%.1fM", value / 1_000_000f);
                if (value >= 1_000) return String.format(Locale.US, "%.1fK", value / 1_000f);
                return String.format(Locale.US, "%.0f", value);
            }
        };
    }

    private void updateBarChartGrouped(List<ExpenseEntity> expenses, List<RevenueEntity> revenues) {
        ArrayList<BarEntry> incomeEntries = new ArrayList<>();
        ArrayList<BarEntry> expenseEntries = new ArrayList<>();
        String[] labels;

        if (isMonthly) {
            labels = new String[]{"W1", "W2", "W3", "W4"};
            float[] weeklyRev = new float[4];
            float[] weeklyExp = new float[4];
            SimpleDateFormat df = new SimpleDateFormat("dd", Locale.US);
            for (RevenueEntity r : revenues) {
                try { int day = Integer.parseInt(df.format(new Date(r.getDate()))); weeklyRev[Math.min((day - 1) / 7, 3)] += r.getAmount(); } catch (Exception ignored) {}
            }
            for (ExpenseEntity e : expenses) {
                try { int day = Integer.parseInt(df.format(new Date(e.getDate()))); weeklyExp[Math.min((day - 1) / 7, 3)] += e.getAmount(); } catch (Exception ignored) {}
            }
            for (int i = 0; i < 4; i++) {
                incomeEntries.add(new BarEntry(i, weeklyRev[i]));
                expenseEntries.add(new BarEntry(i, weeklyExp[i]));
            }
        } else {
            labels = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            float[] monthlyRev = new float[12];
            float[] monthlyExp = new float[12];
            SimpleDateFormat df = new SimpleDateFormat("MM", Locale.US);
            for (RevenueEntity r : revenues) {
                try { int m = Integer.parseInt(df.format(new Date(r.getDate()))) - 1; if (m >= 0 && m < 12) monthlyRev[m] += r.getAmount(); } catch (Exception ignored) {}
            }
            for (ExpenseEntity e : expenses) {
                try { int m = Integer.parseInt(df.format(new Date(e.getDate()))) - 1; if (m >= 0 && m < 12) monthlyExp[m] += e.getAmount(); } catch (Exception ignored) {}
            }
            for (int i = 0; i < 12; i++) {
                incomeEntries.add(new BarEntry(i, monthlyRev[i]));
                expenseEntries.add(new BarEntry(i, monthlyExp[i]));
            }
        }

        BarDataSet set1 = new BarDataSet(incomeEntries, "Income");
        set1.setColor(Color.parseColor("#34D399"));
        set1.setDrawValues(true);
        set1.setValueTextColor(Color.WHITE);
        set1.setValueTextSize(8f);
        set1.setValueFormatter(getCompactFormatter());

        BarDataSet set2 = new BarDataSet(expenseEntries, "Expense");
        set2.setColor(Color.parseColor("#F87171"));
        set2.setDrawValues(true);
        set2.setValueTextColor(Color.WHITE);
        set2.setValueTextSize(8f);
        set2.setValueFormatter(getCompactFormatter());

        BarData data = new BarData(set1, set2);
        data.setBarWidth(0.30f);
        barChart.setData(data);
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(labels.length);
        xAxis.setLabelCount(labels.length, false);
        barChart.groupBars(0f, 0.30f, 0.05f);
        barChart.animateY(800);
        barChart.invalidate();
    }

    private void updateLineChartTrend(List<ExpenseEntity> expenses, List<RevenueEntity> revenues) {
        ArrayList<Entry> expEntries = new ArrayList<>();
        ArrayList<Entry> revEntries = new ArrayList<>();
        XAxis xAxis = lineChart.getXAxis();

        if (isMonthly) {
            float[] dailyExp = new float[31];
            float[] dailyRev = new float[31];
            SimpleDateFormat df = new SimpleDateFormat("dd", Locale.US);
            for (ExpenseEntity e : expenses) {
                try { int day = Integer.parseInt(df.format(new Date(e.getDate()))) - 1; if (day >= 0 && day < 31) dailyExp[day] += e.getAmount(); } catch (Exception ignored) {}
            }
            for (RevenueEntity r : revenues) {
                try { int day = Integer.parseInt(df.format(new Date(r.getDate()))) - 1; if (day >= 0 && day < 31) dailyRev[day] += r.getAmount(); } catch (Exception ignored) {}
            }
            for (int i = 0; i < 31; i++) {
                expEntries.add(new Entry(i + 1, dailyExp[i]));
                revEntries.add(new Entry(i + 1, dailyRev[i]));
            }
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override public String getFormattedValue(float value) { return String.valueOf((int) value); }
            });
            xAxis.setLabelCount(6, false);
            xAxis.setAxisMinimum(1f);
            xAxis.setAxisMaximum(31f);
            xAxis.setGranularity(1f);
        } else {
            float[] monthlyExp = new float[12];
            float[] monthlyRev = new float[12];
            SimpleDateFormat df = new SimpleDateFormat("MM", Locale.US);
            for (ExpenseEntity e : expenses) {
                try { int m = Integer.parseInt(df.format(new Date(e.getDate()))) - 1; if (m >= 0 && m < 12) monthlyExp[m] += e.getAmount(); } catch (Exception ignored) {}
            }
            for (RevenueEntity r : revenues) {
                try { int m = Integer.parseInt(df.format(new Date(r.getDate()))) - 1; if (m >= 0 && m < 12) monthlyRev[m] += r.getAmount(); } catch (Exception ignored) {}
            }
            for (int i = 0; i < 12; i++) {
                expEntries.add(new Entry(i, monthlyExp[i]));
                revEntries.add(new Entry(i, monthlyRev[i]));
            }
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            xAxis.setValueFormatter(new IndexAxisValueFormatter(months));

            // 🔥 FIX CHÍ MẠNG (1.2): Ép buộc đồ thị hiển thị toàn bộ 12 nhãn trục hoành, không giấu Nov, Dec
            xAxis.setLabelCount(12, true);
            xAxis.setAxisMinimum(0f);
            xAxis.setAxisMaximum(11f);
            xAxis.setGranularity(1f);
        }

        LineDataSet revSet = new LineDataSet(revEntries, "Income Trend");
        revSet.setColor(Color.parseColor("#34D399"));
        revSet.setCircleColor(Color.parseColor("#34D399"));
        revSet.setLineWidth(2.5f);
        revSet.setCircleRadius(3.5f);
        revSet.setDrawValues(true);
        revSet.setValueTextColor(Color.WHITE);
        revSet.setValueTextSize(8f);
        revSet.setValueFormatter(getCompactFormatter());
        revSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineDataSet expSet = new LineDataSet(expEntries, "Expense Trend");
        expSet.setColor(Color.parseColor("#F87171"));
        expSet.setCircleColor(Color.parseColor("#F87171"));
        expSet.setLineWidth(2.5f);
        expSet.setCircleRadius(3.5f);
        expSet.setDrawValues(true);
        expSet.setValueTextColor(Color.WHITE);
        expSet.setValueTextSize(8f);
        expSet.setValueFormatter(getCompactFormatter());
        expSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(revSet);
        dataSets.add(expSet);

        lineChart.setData(new LineData(dataSets));
        lineChart.animateX(800);
        lineChart.invalidate();
    }

    private void updateRadarChartDesign(List<ExpenseEntity> expenses) {
        String[] radarLabels = {"Food", "Transport", "Housing", "Health", "Ent.", "Shop"};
        float[] vals = new float[6];
        for (ExpenseEntity e : expenses) {
            String c = e.getCategoryName().toLowerCase();
            if (c.contains("food")) vals[0] += e.getAmount();
            else if (c.contains("trans")) vals[1] += e.getAmount();
            else if (c.contains("hous")) vals[2] += e.getAmount();
            else if (c.contains("heal")) vals[3] += e.getAmount();
            else if (c.contains("ent")) vals[4] += e.getAmount();
            else vals[5] += e.getAmount();
        }

        ArrayList<RadarEntry> entries = new ArrayList<>();
        for (float v : vals) entries.add(new RadarEntry(v));

        RadarDataSet set = new RadarDataSet(entries, "Spending Focus");
        set.setColor(Color.parseColor("#FFD4AF37"));
        set.setFillColor(Color.parseColor("#FFD4AF37"));
        set.setDrawFilled(true);
        set.setFillAlpha(60);
        set.setLineWidth(2f);
        set.setDrawValues(false);

        radarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(radarLabels));
        radarChart.setData(new RadarData(set));
        radarChart.animateXY(800, 800);
        radarChart.invalidate();
    }

    private void triggerExportFlow() {
        if (currentExpenses.isEmpty() && currentRevenues.isEmpty()) {
            Toast.makeText(requireContext(), "No data found for exporting", Toast.LENGTH_SHORT).show();
            return;
        }
        AppDatabase.dbExecutor.execute(() -> {
            try {
                File targetDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

                File csvFile = new File(targetDir, "BudgetReport_" + timestamp + ".csv");
                FileWriter writer = new FileWriter(csvFile);
                writer.append("Type,Category,Amount,Date,Description\n");
                for (RevenueEntity r : currentRevenues) {
                    writer.append(String.format(Locale.US, "INCOME,%s,%d,%s,%s\n", r.getCategoryName(), r.getAmount(), new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(r.getDate())), r.getNote()));
                }
                for (ExpenseEntity e : currentExpenses) {
                    writer.append(String.format(Locale.US, "EXPENSE,%s,%d,%s,%s\n", e.getCategoryName(), e.getAmount(), new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date(e.getDate())), e.getNote()));
                }
                writer.flush();
                writer.close();

                PdfDocument pdf = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
                PdfDocument.Page page = pdf.startPage(pageInfo);
                android.graphics.Canvas canvas = page.getCanvas();
                Paint paint = new Paint();

                paint.setColor(Color.parseColor("#0A1128"));
                canvas.drawRect(0, 0, 595, 842, paint);

                paint.setColor(Color.parseColor("#D4AF37"));
                paint.setTextSize(20f);
                paint.setFakeBoldText(true);
                canvas.drawText("BUDGETMATE FINANCIAL REPORT", 40, 60, paint);

                paint.setColor(Color.WHITE);
                paint.setTextSize(12f);
                paint.setFakeBoldText(false);
                canvas.drawText("Export Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date()), 40, 90, paint);
                canvas.drawText("Period Mode: " + (isMonthly ? "Monthly Analytics" : "Annual Analytics"), 40, 110, paint);

                paint.setColor(Color.parseColor("#111827"));
                canvas.drawRect(40, 140, 555, 220, paint);

                paint.setColor(Color.parseColor("#34D399"));
                canvas.drawText("TOTAL INFLOW (INCOME): " + tvTotalIncome.getText(), 60, 175, paint);
                paint.setColor(Color.parseColor("#F87171"));
                canvas.drawText("TOTAL OUTFLOW (EXPENSES): " + tvTotalExpense.getText(), 60, 200, paint);

                pdf.finishPage(page);
                File pdfFile = new File(targetDir, "BudgetReport_" + timestamp + ".pdf");
                pdf.writeTo(new FileOutputStream(pdfFile));
                pdf.close();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "✅ Exported PDF & CSV successfully to Downloads!", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "❌ Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}