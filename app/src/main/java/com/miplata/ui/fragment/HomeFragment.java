package com.miplata.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.miplata.R;
import com.miplata.data.AppDatabase;
import com.miplata.data.Transaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private TextView tvBalance;
    private PieChart pieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvBalance = view.findViewById(R.id.tvBalance);
        pieChart = view.findViewById(R.id.pieChart);

        AppDatabase db = AppDatabase.getDatabase(getContext());
        db.transactionDao().getBalance().observe(getViewLifecycleOwner(), balance -> {
            if (balance != null) {
                tvBalance.setText(String.format(Locale.getDefault(), "$%,.2f COP", balance));
            }
        });

        db.transactionDao().getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions == null || transactions.isEmpty()) {
                pieChart.clear();
                pieChart.invalidate();
                return;
            }

            Map<String, Float> categoryTotals = new HashMap<>();
            float totalExpenses = 0f;

            for (Transaction t : transactions) {
                if ("DEBIT".equals(t.getType())) {
                    totalExpenses += t.getAmount();
                    String description = t.getDescription().toLowerCase();
                    String category = classifyExpense(description);
                    categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + (float) t.getAmount());
                }
            }

            ArrayList<PieEntry> entries = new ArrayList<>();
            for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
                if (entry.getValue() > 0) {
                    entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                }
            }

            PieDataSet dataSet = new PieDataSet(entries, "Gastos del Mes");

            ArrayList<Integer> colors = new ArrayList<>();
            colors.add(getResources().getColor(R.color.colorAccent));
            colors.add(getResources().getColor(android.R.color.holo_orange_light));
            colors.add(getResources().getColor(android.R.color.holo_purple));
            colors.add(getResources().getColor(android.R.color.holo_blue_light));
            dataSet.setColors(colors);

            PieData pieData = new PieData(dataSet);
            pieData.setValueTextSize(12f);
            pieData.setValueTextColor(Color.WHITE);

            pieChart.setData(pieData);
            pieChart.getDescription().setEnabled(false);
            pieChart.setCenterText(String.format(Locale.getDefault(), "$%,.2f", totalExpenses));
            pieChart.setCenterTextSize(20f);
            pieChart.setCenterTextColor(Color.WHITE);
            pieChart.getLegend().setEnabled(false);
            pieChart.animateY(1400, Easing.EaseInOutQuad);
            pieChart.invalidate();
        });

        return view;
    }

    private String classifyExpense(String description) {
        if (description.contains("comida") || description.contains("restaurante") || description.contains("mercado")) {
            return "Comida";
        } else if (description.contains("transporte") || description.contains("uber") || description.contains("didi")) {
            return "Transporte";
        } else if (description.contains("entretenimiento") || description.contains("cine") || description.contains("spotify")) {
            return "Entretenimiento";
        } else {
            return "Otros";
        }
    }
}
