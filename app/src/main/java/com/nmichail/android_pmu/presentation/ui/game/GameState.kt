package com.nmichail.android_pmu.presentation.ui.game

sealed interface GameState {

    data object Initial : GameState

    data class Loading(
        val roundDurationMs: Long
    ) : GameState

    data class Content(
        val score: Int,
        val remainingMs: Long,
        val paused: Boolean,
        val goldHitPoints: Int,
        val roundDurationMs: Long
    ) : GameState

    data class Error(
        val message: String,
        val roundDurationMs: Long
    ) : GameState
}