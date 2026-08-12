package com.koi.healthtracker;

/** Simple data model representing one logged exercise activity. */
public class ExerciseEntry {
    public int id;
    public String type;
    public int durationMinutes;
    public double calories;
    public String dateTime;

    public ExerciseEntry(int id, String type, int durationMinutes, double calories, String dateTime) {
        this.id = id;
        this.type = type;
        this.durationMinutes = durationMinutes;
        this.calories = calories;
        this.dateTime = dateTime;
    }
}
