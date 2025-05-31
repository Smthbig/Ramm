package com.ramm;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.ramm.databinding.BuymedicineBinding;
import android.content.SharedPreferences;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class BuymedicineActivity extends AppCompatActivity {

    private BuymedicineBinding binding;
    private final Map<String, Float> medPrices = new HashMap<>();
    private final List<Map<String, Object>> cartList = new ArrayList<>();
    private final List<String> medNames = new ArrayList<>();
    private Dialog dialog;
    private String selectedMedicine = "";
    private boolean isVaccination = false;
    private String vaccinationName = "";
    private Bitmap cachedQRBitmap = null;
    private float cachedQRAmount = -1f;
    private BottomSheetBehavior<View> medicineSheetBehavior;

    private List<String> filteredNames = new ArrayList<>();
    private MedicineAdapter adapter;
    private SharedPreferences prefs;
    // config variable
    private static final String PREFS_NAME = "AppSettings";
    private static final String DEFAULT_UPI_ID = "xxxxxxxxx@xx";
    private static final String DEFAULT_UPI_NAME = "med bill ramm health care";
    private static final String DEFAULT_UPI_NOTE = "medicine payment from RAMM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = BuymedicineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupDefaults();
        setupListeners();
        loadMedicines();
        loadTodayPatients();
        setupMedicineBottomSheet();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void setupDefaults() {
        binding.etDiscount.setText("10");
        binding.etQty.setText("1");
    }

    private void setupListeners() {
        binding.btnSelectMedicine.setOnClickListener(v -> setupMedicineBottomSheet());
        binding.btnAdd.setOnClickListener(v -> addToCart());
        binding.btnRefresh.setOnClickListener(
                v -> {
                    updateTotal();
                    showToast("Total updated");
                });
        binding.btnConfirm.setOnClickListener(v -> saveToDatabase());

        binding.rgPayment.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    if (checkedId == R.id.rb_online) showQRDialog();
                });

        SimpleAdapter adapter =
                new SimpleAdapter(
                        this,
                        cartList,
                        android.R.layout.simple_list_item_2,
                        new String[] {"med", "detail"},
                        new int[] {android.R.id.text1, android.R.id.text2});
        binding.listCart.setAdapter(adapter);

        binding.listCart.setOnItemLongClickListener(
                (parent, view, position, id) -> {
                    cartList.remove(position);
                    updateCartUI();
                    showToast("Item removed");
                    return true;
                });
        binding.etDiscount.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        updateTotal(); // Auto-refresh total
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
    }

    private void addToCart() {
        String qtyStr = binding.etQty.getText().toString().trim();

        if (selectedMedicine.isEmpty()) {
            showToast("Please select a medicine");
            return;
        }

        if (qtyStr.isEmpty()) {
            showToast("Please enter quantity");
            return;
        }

        try {
            float qty = Float.parseFloat(qtyStr);
            if (qty <= 0) {
                showToast("Quantity must be greater than 0");
                return;
            }

            float price = medPrices.getOrDefault(selectedMedicine, 0f);
            float total = qty * price;

            Map<String, Object> item = new HashMap<>();
            item.put("med", selectedMedicine);
            item.put("qty", qty);
            item.put("price", price);
            item.put(
                    "detail",
                    String.format(
                            Locale.getDefault(), "Qty: %.0f x %.2f = Rs. %.2f", qty, price, total));

            cartList.add(item);
            updateCartUI();
        } catch (NumberFormatException e) {
            showToast("Invalid quantity format");
        }
    }

    private void updateCartUI() {
        ((BaseAdapter) binding.listCart.getAdapter()).notifyDataSetChanged();
        updateTotal();
    }

    private void updateTotal() {
        float subtotal = 0f;

        for (Map<String, Object> item : cartList) {
            float qty = ((Number) item.get("qty")).floatValue();
            float price = ((Number) item.get("price")).floatValue();
            subtotal += qty * price;
        }

        String discountStr = binding.etDiscount.getText().toString().trim();
        float discount = discountStr.isEmpty() ? 0f : Float.parseFloat(discountStr);
        float finalTotal = subtotal - ((discount / 100f) * subtotal);

        String formatted = String.format(Locale.getDefault(), "Total: Rs. %.2f", finalTotal);
        binding.tvTotal.setText(formatted);

        generateQRInBackground();
    }

    private void saveToDatabase() {
        try {
            final String name =
                    ((Spinner) findViewById(R.id.spinner_patient))
                            .getSelectedItem()
                            .toString()
                            .trim();
            final String discountStr =
                    ((EditText) findViewById(R.id.et_discount)).getText().toString().trim();
            final String payment =
                    ((RadioButton) findViewById(R.id.rb_cash)).isChecked() ? "Cash" : "Online";

            final Calendar calendar = Calendar.getInstance();
            final String date =
                    calendar.get(Calendar.DAY_OF_MONTH)
                            + "-"
                            + (calendar.get(Calendar.MONTH) + 1)
                            + "-"
                            + calendar.get(Calendar.YEAR);

            final StringBuilder meds = new StringBuilder();
            float subtotal = 0f;

            for (Map<String, Object> item : cartList) {
                float qty = Float.parseFloat(item.get("qty").toString());
                float price = Float.parseFloat(item.get("price").toString());
                float total = qty * price;
                subtotal += total;

                meds.append(item.get("med"))
                        .append(" x")
                        .append(qty)
                        .append(" = Rs. ")
                        .append(total)
                        .append("\n");
            }

            float discount = discountStr.isEmpty() ? 0f : Float.parseFloat(discountStr);
            float finalTotal = subtotal - ((discount / 100f) * subtotal);

            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("date", date);
            values.put("medicine_items", meds.toString());
            values.put("subtotal", subtotal);
            values.put("discount", discount);
            values.put("final_amount", finalTotal);
            values.put("payment_mode", payment);

            try (DBHelper dbHelper = new DBHelper(this);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Cursor cursor =
                            db.rawQuery(
                                    "SELECT id FROM patients WHERE name = ? AND date = ?",
                                    new String[] {name, date})) {

                if (cursor.moveToFirst()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    int rows =
                            db.update(
                                    "patients",
                                    values,
                                    "id = ?",
                                    new String[] {String.valueOf(id)});
                    resetForm();
                    showToast(
                            rows > 0
                                    ? "Patient record updated successfully."
                                    : "Failed to update patient record.");
                } else {
                    long insertId = db.insert("patients", null, values);
                    resetForm();
                    showToast(
                            insertId != -1
                                    ? "New patient record saved."
                                    : "Failed to insert new record.");
                }
            }

        } catch (Exception e) {
            showToast("Error saving data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetForm() {
        cartList.clear(); // Clear the cart data
        updateCartUI(); // Refresh the list and total

        binding.tvTotal.setText(""); // Clear total display
        selectedMedicine = ""; // Clear selected medicine if needed
    }

    private void loadMedicines() {
        File file = new File(Environment.getExternalStorageDirectory() + "/clinic/medicines.txt");
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line, name = "", price = "";

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Name:")) {
                    name = line.replace("Name:", "").trim();
                } else if (line.startsWith("Price: Rs.")) {
                    price = line.replace("Price: Rs.", "").trim();
                    if (!name.isEmpty() && !price.isEmpty()) {
                        medNames.add(name);
                        medPrices.put(name, Float.parseFloat(price));
                        name = "";
                        price = "";
                    }
                }
            }
            medNames.sort(String::compareToIgnoreCase);
        } catch (IOException e) {
            showToast("Failed to load medicines: " + e.getMessage());
        }
    }

    public void loadTodayPatients() {
        ArrayList<String> todayPatients = new ArrayList<>();
        Set<String> uniqueNames = new HashSet<>();

        Calendar calendar = Calendar.getInstance();
        String today =
                calendar.get(Calendar.DAY_OF_MONTH)
                        + "-"
                        + (calendar.get(Calendar.MONTH) + 1)
                        + "-"
                        + calendar.get(Calendar.YEAR);

        DBHelper dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT DISTINCT name FROM patients WHERE date = ? ORDER BY id DESC",
                        new String[] {today});

        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex("name"));
            if (name != null && !name.trim().isEmpty() && !name.startsWith("Vaccination #")) {
                uniqueNames.add(name.trim());
            }
        }

        cursor.close();
        db.close();

        todayPatients.addAll(uniqueNames);
        Collections.reverse(todayPatients); // Reverse the list
        todayPatients.add("Vaccination Patient");

        Spinner spinner_patient = findViewById(R.id.spinner_patient);
        ArrayAdapter<String> patientAdapter =
                new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_dropdown_item, todayPatients);
        spinner_patient.setAdapter(patientAdapter);

        spinner_patient.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        String selected = parent.getItemAtPosition(position).toString();
                        if (selected.equals("Vaccination Patient")) {
                            showVaccinationDialog();
                        } else {
                            isVaccination = false;
                            vaccinationName = "";
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
    }

    private void showVaccinationDialog() {
        Context context = this;

        // Card container
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

        // LinearLayout wrapper
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 32);
        layout.setBackgroundColor(Color.TRANSPARENT);

        // Input field with layout
        TextInputLayout inputLayout =
                new TextInputLayout(
                        context,
                        null,
                        com.google.android.material.R.style
                                .Widget_Material3_TextInputLayout_OutlinedBox);
        inputLayout.setHint("Optional: Enter patient name");
        inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        inputLayout.setEndIconDrawable(android.R.drawable.ic_menu_edit);
        inputLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);

        TextInputEditText etName = new TextInputEditText(context);
        etName.setTextSize(16f);
        inputLayout.addView(etName);

        // Buttons layout
        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        btnRow.setPadding(0, 40, 0, 0);

        MaterialButton btnCancel =
                new MaterialButton(
                        context,
                        null,
                        com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnCancel.setText("Cancel");
        btnCancel.setIconResource(android.R.drawable.ic_menu_close_clear_cancel);
        btnCancel.setIconPadding(16);

        MaterialButton btnSave =
                new MaterialButton(
                        context,
                        null,
                        com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnSave.setText("Save");
        btnSave.setIconResource(android.R.drawable.ic_menu_save);
        btnSave.setIconPadding(16);

        btnRow.addView(btnCancel);
        btnRow.addView(btnSave);

        // Assemble
        layout.addView(inputLayout);
        layout.addView(btnRow);
        card.addView(layout);

        // Dialog
        AlertDialog dialog =
                new AlertDialog.Builder(context).setView(card).setCancelable(false).create();

        // Button actions
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(
                v -> {
                    vaccinationName = etName.getText().toString().trim();
                    isVaccination = true;
                    showToast("Ready to save Vaccination data");
                    dialog.dismiss();
                });

        dialog.setOnShowListener(
                d -> {
                    if (dialog.getWindow() != null) {
                        dialog.getWindow()
                                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    }
                });

        dialog.show();
    }

    private void generateQRInBackground() {
        float subtotal = 0f;
        for (var item : cartList) {
            subtotal +=
                    ((Number) item.get("qty")).floatValue()
                            * ((Number) item.get("price")).floatValue();
        }

        String discountStr = binding.etDiscount.getText().toString();
        float discount = discountStr.isEmpty() ? 0 : Float.parseFloat(discountStr);
        float finalAmount = subtotal - ((discount / 100f) * subtotal);

        if (finalAmount == cachedQRAmount) return; // No need to regenerate

        new Thread(
                        () -> {
                            String upiId = prefs.getString("medicine_upi_id", DEFAULT_UPI_ID);
                            String upiName = prefs.getString("medicine_upi_name", DEFAULT_UPI_NAME);
                            String note = prefs.getString("medicine_upi_note", DEFAULT_UPI_NOTE);
                            Bitmap newQr = generateUPIQR(upiId, upiName, finalAmount, note);
                            if (newQr != null) {
                                cachedQRBitmap = newQr;
                                cachedQRAmount = finalAmount;
                            }
                        })
                .start();
    }

    private void showQRDialog() {
        float subtotal = 0f;
        for (Map<String, Object> item : cartList) {
            subtotal +=
                    ((Number) item.get("qty")).floatValue()
                            * ((Number) item.get("price")).floatValue();
        }

        String discountStr = binding.etDiscount.getText().toString();
        float discount = discountStr.isEmpty() ? 0 : Float.parseFloat(discountStr);
        float finalAmount = subtotal - ((discount / 100f) * subtotal);

        Dialog dialog =
                new Dialog(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_qr);

        if (dialog.getWindow() != null) {
            dialog.getWindow()
                    .setLayout(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView qrImage = dialog.findViewById(R.id.img_qr_dialog);
        ProgressBar progress = dialog.findViewById(R.id.qr_progress);
        Button btnClose = dialog.findViewById(R.id.btn_close_dialog);

        qrImage.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        //        DisplayMetrics metrics = getResources().getDisplayMetrics();
        //        int qrSize = (int) (Math.min(metrics.widthPixels, metrics.heightPixels) * 0.85);
        //
        //        qrImage.getLayoutParams().width = qrSize;
        //        qrImage.getLayoutParams().height = qrSize;
        //        qrImage.requestLayout();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.setCancelable(true);
        dialog.show();

        new Thread(
                        () -> {
                            Bitmap qrBitmap;
                            if (cachedQRBitmap == null || cachedQRAmount != finalAmount) {
                                String upiId = prefs.getString("medicine_upi_id", DEFAULT_UPI_ID);
                                String upiName =
                                        prefs.getString("medicine_upi_name", DEFAULT_UPI_NAME);
                                String note =
                                        prefs.getString("medicine_upi_note", DEFAULT_UPI_NOTE);
                                qrBitmap = generateUPIQR(upiId, upiName, finalAmount, note);
                                cachedQRBitmap = qrBitmap;
                                cachedQRAmount = finalAmount;
                            } else {
                                qrBitmap = cachedQRBitmap;
                            }

                            runOnUiThread(
                                    () -> {
                                        progress.setVisibility(View.GONE);
                                        qrImage.setImageBitmap(qrBitmap);
                                        qrImage.setVisibility(View.VISIBLE);
                                    });
                        })
                .start();
    }

    private Bitmap generateUPIQR(String upiId, String name, float amount, String txnNote) {
        String upiUri =
                "upi://pay?pa="
                        + upiId
                        + "&pn="
                        + Uri.encode(name)
                        + "&am="
                        + amount
                        + "&cu=INR"
                        + "&cu=INR&tn="
                        + Uri.encode(txnNote);

        try {
            QRCodeWriter writer = new QRCodeWriter();

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2); // Reduced margin for denser QR

            int size = 800;
            BitMatrix bitMatrix = writer.encode(upiUri, BarcodeFormat.QR_CODE, size, size, hints);

            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

            // Foreground color from theme
            int qrForegroundColor =
                    MaterialColors.getColor(
                            this, com.google.android.material.R.attr.colorPrimary, 0xFF000000);

            // Transparent background
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? qrForegroundColor : Color.TRANSPARENT);
                }
            }

            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {
        private final List<String> items;
        private final Consumer<String> onClick;

        public MedicineAdapter(List<String> items, Consumer<String> onClick) {
            this.items = items;
            this.onClick = onClick;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();

            MaterialCardView card = new MaterialCardView(context);
            card.setLayoutParams(
                    new RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
            card.setCardElevation(6);
            card.setRadius(28);
            card.setUseCompatPadding(true);
            card.setClickable(true);
            card.setFocusable(true);
            card.setStrokeColor(Color.LTGRAY);
            card.setStrokeWidth(1);
            card.setRippleColor(ColorStateList.valueOf(Color.LTGRAY));
            card.setPadding(0, 0, 0, 0);

            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(32, 36, 32, 36); // ample touch area
            layout.setLayoutParams(
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView text = new TextView(context);
            text.setTextSize(16f);
            text.setTextColor(
                    MaterialColors.getColor(
                            context,
                            com.google.android.material.R.attr.colorOnSurface,
                            Color.WHITE));
            text.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);

            layout.addView(text);
            card.addView(layout);

            return new ViewHolder(card, text);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String name = items.get(position);
            holder.nameText.setText(name);
            holder.itemView.setOnClickListener(v -> onClick.accept(name));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameText;

            ViewHolder(View itemView, TextView textView) {
                super(itemView);
                this.nameText = textView;
            }
        }
    }

    private void setupMedicineBottomSheet() {
        View sheet = findViewById(R.id.medicine_bottom_sheet);
        EditText searchInput = findViewById(R.id.et_search_medicine);
        RecyclerView recyclerView = findViewById(R.id.rv_medicine_list);

        medicineSheetBehavior = BottomSheetBehavior.from(sheet);
        medicineSheetBehavior.setPeekHeight(200); // Optional visible area
        medicineSheetBehavior.setHideable(true);
        medicineSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        filteredNames.clear();
        filteredNames.addAll(medNames);

        adapter =
                new MedicineAdapter(
                        filteredNames,
                        selected -> {
                            selectedMedicine = selected;
                            binding.btnSelectMedicine.setText(selected);
                            showToast("Selected: " + selected);
                            medicineSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Improved search with sorted matches
        searchInput.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void afterTextChanged(Editable s) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String filter = s.toString().toLowerCase(Locale.ROOT);
                        filteredNames.clear();

                        List<String> filtered =
                                medNames.stream()
                                        .filter(n -> n.toLowerCase().contains(filter))
                                        .sorted(
                                                Comparator.comparingInt(
                                                        n -> n.toLowerCase().indexOf(filter)))
                                        .collect(Collectors.toList());

                        filteredNames.addAll(filtered);
                        adapter.notifyDataSetChanged();

                        // Scroll to the top of the filtered list
                        if (!filteredNames.isEmpty()) {
                            recyclerView.scrollToPosition(0);
                        }
                    }
                });

        // Expand the sheet when button is clicked
        binding.btnSelectMedicine.setOnClickListener(
                v -> {
                    if (!medNames.isEmpty()) {
                        medicineSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    } else {
                        showToast("No medicines available");
                    }
                });
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
