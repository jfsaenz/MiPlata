package com.miplata.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.miplata.R;
import com.miplata.data.AppDatabase;
import com.miplata.data.Category;
import com.miplata.data.Transaction;
import com.miplata.ui.AddMovementActivity;
import com.miplata.ui.LoginActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovementsFragment extends Fragment implements MovementsAdapter.OnMovementClickListener {

    private RecyclerView rvMovements;
    private MovementsAdapter adapter;
    private int userId;
    private AppDatabase db;
    private ExecutorService executorService;
    private List<Category> userCategories;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getActivity().getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        userId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);
        db = AppDatabase.getDatabase(getContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movements, container, false);

        rvMovements = view.findViewById(R.id.rv_movements);
        rvMovements.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MovementsAdapter(new ArrayList<>(), this);
        rvMovements.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_movement);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddMovementActivity.class);
            startActivity(intent);
        });

        if (userId != -1) {
            db.transactionDao().getAllTransactions(userId).observe(getViewLifecycleOwner(), transactions -> {
                adapter.setTransactions(transactions);
            });

            db.categoryDao().getCategoriesForUser(userId).observe(getViewLifecycleOwner(), categories -> {
                this.userCategories = categories;
            });
        }

        return view;
    }

    @Override
    public void onMovementClick(Transaction transaction) {
        if ("DEBIT".equals(transaction.getType())) {
            showCategoryDialog(transaction);
        }
    }

    @Override
    public void onMovementLongClick(Transaction transaction) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Movimiento")
                .setMessage("¿Estás seguro de que quieres eliminar este movimiento?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    deleteTransaction(transaction);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showCategoryDialog(Transaction transaction) {
        if (userCategories == null || userCategories.isEmpty()) {
            Toast.makeText(getContext(), "No has creado ninguna categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> categoryNames = new ArrayList<>();
        for (Category category : userCategories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, categoryNames);

        new AlertDialog.Builder(getContext())
                .setTitle("Seleccionar Categoría")
                .setAdapter(arrayAdapter, (dialog, which) -> {
                    String selectedCategory = categoryNames.get(which);
                    updateTransactionCategory(transaction, selectedCategory);
                })
                .show();
    }

    private void updateTransactionCategory(Transaction transaction, String category) {
        transaction.setCategory(category);
        executorService.execute(() -> {
            db.transactionDao().update(transaction); // Necesitamos un método de actualización
        });
    }

    private void deleteTransaction(Transaction transaction) {
        executorService.execute(() -> {
            db.transactionDao().delete(transaction);
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Movimiento eliminado", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
