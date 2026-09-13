package com.nmichail.android_pmu

import android.app.Application
import androidx.room.Room
import com.nmichail.android_pmu.data.local.AppDatabase
import com.nmichail.android_pmu.domain.repository.ScoreRepository
import com.nmichail.android_pmu.domain.repository.UserRepository

class MyApplication : Application() {

    private lateinit var database: AppDatabase

    // TODO: После реализации repositoryImpl прокинуть их сюда, паттерн Service Locator
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

        // TODO: Вот здесь ты их создашь, когда фрагмент попросит зависимость, класс Application их предоставит
        // userRepository = UserRepositoryImpl(database.userDao())
        // scoreRepository = ScoreRepositoryImpl(database.scoreDao())
    }
}