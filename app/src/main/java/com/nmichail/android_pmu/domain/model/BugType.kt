package com.nmichail.android_pmu.domain.model

import com.nmichail.android_pmu.R

enum class BugType(
    val speedFactor: Float,
    val sizeRatio: Float,
    val scoreValue: Int,
    val drawableResId: Int
) {
    NORMAL(
        speedFactor = 1.0f,
        sizeRatio = 0.20f,
        scoreValue = 20,
        drawableResId = R.drawable.beetle
    ),
    FAST(
        speedFactor = 2.0f,
        sizeRatio = 0.17f,
        scoreValue = 30,
        drawableResId = R.drawable.beetle2
    ),
    RARE(
        speedFactor = 0.5f,
        sizeRatio = 0.23f,
        scoreValue = 10,
        drawableResId = R.drawable.tarkan
    )
}