package com.nmichail.android_pmu.di

import androidx.room.Room
import com.nmichail.android_pmu.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "pmu_game.db"
        ).build()
    }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().scoreDao() }
}
