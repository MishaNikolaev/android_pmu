package com.nmichail.android_pmu.domain.model

data class GameStats(
    val score: Int,
    val hits: Int,
    val misses: Int
) {
    val accuracy: Float
        get() {
            val totalClicks = hits + misses
            return if (totalClicks > 0) (hits.toFloat() / totalClicks) * 100f else 0f
        }
}