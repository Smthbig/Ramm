package com.ramm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.ramm.databinding.MainBinding;

public class MainActivity extends BaseActivity {

    private MainBinding binding;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private AlertDialog dialog;

    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_THEME_MODE = "theme_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before super.onCreate
        applySavedTheme();

        super.onCreate(savedInstanceState);
        binding = MainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
        setupBottomSheet();
        initializeAnimation();

        addHealthTip("💧", "Remember to drink water regularly!");
        addHealthTip("🥦", "Eat your greens daily.");
        addHealthTip("🧘", "Practice mindfulness.");

        binding.fabAddTip.setOnClickListener(v -> showAddTipDialog());
    }

    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int themeMode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO);
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    private void setupListeners() {
        binding.btnPatient.setOnClickListener(
                v -> startActivity(new Intent(this, PatientactivityActivity.class)));

        binding.btnHistoryFilter.setOnClickListener(
                v -> startActivity(new Intent(this, VisitHistoryActivity.class)));

        binding.btnAdmin.setOnClickListener(
                v -> startActivity(new Intent(this, AdminactivityActivity.class)));

        binding.actionSettings.setOnClickListener(
          v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void setupBottomSheet() {
        View bottomSheet = findViewById(R.id.quick_actions_sheet);
        View titleView = findViewById(R.id.quick_title);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        bottomSheet
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        () -> {
                            int peek = titleView.getHeight() + 30;
                            if (peek > 0) {
                                bottomSheetBehavior.setPeekHeight(peek);
                                bottomSheetBehavior.setHideable(false);
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            }
                        });
    }

    private void addHealthTip(String icon, String message) {
        MaterialCardView card =
                (MaterialCardView)
                        getLayoutInflater()
                                .inflate(
                                        R.layout.item_health_tip,
                                        binding.healthTipsContainer,
                                        false);

        MaterialTextView iconView = card.findViewById(R.id.tip_icon);
        MaterialTextView messageView = card.findViewById(R.id.tip_message);

        iconView.setText(icon);
        messageView.setText(message);

        card.setOnLongClickListener(
                v -> {
                    showEditTipDialog(messageView);
                    return true;
                });

        binding.healthTipsContainer.addView(card);
    }

    private void showAddTipDialog() {
        Context context = this;
        LinearLayout layout = createDialogContentLayout(context);

        TextInputLayout iconLayout = createTextInputLayout(context, "Emoji (e.g., 💧)");
        TextInputEditText iconInput = new TextInputEditText(context);
        iconLayout.addView(iconInput);

        TextInputLayout msgLayout = createTextInputLayout(context, "Health tip message");
        TextInputEditText msgInput = new TextInputEditText(context);
        msgLayout.addView(msgInput);

        layout.addView(iconLayout);
        layout.addView(msgLayout);

        LinearLayout btnRow = createButtonRow(context);
        MaterialButton btnCancel =
                createMaterialButton(
                        context, "Cancel", android.R.drawable.ic_menu_close_clear_cancel);
        MaterialButton btnAdd =
                createMaterialButton(context, "Add", android.R.drawable.ic_input_add);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(
                v -> {
                    String icon = iconInput.getText().toString().trim();
                    String msg = msgInput.getText().toString().trim();

                    boolean valid = true;

                    if (icon.isEmpty()) {
                        iconLayout.setError("Emoji is required");
                        valid = false;
                    } else iconLayout.setError(null);

                    if (msg.isEmpty()) {
                        msgLayout.setError("Message is required");
                        valid = false;
                    } else msgLayout.setError(null);

                    if (valid) {
                        addHealthTip(icon, msg);
                        dialog.dismiss();
                    }
                });

        btnRow.addView(btnCancel);
        btnRow.addView(btnAdd);
        layout.addView(btnRow);

        MaterialCardView card = createDialogCard(context, layout);
        dialog = new AlertDialog.Builder(context).setView(card).setCancelable(false).create();
        dialog.setOnShowListener(
                d ->
                        dialog.getWindow()
                                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)));
        dialog.show();
    }

    private void showEditTipDialog(MaterialTextView messageView) {
        Context context = this;
        LinearLayout layout = createDialogContentLayout(context);

        TextInputLayout msgLayout = createTextInputLayout(context, "Edit message");
        TextInputEditText msgInput = new TextInputEditText(context);
        msgInput.setText(messageView.getText());
        msgLayout.addView(msgInput);
        layout.addView(msgLayout);

        LinearLayout btnRow = createButtonRow(context);
        MaterialButton btnCancel =
                createMaterialButton(
                        context, "Cancel", android.R.drawable.ic_menu_close_clear_cancel);
        MaterialButton btnUpdate =
                createMaterialButton(context, "Update", android.R.drawable.ic_menu_edit);
        MaterialButton btnDelete =
                createMaterialButton(context, "Delete", android.R.drawable.ic_menu_delete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnUpdate.setOnClickListener(
                v -> {
                    String newText = msgInput.getText().toString().trim();
                    if (!newText.isEmpty()) {
                        messageView.setText(newText);
                        dialog.dismiss();
                    } else msgLayout.setError("Message cannot be empty");
                });
        btnDelete.setOnClickListener(
                v -> {
                    View card = (View) messageView.getParent().getParent();
                    ((ViewGroup) card.getParent()).removeView(card);
                    showToast("Tip deleted");
                    dialog.dismiss();
                });

        btnRow.addView(btnCancel);
        btnRow.addView(btnUpdate);
        btnRow.addView(btnDelete);
        layout.addView(btnRow);

        MaterialCardView card = createDialogCard(context, layout);
        dialog = new AlertDialog.Builder(context).setView(card).setCancelable(false).create();
        dialog.setOnShowListener(
                d ->
                        dialog.getWindow()
                                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)));
        dialog.show();
    }

    private LinearLayout createDialogContentLayout(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad =
                (int)
                        TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                24,
                                getResources().getDisplayMetrics());
        layout.setPadding(pad * 2, pad * 2, pad * 2, pad);
        return layout;
    }

    private TextInputLayout createTextInputLayout(Context context, String hint) {
        TextInputLayout layout =
                new TextInputLayout(
                        context,
                        null,
                        com.google.android.material.R.style
                                .Widget_Material3_TextInputLayout_OutlinedBox);
        layout.setHint(hint);
        layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        return layout;
    }

    private LinearLayout createButtonRow(Context context) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.END);
        row.setPadding(0, 40, 0, 0);
        return row;
    }

    private MaterialButton createMaterialButton(Context context, String text, int iconRes) {
        MaterialButton btn =
                new MaterialButton(
                        context,
                        null,
                        com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btn.setText(text);
        btn.setIconResource(iconRes);
        btn.setIconPadding(16);
        return btn;
    }

    private MaterialCardView createDialogCard(Context context, View content) {
        MaterialCardView card = new MaterialCardView(context);
        card.setCardElevation(8f);
        card.setRadius(24f);
        card.setStrokeWidth(1);
        card.setStrokeColor(
                MaterialColors.getColor(
                        context, com.google.android.material.R.attr.colorOutline, Color.GRAY));
        card.setCardBackgroundColor(
                MaterialColors.getColor(
                        context, com.google.android.material.R.attr.colorSurface, Color.WHITE));
        card.setUseCompatPadding(true);
        card.setPreventCornerOverlap(true);
        card.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        card.addView(content);
        return card;
    }

    private void initializeAnimation() {
        binding.lottieBg.setAnimation(R.raw.medical_animation);
        binding.lottieBg.playAnimation();
    }

    private void toggleTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int currentTheme = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO);
        int newTheme =
                (currentTheme == AppCompatDelegate.MODE_NIGHT_YES)
                        ? AppCompatDelegate.MODE_NIGHT_NO
                        : AppCompatDelegate.MODE_NIGHT_YES;

        prefs.edit().putInt(KEY_THEME_MODE, newTheme).apply();
        AppCompatDelegate.setDefaultNightMode(newTheme);
        recreate();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}