package com.miplata.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;

public class RewardRepository {

    private final RewardDao rewardDao;
    private final UserDao userDao;

    public RewardRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        rewardDao = db.rewardDao();
        userDao = db.userDao();
    }

    public LiveData<User> getUserById(int userId) {
        return userDao.getUserById(userId);
    }

    public LiveData<List<Reward>> getRewardsForUser(List<String> categories) {
        return rewardDao.getRewardsForUser(categories);
    }

    public LiveData<List<Reward>> getOtherRewards(List<String> categories) {
        return rewardDao.getOtherRewards(categories);
    }

    public void updateUserPoints(int userId, int newPoints) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            userDao.updateRewardPoints(userId, newPoints);
        });
    }
}
