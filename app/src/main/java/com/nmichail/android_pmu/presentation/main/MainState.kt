package com.nmichail.android_pmu.presentation.main

import com.nmichail.android_pmu.domain.model.User

sealed interface MainState {

    data object Initial : MainState

    data object Loading : MainState

    data class Content(
        val currentUser: User?
    ) : MainState

    data class Error(
        val message: String
    ) : MainState
}