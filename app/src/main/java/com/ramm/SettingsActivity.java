package com.ramm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_THEME_MODE = "theme_mode";

    // Doctor UPI fields
    private EditText etDoctorUpiId, etDoctorUpiName, etDoctorUpiAmount, etDoctorUpiNote;
    // Medicine UPI fields
    private EditText etMedicineUpiId, etMedicineUpiName, etMedicineUpiNote;
    // Theme switch
    private SwitchCompat themeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // View bindings
        etDoctorUpiId = findViewById(R.id.etDoctorUpiId);
        etDoctorUpiName = findViewById(R.id.etDoctorUpiName);
        etDoctorUpiAmount = findViewById(R.id.etDoctorUpiAmount);
        etDoctorUpiNote = findViewById(R.id.etDoctorUpiNote);

        etMedicineUpiId = findViewById(R.id.etMedicineUpiId);
        etMedicineUpiName = findViewById(R.id.etMedicineUpiName);
        etMedicineUpiNote = findViewById(R.id.etMedicineUpiNote);

        themeSwitch = findViewById(R.id.themeSwitch);
        Button btnSave = findViewById(R.id.btnSave);

        // Load existing settings
        loadValues();

        // Save settings
        btnSave.setOnClickListener(v -> saveSettings());

        // Live theme toggle
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int mode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
            AppCompatDelegate.setDefaultNightMode(mode);
        });
    }

    private void loadValues() {
        // Doctor UPI
        etDoctorUpiId.setText(prefs.getString("doctor_upi_id", ""));
        etDoctorUpiName.setText(prefs.getString("doctor_upi_name", ""));
        etDoctorUpiAmount.setText(String.valueOf(prefs.getFloat("doctor_upi_amount", 0f)));
        etDoctorUpiNote.setText(prefs.getString("doctor_upi_note", ""));

        // Medicine UPI
        etMedicineUpiId.setText(prefs.getString("medicine_upi_id", ""));
        etMedicineUpiName.setText(prefs.getString("medicine_upi_name", ""));
        etMedicineUpiNote.setText(prefs.getString("medicine_upi_note", ""));

        // Theme toggle
        int themeMode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO);
        themeSwitch.setChecked(themeMode == AppCompatDelegate.MODE_NIGHT_YES);
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = prefs.edit();

        try {
            editor.putString("doctor_upi_id", etDoctorUpiId.getText().toString().trim());
            editor.putString("doctor_upi_name", etDoctorUpiName.getText().toString().trim());
            editor.putFloat("doctor_upi_amount", parseAmount(etDoctorUpiAmount.getText().toString()));
            editor.putString("doctor_upi_note", etDoctorUpiNote.getText().toString().trim());

            editor.putString("medicine_upi_id", etMedicineUpiId.getText().toString().trim());
            editor.putString("medicine_upi_name", etMedicineUpiName.getText().toString().trim());
            editor.putString("medicine_upi_note", etMedicineUpiNote.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        editor.apply();
        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
    }

    private float parseAmount(String amountStr) throws NumberFormatException {
        amountStr = amountStr.trim();
        return amountStr.isEmpty() ? 0f : Float.parseFloat(amountStr);
    }
}