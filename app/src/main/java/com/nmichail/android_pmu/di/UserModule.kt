package com.nmichail.android_pmu.di

import com.nmichail.android_pmu.data.repository.UserRepositoryImpl
import com.nmichail.android_pmu.domain.repository.UserRepository
import com.nmichail.android_pmu.presentation.main.MainViewModel
import com.nmichail.android_pmu.presentation.registration.RegistrationViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val userModule = module {
    single<UserRepository> { UserRepositoryImpl(get()) }
    viewModel { MainViewModel(get()) }
    viewModel { RegistrationViewModel(get()) }
}