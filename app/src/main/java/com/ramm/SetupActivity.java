package com.ramm;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SetupActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 100;
    private static final String PREF_NAME = "clinic_pref";
    private static final String FIRST_RUN_KEY = "first_run";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isFirstRun()) {
            launchMain();
            return;
        }

        showSplashLayout();

        if (hasAllPermissions()) {
            proceedAfterPermissions();
        } else {
            requestAllPermissions();
        }
    }

    private boolean hasAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                   ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERMISSION_CODE);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, PERMISSION_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, PERMISSION_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_CODE) {
            if (hasAllPermissions()) {
                proceedAfterPermissions();
            } else {
                Toast.makeText(this, "Please allow file access to continue.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == PERMISSION_CODE) {
            if (hasAllPermissions()) {
                proceedAfterPermissions();
            } else {
                Toast.makeText(this, "Storage permission is required to continue.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void proceedAfterPermissions() {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(FIRST_RUN_KEY, false);
        editor.apply();

        new Handler(Looper.getMainLooper()).postDelayed(this::launchMain, 1000);
    }

    private boolean isFirstRun() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return prefs.getBoolean(FIRST_RUN_KEY, true);
    }

    private void launchMain() {
        startActivity(new Intent(this, AdminactivityActivity.class));
        finish();
    }

    private void showSplashLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(0xFFE3F2FD);
        layout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        TextView title = new TextView(this);
        title.setText("Clinic Manager");
        title.setTextSize(24);
        title.setTextColor(0xFF2196F3);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 32);
        layout.addView(title);

        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        layout.addView(progress);

        setContentView(layout);
    }
}