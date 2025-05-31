package com.ramm;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import com.google.android.material.color.MaterialColors;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.ramm.databinding.DoctorfeeBinding;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DoctorfeeActivity extends BaseActivity {

    private DoctorfeeBinding binding;
    private Bitmap qrBitmap;
    private SharedPreferences prefs;

    private static final String PREFS_NAME = "AppSettings";
    private static final String DEFAULT_UPI_ID = "xxxxxxxxx@axl";
    private static final String DEFAULT_UPI_NAME = "OPD fee ramm health care";
    private static final float DEFAULT_UPI_AMOUNT = 800f;
    private static final String DEFAULT_UPI_NOTE = "OPD fee from RAMM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DoctorfeeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        setupLogic();

        new Thread(
                        () -> {
                            String upiId = prefs.getString("doctor_upi_id", DEFAULT_UPI_ID);
                            String upiName = prefs.getString("doctor_upi_name", DEFAULT_UPI_NAME);
                            float amount = prefs.getFloat("doctor_upi_amount", DEFAULT_UPI_AMOUNT);
                            String note = prefs.getString("doctor_upi_note", DEFAULT_UPI_NOTE);
                            qrBitmap = generateUPIQR(upiId, upiName, amount, note);
                        })
                .start();
    }

    private void setupLogic() {
        initPaymentToggle();
        binding.btnSubmit.setOnClickListener(
                v -> {
                    try {
                        savePatientToDatabase();
                    } catch (Exception e) {
                        showToast("Error: " + e.getMessage());
                    }
                });
    }

    private void initPaymentToggle() {
        binding.rgPayment.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    if (checkedId == R.id.rb_online) {
                        showQRDialog();
                    }
                });
    }

    private void savePatientToDatabase() {
        String name = binding.etName.getText().toString().trim();
        String ageValue = binding.etAge.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();

        if (name.isEmpty()) {
            showToast("Please enter name");
            return;
        }

        String ageUnit =
                binding.rbMonths.isChecked()
                        ? "Months"
                        : binding.rbDays.isChecked() ? "Days" : "Years";
        String age = ageValue + " " + ageUnit;
        String payment = binding.rbCash.isChecked() ? "Cash" : "Online";
        String date = getTodayDate();

        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("age", age);
        cv.put("phone", phone);
        cv.put("visit_payment", payment);
        cv.put("medicine_items", "");
        cv.put("subtotal", 0);
        cv.put("discount", 0);
        cv.put("final_amount", 0);
        cv.put("payment_mode", "");
        cv.put("date", date);

        long result = db.insert("patients", null, cv);
        db.close();

        if (result != -1) {
            showToast("Saved successfully");
            resetForm();
        } else {
            showToast("Insert failed");
        }
    }

    private String getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH)
                + "-"
                + (calendar.get(Calendar.MONTH) + 1)
                + "-"
                + calendar.get(Calendar.YEAR);
    }

    private void resetForm() {
        binding.etName.setText("");
        binding.etAge.setText("");
        binding.etPhone.setText("");
        binding.rbYears.setChecked(true);
        binding.rbCash.setChecked(true);
    }

    private void showQRDialog() {
        Dialog dialog =
                new Dialog(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_qr);

        ImageView imgQr = dialog.findViewById(R.id.img_qr_dialog);
        ProgressBar progress = dialog.findViewById(R.id.qr_progress);
        Button btnClose = dialog.findViewById(R.id.btn_close_dialog);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int qrSize = (int) (Math.min(screenWidth, screenHeight) * 0.85);

        imgQr.getLayoutParams().width = qrSize;
        imgQr.getLayoutParams().height = qrSize;
        imgQr.requestLayout();

        imgQr.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        new Thread(
                        () -> {
                            String upiId = prefs.getString("doctor_upi_id", DEFAULT_UPI_ID);
                            String upiName = prefs.getString("doctor_upi_name", DEFAULT_UPI_NAME);
                            float amount = prefs.getFloat("doctor_upi_amount", DEFAULT_UPI_AMOUNT);
                            String note = prefs.getString("doctor_upi_note", DEFAULT_UPI_NOTE);

                            Bitmap generatedQR = generateUPIQR(upiId, upiName, amount, note);

                            runOnUiThread(
                                    () -> {
                                        if (generatedQR != null) {
                                            qrBitmap = generatedQR;
                                            imgQr.setImageBitmap(qrBitmap);
                                            imgQr.setVisibility(View.VISIBLE);
                                            progress.setVisibility(View.GONE);
                                        } else {
                                            showToast("Failed to generate QR");
                                            dialog.dismiss();
                                        }
                                    });
                        })
                .start();

        if (dialog.getWindow() != null) {
            dialog.getWindow()
                    .setLayout(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private Bitmap generateUPIQR(String upiId, String name, float amount, String txnNote) {
        String upiUri =
                "upi://pay?pa="
                        + upiId
                        + "&pn="
                        + Uri.encode(name)
                        + "&am="
                        + amount
                        + "&cu=INR&tn="
                        + Uri.encode(txnNote);
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            int size = 1000;
            BitMatrix bitMatrix = writer.encode(upiUri, BarcodeFormat.QR_CODE, size, size, hints);
            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

            int qrColor =
                    MaterialColors.getColor(
                            this, com.google.android.material.R.attr.colorPrimary, Color.BLACK);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? qrColor : Color.TRANSPARENT);
                }
            }

            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
