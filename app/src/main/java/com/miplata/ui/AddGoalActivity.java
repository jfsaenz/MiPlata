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
import com.miplata.R;
import com.miplata.data.AppDatabase;
import com.miplata.data.Goal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddGoalActivity extends AppCompatActivity {

    private EditText etGoalName, etGoalAmount;
    private AppDatabase db;
    private ExecutorService executorService;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_goal);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etGoalName = findViewById(R.id.et_goal_name);
        etGoalAmount = findViewById(R.id.et_goal_amount);
        Button btnSaveGoal = findViewById(R.id.btn_save_goal);

        db = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        userId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        btnSaveGoal.setOnClickListener(v -> {
            saveGoal();
        });
    }

    private void saveGoal() {
        String name = etGoalName.getText().toString();
        String amountStr = etGoalAmount.getText().toString();

        if (name.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        if (userId != -1) {
            Goal goal = new Goal();
            goal.setUserId(userId);
            goal.setName(name);
            goal.setTargetAmount(amount);
            goal.setCurrentAmount(0);

            executorService.execute(() -> {
                db.goalDao().insert(goal);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Meta guardada", Toast.LENGTH_SHORT).show();
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
