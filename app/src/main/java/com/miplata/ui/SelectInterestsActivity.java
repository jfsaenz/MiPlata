package com.miplata.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.miplata.R;
import com.miplata.data.AppDatabase;
import com.miplata.data.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelectInterestsActivity extends AppCompatActivity {

    private LinearLayout interestsContainer;
    private AppDatabase db;
    private ExecutorService executorService;
    private int userId;
    private User currentUser;

    // --- LISTA DE INTERESES ACTUALIZADA ---
    private final List<String> allInterests = Arrays.asList(
        "Ocio", "Comida", "Ropa", "Gimnasio", "Hogar", "Mascotas", "Belleza"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_interests);

        interestsContainer = findViewById(R.id.interests_container);
        Button btnSave = findViewById(R.id.btn_save_interests);

        db = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        userId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        btnSave.setOnClickListener(v -> saveInterests());

        loadCurrentUserAndPopulate();
    }

    private void loadCurrentUserAndPopulate() {
        if (userId != -1) {
            executorService.execute(() -> {
                currentUser = db.userDao().findByIdSync(userId);
                runOnUiThread(this::populateInterests);
            });
        }
    }

    private void populateInterests() {
        interestsContainer.removeAllViews();
        List<String> userInterests = new ArrayList<>();
        if (currentUser != null && currentUser.getInterests() != null && !currentUser.getInterests().isEmpty()) {
            userInterests.addAll(Arrays.asList(currentUser.getInterests().split(",")));
        }

        for (String interest : allInterests) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(interest);
            checkBox.setTextColor(getResources().getColor(R.color.textColorPrimary));
            if (userInterests.contains(interest)) {
                checkBox.setChecked(true);
            }
            interestsContainer.addView(checkBox);
        }
    }

    private void saveInterests() {
        if (currentUser == null) return;

        List<String> selectedInterests = new ArrayList<>();
        for (int i = 0; i < interestsContainer.getChildCount(); i++) {
            CheckBox checkBox = (CheckBox) interestsContainer.getChildAt(i);
            if (checkBox.isChecked()) {
                selectedInterests.add(checkBox.getText().toString());
            }
        }

        String interestsString = String.join(",", selectedInterests);
        currentUser.setInterests(interestsString);

        executorService.execute(() -> {
            db.userDao().updateUser(currentUser);
            runOnUiThread(() -> {
                Toast.makeText(this, "Gustos guardados", Toast.LENGTH_SHORT).show();
                if (getIntent().getBooleanExtra("isFirstTime", false)) {
                    Intent intent = new Intent(SelectInterestsActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                finish();
            });
        });
    }
}
