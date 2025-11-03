package com.miplata.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.miplata.R;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText etUsername, etPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        etUsername = findViewById(R.id.etUsername);
        etPin = findViewById(R.id.etPin);
        Button btnCreate = findViewById(R.id.btnCreate);

        btnCreate.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String pin = etPin.getText().toString();

            if (username.isEmpty() || pin.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("user_credentials", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("username", username);
            editor.putString("pin", pin);
            editor.apply();

            Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
