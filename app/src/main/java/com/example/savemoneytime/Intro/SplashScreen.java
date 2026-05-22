package com.example.savemoneytime.Intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import com.example.savemoneytime.Intro.IntroViewPager.IntroViewPager;
import com.example.savemoneytime.MainApplication.MainActivity;
import com.example.savemoneytime.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khóa Fullscreen tràn màn hình
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_splash_screen);

        // Chờ 2.5 giây điều hướng thông minh sang Intro hoặc Trang chủ
        new Handler().postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("BudgetMatePrefs", MODE_PRIVATE);
            boolean introSeen = prefs.getBoolean("introSeen", false);

            Intent intent;
            if (!introSeen) {
                intent = new Intent(SplashScreen.this, IntroViewPager.class);
            } else {
                intent = new Intent(SplashScreen.this, MainActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2500);
    }
}