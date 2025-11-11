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

    // --- CORREGIDO: Se usan los nombres de columna correctos 'user_id' y 'date_millis' ---
    @Query("SELECT * FROM transactions WHERE user_id = :userId ORDER BY date_millis DESC")
    LiveData<List<Transaction>> getAllTransactions(int userId);

    @Query("SELECT SUM(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END) FROM transactions WHERE user_id = :userId")
    LiveData<Double> getBalance(int userId);

    @Query("DELETE FROM transactions WHERE user_id = :userId")
    void deleteTransactionsForUser(int userId);
}
