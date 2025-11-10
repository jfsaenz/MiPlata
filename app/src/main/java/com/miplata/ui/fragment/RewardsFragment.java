package com.miplata.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.miplata.R;
import com.miplata.ui.adapter.RewardAdapter;
import com.miplata.ui.viewmodel.RewardViewModel;

public class RewardsFragment extends Fragment {

    private RewardViewModel rewardViewModel;
    private TextView tvRewardPoints;
    private RewardAdapter rewardAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

        tvRewardPoints = view.findViewById(R.id.tv_reward_points);

        // Configurar el RecyclerView único
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_rewards);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rewardAdapter = new RewardAdapter();
        recyclerView.setAdapter(rewardAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rewardViewModel = new ViewModelProvider(this).get(RewardViewModel.class);

        // Observar y actualizar los puntos del usuario
        rewardViewModel.getUserPoints().observe(getViewLifecycleOwner(), points -> {
            if (points != null) {
                tvRewardPoints.setText(String.format("Mis Puntos: %d", points));
            }
        });

        // Observar y actualizar la lista de recompensas para el usuario
        rewardViewModel.getRewardsForUser().observe(getViewLifecycleOwner(), rewards -> {
            rewardAdapter.submitList(rewards);
        });
    }
}
