package com.nmichail.android_pmu.presentation.game.ui

import android.graphics.Bitmap

internal class Bug(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val bitmap: Bitmap,
    val radius: Float,
    val isGold: Boolean
)