package com.miplata.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Update
    void updateUser(User user);

    @Query("SELECT * FROM users WHERE username = :username AND pin = :pin")
    User login(String username, String pin);

    @Query("SELECT * FROM users WHERE username = :username")
    User findByName(String username);

    @Query("SELECT * FROM users")
    LiveData<List<User>> getAllUsers();

    @Query("DELETE FROM users WHERE id = :userId")
    void deleteUserById(int userId);

    // --- Métodos para el sistema de Recompensas y Metas ---

    @Query("SELECT * FROM users WHERE id = :userId")
    LiveData<User> getUserById(int userId);

    // --- MÉTODO AÑADIDO PARA LA PANTALLA DE INTERESES ---
    // Versión síncrona para ser llamada desde un background thread
    @Query("SELECT * FROM users WHERE id = :userId")
    User findByIdSync(int userId);

    @Query("UPDATE users SET reward_points = :newPoints WHERE id = :userId")
    void updateRewardPoints(int userId, int newPoints);
}
