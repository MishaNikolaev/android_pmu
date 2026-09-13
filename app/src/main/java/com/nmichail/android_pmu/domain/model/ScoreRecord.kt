package com.nmichail.android_pmu.domain.model

data class ScoreRecord(
    val playerName: String,
    val score: Int,
    val difficulty: Int,
    val createdAt: Long
)