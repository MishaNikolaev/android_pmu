package com.nmichail.android_pmu.data.repository

import com.nmichail.android_pmu.data.dao.ScoreDao
import com.nmichail.android_pmu.domain.model.ScoreRecord
import com.nmichail.android_pmu.domain.repository.ScoreRepository

// TODO: Андрей, твоя часть
class ScoreRepositoryImpl(
    private val scoreDao: ScoreDao
) : ScoreRepository {

    override suspend fun save(userId: Long, score: Int, difficulty: Int) {
        TODO("Андрей, твоя часть")
    }

    override suspend fun getRecords(): List<ScoreRecord> {
        TODO("Андрей, твоя часть")
    }
}