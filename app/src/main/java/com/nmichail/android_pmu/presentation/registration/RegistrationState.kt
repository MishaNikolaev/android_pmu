package com.nmichail.android_pmu.presentation.registration

import com.nmichail.android_pmu.domain.model.Player
import com.nmichail.android_pmu.domain.model.User

sealed interface RegistrationState {

    data object Initial : RegistrationState

    data object Loading : RegistrationState

    data class Content(
        val name: String,
        val surname: String,
        val otchestvo: String,
        val gender: String,
        val course: Int,
        val difficulty: Int,
        val birthDay: Int,
        val birthMonth: Int,
        val birthYear: Int,
        val users: List<User>,
        val preview: Player?
    ) : RegistrationState

    data class Error(
        val message: String
    ) : RegistrationState
}