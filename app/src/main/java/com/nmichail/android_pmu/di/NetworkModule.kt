package com.nmichail.android_pmu.di

import com.nmichail.android_pmu.data.api.CbrApi
import org.koin.dsl.module
import retrofit2.Retrofit

val networkModule = module {
    single {
        Retrofit.Builder()
            .baseUrl("https://www.cbr.ru/")
            .build()
    }
    single { get<Retrofit>().create(CbrApi::class.java) }
}