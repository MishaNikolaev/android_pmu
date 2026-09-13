package com.nmichail.android_pmu.presentation.ui.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.graphics.drawable.toBitmap
import com.nmichail.android_pmu.R
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class BugsGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    interface BugGameListener {
        fun onBugHit()
        fun onMiss()
    }

    companion object {
        private const val BASE_SPEED = 4f
        private const val MIN_BUGS = 3
        private const val BUG_SIZE_DP = 72f
    }

    private val bugs = mutableListOf<Bug>()
    private val bugBitmaps = mutableListOf<Bitmap>()
    private var listener: BugGameListener? = null
    private var inputEnabled = false
    private var maxBugs = MIN_BUGS
    private var speedFactor = 1f

    fun setBugGameListener(listener: BugGameListener?) {
        this.listener = listener
    }

    fun configure(maxBugs: Int, gameSpeed: Int) {
        this.maxBugs = maxBugs.coerceAtLeast(MIN_BUGS)
        this.speedFactor = 0.4f + gameSpeed / 50f
    }

    fun prepareField() {
        ensureBitmaps()
        bugs.clear()
        if (width == 0 || height == 0) {
            post { prepareField() }
            return
        }
        repeat(MIN_BUGS) { spawnBug() }
        invalidate()
    }

    fun setInputEnabled(enabled: Boolean) {
        inputEnabled = enabled
    }

    fun tick() {
        updateBugs()
        maybeSpawn()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentTime = System.currentTimeMillis()
        bugs.forEach { bug ->
            canvas.save()
            canvas.translate(bug.x, bug.y)
            val movementAngle =
                Math.toDegrees(kotlin.math.atan2(bug.vy.toDouble(), bug.vx.toDouble()))
                    .toFloat() + 90f
//            val wiggle = kotlin.math.sin(currentTime / 60.0).toFloat() * 4f
            val wiggle = kotlin.math.sin(currentTime / 45.0).toFloat() * 12f
            canvas.rotate(movementAngle + wiggle)
            canvas.drawBitmap(
                bug.bitmap,
                -bug.radius,
                -bug.radius,
                null
            )
            canvas.restore()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN || !inputEnabled) {
            return true
        }

        val index = bugs.indexOfLast { bug ->
            hypot(event.x - bug.x, event.y - bug.y) <= bug.radius
        }

        if (index >= 0) {
            bugs.removeAt(index)
            listener?.onBugHit()
            spawnBug()
        } else {
            listener?.onMiss()
        }
        invalidate()
        return true
    }

    private fun updateBugs() {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        bugs.forEach { bug ->
            if (Random.nextFloat() < 0.05f) {
                val currentSpeed = hypot(bug.vx, bug.vy)

                val currentAngle = kotlin.math.atan2(bug.vy.toDouble(), bug.vx.toDouble())

                val deltaAngle = (Random.nextFloat() - 0.5f) * Math.toRadians(60.0)
                val newAngle = currentAngle + deltaAngle

                bug.vx = (cos(newAngle) * currentSpeed).toFloat()
                bug.vy = (sin(newAngle) * currentSpeed).toFloat()
            }

            bug.x += bug.vx
            bug.y += bug.vy

            if (bug.x - bug.radius < 0f) {
                bug.x = bug.radius
                bug.vx = -bug.vx
            } else if (bug.x + bug.radius > w) {
                bug.x = w - bug.radius
                bug.vx = -bug.vx
            }

            if (bug.y - bug.radius < 0f) {
                bug.y = bug.radius
                bug.vy = -bug.vy
            } else if (bug.y + bug.radius > h) {
                bug.y = h - bug.radius
                bug.vy = -bug.vy
            }
        }
    }

    private fun maybeSpawn() {
        if (bugs.size < maxBugs && Random.nextFloat() < 0.03f) {
            spawnBug()
        }
    }

    private fun spawnBug() {
        if (bugBitmaps.isEmpty() || width == 0 || height == 0) return

        val bitmap = bugBitmaps.random()
        val radius = bitmap.width / 2f
        if (radius * 2f >= width || radius * 2f >= height) return

        val speed = (BASE_SPEED + Random.nextFloat() * BASE_SPEED) * speedFactor
        val angle = Random.nextFloat() * (Math.PI * 2).toFloat()

        bugs += Bug(
            x = Random.nextFloat() * (width - 2 * radius) + radius,
            y = Random.nextFloat() * (height - 2 * radius) + radius,
            vx = cos(angle) * speed,
            vy = sin(angle) * speed,
            bitmap = bitmap,
            radius = radius
        )
    }

    private fun ensureBitmaps() {
        if (bugBitmaps.isNotEmpty()) return
        val size = (BUG_SIZE_DP * resources.displayMetrics.density).toInt().coerceAtLeast(48)
        listOf(R.drawable.beetle, R.drawable.beetle2, R.drawable.tarkan).forEach { resId ->
            val drawable = getDrawable(context, resId) ?: return@forEach
            bugBitmaps += drawable.toBitmap(size, size)
        }
    }
}