package com.nmichail.android_pmu.presentation.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmichail.android_pmu.domain.model.Player
import com.nmichail.android_pmu.domain.model.User
import com.nmichail.android_pmu.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegistrationViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow<RegistrationState>(RegistrationState.Initial)
    val state: StateFlow<RegistrationState> = _state.asStateFlow()

    fun loadUsers() {
        val previous = _state.value as? RegistrationState.Content
        viewModelScope.launch {
            if (previous == null) {
                _state.value = RegistrationState.Loading
            }
            try {
                val users = withContext(Dispatchers.IO) {
                    userRepository.getAll()
                }
                _state.value = previous?.copy(users = users) ?: emptyContent(users)
            } catch (e: Exception) {
                _state.value = RegistrationState.Error(
                    message = e.message ?: "Не удалось загрузить игроков"
                )
            }
        }
    }

    fun updateName(value: String) {
        val content = _state.value as? RegistrationState.Content ?: return
        _state.value = content.copy(name = value)
    }

    fun updateSurname(value: String) {
        val content = _state.value as? RegistrationState.Content ?: return
        _state.value = content.copy(surname = value)
    }

    fun updateOtchestvo(value: String) {
        val content = _state.value as? RegistrationState.Content ?: return
        _state.value = content.copy(otchestvo = value)
    }

    fun updateGender(value: String) {
        val content = _state.value as? RegistrationState.Content ?: return
        _state.value = content.copy(gender = value)
    }

    fun updateCourse(course: Int) {
        val content = _state.value as? RegistrationState.Content ?: return
        _state.value = content.copy(course = course.coerceIn(1, 4))
    }

    fun updateDifficulty(value: Int) {
        val content = _state.value as? RegistrationState.Content ?: return
        _state.value = content.copy(difficulty = value)
    }

    fun updateBirthDate(day: Int, month: Int, year: Int) {
        val content = _state.value as? RegistrationState.Content ?: return
        _state.value = content.copy(
            birthDay = day,
            birthMonth = month,
            birthYear = year
        )
    }

    fun applyUser(user: User) {
        val content = _state.value as? RegistrationState.Content ?: return
        _state.value = content.copy(
            name = user.name,
            surname = user.surname,
            otchestvo = user.otchestvo,
            gender = user.gender,
            course = user.course,
            difficulty = user.difficulty,
            birthDay = user.birthDay,
            birthMonth = user.birthMonth,
            birthYear = user.birthYear,
            preview = null
        )
    }

    fun clearForm() {
        val content = _state.value as? RegistrationState.Content ?: return
        _state.value = emptyContent(content.users)
    }

    fun showPreview() {
        val content = _state.value as? RegistrationState.Content ?: return
        _state.value = content.copy(preview = buildPlayer(content))
    }

    fun register(onSuccess: (User) -> Unit, onError: () -> Unit) {
        val content = _state.value as? RegistrationState.Content ?: return
        val player = buildPlayer(content)
        if (player.name.isBlank() || player.surname.isBlank()) {
            onError()
            return
        }
        viewModelScope.launch {
            try {
                val saved = withContext(Dispatchers.IO) {
                    userRepository.register(player)
                }
                val users = withContext(Dispatchers.IO) {
                    userRepository.getAll()
                }
                _state.value = content.copy(preview = player, users = users)
                onSuccess(saved)
            } catch (e: Exception) {
                _state.value = RegistrationState.Error(
                    message = e.message ?: "Не удалось зарегистрировать игрока"
                )
            }
        }
    }

    fun retry() {
        loadUsers()
    }

    private fun emptyContent(users: List<User>): RegistrationState.Content {
        return RegistrationState.Content(
            name = "",
            surname = "",
            otchestvo = "",
            gender = "",
            course = 1,
            difficulty = 50,
            birthDay = 1,
            birthMonth = 1,
            birthYear = 2000,
            users = users,
            preview = null
        )
    }

    private fun buildPlayer(state: RegistrationState.Content): Player {
        val zodiac = getZodiac(state.birthDay, state.birthMonth)
        val gender = state.gender.ifBlank { "Вы не выбрали пол" }
        return Player(
            name = state.name.trim(),
            surname = state.surname.trim(),
            otchestvo = state.otchestvo.trim(),
            course = state.course,
            difficulty = state.difficulty,
            birthDay = state.birthDay,
            birthMonth = state.birthMonth,
            zodiac = zodiac,
            birthYear = state.birthYear,
            gender = gender
        )
    }

    private fun getZodiac(day: Int, month: Int): String {
        return when (month) {
            1 -> if (day < 20) "Козерог" else "Водолей"
            2 -> if (day < 19) "Водолей" else "Рыбы"
            3 -> if (day < 21) "Рыбы" else "Овен"
            4 -> if (day < 20) "Овен" else "Телец"
            5 -> if (day < 21) "Телец" else "Близнецы"
            6 -> if (day < 21) "Близнецы" else "Рак"
            7 -> if (day < 23) "Рак" else "Лев"
            8 -> if (day < 23) "Лев" else "Дева"
            9 -> if (day < 23) "Дева" else "Весы"
            10 -> if (day < 23) "Весы" else "Скорпион"
            11 -> if (day < 22) "Скорпион" else "Стрелец"
            12 -> if (day < 22) "Стрелец" else "Козерог"
            else -> ""
        }
    }
}
