package com.koi.healthtracker;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Private local profile store. Sensitive values are AES-GCM encrypted with a
 * key protected by Android Keystore before being written to SharedPreferences.
 */
public class ProfileManager {
    private static final String PREFS_NAME = "health_tracker_profile";
    private static final String KEY_NAME = "name";
    private static final String KEY_AGE = "age";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_CONSENT = "privacy_consent_given";
    private static final String KEY_PROFILE_SET = "profile_set";
    private static final String KEY_REGISTRATION_DATE = "registration_date";
    private final SharedPreferences prefs;

    public ProfileManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private void putEncrypted(SharedPreferences.Editor editor, String key, String value) {
        String encrypted = CryptoManager.encrypt(value);
        if (encrypted == null) throw new IllegalStateException("Unable to protect local data");
        editor.putString(key, encrypted);
    }

    private String getEncrypted(String key, String fallback) {
        String stored = prefs.getString(key, null);
        if (stored == null) return fallback;
        String value = CryptoManager.decrypt(stored);
        return value == null ? fallback : value;
    }

    public void saveProfile(String name, int age, String gender, double height, double weight) {
        SharedPreferences.Editor editor = prefs.edit();
        putEncrypted(editor, KEY_NAME, name);
        putEncrypted(editor, KEY_AGE, String.valueOf(age));
        putEncrypted(editor, KEY_GENDER, gender);
        putEncrypted(editor, KEY_HEIGHT, String.valueOf(height));
        putEncrypted(editor, KEY_WEIGHT, String.valueOf(weight));
        if (!prefs.contains(KEY_REGISTRATION_DATE)) {
            putEncrypted(editor, KEY_REGISTRATION_DATE,
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date()));
        }
        editor.putBoolean(KEY_PROFILE_SET, true);
        editor.apply();
    }

    public String getRegistrationDate() { return getEncrypted(KEY_REGISTRATION_DATE, "Unknown"); }
    public boolean isProfileSet() { return prefs.getBoolean(KEY_PROFILE_SET, false); }
    public String getName() { return getEncrypted(KEY_NAME, ""); }
    public int getAge() { try { return Integer.parseInt(getEncrypted(KEY_AGE, "0")); } catch (Exception e) { return 0; } }
    public String getGender() { return getEncrypted(KEY_GENDER, ""); }
    public double getHeight() { try { return Double.parseDouble(getEncrypted(KEY_HEIGHT, "0")); } catch (Exception e) { return 0; } }
    public double getWeight() { try { return Double.parseDouble(getEncrypted(KEY_WEIGHT, "0")); } catch (Exception e) { return 0; } }
    public void setConsentGiven(boolean given) { prefs.edit().putBoolean(KEY_CONSENT, given).apply(); }
    public boolean isConsentGiven() { return prefs.getBoolean(KEY_CONSENT, false); }
    public void clearProfile() { prefs.edit().clear().apply(); }
}
