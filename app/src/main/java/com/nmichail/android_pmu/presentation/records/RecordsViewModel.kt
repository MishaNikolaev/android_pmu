package com.nmichail.android_pmu.presentation.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nmichail.android_pmu.domain.repository.ScoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordsViewModel(
    private val scoreRepository: ScoreRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecordsState())
    val state: StateFlow<RecordsState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val records = withContext(Dispatchers.IO) {
                scoreRepository.getRecords()
            }
            _state.update { it.copy(records = records) }
        }
    }
}