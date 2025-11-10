package com.miplata.ui.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;
import com.miplata.data.Reward;
import com.miplata.data.RewardRepository;
import com.miplata.data.User;
import com.miplata.ui.LoginActivity;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RewardViewModel extends AndroidViewModel {

    private final RewardRepository repository;
    private final LiveData<User> user;
    private final MediatorLiveData<List<Reward>> rewardsForUser = new MediatorLiveData<>();

    public RewardViewModel(@NonNull Application application) {
        super(application);
        repository = new RewardRepository(application);

        SharedPreferences prefs = application.getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int userId = prefs.getInt(LoginActivity.KEY_USER_ID, -1);

        user = repository.getUserById(userId);

        // Cuando el usuario carga, obtenemos sus intereses y cargamos las recompensas que coinciden
        rewardsForUser.addSource(user, currentUser -> {
            if (currentUser != null && currentUser.getInterests() != null && !currentUser.getInterests().isEmpty()) {
                List<String> interests = Arrays.asList(currentUser.getInterests().split(","));
                LiveData<List<Reward>> rewards = repository.getRewardsForUser(interests);
                rewardsForUser.addSource(rewards, rewardsForUser::setValue);
            } else {
                // Si el usuario no tiene intereses, la lista de recompensas estará vacía
                rewardsForUser.setValue(Collections.emptyList());
            }
        });
    }

    public LiveData<Integer> getUserPoints() {
        return Transformations.map(user, User::getRewardPoints);
    }

    public LiveData<List<Reward>> getRewardsForUser() {
        return rewardsForUser;
    }

}
