package com.example.savemoneytime.MainApplication.Repository;

import android.app.Application;
import com.example.savemoneytime.database.AppDatabase;
import com.example.savemoneytime.database.ExpenseDao;
import com.example.savemoneytime.database.RevenueDao;
import com.example.savemoneytime.model.ExpenseEntity;
import com.example.savemoneytime.model.RevenueEntity;
import com.example.savemoneytime.network.NewsApiService;
import com.example.savemoneytime.network.NewsResponse;
import com.example.savemoneytime.network.RetrofitClient;
import java.util.List;
import java.util.concurrent.ExecutorService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AppRepository {

    private final ExpenseDao  expenseDao;
    private final RevenueDao  revenueDao;
    private final ExecutorService executor;

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public AppRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        expenseDao  = db.expenseDao();
        revenueDao  = db.revenueDao();
        executor    = AppDatabase.dbExecutor;
    }

    // ── Expense ──────────────────────────────────────────
    public void insertExpense(ExpenseEntity expense, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                expenseDao.insert(expense);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void deleteExpenseById(int id, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                expenseDao.deleteById(id);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void getExpensesByMonth(String month, String year, DataCallback<List<ExpenseEntity>> callback) {
        executor.execute(() -> {
            try {
                List<ExpenseEntity> list = expenseDao.getByMonth(month, year);
                callback.onSuccess(list);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void getTotalExpenseByMonth(String month, String year, DataCallback<Long> callback) {
        executor.execute(() -> {
            try {
                long total = expenseDao.getTotalByMonth(month, year);
                callback.onSuccess(total);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void getAllExpenses(DataCallback<List<ExpenseEntity>> callback) {
        executor.execute(() -> {
            try {
                List<ExpenseEntity> list = expenseDao.getAll();
                callback.onSuccess(list);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    // ── Revenue ───────────────────────────────────────────
    public void insertRevenue(RevenueEntity revenue, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                revenueDao.insert(revenue);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void deleteRevenueById(int id, DataCallback<Void> callback) {
        executor.execute(() -> {
            try {
                revenueDao.deleteById(id);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e.getMessage());
            }
        });
    }

    public void getRevenuesByMonth(String month, String year, DataCallback<List<RevenueEntity>> callback) {
        executor.execute(() -> {
            try {
                List<RevenueEntity> list = revenueDao.getByMonth(month, year);
                callback.onSuccess(list);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void getTotalRevenueByMonth(String month, String year, DataCallback<Long> callback) {
        executor.execute(() -> {
            try {
                long total = revenueDao.getTotalByMonth(month, year);
                callback.onSuccess(total);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    public void getAllRevenues(DataCallback<List<RevenueEntity>> callback) {
        executor.execute(() -> {
            try {
                List<RevenueEntity> list = revenueDao.getAll();
                callback.onSuccess(list);
            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        });
    }

    // ── News API ──────────────────────────────────────────
    public void fetchNews(String query, String lang, int max, String apiKey, DataCallback<NewsResponse> callback) {
        NewsApiService service = RetrofitClient.getNewsService();
        service.getFinanceNews(query, lang, max, apiKey).enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("API error: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}