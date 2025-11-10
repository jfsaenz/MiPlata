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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.miplata.R;
import com.miplata.data.AppDatabase;
import com.miplata.data.Transaction;
import com.miplata.ui.LoginActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private TextView tvBalance;
    private PieChart monthlyExpensesChart;
    private PieChart balanceByAccountChart;
    private int userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = requireActivity().getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        userId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvBalance = view.findViewById(R.id.tvBalance);
        monthlyExpensesChart = view.findViewById(R.id.monthly_expenses_chart);
        balanceByAccountChart = view.findViewById(R.id.balance_by_account_chart);

        if (userId != -1) {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            db.transactionDao().getBalance(userId).observe(getViewLifecycleOwner(), balance -> {
                if (balance != null) {
                    tvBalance.setText(String.format(Locale.US, "$%,.2f COP", balance));
                }
            });

            db.transactionDao().getAllTransactions(userId).observe(getViewLifecycleOwner(), transactions -> {
                updateMonthlyExpensesChart(transactions);
                updateBalanceByAccountChart(transactions);
            });
        }

        return view;
    }

    private void updateMonthlyExpensesChart(List<Transaction> transactions) {
        Map<String, Float> categoryTotals = new HashMap<>();
        float totalExpenses = 0f;

        if (transactions != null) {
            for (Transaction t : transactions) {
                if ("DEBIT".equals(t.getType())) {
                    totalExpenses += t.getAmount();
                    String category = t.getCategory() != null ? t.getCategory() : "Otros";
                    categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + (float) t.getAmount());
                }
            }
        }

        if (totalExpenses == 0f) {
            setupEmptyChartView(monthlyExpensesChart, "Sin gastos este mes");
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        setupDefaultPieDataSet(dataSet);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(monthlyExpensesChart));

        setupDefaultPieChart(monthlyExpensesChart, pieData, String.format(Locale.US, "$%,.2f", totalExpenses));
        monthlyExpensesChart.invalidate();
    }

    private void updateBalanceByAccountChart(List<Transaction> transactions) {
        Map<String, Double> accountBalances = new HashMap<>();

        if (transactions != null) {
            for (Transaction t : transactions) {
                String account = getAccountFromTransaction(t);
                double amount = t.getAmount();
                double currentBalance = accountBalances.getOrDefault(account, 0.0);
                if ("CREDIT".equals(t.getType())) {
                    accountBalances.put(account, currentBalance + amount);
                } else if ("DEBIT".equals(t.getType())) {
                    accountBalances.put(account, currentBalance - amount);
                }
            }
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : accountBalances.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        if (entries.isEmpty()) {
            setupEmptyChartView(balanceByAccountChart, "Aún no hay movimientos");
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        setupDefaultPieDataSet(dataSet);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter(balanceByAccountChart));

        setupDefaultPieChart(balanceByAccountChart, pieData, "Saldo por Cuenta");
        balanceByAccountChart.invalidate();
    }

    private String getAccountFromTransaction(Transaction transaction) {
        String description = transaction.getDescription() != null ? transaction.getDescription().toLowerCase() : "";
        if (description.contains("nequi")) return "Nequi";
        if (description.contains("bancolombia")) return "Bancolombia";
        if (description.contains("daviplata")) return "DaviPlata";
        return "Otra";
    }

    private void setupDefaultPieChart(PieChart chart, PieData data, String centerText) {
        chart.setData(data);
        chart.getDescription().setEnabled(false);
        chart.setRotationEnabled(true);
        chart.setUsePercentValues(true);
        chart.setHoleColor(Color.TRANSPARENT);
        chart.setTransparentCircleColor(Color.TRANSPARENT);
        chart.setHoleRadius(58f);
        chart.setCenterText(centerText);
        chart.setCenterTextSize(20f);
        chart.setCenterTextColor(ContextCompat.getColor(getContext(), R.color.textColorPrimary));
        chart.getLegend().setEnabled(false);
        chart.animateY(1400, Easing.EaseInOutQuad);
    }

    private void setupDefaultPieDataSet(PieDataSet dataSet) {
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(getContext(), R.color.chartColor1));
        colors.add(ContextCompat.getColor(getContext(), R.color.chartColor2));
        colors.add(ContextCompat.getColor(getContext(), R.color.chartColor3));
        colors.add(ContextCompat.getColor(getContext(), R.color.chartColor4));
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(3f);
    }

    private void setupEmptyChartView(PieChart chart, String text) {
        chart.clear();
        chart.setCenterText(text);
        chart.setCenterTextColor(ContextCompat.getColor(getContext(), R.color.textColorSecondary));
        chart.setCenterTextSize(16f);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.invalidate();
    }
}
