package com.nmichail.android_pmu.presentation.ui.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
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
        fun onBonusCollected()
        fun onTiltEnded()
    }

    companion object {
        private const val BASE_SPEED = 4f
        private const val MIN_BUGS = 3
        private const val BUG_SIZE_DP = 72f
        private const val BONUS_SIZE_DP = 56f
        private const val TILT_DURATION_MS = 8_000L
        private const val TILT_FORCE = 0.55f
        private const val MAX_TILT_SPEED = 18f
    }

    private val bugs = mutableListOf<Bug>()
    private val bugBitmaps = mutableListOf<Bitmap>()
    private var listener: BugGameListener? = null
    private var inputEnabled = false
    private var maxBugs = MIN_BUGS
    private var speedFactor = 1f
    private var bonusIntervalMs = 15_000L

    private var bonusVisible = false
    private var bonusX = 0f
    private var bonusY = 0f
    private var bonusRadius = 0f
    private var nextBonusAtMs = 0L

    private var tiltMode = false
    private var tiltUntilMs = 0L
    private var tiltAx = 0f
    private var tiltAy = 0f

    private val bonusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFC107.toInt()
        style = Paint.Style.FILL
    }
    private val bonusStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFF8F00.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val bonusTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF3E2723.toInt()
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    fun setBugGameListener(listener: BugGameListener?) {
        this.listener = listener
    }

    fun configure(maxBugs: Int, gameSpeed: Int, bonusIntervalSec: Int) {
        this.maxBugs = maxBugs.coerceAtLeast(MIN_BUGS)
        this.speedFactor = 0.4f + gameSpeed / 50f
        this.bonusIntervalMs = bonusIntervalSec.coerceAtLeast(1) * 1000L
    }

    fun prepareField() {
        ensureBitmaps()
        bugs.clear()
        bonusVisible = false
        endTiltMode(notify = false)
        nextBonusAtMs = System.currentTimeMillis() + bonusIntervalMs
        if (width == 0 || height == 0) {
            post { prepareField() }
            return
        }
        bonusRadius = BONUS_SIZE_DP * resources.displayMetrics.density / 2f
        bonusTextPaint.textSize = bonusRadius
        repeat(MIN_BUGS) { spawnBug() }
        invalidate()
    }

    fun setInputEnabled(enabled: Boolean) {
        inputEnabled = enabled
    }

    fun setTiltAcceleration(ax: Float, ay: Float) {
        tiltAx = ax
        tiltAy = ay
    }

    fun isTiltMode(): Boolean = tiltMode

    fun tick() {
        val now = System.currentTimeMillis()
        if (tiltMode && now >= tiltUntilMs) {
            endTiltMode(notify = true)
        }
        updateBugs()
        maybeSpawnBug()
        maybeSpawnBonus(now)
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

        if (bonusVisible) {
            canvas.drawCircle(bonusX, bonusY, bonusRadius, bonusPaint)
            canvas.drawCircle(bonusX, bonusY, bonusRadius, bonusStrokePaint)
            canvas.drawText(
                "★",
                bonusX,
                bonusY + bonusTextPaint.textSize / 3f,
                bonusTextPaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN || !inputEnabled) {
            return true
        }

        if (bonusVisible && hypot(event.x - bonusX, event.y - bonusY) <= bonusRadius) {
            collectBonus()
            invalidate()
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

    private fun collectBonus() {
        bonusVisible = false
        tiltMode = true
        tiltUntilMs = System.currentTimeMillis() + TILT_DURATION_MS
        nextBonusAtMs = System.currentTimeMillis() + bonusIntervalMs
        listener?.onBonusCollected()
    }

    private fun endTiltMode(notify: Boolean) {
        val wasActive = tiltMode
        tiltMode = false
        tiltUntilMs = 0L
        tiltAx = 0f
        tiltAy = 0f
        if (notify && wasActive) {
            listener?.onTiltEnded()
        }
    }

    private fun updateBugs() {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        bugs.forEach { bug ->
            if (tiltMode) {
                bug.vx += -tiltAx * TILT_FORCE
                bug.vy += tiltAy * TILT_FORCE
                val speed = hypot(bug.vx, bug.vy)
                if (speed > MAX_TILT_SPEED) {
                    bug.vx = bug.vx / speed * MAX_TILT_SPEED
                    bug.vy = bug.vy / speed * MAX_TILT_SPEED
                }
            } else if (Random.nextFloat() < 0.05f) {
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

    private fun maybeSpawnBug() {
        if (bugs.size < maxBugs && Random.nextFloat() < 0.03f) {
            spawnBug()
        }
    }

    private fun maybeSpawnBonus(now: Long) {
        if (bonusVisible || tiltMode || width == 0 || height == 0) return
        if (now < nextBonusAtMs) return
        spawnBonus()
    }

    private fun spawnBonus() {
        val padding = bonusRadius + 8f
        if (width <= padding * 2 || height <= padding * 2) return
        bonusX = Random.nextFloat() * (width - 2 * padding) + padding
        bonusY = Random.nextFloat() * (height - 2 * padding) + padding
        bonusVisible = true
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