package com.example.savemoneytime.MainApplication.ViewModels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.savemoneytime.MainApplication.Repository.AppRepository;
import com.example.savemoneytime.model.ExpenseEntity;
import com.example.savemoneytime.model.RevenueEntity;
import com.example.savemoneytime.model.TransactionItem;
import com.example.savemoneytime.network.NewsArticle;
import com.example.savemoneytime.network.NewsResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BudgetViewModel extends AndroidViewModel {

    private final AppRepository repository;

    // Luồng dữ liệu theo tháng được chọn
    private final MutableLiveData<Long>    totalIncomeLive  = new MutableLiveData<>(0L);
    private final MutableLiveData<Long>    totalExpenseLive = new MutableLiveData<>(0L);
    private final MutableLiveData<Long>    balanceLive      = new MutableLiveData<>(0L);
    private final MutableLiveData<List<TransactionItem>> transactionsLive = new MutableLiveData<>(new ArrayList<>());

    // 🔥 LUỒNG DỮ LIỆU TỔNG TOÀN BỘ LỊCH SỬ (ALL-TIME DATABASE)
    private final MutableLiveData<Long>    allTimeIncomeLive  = new MutableLiveData<>(0L);
    private final MutableLiveData<Long>    allTimeExpenseLive = new MutableLiveData<>(0L);

    private final MutableLiveData<List<NewsArticle>> newsLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> newsLoadingLive = new MutableLiveData<>(false);
    private final MutableLiveData<String>  newsErrorLive   = new MutableLiveData<>();
    private final MutableLiveData<String>  saveStatusLive  = new MutableLiveData<>();

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
    }

    public LiveData<Long>                  getTotalIncome()     { return totalIncomeLive; }
    public LiveData<Long>                  getTotalExpense()    { return totalExpenseLive; }
    public LiveData<Long>                  getBalance()         { return balanceLive; }
    public LiveData<List<TransactionItem>> getTransactions()    { return transactionsLive; }

    // Getter cho biến All-Time mới
    public LiveData<Long>                  getAllTimeIncome()   { return allTimeIncomeLive; }
    public LiveData<Long>                  getAllTimeExpense()  { return allTimeExpenseLive; }

    public LiveData<List<NewsArticle>>     getNews()            { return newsLive; }
    public LiveData<Boolean>               getNewsLoading()     { return newsLoadingLive; }
    public LiveData<String>                getNewsError()       { return newsErrorLive; }
    public LiveData<String>                getSaveStatus()      { return saveStatusLive; }

    public void loadBalance(String month, String year) {
        repository.getTotalRevenueByMonth(month, year, new AppRepository.DataCallback<Long>() {
            @Override public void onSuccess(Long income) {}
            @Override public void onError(String message) {}
        });
    }

    public void saveExpense(ExpenseEntity expense) {
        repository.insertExpense(expense, new AppRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                saveStatusLive.postValue("✅ Expense saved! " + System.currentTimeMillis());
            }
            @Override public void onError(String message) {}
        });
    }

    public void saveRevenue(RevenueEntity revenue) {
        repository.insertRevenue(revenue, new AppRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                saveStatusLive.postValue("✅ Income saved! " + System.currentTimeMillis());
            }
            @Override public void onError(String message) {}
        });
    }

    public void loadTransactions(String month, String year) {
        repository.getExpensesByMonth(month, year, new AppRepository.DataCallback<List<ExpenseEntity>>() {
            @Override
            public void onSuccess(List<ExpenseEntity> expenses) {
                repository.getRevenuesByMonth(month, year, new AppRepository.DataCallback<List<RevenueEntity>>() {
                    @Override
                    public void onSuccess(List<RevenueEntity> revenues) {
                        List<TransactionItem> merged = new ArrayList<>();
                        long calculatedExpense = 0;
                        long calculatedIncome = 0;

                        for (ExpenseEntity e : expenses) {
                            merged.add(TransactionItem.fromExpense(e));
                            calculatedExpense += e.getAmount();
                        }
                        for (RevenueEntity r : revenues) {
                            merged.add(TransactionItem.fromRevenue(r));
                            calculatedIncome += r.getAmount();
                        }
                        Collections.sort(merged, (a, b) -> Long.compare(b.getDate(), a.getDate()));
                        transactionsLive.postValue(merged);

                        totalIncomeLive.postValue(calculatedIncome);
                        totalExpenseLive.postValue(calculatedExpense);
                        balanceLive.postValue(calculatedIncome - calculatedExpense);

                        // Kích hoạt tính toán All-Time song song
                        calculateAllTimeStats();
                    }
                    @Override public void onError(String msg) {}
                });
            }
            @Override public void onError(String msg) {}
        });
    }

    // 🔥 HÀM TÍNH TOÁN ALL-TIME TỔNG TOÀN BỘ DATABASE NGẦM
    private void calculateAllTimeStats() {
        // Gọi dữ liệu thô không điều kiện thời gian để tổng hợp số liệu tổng của toàn bộ vòng đời app
        repository.getExpensesByMonth("", "", new AppRepository.DataCallback<List<ExpenseEntity>>() {
            @Override
            public void onSuccess(List<ExpenseEntity> allExpenses) {
                repository.getRevenuesByMonth("", "", new AppRepository.DataCallback<List<RevenueEntity>>() {
                    @Override
                    public void onSuccess(List<RevenueEntity> allRevenues) {
                        long totalExp = 0;
                        long totalInc = 0;
                        if (allExpenses != null) {
                            for (ExpenseEntity e : allExpenses) totalExp += e.getAmount();
                        }
                        if (allRevenues != null) {
                            for (RevenueEntity r : allRevenues) totalInc += r.getAmount();
                        }
                        allTimeExpenseLive.postValue(totalExp);
                        allTimeIncomeLive.postValue(totalInc);
                    }
                    @Override public void onError(String msg) {}
                });
            }
            @Override public void onError(String msg) {}
        });
    }

    public void deleteTransaction(TransactionItem item, String month, String year) {
        if (item.isExpense()) {
            repository.deleteExpenseById(item.getId(), new AppRepository.DataCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    loadTransactions(month, year);
                }
                @Override public void onError(String msg) {}
            });
        } else {
            repository.deleteRevenueById(item.getId(), new AppRepository.DataCallback<Void>() {
                @Override
                public void onSuccess(Void data) {
                    loadTransactions(month, year);
                }
                @Override public void onError(String msg) {}
            });
        }
    }

    public void loadNews(String apiKey) {
        newsLoadingLive.postValue(true);
        repository.fetchNews("finance", "en", 8, apiKey, new AppRepository.DataCallback<NewsResponse>() {
            @Override
            public void onSuccess(NewsResponse response) {
                newsLoadingLive.postValue(false);
                if (response != null && response.getArticles() != null) {
                    newsLive.postValue(new ArrayList<>(response.getArticles()));
                }
            }
            @Override public void onError(String message) { newsLoadingLive.postValue(false); }
        });
    }

    public void loadStatsData(String month, String year, AppRepository.DataCallback<long[]> callback) {
        repository.getTotalRevenueByMonth(month, year, new AppRepository.DataCallback<Long>() {
            @Override
            public void onSuccess(Long income) {
                repository.getTotalExpenseByMonth(month, year, new AppRepository.DataCallback<Long>() {
                    @Override
                    public void onSuccess(Long expense) {
                        callback.onSuccess(new long[]{ income, expense });
                    }
                    @Override public void onError(String msg) {}
                });
            }
            @Override public void onError(String msg) {}
        });
    }
}