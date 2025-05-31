package com.ramm;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.*;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BRActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1010;
    private static final long MAX_BACKUP_SIZE = 100 * 1024 * 1024; // 100MB

    private TextView tvStatus;
    private ProgressDialog progressDialog;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private File dbPath;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            String path =
                                    FileUtil.convertUriToFilePath(getApplicationContext(), uri);
                            if (path != null) showMergeDialog(path);
                            else showDialog("Error", "Unable to access the selected file.");
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbPath = getDatabasePath("clinicdb");
        setupUI();
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
                && ContextCompat.checkSelfPermission(
                                this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void setupUI() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 80, 48, 32);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(
                MaterialColors.getColor(
                        this, com.google.android.material.R.attr.colorSurface, 0xFFFFFFFF));

        TextView title = new TextView(this);
        title.setText("Backup & Restore");
        title.setTextSize(24);
        title.setTextColor(
                MaterialColors.getColor(
                        this, com.google.android.material.R.attr.colorOnSurface, 0xFF000000));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 48);
        layout.addView(title);

        MaterialButton btnBackup =
                new MaterialButton(
                        this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnBackup.setText("Create Backup");
        layout.addView(btnBackup);

        MaterialButton btnRestore =
                new MaterialButton(
                        this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        btnRestore.setText("Restore / Merge Backup");
        layout.addView(btnRestore);

        tvStatus = new TextView(this);
        tvStatus.setTextSize(14);
        tvStatus.setPadding(0, 40, 0, 0);
        tvStatus.setTextColor(
                MaterialColors.getColor(
                        this,
                        com.google.android.material.R.attr.colorOnSurfaceVariant,
                        0xFF888888));
        tvStatus.setGravity(Gravity.CENTER);
        layout.addView(tvStatus);

        btnBackup.setOnClickListener(v -> performBackupOperation());
        btnRestore.setOnClickListener(v -> showFilePicker());

        setContentView(layout);
    }

    private void showProgress(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showFilePicker() {
        Intent picker = new Intent(Intent.ACTION_GET_CONTENT);
        picker.setType("*/*");
        picker.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(picker, "Select backup file"));
    }

    private void performBackupOperation() {
        showProgress("Creating backup...");
        executor.execute(
                () -> {
                    boolean success = backupDatabase();
                    handler.post(
                            () -> {
                                hideProgress();
                                if (success) {
                                    updateStatus("Last backup: ");
                                    showDialog("Success", "Backup created successfully.");
                                } else {
                                    showDialog("Failed", "Backup failed.");
                                }
                            });
                });
    }

    private void showMergeDialog(String path) {
        new AlertDialog.Builder(this)
                .setTitle("Select Operation")
                .setMessage("Merge will insert/update records. Overwrite will replace everything.")
                .setPositiveButton("Merge", (d, w) -> performMergeOperation(path))
                .setNegativeButton("Overwrite", (d, w) -> performRestoreOperation(path))
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void performMergeOperation(String path) {
        showProgress("Merging backup...");
        executor.execute(
                () -> {
                    boolean success = mergeDatabaseFromFile(path);
                    handler.post(
                            () -> {
                                hideProgress();
                                updateStatus("Last merge: ");
                                showDialog(
                                        success ? "Success" : "Failed",
                                        success ? "Database merged!" : "Merge failed.");
                            });
                });
    }

    private void performRestoreOperation(String path) {
        showProgress("Restoring...");
        executor.execute(
                () -> {
                    boolean success = restoreDatabaseFromFile(path);
                    handler.post(
                            () -> {
                                hideProgress();
                                updateStatus("Last restore: ");
                                showDialog(
                                        success ? "Success" : "Failed",
                                        success ? "Database restored!" : "Restore failed.");
                            });
                });
    }

    private boolean backupDatabase() {
        try {
            File source = dbPath;
            if (!source.exists()) return false;

            File destDir = new File(Environment.getExternalStorageDirectory(), "Clinic/Backup");
            if (!destDir.exists()) destDir.mkdirs();

            String fileName = "clinicdb_" + System.currentTimeMillis() + ".db";
            File dest = new File(destDir, fileName);

            try (FileInputStream in = new FileInputStream(source);
                    FileOutputStream out = new FileOutputStream(dest)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean restoreDatabaseFromFile(String path) {
        try {
            File source = new File(path);
            if (!source.exists()) return false;

            File dest = dbPath;
            try (FileInputStream in = new FileInputStream(source);
                    FileOutputStream out = new FileOutputStream(dest)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean mergeDatabaseFromFile(String path) {
        SQLiteDatabase sourceDb = null;
        SQLiteDatabase destDb = null;

        try {
            File file = new File(path);
            if (!file.exists()) return false;

            sourceDb = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
            destDb =
                    SQLiteDatabase.openDatabase(
                            dbPath.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
            destDb.beginTransaction();

            Cursor tables =
                    sourceDb.rawQuery(
                            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'",
                            null);

            while (tables.moveToNext()) {
                String table = tables.getString(0);
                Cursor rows = sourceDb.rawQuery("SELECT * FROM " + table, null);
                String[] columns = rows.getColumnNames();

                while (rows.moveToNext()) {
                    ContentValues values = new ContentValues();
                    for (String col : columns) {
                        int idx = rows.getColumnIndex(col);
                        switch (rows.getType(idx)) {
                            case Cursor.FIELD_TYPE_STRING:
                                values.put(col, rows.getString(idx));
                                break;
                            case Cursor.FIELD_TYPE_INTEGER:
                                values.put(col, rows.getLong(idx));
                                break;
                            case Cursor.FIELD_TYPE_FLOAT:
                                values.put(col, rows.getDouble(idx));
                                break;
                            case Cursor.FIELD_TYPE_BLOB:
                                values.put(col, rows.getBlob(idx));
                                break;
                            default:
                                values.putNull(col);
                        }
                    }
                    destDb.insertWithOnConflict(
                            table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                }
                rows.close();
            }

            tables.close();
            destDb.setTransactionSuccessful();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (destDb != null) {
                if (destDb.inTransaction()) destDb.endTransaction();
                destDb.close();
            }
            if (sourceDb != null) sourceDb.close();
        }
    }

    private void updateStatus(String prefix) {
        String time =
                new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
        tvStatus.setText(prefix + time);
    }

    private void showDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
