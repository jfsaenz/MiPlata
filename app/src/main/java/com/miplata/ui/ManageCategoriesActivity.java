package com.miplata.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.miplata.R;
import com.miplata.data.AppDatabase;
import com.miplata.data.Category;
import com.miplata.ui.fragment.CategoriesAdapter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManageCategoriesActivity extends AppCompatActivity {

    private EditText etNewCategory;
    private RecyclerView rvCategories;
    private CategoriesAdapter adapter;
    private AppDatabase db;
    private ExecutorService executorService;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etNewCategory = findViewById(R.id.et_new_category);
        Button btnAddCategory = findViewById(R.id.btn_add_category);
        rvCategories = findViewById(R.id.rv_categories);

        db = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        userId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        setupRecyclerView();

        btnAddCategory.setOnClickListener(v -> {
            addCategory();
        });
    }

    private void setupRecyclerView() {
        adapter = new CategoriesAdapter(new ArrayList<>(), category -> {
            // Lógica para eliminar categoría
            executorService.execute(() -> {
                db.categoryDao().delete(category);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Categoría eliminada", Toast.LENGTH_SHORT).show();
                });
            });
        });
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(adapter);

        if (userId != -1) {
            db.categoryDao().getCategoriesForUser(userId).observe(this, categories -> {
                adapter.setCategories(categories);
            });
        }
    }

    private void addCategory() {
        String categoryName = etNewCategory.getText().toString();
        if (categoryName.isEmpty()) {
            Toast.makeText(this, "Escribe un nombre para la categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId != -1) {
            Category category = new Category();
            category.setUserId(userId);
            category.setName(categoryName);

            executorService.execute(() -> {
                db.categoryDao().insert(category);
                runOnUiThread(() -> {
                    etNewCategory.setText("");
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
