package com.ramm;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppSettings"; // Unified name
    private static final String KEY_THEME_MODE = "theme_mode"; // Unified key

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedMode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO);

        // Set global night mode early in app lifecycle
        AppCompatDelegate.setDefaultNightMode(savedMode);

        super.attachBaseContext(newBase);
    }
}