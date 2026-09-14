package com.nmichail.android_pmu.data.repository

import com.nmichail.android_pmu.data.dao.ScoreDao
import com.nmichail.android_pmu.data.entity.ScoreEntity
import com.nmichail.android_pmu.domain.model.ScoreRecord
import com.nmichail.android_pmu.domain.repository.ScoreRepository

class ScoreRepositoryImpl(
    private val scoreDao: ScoreDao
) : ScoreRepository {

    override suspend fun save(userId: Long, score: Int, difficulty: Int) {
        scoreDao.insert(
            ScoreEntity(
                id = 0,
                userId = userId,
                score = score,
                difficulty = difficulty,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun getRecords(): List<ScoreRecord> {
        return scoreDao.getRecords()
    }
}