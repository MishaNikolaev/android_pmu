package com.nmichail.android_pmu.domain.repository

import com.nmichail.android_pmu.domain.model.GoldRate

interface GoldRateRepository {

    suspend fun loadRate(): GoldRate
}