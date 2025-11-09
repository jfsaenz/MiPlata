package com.miplata.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.miplata.R;
import com.miplata.data.AppDatabase;
import com.miplata.data.Transaction;
import java.util.ArrayList;

public class MovementsFragment extends Fragment {

    private RecyclerView rvMovements;
    private MovementsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movements, container, false);

        rvMovements = view.findViewById(R.id.rv_movements);
        rvMovements.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MovementsAdapter(new ArrayList<Transaction>());
        rvMovements.setAdapter(adapter);

        AppDatabase db = AppDatabase.getDatabase(getContext());
        db.transactionDao().getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            adapter.setTransactions(transactions);
        });

        return view;
    }
}
