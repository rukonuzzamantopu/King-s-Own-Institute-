package com.koi.healthtracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class DietAdapter extends BaseAdapter {
    private final Context context; private final List<DietEntry> items;
    public DietAdapter(Context context, List<DietEntry> items) { this.context = context; this.items = items; }
    @Override public int getCount() { return items.size(); }
    @Override public Object getItem(int position) { return items.get(position); }
    @Override public long getItemId(int position) { return items.get(position).id; }
    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView == null ? LayoutInflater.from(context).inflate(R.layout.item_diet, parent, false) : convertView;
        DietEntry e = items.get(position);
        ((TextView)v.findViewById(R.id.tvMealName)).setText("🍎 " + e.mealName);
        ((TextView)v.findViewById(R.id.tvMealStats)).setText(e.mealType + "    •    " + e.calories + " kcal");
        ((TextView)v.findViewById(R.id.tvMealDate)).setText(e.dateTime);
        return v;
    }
}
