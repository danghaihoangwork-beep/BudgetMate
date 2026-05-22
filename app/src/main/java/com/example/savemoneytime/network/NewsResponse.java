package com.example.savemoneytime.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsResponse {

    @SerializedName("totalArticles")
    private int totalArticles;

    @SerializedName("articles")
    private List<NewsArticle> articles;

    public List<NewsArticle> getArticles() {
        return articles;
    }

    public int getTotalArticles() {
        return totalArticles;
    }
}