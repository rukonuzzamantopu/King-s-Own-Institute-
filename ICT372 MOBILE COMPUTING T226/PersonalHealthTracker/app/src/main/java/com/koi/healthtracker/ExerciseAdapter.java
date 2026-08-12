package com.koi.healthtracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

public class ExerciseAdapter extends BaseAdapter {
    private final Context context; private final List<ExerciseEntry> items;
    public ExerciseAdapter(Context context, List<ExerciseEntry> items) { this.context = context; this.items = items; }
    @Override public int getCount() { return items.size(); }
    @Override public Object getItem(int position) { return items.get(position); }
    @Override public long getItemId(int position) { return items.get(position).id; }
    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView == null ? LayoutInflater.from(context).inflate(R.layout.item_exercise, parent, false) : convertView;
        ExerciseEntry e = items.get(position);
        ((TextView)v.findViewById(R.id.tvExerciseType)).setText("🏃 " + e.type);
        ((TextView)v.findViewById(R.id.tvExerciseStats)).setText(String.format(Locale.getDefault(), "%d min    %.0f kcal burned", e.durationMinutes, e.calories));
        ((TextView)v.findViewById(R.id.tvExerciseDate)).setText(e.dateTime);
        return v;
    }
}
