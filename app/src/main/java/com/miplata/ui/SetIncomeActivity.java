package com.miplata.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.miplata.R;
import com.miplata.data.AppDatabase;
import com.miplata.data.Transaction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SetIncomeActivity extends AppCompatActivity {

    private EditText etMonthlyIncome;
    private Button btnContinue;
    private AppDatabase db;
    private ExecutorService executorService;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_income);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Saldo Inicial");

        etMonthlyIncome = findViewById(R.id.et_monthly_income);
        btnContinue = findViewById(R.id.btn_continue_to_interests);

        db = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        userId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        if (userId == -1) {
            Toast.makeText(this, "Error: No se pudo encontrar el usuario.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnContinue.setOnClickListener(v -> saveInitialIncome());
    }

    private void saveInitialIncome() {
        String incomeStr = etMonthlyIncome.getText().toString();

        if (incomeStr.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa tu ingreso mensual", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double incomeAmount = Double.parseDouble(incomeStr);

            Transaction initialIncome = new Transaction();
            initialIncome.setUserId(userId);
            initialIncome.setAmount(incomeAmount);
            initialIncome.setType("CREDIT");
            initialIncome.setDescription("Ingreso Inicial");
            initialIncome.setCategory("Ingresos");
            initialIncome.setDateMillis(System.currentTimeMillis());

            executorService.execute(() -> {
                db.transactionDao().insert(initialIncome);
                runOnUiThread(() -> {
                    // Navegar a la siguiente pantalla
                    Intent intent = new Intent(SetIncomeActivity.this, SelectInterestsActivity.class);
                    startActivity(intent);
                    finish(); // Cierra esta actividad para que el usuario no pueda volver
                });
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, ingresa un número válido", Toast.LENGTH_SHORT).show();
        }
    }
}
