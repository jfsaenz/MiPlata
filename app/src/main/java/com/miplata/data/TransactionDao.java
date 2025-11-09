package com.miplata.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insert(Transaction transaction);

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    LiveData<List<Transaction>> getAllTransactions();

    @Query("SELECT SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END) FROM transactions")
    LiveData<Double> getBalance();
}
