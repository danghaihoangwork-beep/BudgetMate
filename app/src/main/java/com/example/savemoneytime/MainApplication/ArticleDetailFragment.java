package com.example.savemoneytime.MainApplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.savemoneytime.R;

public class ArticleDetailFragment extends Fragment {

    private static final String ARG_URL   = "article_url";
    private static final String ARG_TITLE = "article_title";

    private WebView     webView;
    private ProgressBar webProgress;
    private TextView    tvToolbarTitle;
    private ImageView   btnBack, btnShare, btnOpenBrowser;

    private String articleUrl;
    private String articleTitle;

    public static ArticleDetailFragment newInstance(String url, String title) {
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            articleUrl   = getArguments().getString(ARG_URL, "");
            articleTitle = getArguments().getString(ARG_TITLE, "Article");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article_detail, container, false);

        webView       = view.findViewById(R.id.web_view);
        webProgress   = view.findViewById(R.id.web_progress);
        tvToolbarTitle = view.findViewById(R.id.tv_article_toolbar_title);
        btnBack        = view.findViewById(R.id.btn_back);
        btnShare       = view.findViewById(R.id.btn_share);
        btnOpenBrowser = view.findViewById(R.id.btn_open_browser);

        tvToolbarTitle.setText(articleTitle);

        setupWebView();
        setupButtons();

        if (!articleUrl.isEmpty()) {
            webView.loadUrl(articleUrl);
        }

        return view;
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                webProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webProgress.setVisibility(View.GONE);
                injectDarkMode(view); // Ép website biến thành nền đen chữ trắng
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                webProgress.setProgress(newProgress);
                webProgress.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void injectDarkMode(WebView view) {
        String css = "body { background-color: #0A1128 !important; color: #E0E6ED !important; } "
                + "a { color: #D4AF37 !important; } "
                + "p, span, div { color: #E0E6ED !important; } "
                + "h1, h2, h3 { color: #FFFFFF !important; }";
        String js = "javascript:(function() {"
                + "var style = document.createElement('style');"
                + "style.innerHTML = '" + css + "';"
                + "document.head.appendChild(style);"
                + "})()";
        view.loadUrl(js);
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                }
            }
        });

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, articleTitle);
            shareIntent.putExtra(Intent.EXTRA_TEXT, articleTitle + "\n\n" + articleUrl);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        btnOpenBrowser.setOnClickListener(v -> {
            if (!articleUrl.isEmpty()) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl)));
            }
        });
    }

    @Override
    public void onDestroyView() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }
        super.onDestroyView();
    }
}