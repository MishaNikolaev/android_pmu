package com.nmichail.android_pmu.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmichail.android_pmu.domain.repository.GoldRateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(
	private val goldRateRepository: GoldRateRepository
) : ViewModel() {

	companion object {

		const val HIT_POINTS = 10
		const val MISS_PENALTY = 5
	}

	private val _state = MutableStateFlow<GameState>(GameState.Initial)
	val state: StateFlow<GameState> = _state.asStateFlow()

	fun startRound(durationMs: Long) {
		val current = _state.value
		if (current is GameState.Content && current.remainingMs > 0L) return

		viewModelScope.launch {
			_state.value = GameState.Loading(roundDurationMs = durationMs)
			try {
				val goldHitPoints = withContext(Dispatchers.IO) {
					val rate = goldRateRepository.loadRate()
					(rate.valueRubPerGram / 100.0).toInt()
				}
				_state.value = GameState.Content(
					score = 0,
					remainingMs = durationMs,
					paused = false,
					goldHitPoints = goldHitPoints,
					roundDurationMs = durationMs
				)
			} catch (e: Exception) {
				_state.value = GameState.Error(
					message = e.message ?: "Не удалось загрузить курс золота",
					roundDurationMs = durationMs
				)
			}
		}
	}

	fun retry() {
		val durationMs = when (val current = _state.value) {
			is GameState.Loading -> current.roundDurationMs
			is GameState.Content -> current.roundDurationMs
			is GameState.Error   -> current.roundDurationMs
			GameState.Initial    -> return
		}
		if (durationMs <= 0L) return
		startRound(durationMs)
	}

	fun onBugHit(points: Int = HIT_POINTS) {
		val content = _state.value as? GameState.Content ?: return
		_state.value = content.copy(score = content.score + points)
	}

	fun onMiss(penalty: Int = MISS_PENALTY) {
		val content = _state.value as? GameState.Content ?: return
		_state.value = content.copy(score = (content.score - penalty).coerceAtLeast(0))
	}

	fun onGoldBugHit() {
		val content = _state.value as? GameState.Content ?: return
		_state.value = content.copy(score = content.score + content.goldHitPoints)
	}

	fun setRemainingMs(ms: Long) {
		val content = _state.value as? GameState.Content ?: return
		_state.value = content.copy(remainingMs = ms)
	}

	fun togglePause() {
		val content = _state.value as? GameState.Content ?: return
		_state.value = content.copy(paused = !content.paused)
	}

	fun finishRound() {
		_state.value = GameState.Initial
	}
}