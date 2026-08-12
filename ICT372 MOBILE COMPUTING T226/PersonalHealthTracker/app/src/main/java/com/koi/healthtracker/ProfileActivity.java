package com.koi.healthtracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private EditText etName, etAge, etHeight, etWeight;
    private Spinner spinnerGender;
    private CheckBox cbConsent;
    private ProfileManager profileManager;
    private boolean editMode;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toast.makeText(
                this,
                "Welcome! Monitor your health securely. Your data is stored locally on this device.",
                Toast.LENGTH_LONG
        ).show();

        profileManager = new ProfileManager(this);
        editMode = getIntent().getBooleanExtra("EDIT_MODE", false);

        if (profileManager.isProfileSet() && !editMode) { goToMainMenu(); return; }

        etName = findViewById(R.id.etName); etAge = findViewById(R.id.etAge);
        etHeight = findViewById(R.id.etHeight); etWeight = findViewById(R.id.etWeight);
        spinnerGender = findViewById(R.id.spinnerGender); cbConsent = findViewById(R.id.cbConsent);
        Button btnSave = findViewById(R.id.btnSaveProfile);
        btnSave.setText(editMode ? getString(R.string.btn_update_profile) : getString(R.string.btn_save_continue));
        setupGenderSpinner();
        if (editMode) populateExistingProfile();
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void setupGenderSpinner() {
        String[] genders = getResources().getStringArray(R.array.gender_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, genders) {
            @Override public boolean isEnabled(int position) { return position != 0; }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void populateExistingProfile() {
        etName.setText(profileManager.getName()); etAge.setText(String.valueOf(profileManager.getAge()));
        etHeight.setText(String.valueOf(profileManager.getHeight())); etWeight.setText(String.valueOf(profileManager.getWeight()));
        String gender = profileManager.getGender();
        String[] genders = getResources().getStringArray(R.array.gender_options);
        for (int i = 0; i < genders.length; i++) if (genders[i].equals(gender)) { spinnerGender.setSelection(i); break; }
        cbConsent.setChecked(profileManager.isConsentGiven());
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        if (name.isEmpty()) { etName.setError(getString(R.string.err_name_required)); return; }
        int age; double height, weight;
        try { age = Integer.parseInt(ageStr); if (age <= 0 || age > 120) throw new Exception(); }
        catch (Exception e) { etAge.setError(getString(R.string.err_age_invalid)); return; }
        try { height = Double.parseDouble(heightStr); if (height <= 0 || height > 300) throw new Exception(); }
        catch (Exception e) { etHeight.setError(getString(R.string.err_height_invalid)); return; }
        try { weight = Double.parseDouble(weightStr); if (weight <= 0 || weight > 400) throw new Exception(); }
        catch (Exception e) { etWeight.setError(getString(R.string.err_weight_invalid)); return; }
        if (spinnerGender.getSelectedItemPosition() == 0) { Toast.makeText(this, R.string.msg_gender_required, Toast.LENGTH_SHORT).show(); return; }
        if (!cbConsent.isChecked()) { Toast.makeText(this, R.string.msg_consent_required, Toast.LENGTH_LONG).show(); return; }
        try {
            profileManager.saveProfile(name, age, spinnerGender.getSelectedItem().toString(), height, weight);
            profileManager.setConsentGiven(true);
            String message = editMode ? getString(R.string.msg_profile_updated) : getString(R.string.msg_profile_saved);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            goToMainMenu();
        } catch (Exception e) { Toast.makeText(this, "Unable to securely save your profile. Please try again.", Toast.LENGTH_LONG).show(); }
    }

    private void goToMainMenu() {
        Intent intent = new Intent(ProfileActivity.this, MainMenuActivity.class);
        intent.putExtra("USER_NAME", profileManager.getName());
        intent.putExtra("REGISTRATION_DATE", profileManager.getRegistrationDate());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent); finish();
    }
}
