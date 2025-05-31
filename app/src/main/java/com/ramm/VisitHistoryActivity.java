package com.ramm;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.*;

public class VisitHistoryActivity extends AppCompatActivity {

    private EditText etFromDate, etToDate, etSearch;
    private Button btnFilter, btnSearch;
    private Spinner spinnerFilterOptions;
    private ListView listHistory;
    private ArrayList<HashMap<String, Object>> currentList = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> fullList = new ArrayList<>();
    private SimpleAdapter adapter;

    private final String[] filterOptions = {"Name", "Age", "Phone", "Amount"};
    private final String[] filterHints = {
        "Search Name", "Search Age", "Search Phone", "Min-Max (e.g. 100-500)"
    };

    private DBHelper dbHelper;
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_history);

        etFromDate = findViewById(R.id.et_from_date);
        etToDate = findViewById(R.id.et_to_date);
        etSearch = findViewById(R.id.et_search);
        btnFilter = findViewById(R.id.btn_filter);
        btnSearch = findViewById(R.id.btn_search);
        spinnerFilterOptions = findViewById(R.id.spinner_filter_options);
        listHistory = findViewById(R.id.list_history);

        dbHelper = new DBHelper(this);

        // Spinner Setup
        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterOptions.setAdapter(spinnerAdapter);

        spinnerFilterOptions.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        etSearch.setHint(filterHints[position]);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

        etFromDate.setOnClickListener(v -> showDateDialog(etFromDate));
        etToDate.setOnClickListener(v -> showDateDialog(etToDate));

        btnFilter.setOnClickListener(
                v -> {
                    String from = etFromDate.getText().toString().trim();
                    String to = etToDate.getText().toString().trim();
                    if (from.isEmpty() || to.isEmpty()) {
                        toast("Select both dates");
                    } else {
                        filterByDate(from, to);
                    }
                });

        btnSearch.setOnClickListener(
                v -> {
                    String keyword = etSearch.getText().toString().trim();
                    if (keyword.isEmpty()) {
                        toast("Enter keyword");
                    } else {
                        filterByKeyword(keyword);
                    }
                });

        listHistory.setOnItemLongClickListener(
                (parent, view, position, id) -> {
                    showEditDialog(position);
                    return true;
                });

        loadAllData();
    }

    private void showDateDialog(EditText target) {
        new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {
                            String date =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d-%02d-%04d",
                                            dayOfMonth,
                                            month + 1,
                                            year);
                            target.setText(date);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void loadAllData() {
        fullList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM patients ORDER BY id DESC", null);
        while (cursor.moveToNext()) {
            fullList.add(buildMap(cursor));
        }
        cursor.close();
        db.close();
        updateListView(fullList);
    }

    private void updateListView(List<HashMap<String, Object>> data) {
        currentList.clear();
        currentList.addAll(data);
        adapter =
                new SimpleAdapter(
                        this,
                        currentList,
                        android.R.layout.simple_list_item_1,
                        new String[] {"info"},
                        new int[] {android.R.id.text1});
        listHistory.setAdapter(adapter);
    }

    private void filterByDate(String from, String to) {
        ArrayList<HashMap<String, Object>> filtered = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor =
                db.rawQuery(
                        "SELECT * FROM patients WHERE date BETWEEN ? AND ? ORDER BY id DESC",
                        new String[] {from, to});
        while (cursor.moveToNext()) {
            filtered.add(buildMap(cursor));
        }
        cursor.close();
        db.close();
        updateListView(filtered);
    }

    private void filterByKeyword(String keyword) {
        ArrayList<HashMap<String, Object>> filtered = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int selectedFilter = spinnerFilterOptions.getSelectedItemPosition();

        if (selectedFilter == 3 && keyword.contains("-")) { // Amount range
            try {
                String[] parts = keyword.split("-");
                float min = Float.parseFloat(parts[0].trim());
                float max = Float.parseFloat(parts[1].trim());
                Cursor cursor =
                        db.rawQuery(
                                "SELECT * FROM patients WHERE final_amount BETWEEN ? AND ? ORDER BY id DESC",
                                new String[] {String.valueOf(min), String.valueOf(max)});
                while (cursor.moveToNext()) {
                    filtered.add(buildMap(cursor));
                }
                cursor.close();
            } catch (Exception e) {
                toast("Invalid amount format");
            }
        } else {
            String column =
                    switch (selectedFilter) {
                        case 1 -> "age";
                        case 2 -> "phone";
                        default -> "name";
                    };
            String search = "%" + keyword + "%";
            Cursor cursor =
                    db.rawQuery(
                            "SELECT * FROM patients WHERE " + column + " LIKE ? ORDER BY id DESC",
                            new String[] {search});
            while (cursor.moveToNext()) {
                filtered.add(buildMap(cursor));
            }
            cursor.close();
        }

        db.close();
        updateListView(filtered);
    }

    private HashMap<String, Object> buildMap(Cursor cursor) {
        HashMap<String, Object> map = new HashMap<>();
        String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String age = cursor.getString(cursor.getColumnIndexOrThrow("age"));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
        String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
        String visitPayment = cursor.getString(cursor.getColumnIndexOrThrow("visit_payment"));
        String medicines = cursor.getString(cursor.getColumnIndexOrThrow("medicine_items"));
        String finalAmount = cursor.getString(cursor.getColumnIndexOrThrow("final_amount"));
        String paymentMode = cursor.getString(cursor.getColumnIndexOrThrow("payment_mode"));

        map.put("id", id);
        map.put("name", name);
        map.put("age", age);
        map.put("phone", phone);
        map.put("date", date);
        map.put("visit_payment", visitPayment);
        map.put("medicine_items", medicines);
        map.put("final_amount", finalAmount);
        map.put("payment_mode", paymentMode);

        String info =
                "Name: "
                        + name
                        + "\nAge: "
                        + age
                        + "\nPhone: "
                        + phone
                        + "\nDate: "
                        + date
                        + "\nVisit Payment: "
                        + visitPayment;

        if (medicines != null && !medicines.isEmpty()) {
            info +=
                    "\n\n-- Medicines --\n"
                            + medicines
                            + "\nPayment Mode: "
                            + paymentMode
                            + "\nTotal: ₹"
                            + finalAmount;
        }

        map.put("info", info);
        return map;
    }

    private void showEditDialog(int position) {
        if (position < 0 || position >= fullList.size()) {
            toast("Invalid item");
            return;
        }

        HashMap<String, Object> item = fullList.get(position);
        Context context = this;

        MaterialCardView card = new MaterialCardView(context);
        card.setCardElevation(8f);
        card.setRadius(28f);
        card.setStrokeWidth(1);
        card.setUseCompatPadding(true);
        card.setCardBackgroundColor(
                MaterialColors.getColor(
                        context, com.google.android.material.R.attr.colorSurface, Color.WHITE));

        ScrollView scroll = new ScrollView(context);
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(48, 48, 48, 32);

        TextInputEditText etName = createTextField(context, "Name", item.get("name"), container);
        TextInputEditText etAge = createTextField(context, "Age", item.get("age"), container);
        etAge.setInputType(InputType.TYPE_CLASS_NUMBER);
        TextInputEditText etPhone = createTextField(context, "Phone", item.get("phone"), container);
        etPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        TextInputEditText etDate =
                createTextField(context, "Visit Date", item.get("date"), container);
        etDate.setFocusable(false);
        etDate.setOnClickListener(v -> showDateDialog(etDate));
        TextInputEditText etVisitPay =
                createTextField(context, "Visit Payment", item.get("visit_payment"), container);
        etVisitPay.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        TextInputEditText etMeds =
                createTextField(context, "Medicine Items", item.get("medicine_items"), container);
        TextInputEditText etMedPay =
                createTextField(context, "Payment Mode", item.get("payment_mode"), container);
        TextInputEditText etFinalAmt =
                createTextField(context, "Final Amount", item.get("final_amount"), container);
        etFinalAmt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        btnRow.setPadding(0, 32, 0, 0);
        btnRow.setWeightSum(2);

        MaterialButton btnSave =
                new MaterialButton(
                        context, null, com.google.android.material.R.attr.materialButtonStyle);
        btnSave.setText("Save");
        btnSave.setIconResource(android.R.drawable.ic_menu_save);
        btnSave.setCornerRadius(20);
        btnSave.setLayoutParams(
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        MaterialButton btnDelete =
                new MaterialButton(
                        context,
                        null,
                        com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnDelete.setText("Delete");
        btnDelete.setIconResource(android.R.drawable.ic_menu_delete);
        btnDelete.setCornerRadius(20);
        btnDelete.setLayoutParams(
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        MaterialButton btnCancel =
                new MaterialButton(
                        context,
                        null,
                        com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnCancel.setText("Cancel");
        btnCancel.setIconResource(android.R.drawable.ic_menu_close_clear_cancel);
        btnCancel.setCornerRadius(20);
        LinearLayout.LayoutParams cancelParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cancelParams.topMargin = 24;
        btnCancel.setLayoutParams(cancelParams);

        btnRow.addView(btnSave);
        btnRow.addView(btnDelete);
        container.addView(btnRow);
        container.addView(btnCancel);

        scroll.addView(container);
        card.addView(scroll);

        AlertDialog dialog = new AlertDialog.Builder(context).setView(card).create();
        dialog.setOnShowListener(
                d -> {
                    if (dialog.getWindow() != null) {
                        dialog.getWindow()
                                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    }
                });

        btnSave.setOnClickListener(
                v -> {
                    try {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        ContentValues cv = new ContentValues();
                        cv.put("name", etName.getText().toString());
                        cv.put("age", etAge.getText().toString());
                        cv.put("phone", etPhone.getText().toString());
                        cv.put("date", etDate.getText().toString());
                        cv.put("visit_payment", etVisitPay.getText().toString());
                        cv.put("medicine_items", etMeds.getText().toString());
                        cv.put("final_amount", etFinalAmt.getText().toString());
                        cv.put("payment_mode", etMedPay.getText().toString());
                        db.update(
                                "patients", cv, "id = ?", new String[] {item.get("id").toString()});
                        db.close();
                        dialog.dismiss();
                        loadAllData();
                        toast("Updated");
                    } catch (Exception e) {
                        toast("Update failed");
                    }
                });

        btnDelete.setOnClickListener(
                v -> {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete("patients", "id = ?", new String[] {item.get("id").toString()});
                    db.close();
                    dialog.dismiss();
                    loadAllData();
                    toast("Deleted");
                });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private TextInputEditText createTextField(
            Context context, String hint, Object value, LinearLayout parent) {
        TextInputLayout layout =
                new TextInputLayout(
                        context,
                        null,
                        com.google.android.material.R.style
                                .Widget_Material3_TextInputLayout_OutlinedBox);
        layout.setHint(hint);
        layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);

        TextInputEditText editText = new TextInputEditText(context);
        editText.setText(value != null ? value.toString() : "");
        editText.setTextSize(16f);
        layout.addView(editText);

        LinearLayout wrapper = new LinearLayout(context);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setLayoutParams(
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        wrapper.addView(layout);
        parent.addView(wrapper);
        return editText;
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
