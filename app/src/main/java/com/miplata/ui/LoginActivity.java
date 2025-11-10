package com.miplata.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.miplata.R;
import com.miplata.data.AppDatabase;
import com.miplata.data.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "MiPlataPrefs";
    public static final String KEY_USER_ID = "user_id";

    private EditText etUsername, etPin;
    private AppDatabase db;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPin = findViewById(R.id.etPin);
        Button btnConfirm = findViewById(R.id.btnConfirm);
        TextView tvCreateAccount = findViewById(R.id.tvCreateAccount);
        TextView tvTerms = findViewById(R.id.tvTerms);

        db = AppDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        btnConfirm.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String pin = etPin.getText().toString();

            if (username.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            executorService.execute(() -> {
                User user = db.userDao().findByCredentials(username, pin);
                runOnUiThread(() -> {
                    if (user != null) {
                        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        prefs.edit().putInt(KEY_USER_ID, user.getId()).apply();

                        if (user.getInterests() == null || user.getInterests().isEmpty()) {
                            Intent intent = new Intent(LoginActivity.this, SelectInterestsActivity.class);
                            intent.putExtra("isFirstTime", true);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                        finish();
                    } else {
                        Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        tvCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        tvTerms.setOnClickListener(v -> {
            showTermsDialog();
        });
    }

    private void showTermsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Términos y Condiciones")
                .setMessage("1. Aceptación de los Términos\n\n... (texto completo)")
                .setPositiveButton("Cerrar", null)
                .show();
    }
}
