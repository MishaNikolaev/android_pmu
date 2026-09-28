package com.nmichail.android_pmu.domain.model

data class Bug(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val size: Float,
    val type: BugType,
    val scoreValue: Int,
    val drawableResId: Int,
    val isGold: Boolean = false
)