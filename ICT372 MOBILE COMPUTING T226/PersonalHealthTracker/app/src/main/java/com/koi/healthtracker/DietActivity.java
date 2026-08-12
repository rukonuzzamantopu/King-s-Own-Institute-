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

/** Lets the user log a meal / diet entry with input validation. */
public class DietActivity extends AppCompatActivity {

    private final String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack"};

    private EditText etMealName, etCalories;
    private Spinner spinnerMealType;
    private ListView lvDiet;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet);

        dbHelper = new DBHelper(this);

        etMealName = findViewById(R.id.etMealName);
        etCalories = findViewById(R.id.etCalories);
        spinnerMealType = findViewById(R.id.spinnerMealType);
        lvDiet = findViewById(R.id.lvDiet);
        Button btnAdd = findViewById(R.id.btnAddDiet);
        Button btnBack = findViewById(R.id.btnBackToMenu);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mealTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMealType.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> addDietEntry());
        btnBack.setOnClickListener(v -> finish());

        // Retrieve the name passed from MainMenuActivity via putExtra(), using
        // getIntent().getExtras() as required, to personalise this screen.
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("USER_NAME")) {
            String userName = extras.getString("USER_NAME");
            if (userName != null && !userName.isEmpty()) {
                TextView tvTitle = findViewById(R.id.tvTitle);
                tvTitle.setText(getString(R.string.title_log_diet, userName));
            }
        }
    }

    private void addDietEntry() {
        String mealName = etMealName.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();

        // ---- Input validation ----
        if (mealName.isEmpty()) {
            etMealName.setError("Meal name is required");
            return;
        }
        if (caloriesStr.isEmpty()) {
            etCalories.setError("Calories are required");
            return;
        }
        int calories;
        try {
            calories = Integer.parseInt(caloriesStr);
            if (calories < 0 || calories > 5000) {
                etCalories.setError("Enter a realistic calorie value (0-5000)");
                return;
            }
        } catch (NumberFormatException e) {
            etCalories.setError("Calories must be a whole number");
            return;
        }

        try {
            Object selectedItem = spinnerMealType.getSelectedItem();
            if (selectedItem == null) {
                Toast.makeText(this, "Please select a meal type", Toast.LENGTH_SHORT).show();
                return;
            }
            String mealType = selectedItem.toString();
            long id = dbHelper.addDiet(mealName, mealType, calories);
            if (id == -1) {
                Toast.makeText(this, "Could not save entry. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, mealName + " added to " + mealType, Toast.LENGTH_SHORT).show();

            etMealName.setText("");
            etCalories.setText("");
            refreshList();
        } catch (Exception e) {
            // Catch-all guard so an unexpected runtime error never crashes the screen.
            Toast.makeText(this, "Something went wrong while saving your entry.", Toast.LENGTH_LONG).show();
        }
    }

    private void refreshList() {
        try {
            List<DietEntry> entries = dbHelper.getAllDiet();
            lvDiet.setAdapter(new DietAdapter(this, entries));
        } catch (Exception e) {
            Toast.makeText(this, "Could not load diet history.", Toast.LENGTH_SHORT).show();
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
