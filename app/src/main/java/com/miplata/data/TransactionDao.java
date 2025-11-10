package com.miplata.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY dateMillis DESC")
    LiveData<List<Transaction>> getAllTransactions(int userId);

    @Query("SELECT SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END) FROM transactions WHERE userId = :userId")
    LiveData<Double> getBalance(int userId);

    @Query("DELETE FROM transactions WHERE userId = :userId")
    void deleteTransactionsForUser(int userId);
}
