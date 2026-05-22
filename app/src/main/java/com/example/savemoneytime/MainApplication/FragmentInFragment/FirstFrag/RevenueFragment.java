package com.example.savemoneytime.MainApplication.FragmentInFragment.FirstFrag;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.savemoneytime.MainApplication.Adapters.CategoryAdapter;
import com.example.savemoneytime.R;
import com.example.savemoneytime.database.AppDatabase;
import com.example.savemoneytime.model.RevenueEntity;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RevenueFragment extends Fragment {

    private TextView   tvAmountDisplay;
    private TextView   tvSelectedDate;
    private EditText   edtTitle;
    private EditText   edtNote;
    private RecyclerView rvCategories;

    private final StringBuilder amountBuffer    = new StringBuilder();
    private String              selectedCategory = "";
    private final Calendar      customCalendar   = Calendar.getInstance();
    private AppDatabase         db;

    private final List<String> revenueCategories = Arrays.asList(
            "💼 Salary", "💰 Freelance", "📈 Investment", "🏦 Savings",
            "🎁 Gift", "🏠 Rental", "💳 Refund", "➕ Other"
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_revenue, container, false);
        db = AppDatabase.getInstance(requireContext());

        tvAmountDisplay = view.findViewById(R.id.tv_amount_display);
        tvSelectedDate  = view.findViewById(R.id.tv_selected_date);
        edtTitle        = view.findViewById(R.id.edt_title);
        edtNote         = view.findViewById(R.id.edt_note);
        rvCategories    = view.findViewById(R.id.rv_categories);

        View btnPrev = view.findViewById(R.id.btn_prev_day);
        View btnNext = view.findViewById(R.id.btn_next_day);
        if (btnPrev != null) btnPrev.setVisibility(View.GONE);
        if (btnNext != null) btnNext.setVisibility(View.GONE);

        updateDateDisplay();

        tvSelectedDate.setOnClickListener(v -> openCalendarDialog());

        CategoryAdapter adapter = new CategoryAdapter(revenueCategories, name -> selectedCategory = name);
        rvCategories.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(adapter);

        setupNumpad(view);

        return view;
    }

    private void openCalendarDialog() {
        Locale.setDefault(Locale.ENGLISH);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = Locale.ENGLISH;
        if (getActivity() != null && getActivity().getResources() != null) {
            getActivity().getResources().updateConfiguration(config, getActivity().getResources().getDisplayMetrics());
        }

        DatePickerDialog picker = new DatePickerDialog(
                requireActivity(),
                R.style.CustomDatePickerTheme,
                (view, year, month, dayOfMonth) -> {
                    customCalendar.set(Calendar.YEAR, year);
                    customCalendar.set(Calendar.MONTH, month);
                    customCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                },
                customCalendar.get(Calendar.YEAR),
                customCalendar.get(Calendar.MONTH),
                customCalendar.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH);
        tvSelectedDate.setText(sdf.format(customCalendar.getTime()));
    }

    private void setupNumpad(View view) {
        int[] numBtnIds = {
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3,
                R.id.btn_4, R.id.btn_5, R.id.btn_6, R.id.btn_7,
                R.id.btn_8, R.id.btn_9
        };
        for (int i = 0; i < numBtnIds.length; i++) {
            final String digit = String.valueOf(i);
            view.findViewById(numBtnIds[i]).setOnClickListener(v -> {
                if (amountBuffer.length() < 12) {
                    amountBuffer.append(digit);
                    refreshAmountDisplay();
                }
            });
        }

        view.findViewById(R.id.btn_del).setOnClickListener(v -> {
            if (amountBuffer.length() > 0) {
                amountBuffer.deleteCharAt(amountBuffer.length() - 1);
                refreshAmountDisplay();
            }
        });

        view.findViewById(R.id.btn_save).setOnClickListener(v -> saveRevenue());
    }

    private void refreshAmountDisplay() {
        if (amountBuffer.length() == 0) {
            tvAmountDisplay.setText("0");
            return;
        }
        long value = Long.parseLong(amountBuffer.toString());
        tvAmountDisplay.setText(String.format(Locale.US, "%,d", value));
    }

    private void saveRevenue() {
        String title = edtTitle.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(requireContext(), "Please enter a description", Toast.LENGTH_SHORT).show();
            return;
        }
        if (amountBuffer.length() == 0 || Long.parseLong(amountBuffer.toString()) == 0) {
            Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(selectedCategory)) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        long amount = Long.parseLong(amountBuffer.toString());
        String note = edtNote.getText().toString().trim();

        RevenueEntity revenue = new RevenueEntity(
                title, amount, selectedCategory, customCalendar.getTimeInMillis(), note
        );

        AppDatabase.dbExecutor.execute(() -> {
            db.revenueDao().insert(revenue);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "✅ Income saved!", Toast.LENGTH_SHORT).show();
                    resetForm();
                });
            }
        });
    }

    private void resetForm() {
        amountBuffer.setLength(0);
        tvAmountDisplay.setText("0");
        edtTitle.setText("");
        edtNote.setText("");
        selectedCategory = "";
        customCalendar.setTimeInMillis(System.currentTimeMillis());
        updateDateDisplay();
    }
}