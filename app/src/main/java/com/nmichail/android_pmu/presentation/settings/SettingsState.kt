package com.nmichail.android_pmu.presentation.settings

import com.nmichail.android_pmu.domain.model.GameSettings

sealed interface SettingsState {

    data object Initial : SettingsState

    data class Content(
        val gameSettings: GameSettings
    ) : SettingsState
}
