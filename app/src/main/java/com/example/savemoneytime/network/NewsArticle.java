package com.example.savemoneytime.network;

import com.google.gson.annotations.SerializedName;

public class NewsArticle {

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("url")
    private String url;

    @SerializedName("image")
    private String imageUrl;

    @SerializedName("publishedAt")
    private String publishedAt;

    @SerializedName("source")
    private Source source;

    public String getTitle() {
        return title != null ? title : "";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public String getUrl() {
        return url != null ? url : "";
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "";
    }

    public String getPublishedAt() {
        return publishedAt != null ? publishedAt : "";
    }

    public String getSourceName() {
        return (source != null && source.name != null) ? source.name : "Unknown";
    }

    public static class Source {
        @SerializedName("name")
        public String name;

        @SerializedName("url")
        public String url;
    }
}