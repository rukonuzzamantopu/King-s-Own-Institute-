package com.koi.healthtracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {
    private ProfileManager profileManager;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_main_menu);
        profileManager = new ProfileManager(this);
        TextView tvWelcome = findViewById(R.id.tvWelcome), tvProfileSummary = findViewById(R.id.tvProfileSummary);
        Bundle extras = getIntent().getExtras();
        String displayName = extras != null && extras.containsKey("USER_NAME") ? extras.getString("USER_NAME") : profileManager.getName();
        String registrationDate = extras != null && extras.containsKey("REGISTRATION_DATE") ? extras.getString("REGISTRATION_DATE") : profileManager.getRegistrationDate();
        renderHeader(tvWelcome, tvProfileSummary, displayName, registrationDate);
        findViewById(R.id.btnExercise).setOnClickListener(v -> open(ExerciseActivity.class, profileManager.getName()));
        findViewById(R.id.btnDiet).setOnClickListener(v -> open(DietActivity.class, profileManager.getName()));
        findViewById(R.id.btnSummary).setOnClickListener(v -> open(SummaryActivity.class, profileManager.getName()));
        findViewById(R.id.btnMyData).setOnClickListener(v -> startActivity(new Intent(this, MyDataActivity.class)));
        findViewById(R.id.btnPrivacy).setOnClickListener(v -> startActivity(new Intent(this, PrivacyInfoActivity.class)));
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class); intent.putExtra("EDIT_MODE", true); startActivity(intent);
        });
    }
    private void renderHeader(TextView tvWelcome, TextView tvProfileSummary, String name, String registrationDate) {
        tvWelcome.setText(getString(R.string.welcome_message, name));
        tvProfileSummary.setText(getString(R.string.profile_summary_format, profileManager.getAge(), profileManager.getGender(), profileManager.getHeight(), profileManager.getWeight(), registrationDate));
    }

    @Override protected void onResume() {
        super.onResume();
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvProfileSummary = findViewById(R.id.tvProfileSummary);
        renderHeader(tvWelcome, tvProfileSummary, profileManager.getName(), profileManager.getRegistrationDate());
    }

    private void open(Class<?> target, String name) { Intent intent = new Intent(this, target); intent.putExtra("USER_NAME", name); startActivity(intent); }
}
