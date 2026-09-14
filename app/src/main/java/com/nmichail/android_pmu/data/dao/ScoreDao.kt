package com.nmichail.android_pmu.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nmichail.android_pmu.data.entity.ScoreEntity
import com.nmichail.android_pmu.domain.model.ScoreRecord

@Dao
interface ScoreDao {

    @Insert
    suspend fun insert(score: ScoreEntity): Long

    @Query(
        """
        SELECT u.surname || ' ' || u.name || ' ' || u.otchestvo AS playerName,
               s.score,
               s.difficulty,
               s.createdAt
        FROM scores AS s
        INNER JOIN users AS u ON u.id = s.userId
        ORDER BY s.score DESC, s.createdAt DESC
        """
    )
    suspend fun getRecords(): List<ScoreRecord>
}