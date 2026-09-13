package com.nmichail.android_pmu.domain.repository

import com.nmichail.android_pmu.domain.model.ScoreRecord

interface ScoreRepository {

    suspend fun save(userId: Long, score: Int, difficulty: Int)

    suspend fun getRecords(): List<ScoreRecord>
}