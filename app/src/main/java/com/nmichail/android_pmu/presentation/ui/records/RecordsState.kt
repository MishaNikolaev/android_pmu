package com.nmichail.android_pmu.presentation.ui.records

import com.nmichail.android_pmu.domain.model.ScoreRecord

data class RecordsState(
    val records: List<ScoreRecord> = emptyList()
)