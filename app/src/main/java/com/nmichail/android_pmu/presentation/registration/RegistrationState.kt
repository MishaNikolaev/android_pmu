package com.nmichail.android_pmu.presentation.registration

import com.nmichail.android_pmu.domain.model.Player
import com.nmichail.android_pmu.domain.model.User

sealed interface RegistrationState {

    data object Initial : RegistrationState

    data object Loading : RegistrationState

    data class Content(
        val name: String = "",
        val surname: String = "",
        val otchestvo: String = "",
        val gender: String = "",
        val course: Int = 1,
        val difficulty: Int = 50,
        val birthDay: Int = 1,
        val birthMonth: Int = 1,
        val birthYear: Int = 2000,
        val users: List<User> = emptyList(),
        val preview: Player? = null
    ) : RegistrationState

    data class Error(
        val message: String
    ) : RegistrationState
}