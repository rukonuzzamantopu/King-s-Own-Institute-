package com.koi.healthtracker;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Locale;

/**
 * Lets the user log an exercise session. Calories burned are estimated using
 * a standard MET (Metabolic Equivalent of Task) formula based on the
 * activity type, duration, and the user's stored body weight:

 *      calories = duration_minutes * (MET * 3.5 * weight_kg) / 200
 */
public class ExerciseActivity extends AppCompatActivity {

    private final String[] exerciseTypes = {
            "Walking", "Running", "Cycling", "Swimming", "Gym Workout", "Yoga"
    };
    // MET values corresponding to exerciseTypes, in the same order.
    private final double[] metValues = {3.5, 8.0, 6.0, 7.0, 6.0, 2.5};

    private Spinner spinnerExerciseType;
    private EditText etDuration;
    private ListView lvExercise;
    private DBHelper dbHelper;
    private ProfileManager profileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        dbHelper = new DBHelper(this);
        profileManager = new ProfileManager(this);

        spinnerExerciseType = findViewById(R.id.spinnerExerciseType);
        etDuration = findViewById(R.id.etDuration);
        lvExercise = findViewById(R.id.lvExercise);
        Button btnAdd = findViewById(R.id.btnAddExercise);
        Button btnBack = findViewById(R.id.btnBackToMenu);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, exerciseTypes);
        spinnerExerciseType.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> addExercise());
        btnBack.setOnClickListener(v -> finish());

        // Retrieve the name passed from MainMenuActivity via putExtra(), using
        // getIntent().getExtras() as required, to personalise this screen.
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("USER_NAME")) {
            String userName = extras.getString("USER_NAME");
            if (userName != null && !userName.isEmpty()) {
                TextView tvTitle = findViewById(R.id.tvTitle);
                tvTitle.setText(getString(R.string.title_log_exercise, userName));
            }
        }

        refreshList();
    }

    private void addExercise() {
        String durationStr = etDuration.getText().toString().trim();

        // ---- Input validation ----
        if (durationStr.isEmpty()) {
            etDuration.setError("Duration is required");
            return;
        }
        int duration;
        try {
            duration = Integer.parseInt(durationStr);
            if (duration <= 0 || duration > 600) {
                etDuration.setError("Enter a realistic duration (1-600 min)");
                return;
            }
        } catch (NumberFormatException e) {
            etDuration.setError("Duration must be a whole number");
            return;
        }

        try {
            int typeIndex = spinnerExerciseType.getSelectedItemPosition();
            if (typeIndex < 0 || typeIndex >= exerciseTypes.length) {
                Toast.makeText(this, "Please select an exercise type", Toast.LENGTH_SHORT).show();
                return;
            }
            String type = exerciseTypes[typeIndex];
            double met = metValues[typeIndex];

            double weight = profileManager.getWeight();
            if (weight <= 0) weight = 70; // sensible fallback if profile weight missing

            double calories = duration * (met * 3.5 * weight) / 200.0;

            long id = dbHelper.addExercise(type, duration, calories);
            if (id == -1) {
                Toast.makeText(this, "Could not save entry. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, String.format(Locale.getDefault(),
                    "%s logged — approx. %.0f kcal burned", type, calories), Toast.LENGTH_SHORT).show();

            etDuration.setText("");
            refreshList();
        } catch (Exception e) {
            // Catch-all guard so an unexpected runtime error never crashes the screen.
            Toast.makeText(this, "Something went wrong while saving your entry.", Toast.LENGTH_LONG).show();
        }
    }

    private void refreshList() {
        try {
            List<ExerciseEntry> entries = dbHelper.getAllExercise();
            lvExercise.setAdapter(new ExerciseAdapter(this, entries));
        } catch (Exception e) {
            Toast.makeText(this, "Could not load exercise history.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
