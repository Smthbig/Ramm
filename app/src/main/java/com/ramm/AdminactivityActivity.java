package com.ramm;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.ramm.databinding.AdminactivityBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import android.text.TextWatcher;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import android.graphics.Typeface;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

public class AdminactivityActivity extends AppCompatActivity {

    private AdminactivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AdminactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        binding.btnPurchh.setOnClickListener(
                v -> startActivity(new Intent(this, ReportActivity.class)));
        binding.btnBackupRestore.setOnClickListener(
                v -> startActivity(new Intent(this, BRActivity.class)));
        binding.btnList.setOnClickListener(v -> showMedicineListDialog());
    }

    private void showMedicineListDialog() {
        Context context = this;

        // Root card
        MaterialCardView card = new MaterialCardView(context);
        card.setCardElevation(8f);
        card.setRadius(24f);
        card.setUseCompatPadding(true);
        card.setPreventCornerOverlap(false);
        card.setCardBackgroundColor(
                MaterialColors.getColor(
                        context, com.google.android.material.R.attr.colorSurface, Color.WHITE));
        card.setStrokeColor(
                MaterialColors.getColor(
                        context, com.google.android.material.R.attr.colorOutline, Color.GRAY));
        card.setStrokeWidth(1);

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(48, 48, 48, 32);
        card.addView(container);

        // Title
        MaterialTextView title = new MaterialTextView(context);
        title.setText("Medicine List");
        title.setTextSize(20f);
        title.setTextColor(
                MaterialColors.getColor(
                        context, com.google.android.material.R.attr.colorOnSurface, Color.BLACK));
        title.setTypeface(null, Typeface.BOLD);
        container.addView(title);

        // "Add Medicine" Button
        MaterialButton btnAdd =
                new MaterialButton(
                        context,
                        null,
                        com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnAdd.setText("Add Medicine");
        btnAdd.setCornerRadius(16);
        LinearLayout.LayoutParams addBtnParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addBtnParams.topMargin = 24;
        container.addView(btnAdd, addBtnParams);

        // ListView inside a card
        MaterialCardView listCard = new MaterialCardView(context);
        listCard.setCardElevation(4f);
        listCard.setRadius(16f);
        listCard.setCardBackgroundColor(
                MaterialColors.getColor(
                        context,
                        com.google.android.material.R.attr.colorSurfaceContainer,
                        Color.LTGRAY));

        ListView listView = new ListView(context);
        listView.setDivider(
                new ColorDrawable(
                        MaterialColors.getColor(
                                context,
                                com.google.android.material.R.attr.colorOutline,
                                Color.GRAY)));
        listView.setDividerHeight(1);
        listView.setPadding(0, 16, 0, 16);
        listCard.addView(
                listView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600));
        container.addView(listCard);

        // Load & set adapter
        ArrayList<HashMap<String, Object>> medList = loadMedicinesFromFile();
        SimpleAdapter adapter =
                new SimpleAdapter(
                        context,
                        medList,
                        android.R.layout.simple_list_item_1,
                        new String[] {"text"},
                        new int[] {android.R.id.text1});
        listView.setAdapter(adapter);

        // Handle long click for edit
        listView.setOnItemLongClickListener(
                (parent, v, pos, id) -> {
                    showMedicineEditDialog(medList, adapter, pos);
                    return true;
                });

        // Dialog
        AlertDialog dialog =
                new AlertDialog.Builder(context)
                        .setView(card)
                        .setCancelable(true)
                        .setNegativeButton("Close", null)
                        .create();

        dialog.setOnShowListener(
                d -> {
                    if (dialog.getWindow() != null) {
                        dialog.getWindow()
                                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    }
                });

        // Add medicine action
        btnAdd.setOnClickListener(v -> showAddMedicineDialog(medList, adapter));

        dialog.show();
    }

    private ArrayList<HashMap<String, Object>> loadMedicinesFromFile() {
        ArrayList<HashMap<String, Object>> list = new ArrayList<>();
        File medFile = new File(FileUtil.getExternalStorageDir() + "/clinic/medicines.txt");

        if (medFile.exists()) {
            String content = FileUtil.readFile(medFile.getAbsolutePath());
            for (String entry : content.split("\\n\\n")) {
                if (!entry.trim().isEmpty()) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("text", entry.trim());
                    list.add(map);
                }
            }
        }

        return list;
    }

    private void showAddMedicineDialog(
            ArrayList<HashMap<String, Object>> medList, SimpleAdapter adapter) {

        Context context = this;

        // Root MaterialCardView
        MaterialCardView card = new MaterialCardView(context);
        card.setCardElevation(8f);
        card.setRadius(28f);
        card.setStrokeWidth(1);
        card.setPreventCornerOverlap(false);
        card.setUseCompatPadding(true);
        card.setStrokeColor(
                MaterialColors.getColor(
                        context, com.google.android.material.R.attr.colorOutline, Color.GRAY));
        card.setCardBackgroundColor(
                MaterialColors.getColor(
                        context, com.google.android.material.R.attr.colorSurface, Color.WHITE));
        card.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Main layout
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 32);
        layout.setBackgroundColor(Color.TRANSPARENT);

        // Name input
        TextInputLayout nameInputLayout =
                new TextInputLayout(
                        context,
                        null,
                        com.google.android.material.R.style
                                .Widget_Material3_TextInputLayout_OutlinedBox);
        nameInputLayout.setHint("Medicine Name");
        nameInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        nameInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);

        TextInputEditText etName = new TextInputEditText(context);
        etName.setInputType(InputType.TYPE_CLASS_TEXT);
        etName.setTextSize(16f);
        nameInputLayout.addView(etName);

        // Price input
        TextInputLayout priceInputLayout =
                new TextInputLayout(
                        context,
                        null,
                        com.google.android.material.R.style
                                .Widget_Material3_TextInputLayout_OutlinedBox);
        priceInputLayout.setHint("Price (Rs.)");
        priceInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        priceInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);

        TextInputEditText etPrice = new TextInputEditText(context);
        etPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etPrice.setTextSize(16f);
        priceInputLayout.addView(etPrice);

        // Buttons row
        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        btnRow.setWeightSum(2);
        btnRow.setPadding(0, 32, 0, 0);

        // Cancel button
        MaterialButton btnCancel =
                new MaterialButton(
                        context,
                        null,
                        com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnCancel.setText("Cancel");
        btnCancel.setIconResource(android.R.drawable.ic_menu_close_clear_cancel);
        btnCancel.setIconPadding(32);
        btnCancel.setCornerRadius(20);
        LinearLayout.LayoutParams cancelParams =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        cancelParams.setMarginEnd(12);
        btnCancel.setLayoutParams(cancelParams);

        // Save button
        MaterialButton btnSave =
                new MaterialButton(
                        context,
                        null,
                        com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnSave.setText("Save");
        btnSave.setIconResource(android.R.drawable.ic_menu_save);
        btnSave.setIconPadding(32);
        btnSave.setCornerRadius(20);
        LinearLayout.LayoutParams saveParams =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        btnSave.setLayoutParams(saveParams);

        btnRow.addView(btnCancel);
        btnRow.addView(btnSave);

        // Add to layout
        layout.addView(nameInputLayout);
        layout.addView(priceInputLayout);
        layout.addView(btnRow);
        card.addView(layout);

        // Dialog
        AlertDialog dialog =
                new AlertDialog.Builder(context).setView(card).setCancelable(true).create();

        dialog.setOnShowListener(
                d -> {
                    if (dialog.getWindow() != null) {
                        dialog.getWindow()
                                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    }
                });

        // Actions
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(
                v -> {
                    String name = etName.getText().toString().trim();
                    String price = etPrice.getText().toString().trim();

                    if (name.isEmpty() || price.isEmpty()) {
                        Toast.makeText(context, "Please enter name and price", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    try {
                        float val = Float.parseFloat(price);
                        if (val <= 0) throw new NumberFormatException();

                        String entry = "Name: " + name + "\nPrice: Rs. " + price;
                        HashMap<String, Object> newMed = new HashMap<>();
                        newMed.put("text", entry);
                        medList.add(0, newMed);
                        saveMedicinesToFile(medList);
                        adapter.notifyDataSetChanged();

                        Toast.makeText(
                                        context,
                                        "Saved: " + name + " Rs." + price,
                                        Toast.LENGTH_SHORT)
                                .show();
                        dialog.dismiss();
                    } catch (NumberFormatException e) {
                        Toast.makeText(context, "Invalid price format", Toast.LENGTH_SHORT).show();
                    }
                });

        dialog.show();
    }

    private void showMedicineEditDialog(
            ArrayList<HashMap<String, Object>> medList, SimpleAdapter adapter, int position) {

        Context context = this;
        String original = medList.get(position).get("text").toString();
        String name = "", price = "";

        try {
            name = original.split("Name:")[1].split("Price:")[0].trim();
            price = original.split("Price: Rs.")[1].trim();
        } catch (Exception ignored) {
        }

        // Root Card
        MaterialCardView card = new MaterialCardView(context);
        card.setCardElevation(8f);
        card.setRadius(28f);
        card.setStrokeWidth(1);
        card.setPreventCornerOverlap(false);
        card.setUseCompatPadding(true);
        card.setStrokeColor(
                MaterialColors.getColor(
                        context, com.google.android.material.R.attr.colorOutline, Color.GRAY));
        card.setCardBackgroundColor(
                MaterialColors.getColor(
                        context, com.google.android.material.R.attr.colorSurface, Color.WHITE));
        card.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Container
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 32);
        layout.setBackgroundColor(Color.TRANSPARENT);

        // Title
        TextView title = new TextView(context);
        title.setText("Edit Medicine");
        title.setTextSize(18f);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setTextColor(
                MaterialColors.getColor(
                        context, com.google.android.material.R.attr.colorPrimary, Color.BLACK));
        layout.addView(title);

        // Name input
        TextInputLayout tilName =
                new TextInputLayout(
                        context,
                        null,
                        com.google.android.material.R.style
                                .Widget_Material3_TextInputLayout_OutlinedBox);
        tilName.setHint("Medicine Name");
        tilName.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        tilName.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);

        TextInputEditText etName = new TextInputEditText(context);
        etName.setInputType(InputType.TYPE_CLASS_TEXT);
        etName.setText(name);
        etName.setTextSize(16f);
        tilName.addView(etName);

        // Price input
        TextInputLayout tilPrice =
                new TextInputLayout(
                        context,
                        null,
                        com.google.android.material.R.style
                                .Widget_Material3_TextInputLayout_OutlinedBox);
        tilPrice.setHint("Price (Rs)");
        tilPrice.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        tilPrice.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);

        TextInputEditText etPrice = new TextInputEditText(context);
        etPrice.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etPrice.setText(price);
        etPrice.setTextSize(16f);
        tilPrice.addView(etPrice);

        // Save + Delete buttons row
        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        btnRow.setPadding(0, 32, 0, 0);
        btnRow.setWeightSum(2);

        MaterialButton btnSave =
                new MaterialButton(
                        context,
                        null,
                        com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnSave.setText("Save");
        btnSave.setIconResource(android.R.drawable.ic_menu_save);
        btnSave.setIconPadding(32);
        btnSave.setCornerRadius(20);
        LinearLayout.LayoutParams saveParams =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        saveParams.setMarginEnd(8);
        btnSave.setLayoutParams(saveParams);

        MaterialButton btnDelete =
                new MaterialButton(
                        context,
                        null,
                        com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnDelete.setText("Delete");
        btnDelete.setIconResource(android.R.drawable.ic_menu_delete);
        btnDelete.setIconPadding(32);
        btnDelete.setCornerRadius(20);
        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        btnDelete.setLayoutParams(deleteParams);

        btnRow.addView(btnSave);
        btnRow.addView(btnDelete);

        // Cancel button (separate row)
        MaterialButton btnCancel =
                new MaterialButton(
                        context,
                        null,
                        com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnCancel.setText("Cancel");
        btnCancel.setIconResource(android.R.drawable.ic_menu_close_clear_cancel);
        btnCancel.setIconPadding(32);
        btnCancel.setCornerRadius(20);
        LinearLayout.LayoutParams cancelParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cancelParams.setMargins(0, 24, 0, 0);
        btnCancel.setLayoutParams(cancelParams);

        // Add views to layout
        layout.addView(tilName);
        layout.addView(tilPrice);
        layout.addView(btnRow);
        layout.addView(btnCancel);
        card.addView(layout);

        // Dialog
        AlertDialog dialog =
                new AlertDialog.Builder(context).setView(card).setCancelable(true).create();

        dialog.setOnShowListener(
                d -> {
                    if (dialog.getWindow() != null) {
                        dialog.getWindow()
                                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    }
                });

        // Actions
        btnSave.setOnClickListener(
                v -> {
                    String newEntry =
                            "Name: "
                                    + etName.getText().toString().trim()
                                    + "\nPrice: Rs. "
                                    + etPrice.getText().toString().trim();
                    medList.get(position).put("text", newEntry);
                    saveMedicinesToFile(medList);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });

        btnDelete.setOnClickListener(
                v -> {
                    medList.remove(position);
                    saveMedicinesToFile(medList);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void saveMedicinesToFile(ArrayList<HashMap<String, Object>> medList) {
        StringBuilder sb = new StringBuilder();
        for (HashMap<String, Object> m : medList) {
            sb.append(m.get("text")).append("\n\n");
        }
        File file = new File(FileUtil.getExternalStorageDir() + "/clinic/medicines.txt");
        FileUtil.writeFile(file.getAbsolutePath(), sb.toString().trim());
    }
}
