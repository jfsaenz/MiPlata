package com.miplata.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.miplata.R;
import com.miplata.data.AppDatabase;
import com.miplata.data.Transaction;
import com.miplata.ui.LoginActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountFragment extends Fragment {

    private AppDatabase db;
    private ExecutorService executorService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getDatabase(getContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        Button btnLogout = view.findViewById(R.id.btn_logout);
        Button btnDeleteAccount = view.findViewById(R.id.btn_delete_account);
        Button btnNotificationPermission = view.findViewById(R.id.btn_notification_permission);
        Button btnSimulateNotification = view.findViewById(R.id.btn_simulate_notification);

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        btnDeleteAccount.setOnClickListener(v -> {
            SharedPreferences prefs = getActivity().getSharedPreferences("user_credentials", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();

            Toast.makeText(getActivity(), "Cuenta eliminada", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        btnNotificationPermission.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        });

        btnSimulateNotification.setOnClickListener(v -> {
            simulateTransaction();
        });

        return view;
    }

    private void simulateTransaction() {
        Transaction t = new Transaction();
        boolean isDebit = Math.random() < 0.5;

        if (isDebit) {
            t.setType("DEBIT");
            t.setAmount( (int) (Math.random() * 50000) + 5000);
            t.setDescription("Compra de prueba en el Ara");
        } else {
            t.setType("CREDIT");
            t.setAmount( (int) (Math.random() * 100000) + 10000);
            t.setDescription("Abono de prueba de Nequi");
        }
        t.setDateMillis(System.currentTimeMillis());

        executorService.execute(() -> {
            db.transactionDao().insert(t);
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Transacción simulada creada", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
