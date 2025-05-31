package com.ramm;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportActivity extends AppCompatActivity {

    private EditText etFromDate, etToDate;
    private TextView tvPatientCount;
    private RecyclerView listMedicines;
    private Button btnExport, btnShow;

    private final ArrayList<HashMap<String, Object>> reportList = new ArrayList<>();
    private ReportAdapter adapter;
    private final Calendar calendar = Calendar.getInstance();
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        etFromDate = findViewById(R.id.et_from_date);
        etToDate = findViewById(R.id.et_to_date);
        tvPatientCount = findViewById(R.id.tv_patient_count);
        btnExport = findViewById(R.id.btn_export_pdf);
        btnShow = findViewById(R.id.btn_show_report);
        listMedicines = findViewById(R.id.list_medicines);

        listMedicines.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportAdapter(this, reportList);
        listMedicines.setAdapter(adapter);

        dbHelper = new DBHelper(this);

        etFromDate.setOnClickListener(v -> showDateDialog(etFromDate));
        etToDate.setOnClickListener(v -> showDateDialog(etToDate));

        btnShow.setOnClickListener(v -> {
            String from = etFromDate.getText().toString().trim();
            String to = etToDate.getText().toString().trim();
            if (from.isEmpty() || to.isEmpty()) {
                showToast("Please select both dates");
            } else {
                loadReportData(from, to);
            }
        });

        btnExport.setOnClickListener(v -> exportPDF());
    }

    private void showDateDialog(EditText target) {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String date = String.format(Locale.getDefault(), "%02d-%02d-%d", day, month + 1, year);
                    target.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void loadReportData(String from, String to) {
        reportList.clear();
        Map<String, Float> qtyMap = new HashMap<>();
        Map<String, Float> totalMap = new HashMap<>();
        Map<String, Float> paidMap = new HashMap<>();

        try (var db = dbHelper.getReadableDatabase();
             var cursor = db.rawQuery(
                     "SELECT medicine_items, final_amount FROM patients WHERE date BETWEEN ? AND ?",
                     new String[]{from, to})) {

            while (cursor.moveToNext()) {
                String meds = cursor.getString(cursor.getColumnIndexOrThrow("medicine_items"));
                float finalAmt = cursor.getFloat(cursor.getColumnIndexOrThrow("final_amount"));

                if (meds != null && !meds.isEmpty()) {
                    float sum = 0;
                    Map<String, Float> thisTotal = new HashMap<>();
                    Map<String, Float> thisQty = new HashMap<>();

                    for (String line : meds.split("\n")) {
                        try {
                            String[] parts = line.split("x");
                            String name = parts[0].trim();
                            String[] sub = parts[1].split("=");
                            float qty = Float.parseFloat(sub[0].trim());
                            float price = Float.parseFloat(sub[1].replace("Rs.", "").trim());

                            thisQty.put(name, qty);
                            thisTotal.put(name, price);
                            sum += price;
                        } catch (Exception ignored) {
                        }
                    }

                    for (String name : thisTotal.keySet()) {
                        float qty = thisQty.get(name);
                        float price = thisTotal.get(name);
                        float paid = sum > 0 ? (price / sum) * finalAmt : 0;

                        qtyMap.merge(name, qty, Float::sum);
                        totalMap.merge(name, price, Float::sum);
                        paidMap.merge(name, paid, Float::sum);
                    }
                }
            }
        }

        List<Map.Entry<String, Float>> sorted = new ArrayList<>(paidMap.entrySet());
        sorted.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));

        float totalSum = 0, paidSum = 0;

        for (var entry : sorted) {
            String name = entry.getKey();
            float qty = qtyMap.get(name);
            float total = totalMap.get(name);
            float paid = paidMap.get(name);

            totalSum += total;
            paidSum += paid;

            HashMap<String, Object> map = new HashMap<>();
            map.put("title", name);
            map.put("subtitle", "Qty: " + qty + " | Total: Rs. " + total + " | Paid: Rs. " + paid);
            reportList.add(map);
        }

        tvPatientCount.setText("Total Medicines: " + reportList.size()
                + " | Grand Total: Rs. " + totalSum
                + " | Paid: Rs. " + paidSum);

        adapter.notifyDataSetChanged();
    }

    private void showEditDialog(String medName) {
        Context context = this;

        MaterialCardView card = new MaterialCardView(context);
        card.setCardElevation(8f);
        card.setRadius(24f);
        card.setStrokeWidth(1);
        card.setStrokeColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOutline, Color.GRAY));
        card.setCardBackgroundColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, Color.WHITE));
        card.setUseCompatPadding(true);
        card.setPreventCornerOverlap(true);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 32);

        TextInputLayout qtyLayout = new TextInputLayout(context, null,
                com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox);
        qtyLayout.setHint("New Quantity");
        qtyLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        TextInputEditText etQty = new TextInputEditText(context);
        etQty.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        qtyLayout.addView(etQty);

        TextInputLayout totalLayout = new TextInputLayout(context, null,
                com.google.android.material.R.style.Widget_Material3_TextInputLayout_OutlinedBox);
        totalLayout.setHint("New Total (Rs.)");
        totalLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        TextInputEditText etTotal = new TextInputEditText(context);
        etTotal.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        totalLayout.addView(etTotal);

        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);
        btnRow.setPadding(0, 40, 0, 0);

        MaterialButton btnSave = new MaterialButton(context, null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnSave.setText("Save");
        btnSave.setIconResource(android.R.drawable.ic_menu_save);

        MaterialButton btnCancel = new MaterialButton(context, null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnCancel.setText("Cancel");
        btnCancel.setIconResource(android.R.drawable.ic_menu_close_clear_cancel);

        btnRow.addView(btnCancel);
        btnRow.addView(btnSave);

        layout.addView(qtyLayout);
        layout.addView(totalLayout);
        layout.addView(btnRow);
        card.addView(layout);

        AlertDialog dialog = new AlertDialog.Builder(context).setView(card).create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            try {
                float qty = Float.parseFloat(etQty.getText().toString().trim());
                float total = Float.parseFloat(etTotal.getText().toString().trim());
                updateMedicineInDatabase(medName, qty, total);
                loadReportData(etFromDate.getText().toString(), etToDate.getText().toString());
                showToast("Updated");
                dialog.dismiss();
            } catch (Exception e) {
                showToast("Invalid input");
            }
        });

        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        });

        dialog.show();
    }

    private void updateMedicineInDatabase(String medName, float newQty, float newTotal) {
        try (var db = dbHelper.getWritableDatabase();
             var cursor = db.rawQuery("SELECT id, medicine_items FROM patients", null)) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String items = cursor.getString(cursor.getColumnIndexOrThrow("medicine_items"));
                if (items.contains(medName)) {
                    StringBuilder updated = new StringBuilder();
                    for (String line : items.split("\n")) {
                        if (line.startsWith(medName)) {
                            updated.append(medName).append(" x").append(newQty).append(" = Rs. ").append(newTotal).append("\n");
                        } else {
                            updated.append(line).append("\n");
                        }
                    }
                    ContentValues cv = new ContentValues();
                    cv.put("medicine_items", updated.toString().trim());
                    db.update("patients", cv, "id = ?", new String[]{String.valueOf(id)});
                }
            }
        }
    }

    private void exportPDF() {
        if (reportList.isEmpty()) {
            showToast("No data to export");
            return;
        }

        Paint paint = new Paint();
        Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setStyle(Paint.Style.STROKE);
        line.setColor(Color.BLACK);
        line.setStrokeWidth(1);

        int pageWidth = 595;
        int pageHeight = 842;
        int x = 20, y = 50, rowHeight = 30;
        float total = 0f, paid = 0f;

        PdfDocument pdf = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdf.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(16);
        canvas.drawText("Medicine Sales Report", x + 160, y, paint);
        y += rowHeight;

        String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
        paint.setTextSize(12);
        canvas.drawText("Exported: " + timestamp, x, y, paint);
        y += rowHeight;

        for (int i = 0; i < reportList.size(); i++) {
            if (y + rowHeight > pageHeight - 50) {
                pdf.finishPage(page);
                page = pdf.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
                drawHeader(canvas, paint, line, x, y, rowHeight);
                y += rowHeight;
            }

            HashMap<String, Object> item = reportList.get(i);
            String name = item.get("title").toString();
            String subtitle = item.get("subtitle").toString();
            String[] parts = subtitle.split("\\|");
            String qty = parts[0].split(":")[1].trim();
            float t = Float.parseFloat(parts[1].split(":")[1].replace("Rs.", "").trim());
            float p = Float.parseFloat(parts[2].split(":")[1].replace("Rs.", "").trim());

            total += t;
            paid += p;

            canvas.drawRect(x, y, pageWidth - x, y + rowHeight, line);
            canvas.drawText(name, x + 10, y + 20, paint);
            canvas.drawText(qty, x + 200, y + 20, paint);
            canvas.drawText(String.format("%.2f", t), x + 300, y + 20, paint);
            canvas.drawText(String.format("%.2f", p), x + 400, y + 20, paint);
            y += rowHeight;
        }

        canvas.drawRect(x, y, pageWidth - x, y + rowHeight, line);
        paint.setFakeBoldText(true);
        canvas.drawText("TOTAL", x + 200, y + 20, paint);
        canvas.drawText(String.format("%.2f", total), x + 300, y + 20, paint);
        canvas.drawText(String.format("%.2f", paid), x + 400, y + 20, paint);

        pdf.finishPage(page);

        try {
            File folder = new File(Environment.getExternalStorageDirectory(), "Clinic/MedicineReports");
            if (!folder.exists()) folder.mkdirs();

            File file = new File(folder, "medicine_report_" + System.currentTimeMillis() + ".pdf");
            try (FileOutputStream out = new FileOutputStream(file)) {
                pdf.writeTo(out);
                showToast("PDF saved: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            showToast("Export failed");
        }

        pdf.close();
    }

    private void drawHeader(Canvas canvas, Paint paint, Paint line, int x, int y, int rowHeight) {
        paint.setFakeBoldText(true);
        canvas.drawRect(x, y, 575, y + rowHeight, line);
        canvas.drawText("Medicine", x + 10, y + 20, paint);
        canvas.drawText("Qty", x + 200, y + 20, paint);
        canvas.drawText("Total", x + 300, y + 20, paint);
        canvas.drawText("Paid", x + 400, y + 20, paint);
        paint.setFakeBoldText(false);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // RecyclerView Adapter
    public static class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
        private final Context context;
        private final List<HashMap<String, Object>> data;

        public ReportAdapter(Context context, List<HashMap<String, Object>> data) {
            this.context = context;
            this.data = data;
        }

        public static class ReportViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardView;
            TextView title, subtitle;

            public ReportViewHolder(View itemView) {
                super(itemView);
                cardView = (MaterialCardView) itemView;
                title = itemView.findViewById(R.id.tv_title);
                subtitle = itemView.findViewById(R.id.tv_subtitle);
            }
        }

        @Override
        public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MaterialCardView card = new MaterialCardView(context);
            card.setCardElevation(4f);
            card.setRadius(16f);
            card.setStrokeWidth(1);
            card.setUseCompatPadding(true);
            card.setPreventCornerOverlap(true);
            card.setCardBackgroundColor(
                    MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, Color.WHITE));
            card.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearLayout content = new LinearLayout(context);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(32, 24, 32, 24);

            TextView title = new TextView(context);
            title.setId(R.id.tv_title);
            title.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
            title.setTypeface(null, Typeface.BOLD);
            title.setTextColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, Color.BLACK));

            TextView subtitle = new TextView(context);
            subtitle.setId(R.id.tv_subtitle);
            subtitle.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Body2);
            subtitle.setTextColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.DKGRAY));

            content.addView(title);
            content.addView(subtitle);
            card.addView(content);

            return new ReportViewHolder(card);
        }

        @Override
        public void onBindViewHolder(ReportViewHolder holder, int position) {
            var item = data.get(position);
            holder.title.setText(item.get("title").toString());
            holder.subtitle.setText(item.get("subtitle").toString());

            holder.cardView.setOnLongClickListener(v -> {
                if (context instanceof ReportActivity) {
                    ((ReportActivity) context).showEditDialog(item.get("title").toString());
                }
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}