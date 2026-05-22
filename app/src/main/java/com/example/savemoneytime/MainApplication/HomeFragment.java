package com.example.savemoneytime.MainApplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.savemoneytime.MainApplication.ViewModels.BudgetViewModel;
import com.example.savemoneytime.R;
import com.example.savemoneytime.model.ExpenseEntity;
import com.example.savemoneytime.model.RevenueEntity;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private BudgetViewModel viewModel;

    // Giao diện thẻ tổng quát số dư thật
    private TextView tvTotalBalance, tvTotalIncome, tvTotalExpenses;

    // Giao diện nhập liệu và nhãn tiêu đề đổi chữ
    private TextView tvAmountDisplay, tvSelectedDate, tvSeeAll, tvSpendingLabel;
    private TextView tabExpenses, tabIncome;
    private EditText etDescription;
    private LinearLayout layoutCategories, layoutDatePicker;

    // Biến trạng thái
    private String currentAmountInput = "";
    private boolean isExpenseTab = true;
    private String selectedCategory = "Food";
    private Calendar selectedCalendar;
    private String currentMonth, currentYear;

    // 🔥 FIX CHÍ MẠNG: Ép cứng chuẩn Mỹ (Locale.US) để ép xuất hiện dấu phẩy (,) hàng nghìn
    private final DecimalFormat formatter = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.US));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH);

    private final String[] MONTH_LABELS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    // CẤU HÌNH DANH MỤC RIÊNG BIỆT CHO 2 TAB THU / CHI KHÁC NHAU
    private final String[] EXPENSE_CATEGORIES = {"🍜  Food", "🚗  Transport", "🏠  Housing", "💊  Health", "🛍️  Shopping", "🧾  Bills"};
    private final String[] INCOME_CATEGORIES  = {"💰  Salary", "💼  Business", "🎁  Bonus", "📈  Investment", "💵  Allowance", "🪙  Others"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);
        selectedCalendar = Calendar.getInstance();

        updateCurrentDateQueryStrings();

        // Ánh xạ các thẻ tổng quan dữ liệu
        tvTotalBalance = view.findViewById(R.id.tv_total_balance);
        tvTotalIncome = view.findViewById(R.id.tv_total_income);
        tvTotalExpenses = view.findViewById(R.id.tv_total_expenses);

        // Ánh xạ thành phần nhập liệu
        tvSpendingLabel = view.findViewById(R.id.tv_spending_label);
        tvAmountDisplay = view.findViewById(R.id.tv_amount_display);
        tvSelectedDate = view.findViewById(R.id.tv_selected_date);
        tvSeeAll = view.findViewById(R.id.tv_see_all);
        tabExpenses = view.findViewById(R.id.tab_expenses);
        tabIncome = view.findViewById(R.id.tab_income);
        etDescription = view.findViewById(R.id.et_description);
        layoutCategories = view.findViewById(R.id.layout_categories);
        layoutDatePicker = view.findViewById(R.id.layout_date_picker);

        setupKeypad(view);
        setupTabs();
        setupCategories();
        setupDatePicker();
        setupSeeAllLink();

        registerLiveObservers();

        refreshDatabaseData();
        updateAmountDisplay();
        tvSelectedDate.setText(dateFormat.format(selectedCalendar.getTime()));

        // Cập nhật danh sách chip danh mục ban đầu (Mặc định là Expense)
        updateCategoryChipsUI();
    }

    private void updateCurrentDateQueryStrings() {
        int monthNumerical = selectedCalendar.get(Calendar.MONTH) + 1;
        currentMonth = String.format(Locale.US, "%02d", monthNumerical);
        currentYear = String.format(Locale.US, "%d", selectedCalendar.get(Calendar.YEAR));
    }

    private void refreshDatabaseData() {
        if (viewModel != null) {
            viewModel.loadTransactions(currentMonth, currentYear);
        }
    }

    private void registerLiveObservers() {
        viewModel.getBalance().observe(getViewLifecycleOwner(), balance -> {
            if (balance != null) tvTotalBalance.setText("$" + formatter.format(balance));
        });
        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), income -> {
            if (income != null) tvTotalIncome.setText("$" + formatter.format(income));
        });
        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> {
            if (expense != null) tvTotalExpenses.setText("$" + formatter.format(expense));
        });

        viewModel.getSaveStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null && status.startsWith("✅")) {
                refreshDatabaseData();
            }
        });
    }

    // CƠ CHẾ THAY ĐỔI RUỘT CHỮ DANH MỤC ĐỘNG THEO THỜI GIAN THỰC
    private void updateCategoryChipsUI() {
        int childCount = layoutCategories.getChildCount();
        String[] targetSource = isExpenseTab ? EXPENSE_CATEGORIES : INCOME_CATEGORIES;

        int index = 0;
        for (int i = 0; i < childCount; i++) {
            View child = layoutCategories.getChildAt(i);
            if (child instanceof TextView) {
                TextView chip = (TextView) child;
                if (index < targetSource.length) {
                    chip.setText(targetSource[index]);
                    index++;
                }

                // Trả Chip đầu tiên (vị trí 0) về trạng thái Active nổi bật, các chip còn lại ở dạng Idle xám
                if (i == 0) {
                    chip.setBackgroundResource(R.drawable.bg_chip_active);
                    chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.bm_ink));
                    selectedCategory = chip.getText().toString().replaceAll("[^a-zA-Z ]", "").trim();
                } else {
                    chip.setBackgroundResource(R.drawable.bg_chip_idle);
                    chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.bm_text_dim));
                }
            }
        }
    }

    private void setupSeeAllLink() {
        tvSeeAll.setOnClickListener(v -> {
            try {
                View vp = null;
                int resIdCamel = getResources().getIdentifier("viewPager", "id", requireActivity().getPackageName());
                int resIdLower = getResources().getIdentifier("viewpager", "id", requireActivity().getPackageName());
                int resIdSnake = getResources().getIdentifier("view_pager", "id", requireActivity().getPackageName());

                if (resIdCamel != 0) vp = requireActivity().findViewById(resIdCamel);
                if (vp == null && resIdLower != 0) vp = requireActivity().findViewById(resIdLower);
                if (vp == null && resIdSnake != 0) vp = requireActivity().findViewById(resIdSnake);

                if (vp instanceof androidx.viewpager2.widget.ViewPager2) {
                    ((androidx.viewpager2.widget.ViewPager2) vp).setCurrentItem(1, true);
                } else if (vp instanceof androidx.viewpager.widget.ViewPager) {
                    ((androidx.viewpager.widget.ViewPager) vp).setCurrentItem(1, true);
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Navigating...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupKeypad(View view) {
        int[] numberIds = {
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9
        };

        for (int id : numberIds) {
            view.findViewById(id).setOnClickListener(v -> {
                String digit = ((Button) v).getText().toString();
                if (currentAmountInput.equals("0") || currentAmountInput.isEmpty()) {
                    currentAmountInput = digit;
                } else if (currentAmountInput.length() < 12) {
                    currentAmountInput += digit;
                }
                updateAmountDisplay();
            });
        }

        view.findViewById(R.id.btn_clear).setOnClickListener(v -> {
            if (!currentAmountInput.isEmpty()) {
                currentAmountInput = currentAmountInput.substring(0, currentAmountInput.length() - 1);
            }
            updateAmountDisplay();
        });

        view.findViewById(R.id.btn_save).setOnClickListener(v -> saveTransaction());
    }

    private void updateAmountDisplay() {
        if (currentAmountInput.isEmpty()) {
            tvAmountDisplay.setText(isExpenseTab ? "−$0" : "+$0");
            return;
        }
        try {
            long amount = Long.parseLong(currentAmountInput);
            tvAmountDisplay.setText((isExpenseTab ? "−$" : "+$") + formatter.format(amount));
        } catch (NumberFormatException e) {
            tvAmountDisplay.setText(isExpenseTab ? "−$0" : "+$0");
        }
    }

    private void setupTabs() {
        tabExpenses.setOnClickListener(v -> {
            isExpenseTab = true;
            tabExpenses.setBackgroundResource(R.drawable.bg_segment_active_expense);
            tabExpenses.setTextColor(ContextCompat.getColor(requireContext(), R.color.bm_ink));
            tabIncome.setBackgroundResource(R.drawable.bg_segment_inactive);
            tabIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.bm_muted));

            // Đổi nhãn chữ và thay thế danh mục
            if (tvSpendingLabel != null) tvSpendingLabel.setText("SPENDING");
            updateCategoryChipsUI();
            updateAmountDisplay();
        });

        tabIncome.setOnClickListener(v -> {
            isExpenseTab = false;
            tabIncome.setBackgroundResource(R.drawable.bg_segment_active_income);
            tabIncome.setTextColor(ContextCompat.getColor(requireContext(), R.color.bm_ink));
            tabExpenses.setBackgroundResource(R.drawable.bg_segment_inactive);
            tabExpenses.setTextColor(ContextCompat.getColor(requireContext(), R.color.bm_muted));

            // Đổi nhãn chữ và thay thế danh mục
            if (tvSpendingLabel != null) tvSpendingLabel.setText("INCOME");
            updateCategoryChipsUI();
            updateAmountDisplay();
        });
    }

    private void setupCategories() {
        int childCount = layoutCategories.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = layoutCategories.getChildAt(i);
            if (child instanceof TextView) {
                TextView chip = (TextView) child;
                chip.setOnClickListener(v -> {
                    for (int j = 0; j < childCount; j++) {
                        TextView c = (TextView) layoutCategories.getChildAt(j);
                        c.setBackgroundResource(R.drawable.bg_chip_idle);
                        c.setTextColor(ContextCompat.getColor(requireContext(), R.color.bm_text_dim));
                    }
                    chip.setBackgroundResource(R.drawable.bg_chip_active);
                    chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.bm_ink));
                    selectedCategory = chip.getText().toString().replaceAll("[^a-zA-Z ]", "").trim();
                });
            }
        }
    }

    private void setupDatePicker() {
        layoutDatePicker.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    R.style.CustomDatePickerTheme,
                    (viewItem, year, month, dayOfMonth) -> {
                        selectedCalendar.set(year, month, dayOfMonth);
                        tvSelectedDate.setText(dateFormat.format(selectedCalendar.getTime()));

                        updateCurrentDateQueryStrings();
                        refreshDatabaseData();
                    },
                    selectedCalendar.get(Calendar.YEAR),
                    selectedCalendar.get(Calendar.MONTH),
                    selectedCalendar.get(Calendar.DAY_OF_MONTH)
            );
            dialog.show();
        });
    }

    private void saveTransaction() {
        if (currentAmountInput.isEmpty() || currentAmountInput.equals("0")) {
            Toast.makeText(getContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        long amount = Long.parseLong(currentAmountInput);
        String note = etDescription.getText().toString().trim();
        String title = note.isEmpty() ? selectedCategory : note;
        long timestamp = selectedCalendar.getTimeInMillis();

        if (isExpenseTab) {
            ExpenseEntity expense = new ExpenseEntity(title, amount, selectedCategory, timestamp, note);
            viewModel.saveExpense(expense);
        } else {
            RevenueEntity revenue = new RevenueEntity(title, amount, selectedCategory, timestamp, note);
            viewModel.saveRevenue(revenue);
        }

        Toast.makeText(getContext(), "Transaction Saved!", Toast.LENGTH_SHORT).show();

        currentAmountInput = "";
        etDescription.setText("");
        updateAmountDisplay();
    }
}