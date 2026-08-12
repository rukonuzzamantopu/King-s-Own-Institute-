package com.koi.healthtracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

/**
 * GDPR Right to Access view: gives the user one place to inspect the complete
 * personal and health data currently stored by this application.
 */
public class MyDataActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private ProfileManager profileManager;
    private TextView tvData;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_data);
        dbHelper = new DBHelper(this);
        profileManager = new ProfileManager(this);
        tvData = findViewById(R.id.tvMyData);
        findViewById(R.id.btnBackToMenu).setOnClickListener(v -> finish());
        renderData();
    }

    private void renderData() {
        StringBuilder out = new StringBuilder();
        out.append("PERSONAL INFORMATION\n\n")
                .append("Name: ").append(profileManager.getName()).append('\n')
                .append("Age: ").append(profileManager.getAge()).append(" years\n")
                .append("Gender: ").append(profileManager.getGender()).append('\n')
                .append(String.format(Locale.getDefault(), "Height: %.1f cm\nWeight: %.1f kg\n", profileManager.getHeight(), profileManager.getWeight()))
                .append("Registration: ").append(profileManager.getRegistrationDate()).append("\n\n");

        out.append("EXERCISE RECORDS\n\n");
        List<ExerciseEntry> exercises = dbHelper.getAllExercise();
        if (exercises.isEmpty()) out.append("No exercise records stored.\n");
        for (ExerciseEntry e : exercises) {
            out.append("• ").append(e.type).append(" — ").append(e.durationMinutes)
                    .append(" min — ").append(String.format(Locale.getDefault(), "%.0f", e.calories))
                    .append(" kcal\n  ").append(e.dateTime).append("\n");
        }

        out.append("\nDIET RECORDS\n\n");
        List<DietEntry> diets = dbHelper.getAllDiet();
        if (diets.isEmpty()) out.append("No diet records stored.\n");
        for (DietEntry e : diets) {
            out.append("• ").append(e.mealName).append(" ( ").append(e.mealType).append(" ) — ")
                    .append(e.calories).append(" kcal\n  ").append(e.dateTime).append("\n");
        }

        out.append("\nACCESS CONTROL\n\n")
                .append("✓ You can review all stored data on this screen.\n")
                .append("✓ You can permanently delete all data from Summary.\n")
                .append("✓ Data is decrypted only in memory for display.\n")
                .append("✓ No health data is transmitted to a server.\n");
        tvData.setText(out.toString());
    }

    @Override protected void onResume() { super.onResume(); if (tvData != null) renderData(); }
    @Override protected void onDestroy() { super.onDestroy(); if (dbHelper != null) dbHelper.close(); }
}
