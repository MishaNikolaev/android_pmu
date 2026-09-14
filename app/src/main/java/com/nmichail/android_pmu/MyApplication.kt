package com.nmichail.android_pmu

import android.app.Application
import androidx.room.Room
import com.nmichail.android_pmu.data.local.AppDatabase
import com.nmichail.android_pmu.data.repository.ScoreRepositoryImpl
import com.nmichail.android_pmu.data.repository.UserRepositoryImpl
import com.nmichail.android_pmu.domain.repository.ScoreRepository
import com.nmichail.android_pmu.domain.repository.UserRepository

class MyApplication : Application() {

    private lateinit var database: AppDatabase

    lateinit var userRepository: UserRepository
        private set

    lateinit var scoreRepository: ScoreRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "pmu_game.db"
        ).build()

        userRepository = UserRepositoryImpl(database.userDao())
        scoreRepository = ScoreRepositoryImpl(database.scoreDao())
    }
}