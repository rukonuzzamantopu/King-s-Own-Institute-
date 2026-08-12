package com.koi.healthtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Encrypted-at-rest local health database. Every personal/health field is
 * encrypted with AES-GCM before being written to SQLite. The encryption key
 * is held in Android Keystore and no INTERNET permission is requested.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private static final String DATABASE_NAME = "health_tracker.db";
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_EXERCISE = "exercise";
    public static final String TABLE_DIET = "diet";

    public DBHelper(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_EXERCISE + " (id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT NOT NULL, duration TEXT NOT NULL, calories TEXT NOT NULL, entry_datetime TEXT NOT NULL)");
        db.execSQL("CREATE TABLE " + TABLE_DIET + " (id INTEGER PRIMARY KEY AUTOINCREMENT, meal_name TEXT NOT NULL, meal_type TEXT NOT NULL, calories TEXT NOT NULL, entry_datetime TEXT NOT NULL)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DIET);
        onCreate(db);
    }

    private String nowTimestamp() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private String enc(String value) { return CryptoManager.encrypt(value); }
    private String dec(String value) { return CryptoManager.decrypt(value); }

    public long addExercise(String type, int durationMinutes, double calories) {
        try {
            String et = enc(type), ed = enc(String.valueOf(durationMinutes)), ec = enc(String.valueOf(calories)), dt = enc(nowTimestamp());
            if (et == null || ed == null || ec == null || dt == null) return -1;
            ContentValues cv = new ContentValues();
            cv.put("type", et); cv.put("duration", ed); cv.put("calories", ec); cv.put("entry_datetime", dt);
            return getWritableDatabase().insert(TABLE_EXERCISE, null, cv);
        } catch (Exception e) { Log.e(TAG, "addExercise failed", e); return -1; }
    }

    public List<ExerciseEntry> getAllExercise() {
        List<ExerciseEntry> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(TABLE_EXERCISE, null, null, null, null, null, "id DESC")) {
            if (c.moveToFirst()) do {
                String type = dec(c.getString(c.getColumnIndexOrThrow("type")));
                String duration = dec(c.getString(c.getColumnIndexOrThrow("duration")));
                String calories = dec(c.getString(c.getColumnIndexOrThrow("calories")));
                String dt = dec(c.getString(c.getColumnIndexOrThrow("entry_datetime")));
                if (type != null && duration != null && calories != null && dt != null)
                    list.add(new ExerciseEntry(c.getInt(c.getColumnIndexOrThrow("id")), type, Integer.parseInt(duration), Double.parseDouble(calories), dt));
            } while (c.moveToNext());
        } catch (Exception e) { Log.e(TAG, "getAllExercise failed", e); }
        return list;
    }

    public double getTotalCaloriesBurned() {
        double total = 0;
        for (ExerciseEntry e : getAllExercise()) total += e.calories;
        return total;
    }

    public long addDiet(String mealName, String mealType, int calories) {
        try {
            String en = enc(mealName), et = enc(mealType), ec = enc(String.valueOf(calories)), dt = enc(nowTimestamp());
            if (en == null || et == null || ec == null || dt == null) return -1;
            ContentValues cv = new ContentValues();
            cv.put("meal_name", en); cv.put("meal_type", et); cv.put("calories", ec); cv.put("entry_datetime", dt);
            return getWritableDatabase().insert(TABLE_DIET, null, cv);
        } catch (Exception e) { Log.e(TAG, "addDiet failed", e); return -1; }
    }

    public List<DietEntry> getAllDiet() {
        List<DietEntry> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(TABLE_DIET, null, null, null, null, null, "id DESC")) {
            if (c.moveToFirst()) do {
                String name = dec(c.getString(c.getColumnIndexOrThrow("meal_name")));
                String type = dec(c.getString(c.getColumnIndexOrThrow("meal_type")));
                String calories = dec(c.getString(c.getColumnIndexOrThrow("calories")));
                String dt = dec(c.getString(c.getColumnIndexOrThrow("entry_datetime")));
                if (name != null && type != null && calories != null && dt != null)
                    list.add(new DietEntry(c.getInt(c.getColumnIndexOrThrow("id")), name, type, Integer.parseInt(calories), dt));
            } while (c.moveToNext());
        } catch (Exception e) { Log.e(TAG, "getAllDiet failed", e); }
        return list;
    }

    public int getTotalCaloriesConsumed() {
        int total = 0;
        for (DietEntry e : getAllDiet()) total += e.calories;
        return total;
    }

    public void clearAllData() {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_EXERCISE, null, null);
            db.delete(TABLE_DIET, null, null);
        } catch (SQLException e) { Log.e(TAG, "clearAllData failed", e); }
    }
}
