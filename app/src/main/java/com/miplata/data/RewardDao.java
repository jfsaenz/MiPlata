package com.miplata.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RewardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Reward... rewards);

    @Query("SELECT * FROM rewards WHERE category IN (:categories) ORDER BY cost_in_points ASC")
    LiveData<List<Reward>> getRewardsForUser(List<String> categories);

    @Query("SELECT * FROM rewards WHERE category NOT IN (:categories) ORDER BY cost_in_points ASC")
    LiveData<List<Reward>> getOtherRewards(List<String> categories);
}
