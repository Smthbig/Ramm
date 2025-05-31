package com.ramm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.Calendar;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "clinicdb";  
    private static final int DB_VERSION = 2;  
    private static final String TABLE_NAME = "patients";  

    public DBHelper(Context context) {  
        super(context, DB_NAME, null, DB_VERSION);  
    }  

    @Override  
    public void onCreate(SQLiteDatabase db) {  
        String query = String.format(  
            "CREATE TABLE IF NOT EXISTS %s (" +  
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +  
            "name TEXT, " +  
            "age TEXT, " +  
            "phone TEXT, " +  
            "visit_payment TEXT, " +  
            "medicine_items TEXT, " +  
            "subtotal REAL, " +  
            "discount REAL, " +  
            "final_amount REAL, " +  
            "payment_mode TEXT, " +  
            "date TEXT)", TABLE_NAME);  
        db.execSQL(query);  
    }  

    public long addPatient(String name, String age, String phone, String visitPayment, String date) {  
        try (SQLiteDatabase db = this.getWritableDatabase()) {  
            ContentValues cv = new ContentValues();  
            cv.put("name", name);  
            cv.put("age", age);  
            cv.put("phone", phone);  
            cv.put("visit_payment", visitPayment);  
            cv.put("medicine_items", "");  
            cv.put("subtotal", 0);  
            cv.put("discount", 0);  
            cv.put("final_amount", 0);  
            cv.put("payment_mode", "");  
            cv.put("date", date);  
            return db.insert(TABLE_NAME, null, cv);  
        }  
    }  

    public int updateMedicineForPatient(String name, String date, String items, float subtotal, float discount, float finalAmount, String paymentMode) {  
        try (SQLiteDatabase db = this.getWritableDatabase()) {  
            ContentValues cv = new ContentValues();  
            cv.put("medicine_items", items);  
            cv.put("subtotal", subtotal);  
            cv.put("discount", discount);  
            cv.put("final_amount", finalAmount);  
            cv.put("payment_mode", paymentMode);  
            return db.update(TABLE_NAME, cv, "name=? AND date=?", new String[]{name, date});  
        }  
    }  

    public Cursor getAllPatients() {  
        SQLiteDatabase db = this.getReadableDatabase();  
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC", null);  
    }  

    public Cursor getTodayPatients(String today) {  
        SQLiteDatabase db = this.getReadableDatabase();  
        return db.rawQuery("SELECT DISTINCT name FROM " + TABLE_NAME + " WHERE date = ?", new String[]{today});  
    }  

    public int getTotalPatientsForCurrentMonth() {  
        Calendar calendar = Calendar.getInstance();  
        int month = calendar.get(Calendar.MONTH) + 1;  
        int year = calendar.get(Calendar.YEAR);  
        String pattern = "%-" + String.format("%02d", month) + "-" + year; // Fixed formatting for month.  

        try (SQLiteDatabase db = this.getReadableDatabase();  
             Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE date LIKE ?", new String[]{pattern})) {  

            if (cursor.moveToFirst()) {  
                return cursor.getInt(0);  
            }  
            return 0;  
        }  
    }  

    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
        if (oldVersion < 2) {  
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN phone TEXT");  
        }  
    }  
}