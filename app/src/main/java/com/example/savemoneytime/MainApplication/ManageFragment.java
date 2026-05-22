package com.example.savemoneytime.MainApplication;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savemoneytime.MainApplication.ViewModels.BudgetViewModel;
import com.example.savemoneytime.R;
import com.example.savemoneytime.model.TransactionItem;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageFragment extends Fragment {

    private BudgetViewModel viewModel;
    private RecyclerView rvTransactions;
    private AdvancedTransactionAdapter adapter;

    private LinearLayout filterAll, filterExpenses, filterIncome;
    private TextView filterAllCount, filterExpensesCount, filterIncomeCount;
    private TextView tvCurrentMonthTitle;

    private int currentFilterMode = 0; // 0: All, 1: Expenses, 2: Income
    private List<TransactionItem> rawListFromDb = new ArrayList<>();

    private int selectedMonth;
    private int selectedYear;
    private String currentMonthStr, currentYearStr;

    private final String[] MONTH_LABELS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Calendar c = Calendar.getInstance();
        selectedMonth = c.get(Calendar.MONTH);
        selectedYear = c.get(Calendar.YEAR);

        viewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);
        rvTransactions = view.findViewById(R.id.rv_transactions);

        filterAll = view.findViewById(R.id.filter_all);
        filterExpenses = view.findViewById(R.id.filter_expenses);
        filterIncome = view.findViewById(R.id.filter_income);

        filterAllCount = view.findViewById(R.id.filter_all_count);
        filterExpensesCount = view.findViewById(R.id.filter_expenses_count);
        filterIncomeCount = view.findViewById(R.id.filter_income_count);

        tvCurrentMonthTitle = view.findViewById(R.id.tv_current_month_title);

        View btnMonthSelector = view.findViewById(R.id.btn_month_selector);
        if (btnMonthSelector != null) {
            btnMonthSelector.setOnClickListener(v -> showMonthYearWheelPickerDialog());
        }

        updateMonthYearDisplayTitle();
        setupFilterListeners();

        adapter = new AdvancedTransactionAdapter();
        if (rvTransactions != null) {
            rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTransactions.setAdapter(adapter);
        }

        observeViewModel();
    }

    private void updateMonthYearDisplayTitle() {
        if (tvCurrentMonthTitle != null) {
            tvCurrentMonthTitle.setText(MONTH_LABELS[selectedMonth] + " " + selectedYear);
        }
        currentMonthStr = String.format(Locale.US, "%02d", selectedMonth + 1);
        currentYearStr = String.valueOf(selectedYear);
    }

    private void showMonthYearWheelPickerDialog() {
        if (getContext() == null) return;

        LinearLayout dialogContainer = new LinearLayout(getContext());
        dialogContainer.setOrientation(LinearLayout.HORIZONTAL);
        dialogContainer.setGravity(android.view.Gravity.CENTER);
        dialogContainer.setPadding(40, 40, 40, 20);

        final NumberPicker monthPicker = new NumberPicker(getContext());
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(MONTH_LABELS);
        monthPicker.setValue(selectedMonth);
        monthPicker.setWrapSelectorWheel(false);

        final NumberPicker yearPicker = new NumberPicker(getContext());
        yearPicker.setMinValue(2020);
        yearPicker.setMaxValue(2035);
        yearPicker.setValue(selectedYear);
        yearPicker.setWrapSelectorWheel(false);

        LinearLayout.LayoutParams pickerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pickerParams.setMargins(45, 0, 45, 0);
        monthPicker.setLayoutParams(pickerParams);
        yearPicker.setLayoutParams(pickerParams);

        dialogContainer.addView(monthPicker);
        dialogContainer.addView(yearPicker);

        AlertDialog dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Select Month & Year")
                .setView(dialogContainer)
                .setPositiveButton("OK", (dialogInterface, which) -> {
                    selectedMonth = monthPicker.getValue();
                    selectedYear = yearPicker.getValue();
                    updateMonthYearDisplayTitle();
                    refreshBalanceData();
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFD4AF37"));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FF9CA3AF"));
    }

    private void setupFilterListeners() {
        View.OnClickListener clickListener = v -> {
            int id = v.getId();
            if (id == R.id.filter_all) {
                currentFilterMode = 0;
            } else if (id == R.id.filter_expenses) {
                currentFilterMode = 1;
            } else if (id == R.id.filter_income) {
                currentFilterMode = 2;
            }
            processAndGroupData(rawListFromDb);
        };

        if (filterAll != null) filterAll.setOnClickListener(clickListener);
        if (filterExpenses != null) filterExpenses.setOnClickListener(clickListener);
        if (filterIncome != null) filterIncome.setOnClickListener(clickListener);
    }

    private void updateFilterPillsUI(int expSize, int incSize) {
        if (getContext() == null || filterAll == null) return;

        if (filterAllCount != null) filterAllCount.setText(String.valueOf(expSize + incSize));
        if (filterExpensesCount != null) filterExpensesCount.setText(String.valueOf(expSize));
        if (filterIncomeCount != null) filterIncomeCount.setText(String.valueOf(incSize));

        resetPillState(filterAll, filterAllCount, 0);
        resetPillState(filterExpenses, filterExpensesCount, 1);
        resetPillState(filterIncome, filterIncomeCount, 2);

        if (currentFilterMode == 0) {
            setActivePillState(filterAll, filterAllCount);
        } else if (currentFilterMode == 1) {
            setActivePillState(filterExpenses, filterExpensesCount);
        } else if (currentFilterMode == 2) {
            setActivePillState(filterIncome, filterIncomeCount);
        }
    }

    private void setActivePillState(LinearLayout pill, TextView count) {
        pill.setBackgroundResource(R.drawable.bg_chip_active);
        TextView title = (TextView) pill.getChildAt(0);
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.bm_ink));
        if (count != null) {
            count.setTextColor(ContextCompat.getColor(requireContext(), R.color.bm_ink));
            count.setAlpha(0.7f);
        }
    }

    private void resetPillState(LinearLayout pill, TextView count, int type) {
        pill.setBackgroundResource(R.drawable.bg_chip_idle);
        TextView title = (TextView) pill.getChildAt(0);
        title.setTextColor(ContextCompat.getColor(requireContext(), R.color.bm_text_dim));
        if (count != null) {
            count.setTextColor(ContextCompat.getColor(requireContext(), type == 0 ? R.color.bm_ink : R.color.bm_muted_2));
            count.setAlpha(1.0f);
        }
    }

    private void observeViewModel() {
        viewModel.getTransactions().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                rawListFromDb = items;
                processAndGroupData(items);
            }
        });
        refreshBalanceData();
    }

    private void refreshBalanceData() {
        if (viewModel == null) return;
        viewModel.loadTransactions(currentMonthStr, currentYearStr);
    }

    private void processAndGroupData(List<TransactionItem> originalList) {
        if (originalList == null) return;

        List<Object> flattenedList = new ArrayList<>();
        long totalIncome = 0;
        long totalExpense = 0;
        int expCount = 0;
        int incCount = 0;

        List<TransactionItem> filteredList = new ArrayList<>();
        for (TransactionItem item : originalList) {
            if (item.isExpense()) {
                totalExpense += item.getAmount();
                expCount++;
                if (currentFilterMode == 0 || currentFilterMode == 1) filteredList.add(item);
            } else {
                totalIncome += item.getAmount();
                incCount++;
                if (currentFilterMode == 0 || currentFilterMode == 2) filteredList.add(item);
            }
        }

        updateFilterPillsUI(expCount, incCount);

        DashboardItem dashboard = new DashboardItem(totalIncome, totalExpense);
        flattenedList.add(dashboard);

        Map<String, List<TransactionItem>> groupedMap = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.US);

        for (TransactionItem item : filteredList) {
            String dateKey = sdf.format(new Date(item.getDate())).toUpperCase();
            if (!groupedMap.containsKey(dateKey)) {
                groupedMap.put(dateKey, new ArrayList<>());
            }
            groupedMap.get(dateKey).add(item);
        }

        for (Map.Entry<String, List<TransactionItem>> entry : groupedMap.entrySet()) {
            long dailyNet = 0;
            for (TransactionItem t : entry.getValue()) {
                if (t.isExpense()) dailyNet -= t.getAmount();
                else dailyNet += t.getAmount();
            }
            flattenedList.add(new DateHeaderItem(entry.getKey(), dailyNet));
            flattenedList.addAll(entry.getValue());
        }

        adapter.setItems(flattenedList);
    }

    // ─────────────────────────────────────────────────────────────
    // ADAPTER ĐỒNG BỘ TRỌN VẸN
    // ─────────────────────────────────────────────────────────────
    private class AdvancedTransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_DASHBOARD   = 0;
        private static final int TYPE_DATE_HEADER = 1;
        private static final int TYPE_TRANSACTION = 2;

        private final List<Object> dataset = new ArrayList<>();

        // 🔥 FIX CHÍ MẠNG: Ép cứng chuẩn Mỹ (Locale.US) để ép xuất hiện dấu phẩy (,) hàng nghìn
        private final DecimalFormat formatter = new DecimalFormat("#,##0", new DecimalFormatSymbols(Locale.US));

        public void setItems(List<Object> newList) {
            dataset.clear();
            dataset.addAll(newList);
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            Object obj = dataset.get(position);
            if (obj instanceof DashboardItem) return TYPE_DASHBOARD;
            if (obj instanceof DateHeaderItem) return TYPE_DATE_HEADER;
            return TYPE_TRANSACTION;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inf = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_DASHBOARD) {
                View v = inf.inflate(R.layout.item_dashboard_card, parent, false);
                return new DashboardVH(v);
            } else if (viewType == TYPE_DATE_HEADER) {
                View v = inf.inflate(R.layout.item_month_header, parent, false);
                return new DateHeaderVH(v);
            } else {
                View v = inf.inflate(R.layout.item_transaction, parent, false);
                return new TransactionVH(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Object data = dataset.get(position);

            if (holder instanceof DashboardVH) {
                DashboardVH dvh = (DashboardVH) holder;
                DashboardItem db = (DashboardItem) data;
                long netFlow = db.income - db.expense;

                dvh.tvNetFlow.setText((netFlow >= 0 ? "+$" : "-$") + formatter.format(Math.abs(netFlow)));
                dvh.tvNetFlow.setTextColor(netFlow >= 0 ? Color.parseColor("#FF34D399") : Color.parseColor("#FFF87171"));

                long dailyAvg = db.expense / 30;
                dvh.tvAvgPerDay.setText("$" + formatter.format(dailyAvg));

                long burnRate = db.income > 0 ? (db.expense * 100 / db.income) : 0;
                dvh.tvBurnRate.setText(burnRate + "%");

                dvh.tvSaved.setText("$" + formatter.format(Math.abs(netFlow)));
                dvh.tvSaved.setTextColor(netFlow >= 0 ? Color.parseColor("#FF34D399") : Color.parseColor("#FF9CA3AF"));

            } else if (holder instanceof DateHeaderVH) {
                DateHeaderVH hvh = (DateHeaderVH) holder;
                DateHeaderItem item = (DateHeaderItem) data;
                hvh.tvDateHeader.setText(item.dateText);

                hvh.tvDailyNet.setTextColor(ContextCompat.getColor(hvh.itemView.getContext(),
                        item.netAmount >= 0 ? R.color.bm_income : R.color.bm_expense));
                hvh.tvDailyNet.setText((item.netAmount >= 0 ? "+$" : "−$") + formatter.format(Math.abs(item.netAmount)));

            } else if (holder instanceof TransactionVH) {
                TransactionVH tvh = (TransactionVH) holder;
                TransactionItem t = (TransactionItem) data;

                tvh.tvTitle.setText(t.getTitle());
                tvh.tvCategory.setText(t.getCategoryName());

                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
                tvh.tvDate.setText(timeFormat.format(new Date(t.getDate())));

                tvh.tvIcon.setText(getEmojiForCategory(t.getCategoryName() != null ? t.getCategoryName() : t.getTitle()));

                if (t.isExpense()) {
                    tvh.tvAmount.setText("−$" + formatter.format(t.getAmount()));
                    tvh.tvAmount.setTextColor(ContextCompat.getColor(tvh.itemView.getContext(), R.color.bm_expense));
                } else {
                    tvh.tvAmount.setText("+$" + formatter.format(t.getAmount()));
                    tvh.tvAmount.setTextColor(ContextCompat.getColor(tvh.itemView.getContext(), R.color.bm_income));
                }

                tvh.itemView.setOnClickListener(v -> showOptionsDialog(t));
            }
        }

        private void showOptionsDialog(TransactionItem item) {
            if (getContext() == null) return;

            new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Dialog_Alert)
                    .setTitle("Manage Transaction")
                    .setMessage("Do you want to delete \"" + item.getTitle() + "\"?")
                    .setPositiveButton("DELETE", (dialog, which) -> {
                        viewModel.deleteTransaction(item, currentMonthStr, currentYearStr);
                        Toast.makeText(getContext(), "Transaction Deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("CANCEL", null)
                    .show()
                    .getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FFF87171"));
        }

        private String getEmojiForCategory(String title) {
            if (title == null) return "📝";
            String lower = title.toLowerCase();
            if (lower.contains("food") || lower.contains("lunch") || lower.contains("eat") || lower.contains("mi")) return "🍜";
            if (lower.contains("uber") || lower.contains("transport") || lower.contains("car") || lower.contains("xe")) return "🚗";
            if (lower.contains("house") || lower.contains("housing") || lower.contains("rent")) return "🏠";
            if (lower.contains("health") || lower.contains("pill") || lower.contains("medic")) return "💊";
            if (lower.contains("salary") || lower.contains("income") || lower.contains("luong")) return "💰";
            if (lower.contains("shop") || lower.contains("buy")) return "🛍️";
            return "🧾";
        }

        @Override
        public int getItemCount() { return dataset.size(); }

        class DashboardVH extends RecyclerView.ViewHolder {
            TextView tvNetFlow, tvAvgPerDay, tvBurnRate, tvSaved;
            DashboardVH(View v) {
                super(v);
                tvNetFlow = v.findViewById(R.id.tv_net_flow);
                tvAvgPerDay = v.findViewById(R.id.tv_avg_per_day);
                tvBurnRate = v.findViewById(R.id.tv_burn_rate);
                tvSaved = v.findViewById(R.id.tv_saved);
            }
        }

        class DateHeaderVH extends RecyclerView.ViewHolder {
            TextView tvDateHeader, tvDailyNet;
            DateHeaderVH(View v) {
                super(v);
                tvDateHeader = v.findViewById(R.id.tv_date_header);
                tvDailyNet = v.findViewById(R.id.tv_daily_net);
            }
        }

        class TransactionVH extends RecyclerView.ViewHolder {
            TextView tvIcon, tvTitle, tvCategory, tvDate, tvAmount;
            TransactionVH(View v) {
                super(v);
                tvIcon = v.findViewById(R.id.tv_trans_icon);
                tvTitle = v.findViewById(R.id.tv_trans_title);
                tvCategory = v.findViewById(R.id.tv_trans_category);
                tvDate = v.findViewById(R.id.tv_trans_date);
                tvAmount = v.findViewById(R.id.tv_trans_amount);
            }
        }
    }

    private static class DashboardItem {
        long income, expense;
        DashboardItem(long i, long e) { this.income = i; this.expense = e; }
    }

    private static class DateHeaderItem {
        String dateText; long netAmount;
        DateHeaderItem(String t, long n) { this.dateText = t; this.netAmount = n; }
    }
}