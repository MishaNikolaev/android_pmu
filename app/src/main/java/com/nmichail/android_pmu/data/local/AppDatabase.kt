package com.nmichail.android_pmu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nmichail.android_pmu.data.dao.ScoreDao
import com.nmichail.android_pmu.data.dao.UserDao
import com.nmichail.android_pmu.data.entity.ScoreEntity
import com.nmichail.android_pmu.data.entity.UserEntity

@Database(
    entities = [UserEntity::class, ScoreEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun scoreDao(): ScoreDao
}