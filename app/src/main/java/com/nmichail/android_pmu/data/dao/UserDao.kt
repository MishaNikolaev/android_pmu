package com.nmichail.android_pmu.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nmichail.android_pmu.data.entity.UserEntity

@Dao
interface UserDao {

    @Insert
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users ORDER BY surname ASC, name ASC")
    suspend fun getAll(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Long): UserEntity?
}