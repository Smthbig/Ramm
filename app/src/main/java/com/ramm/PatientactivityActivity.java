package com.ramm;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.ramm.databinding.PatientactivityBinding;

import java.util.ArrayList;
import java.util.Random;

public class PatientactivityActivity extends BaseActivity {

    private PatientactivityBinding binding;
    private Intent i;
    private LottieAnimationView lottie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = PatientactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        i = new Intent();
        lottie = findViewById(R.id.lottie_bg);

        setupListeners();
        playLottie();
    }

    private void setupListeners() {
        binding.btnPatient.setOnClickListener(
                v -> {
                    i.setClass(getApplicationContext(), DoctorfeeActivity.class);
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                });

        binding.btnBuymedicine.setOnClickListener(
                v -> {
                    i.setClass(getApplicationContext(), BuymedicineActivity.class);
                    startActivity(i);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                });
    }

    private void playLottie() {
        if (lottie != null) lottie.playAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lottie != null) lottie.resumeAnimation();
    }

    @Override
    protected void onPause() {
        if (lottie != null) lottie.pauseAnimation();
        super.onPause();
    }

    public void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public int getLocationX(View v) {
        int[] location = new int[2];
        v.getLocationInWindow(location);
        return location[0];
    }

    public int getLocationY(View v) {
        int[] location = new int[2];
        v.getLocationInWindow(location);
        return location[1];
    }

    public int getRandom(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    public ArrayList<Double> getCheckedItemPositionsToArray(ListView list) {
        ArrayList<Double> result = new ArrayList<>();
        SparseBooleanArray arr = list.getCheckedItemPositions();
        for (int i = 0; i < arr.size(); i++) {
            if (arr.valueAt(i)) result.add((double) arr.keyAt(i));
        }
        return result;
    }

    public float getDip(int input) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, input, getResources().getDisplayMetrics());
    }

    public int getDisplayWidthPixels() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    public int getDisplayHeightPixels() {
        return getResources().getDisplayMetrics().heightPixels;
    }
}
