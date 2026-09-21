package com.nmichail.android_pmu.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmichail.android_pmu.domain.model.User
import com.nmichail.android_pmu.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow<MainState>(MainState.Initial)
    val state: StateFlow<MainState> = _state.asStateFlow()

    fun setCurrentUser(user: User?) {
        val content = _state.value as? MainState.Content
        _state.value = content?.copy(currentUser = user) ?: MainState.Content(currentUser = user)
    }

    fun restoreUser(userId: Long) {
        if (userId <= 0) return
        viewModelScope.launch {
            _state.value = MainState.Loading
            try {
                val user = withContext(Dispatchers.IO) {
                    userRepository.getById(userId)
                }
                _state.value = MainState.Content(currentUser = user)
            } catch (e: Exception) {
                _state.value = MainState.Error(
                    message = e.message ?: "Не удалось восстановить игрока"
                )
            }
        }
    }
}