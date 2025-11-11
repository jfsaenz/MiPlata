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
import com.miplata.ui.ManageCategoriesActivity;
import com.miplata.ui.SelectInterestsActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountFragment extends Fragment {

    private AppDatabase db;
    private ExecutorService executorService;
    private int userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = AppDatabase.getDatabase(getContext());
        executorService = Executors.newSingleThreadExecutor();

        SharedPreferences prefs = getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        userId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        Button btnLogout = view.findViewById(R.id.btn_logout);
        Button btnDeleteAccount = view.findViewById(R.id.btn_delete_account);
        Button btnNotificationPermission = view.findViewById(R.id.btn_notification_permission);
        Button btnSimulateNotification = view.findViewById(R.id.btn_simulate_notification);
        Button btnEditInterests = view.findViewById(R.id.btn_edit_interests);
        Button btnManageCategories = view.findViewById(R.id.btn_manage_categories);

        btnLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().remove(LoginActivity.KEY_USER_ID).apply();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        btnDeleteAccount.setOnClickListener(v -> {
            if (userId != -1) {
                executorService.execute(() -> {
                    // --- CORRECCIÓN FINAL: Se usa el nombre de método correcto ---
                    db.userDao().deleteUserById(userId);

                    SharedPreferences prefs = getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
                    prefs.edit().remove(LoginActivity.KEY_USER_ID).apply();

                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Cuenta y datos eliminados", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    });
                });
            }
        });

        btnNotificationPermission.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        });

        btnSimulateNotification.setOnClickListener(v -> {
            simulateTransaction();
        });

        btnEditInterests.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SelectInterestsActivity.class);
            startActivity(intent);
        });

        btnManageCategories.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ManageCategoriesActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void simulateTransaction() {
        if (userId == -1) return;

        Transaction t = new Transaction();
        t.setUserId(userId);
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
