package com.koi.healthtracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/**
 * Aggregates data from the exercise and diet tables to build a summary
 * report, and provides a GDPR / APP-compliant "right to erasure" action.
 */
public class SummaryActivity extends AppCompatActivity {

    private static final int RECOMMENDED_DAILY_CALORIES = 2000;

    private DBHelper dbHelper;
    private ProfileManager profileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        dbHelper = new DBHelper(this);
        profileManager = new ProfileManager(this);

        Button btnBack = findViewById(R.id.btnBackToMenu);
        Button btnDelete = findViewById(R.id.btnDeleteData);
        Button btnMyData = findViewById(R.id.btnViewMyData);

        btnBack.setOnClickListener(v -> finish());
        btnDelete.setOnClickListener(v -> confirmDeleteData());
        btnMyData.setOnClickListener(v -> startActivity(new Intent(this, MyDataActivity.class)));

        // Retrieve the name passed from MainMenuActivity via putExtra(), using
        // getIntent().getExtras() as required, to personalise this screen.
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("USER_NAME")) {
            String userName = extras.getString("USER_NAME");
            if (userName != null && !userName.isEmpty()) {
                TextView tvTitle = findViewById(R.id.tvTitle);
                tvTitle.setText(getString(R.string.title_summary, userName));
            }
        }

        renderSummary();
    }

    private void renderSummary() {
        TextView tvProfileInfo = findViewById(R.id.tvProfileInfo);
        TextView tvConsumed = findViewById(R.id.tvConsumed);
        TextView tvBurned = findViewById(R.id.tvBurned);
        TextView tvNetBalance = findViewById(R.id.tvNetBalance);
        TextView tvBmi = findViewById(R.id.tvBmi);
        TextView tvCounts = findViewById(R.id.tvCounts);
        ProgressBar progressCalories = findViewById(R.id.progressCalories);

        try {
            tvProfileInfo.setText(String.format(Locale.getDefault(),
                    "Name: %s\nAge: %d   Gender: %s\nHeight: %.1f cm   Weight: %.1f kg\nRegistration Date: %s",
                    profileManager.getName(), profileManager.getAge(), profileManager.getGender(),
                    profileManager.getHeight(), profileManager.getWeight(),
                    profileManager.getRegistrationDate()));

            int consumed = dbHelper.getTotalCaloriesConsumed();
            double burned = dbHelper.getTotalCaloriesBurned();
            double net = consumed - burned;

            double heightM = profileManager.getHeight() / 100.0;
            if (heightM > 0 && profileManager.getWeight() > 0) {
                double bmi = profileManager.getWeight() / (heightM * heightM);
                String category = bmi < 18.5 ? "Underweight" : (bmi < 25 ? "Normal range" : (bmi < 30 ? "Overweight" : "Obesity range"));
                tvBmi.setText(String.format(Locale.getDefault(), "BMI: %.1f — %s\nEducational indicator only; not a medical diagnosis.", bmi, category));
            } else {
                tvBmi.setText("BMI: unavailable — complete a valid height and weight profile.");
            }

            tvConsumed.setText(String.format(Locale.getDefault(),
                    "Consumed: %d kcal", consumed));
            tvBurned.setText(String.format(Locale.getDefault(),
                    "Burned: %.0f kcal", burned));

            int progress = (int) Math.min(100,
                    (consumed / (double) RECOMMENDED_DAILY_CALORIES) * 100);
            progressCalories.setProgress(Math.max(progress, 0));

            String balanceLabel = net >= 0
                    ? String.format(Locale.getDefault(), "Net balance: +%.0f kcal (surplus)", net)
                    : String.format(Locale.getDefault(), "Net balance: %.0f kcal (deficit)", net);
            tvNetBalance.setText(balanceLabel);

            int exerciseCount = dbHelper.getAllExercise().size();
            int dietCount = dbHelper.getAllDiet().size();
            tvCounts.setText(String.format(Locale.getDefault(),
                    "Exercise sessions logged: %d\nMeals logged: %d",
                    exerciseCount, dietCount));
        } catch (Exception e) {
            // Guard against any unexpected data issue so the report screen
            // degrades gracefully instead of crashing.
            Toast.makeText(this, "Could not fully load your summary report.", Toast.LENGTH_LONG).show();
        }
    }

    private void confirmDeleteData() {
        new AlertDialog.Builder(this)
                .setTitle("Delete All Data")
                .setMessage("This will permanently erase your profile and all logged exercise/diet " +
                        "data from this device, consistent with your right to erasure under GDPR " +
                        "and the Australian Privacy Principles. This cannot be undone. Continue?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.clearAllData();
                    profileManager.clearProfile();
                    CryptoManager.deleteKey();
                    Toast.makeText(this, "All data deleted", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SummaryActivity.this, ProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderSummary();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
