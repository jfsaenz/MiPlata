package com.miplata.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.miplata.R;
import com.miplata.data.AppDatabase;
import com.miplata.data.Transaction;
import com.miplata.ui.LoginActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private TextView tvBalance;
    private PieChart pieChart;
    private BarChart barChart;
    private int userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        userId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvBalance = view.findViewById(R.id.tvBalance);
        pieChart = view.findViewById(R.id.pieChart);
        barChart = view.findViewById(R.id.barChart);

        if (userId != -1) {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            db.transactionDao().getBalance(userId).observe(getViewLifecycleOwner(), balance -> {
                if (balance != null) {
                    tvBalance.setText(String.format(Locale.getDefault(), "$%,.2f COP", balance));
                }
            });

            db.transactionDao().getAllTransactions(userId).observe(getViewLifecycleOwner(), transactions -> {
                updatePieChart(transactions);
                updateBarChart(transactions);
            });
        }

        return view;
    }

    private void updatePieChart(java.util.List<Transaction> transactions) {
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
                String category = t.getCategory() != null ? t.getCategory() : "Otros";
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
        colors.add(getResources().getColor(R.color.colorPrimaryDark));
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
    }

    private void updateBarChart(java.util.List<Transaction> transactions) {
        // Datos de ejemplo
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 150000f));
        entries.add(new BarEntry(1, 300000f));
        entries.add(new BarEntry(2, 80000f));

        final String[] labels = new String[]{"Nequi", "Bancolombia", "DaviPlata"};

        BarDataSet dataSet = new BarDataSet(entries, "Saldo por Cuenta");
        dataSet.setColor(getResources().getColor(R.color.colorPrimaryDark));

        BarData barData = new BarData(dataSet);
        barData.setValueTextSize(12f);
        barData.setValueTextColor(Color.WHITE);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);

        barChart.getAxisLeft().setTextColor(Color.WHITE);
        barChart.getAxisRight().setEnabled(false);
        barChart.animateY(1400, Easing.EaseInOutQuad);
        barChart.invalidate();
    }
}
