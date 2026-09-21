package com.nmichail.android_pmu.di

import com.nmichail.android_pmu.data.repository.GoldRateRepositoryImpl
import com.nmichail.android_pmu.domain.repository.GoldRateRepository
import com.nmichail.android_pmu.presentation.game.GameViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val goldModule = module {
    single<GoldRateRepository> { GoldRateRepositoryImpl(get()) }
    viewModel { GameViewModel(get()) }
}