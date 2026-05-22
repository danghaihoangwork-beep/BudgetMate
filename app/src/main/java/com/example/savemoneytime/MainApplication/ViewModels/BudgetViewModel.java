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

    private final MutableLiveData<Long>    totalIncomeLive  = new MutableLiveData<>(0L);
    private final MutableLiveData<Long>    totalExpenseLive = new MutableLiveData<>(0L);
    private final MutableLiveData<Long>    balanceLive      = new MutableLiveData<>(0L);

    private final MutableLiveData<List<TransactionItem>> transactionsLive = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<NewsArticle>> newsLive = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> newsLoadingLive = new MutableLiveData<>(false);
    private final MutableLiveData<String>  newsErrorLive   = new MutableLiveData<>();

    private final MutableLiveData<String>  saveStatusLive  = new MutableLiveData<>();

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        repository = new AppRepository(application);
    }

    public LiveData<Long>                  getTotalIncome()  { return totalIncomeLive; }
    public LiveData<Long>                  getTotalExpense() { return totalExpenseLive; }
    public LiveData<Long>                  getBalance()      { return balanceLive; }
    public LiveData<List<TransactionItem>> getTransactions() { return transactionsLive; }
    public LiveData<List<NewsArticle>>     getNews()         { return newsLive; }
    public LiveData<Boolean>               getNewsLoading()  { return newsLoadingLive; }
    public LiveData<String>                getNewsError()    { return newsErrorLive; }
    public LiveData<String>                getSaveStatus()   { return saveStatusLive; }

    public void loadBalance(String month, String year) {
        // Hàm này giữ lại để không làm lỗi cấu trúc cũ, nhưng số liệu thật sẽ được đồng bộ trực tiếp từ hàm loadTransactions bên dưới
        repository.getTotalRevenueByMonth(month, year, new AppRepository.DataCallback<Long>() {
            @Override
            public void onSuccess(Long income) {
                // Đã chuyển giao việc gán số cho luồng loadTransactions thống nhất
            }
            @Override public void onError(String message) {}
        });
    }

    public void saveExpense(ExpenseEntity expense) {
        repository.insertExpense(expense, new AppRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Phát tín hiệu thông báo đã ghi file thành công vào Database ngầm
                saveStatusLive.postValue("✅ Expense saved! " + System.currentTimeMillis());
            }
            @Override
            public void onError(String message) {
                saveStatusLive.postValue("❌ Error: " + message);
            }
        });
    }

    public void saveRevenue(RevenueEntity revenue) {
        repository.insertRevenue(revenue, new AppRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                // Phát tín hiệu thông báo đã ghi file thành công vào Database ngầm
                saveStatusLive.postValue("✅ Income saved! " + System.currentTimeMillis());
            }
            @Override
            public void onError(String message) {
                saveStatusLive.postValue("❌ Error: " + message);
            }
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
                            calculatedExpense += e.getAmount(); // Cộng dồn chi tiêu thật
                        }
                        for (RevenueEntity r : revenues) {
                            merged.add(TransactionItem.fromRevenue(r));
                            calculatedIncome += r.getAmount(); // Cộng dồn thu nhập thật
                        }
                        Collections.sort(merged, (a, b) -> Long.compare(b.getDate(), a.getDate()));
                        transactionsLive.postValue(merged);

                        // 🔥 ĐỒNG BỘ TUYỆT ĐỐI: Ép số liệu tổng ở trang Home khớp 100% với danh sách lịch sử thật
                        totalIncomeLive.postValue(calculatedIncome);
                        totalExpenseLive.postValue(calculatedExpense);
                        balanceLive.postValue(calculatedIncome - calculatedExpense);
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
        Boolean currentlyLoading = newsLoadingLive.getValue();
        if (currentlyLoading != null && currentlyLoading) return;

        newsLoadingLive.postValue(true);
        newsErrorLive.postValue(null);

        repository.fetchNews(
                "finance OR stock market OR economy",
                "en",
                8,
                apiKey,
                new AppRepository.DataCallback<NewsResponse>() {
                    @Override
                    public void onSuccess(NewsResponse response) {
                        newsLoadingLive.postValue(false);
                        if (response != null && response.getArticles() != null && !response.getArticles().isEmpty()) {
                            List<NewsArticle> safeCopy = new ArrayList<>(response.getArticles());
                            newsLive.postValue(safeCopy);
                        } else {
                            newsErrorLive.postValue("no_data");
                        }
                    }

                    @Override
                    public void onError(String message) {
                        newsLoadingLive.postValue(false);
                        newsErrorLive.postValue(message != null ? message : "network_error");
                    }
                }
        );
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