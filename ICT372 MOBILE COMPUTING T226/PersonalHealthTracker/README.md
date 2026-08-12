# Personal Health Tracker — High-Distinction Build

A multi-activity Android health-tracking application developed for ICT372 Mobile Computing Assessment 2.

## Implemented assessment features
- Multi-page navigation with explicit Android Intents.
- Intent data sharing (`USER_NAME`, `REGISTRATION_DATE`) between activities.
- Profile creation and editable profile management with validation and explicit consent.
- Exercise logging with MET-based calorie estimation and encrypted SQLite persistence.
- Diet logging with validation and encrypted SQLite persistence.
- Summary report with calories consumed/burned, net balance, activity counts and BMI educational indicator.
- **My Data** screen implementing the GDPR-style Right to Access by showing complete stored profile, exercise and diet records in one place.
- **Delete My Data** flow implementing the Right to Erasure, including database, profile and encryption-key cleanup.
- AES-GCM encryption for sensitive persisted profile/health values using an Android Keystore-protected key.
- No `INTERNET` permission; health data is not transmitted to a server.
- Internal activities use `android:exported="false"`.
- `android:allowBackup="false"` and `android:usesCleartextTraffic="false"` strengthen the local privacy boundary.
- Persistent privacy/security indicators on major screens.
- Card-style exercise and diet history for improved usability and visual polish.
- Privacy screen documents consent, data minimisation, purpose limitation, access, erasure, secure storage and APP/GDPR considerations.

## Demonstration path
1. Create a profile and tick the consent checkbox.
2. Demonstrate validation using an invalid input, then enter valid data.
3. Show Main Menu Intent navigation.
4. Add an exercise and demonstrate automatic calorie calculation.
5. Add a diet entry.
6. Open Summary and show calorie comparison, BMI and counts.
7. Open **My Data** and demonstrate the complete stored-data view (Right to Access).
8. Open Privacy & Data Security and explain the controls.
9. Demonstrate Edit Profile without deleting exercise/diet history.
10. Demonstrate Delete My Data and show that the app returns to the profile setup screen.

## Important report evidence
For the submitted report, capture Android Studio/device screenshots with the required system time/date visible. Explain each Intent, data-sharing path, storage mechanism, validation rule, privacy control, test result and limitation rather than relying on screenshots alone.
