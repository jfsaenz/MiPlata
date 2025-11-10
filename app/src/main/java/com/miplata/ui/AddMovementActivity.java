package com.miplata.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.miplata.R;
import com.miplata.data.AppDatabase;
import com.miplata.data.Category;
import com.miplata.data.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddMovementActivity extends AppCompatActivity {

    private EditText etDescription, etAmount;
    private RadioGroup rgType;
    private Spinner spinnerCategory;
    private AppDatabase db;
    private ExecutorService executorService;
    private int userId;
    private ArrayAdapter<String> categoryAdapter;
    private List<Category> userCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movement);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etDescription = findViewById(R.id.et_description);
        etAmount = findViewById(R.id.et_amount);
        rgType = findViewById(R.id.rg_type);
        spinnerCategory = findViewById(R.id.spinner_category);
        Button btnSave = findViewById(R.id.btn_save);

        db = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        userId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        setupCategorySpinner();

        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_expense) {
                spinnerCategory.setVisibility(View.VISIBLE);
            } else {
                spinnerCategory.setVisibility(View.GONE);
            }
        });

        btnSave.setOnClickListener(v -> {
            saveTransaction();
        });
    }

    private void setupCategorySpinner() {
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        if (userId != -1) {
            db.categoryDao().getCategoriesForUser(userId).observe(this, categories -> {
                userCategories = categories;
                List<String> categoryNames = new ArrayList<>();
                for (Category category : categories) {
                    categoryNames.add(category.getName());
                }
                categoryAdapter.clear();
                categoryAdapter.addAll(categoryNames);
                categoryAdapter.notifyDataSetChanged();
            });
        }
    }

    private void saveTransaction() {
        String description = etDescription.getText().toString();
        String amountStr = etAmount.getText().toString();

        if (description.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String type = rgType.getCheckedRadioButtonId() == R.id.rb_expense ? "DEBIT" : "CREDIT";

        String category = null;
        if (type.equals("DEBIT") && spinnerCategory.getSelectedItem() != null) {
            category = spinnerCategory.getSelectedItem().toString();
        }

        if (userId != -1) {
            Transaction transaction = new Transaction();
            transaction.setUserId(userId);
            transaction.setDescription(description);
            transaction.setAmount(amount);
            transaction.setType(type);
            transaction.setCategory(category);
            transaction.setDateMillis(System.currentTimeMillis());

            executorService.execute(() -> {
                db.transactionDao().insert(transaction);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Movimiento guardado", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
