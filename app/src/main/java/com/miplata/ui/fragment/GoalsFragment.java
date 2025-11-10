package com.miplata.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.miplata.data.Goal;
import com.miplata.ui.AddGoalActivity;
import com.miplata.ui.LoginActivity;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoalsFragment extends Fragment implements GoalsAdapter.OnGoalClickListener {

    private RecyclerView rvGoals;
    private GoalsAdapter adapter;
    private int userId;
    private AppDatabase db;
    private ExecutorService executorService;

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
        View view = inflater.inflate(R.layout.fragment_goals, container, false);

        rvGoals = view.findViewById(R.id.rv_goals);
        rvGoals.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GoalsAdapter(new ArrayList<>(), this);
        rvGoals.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_goal);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddGoalActivity.class);
            startActivity(intent);
        });

        if (userId != -1) {
            db.goalDao().getGoalsForUser(userId).observe(getViewLifecycleOwner(), goals -> {
                adapter.setGoals(goals);
            });
        }

        return view;
    }

    @Override
    public void onGoalClick(Goal goal) {
        final CharSequence[] options = {"Abonar a Meta", "Modificar Meta", "Eliminar Meta", "Cancelar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("¿Qué deseas hacer?");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Abonar a Meta")) {
                showAddContributionDialog(goal);
            } else if (options[item].equals("Modificar Meta")) {
                // Lógica para modificar la meta
            } else if (options[item].equals("Eliminar Meta")) {
                deleteGoal(goal);
            } else if (options[item].equals("Cancelar")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void showAddContributionDialog(Goal goal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Abonar a " + goal.getName());

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("Abonar", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if (!amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                updateGoal(goal, amount);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateGoal(Goal goal, double amount) {
        goal.setCurrentAmount(goal.getCurrentAmount() + amount);
        executorService.execute(() -> {
            db.goalDao().update(goal);
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "¡Abono exitoso!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void deleteGoal(Goal goal) {
        executorService.execute(() -> {
            db.goalDao().delete(goal);
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Meta eliminada", Toast.LENGTH_SHORT).show();
            });
        });
    }
}
