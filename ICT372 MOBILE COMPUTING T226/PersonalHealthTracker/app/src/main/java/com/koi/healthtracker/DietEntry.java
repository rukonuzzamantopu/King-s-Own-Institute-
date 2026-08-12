package com.koi.healthtracker;

/** Simple data model representing one logged meal / diet entry. */
public class DietEntry {
    public int id;
    public String mealName;
    public String mealType;
    public int calories;
    public String dateTime;

    public DietEntry(int id, String mealName, String mealType, int calories, String dateTime) {
        this.id = id;
        this.mealName = mealName;
        this.mealType = mealType;
        this.calories = calories;
        this.dateTime = dateTime;
    }
}
