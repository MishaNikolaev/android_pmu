package com.nmichail.android_pmu.presentation.settings

import androidx.lifecycle.ViewModel
import com.nmichail.android_pmu.domain.model.GameSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {

    private val _state = MutableStateFlow<SettingsState>(
        SettingsState.Content(
            gameSettings = GameSettings(
                gameSpeed = 50,
                maxTarakani = 5,
                bonusIntervalSec = 5,
                roundDurationSec = 60
            )
        )
    )
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun updateSettings(transform: (GameSettings) -> GameSettings) {
        val content = _state.value as? SettingsState.Content ?: return
        _state.value = content.copy(gameSettings = transform(content.gameSettings))
    }
}