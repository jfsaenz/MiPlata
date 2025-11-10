package com.miplata.data;

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

    @Query("SELECT * FROM users WHERE id = :userId")
    User findById(int userId);

    @Update
    void updateUser(User user);

    @Query("DELETE FROM users WHERE id = :userId")
    void deleteUser(int userId);
}
