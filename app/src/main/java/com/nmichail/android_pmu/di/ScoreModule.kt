package com.nmichail.android_pmu.di

import com.nmichail.android_pmu.data.repository.ScoreRepositoryImpl
import com.nmichail.android_pmu.domain.repository.ScoreRepository
import com.nmichail.android_pmu.presentation.records.RecordsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val scoreModule = module {
    single<ScoreRepository> { ScoreRepositoryImpl(get()) }
    viewModel { RecordsViewModel(get()) }
}