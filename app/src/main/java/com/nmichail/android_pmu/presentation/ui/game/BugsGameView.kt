package com.nmichail.android_pmu.presentation.ui.game

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View

class BugsGameView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null
) : View(context, attrs) {

	interface BugGameListener {

		fun onBugHit()
		fun onMiss()
	}

	companion object {

		private const val MIN_BUGS = 0
	}

	private var listener: BugGameListener? = null
	private var inputEnabled = false
	private var maxBugs = MIN_BUGS
	private var speedFactor = 1f

	fun setBugGameListener(listener: BugGameListener?) {
		this.listener = listener
	}

	fun configure(maxBugs: Int, gameSpeed: Int) {
		this.maxBugs = maxBugs.coerceAtLeast(MIN_BUGS)
		this.speedFactor = 0.0f
	}

	fun prepareField() {

	}

	fun setInputEnabled(enabled: Boolean) {
		inputEnabled = enabled
	}

	fun tick() {
		invalidate()
	}
}