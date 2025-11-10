package com.miplata.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {

    @Insert
    void insert(User user);

    @Query("SELECT * FROM users WHERE username = :username AND pin = :pin")
    User findByCredentials(String username, String pin);

    @Query("SELECT * FROM users WHERE username = :username")
    User findByName(String username);

    // --- MÉTODO SINCRÓNICO RESTAURADO ---
    @Query("SELECT * FROM users WHERE id = :userId")
    User findByIdSync(int userId); // Para operaciones directas en hilos secundarios

    // --- MÉTODO ASINCRÓNICO PARA OBSERVAR ---
    @Query("SELECT * FROM users WHERE id = :userId")
    LiveData<User> getUserById(int userId); // Para observar cambios desde la UI

    @Query("UPDATE users SET reward_points = :newPoints WHERE id = :userId")
    void updateRewardPoints(int userId, int newPoints);

    @Update
    void updateUser(User user);

    @Query("DELETE FROM users WHERE id = :userId")
    void deleteUser(int userId);
}
