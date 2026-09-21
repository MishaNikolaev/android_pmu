package com.nmichail.android_pmu.presentation.game.ui

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

    companion object {
        private const val BASE_SPEED = 4f
        private const val MIN_BUGS = 3
        private const val BUG_SIZE_DP = 72f
        private const val BONUS_SIZE_DP = 56f
        private const val NAKLON_DURATION_MS = 8_000L
        private const val NAKLON_FORCE = 0.55f
        private const val MAX_NAKLON_SPEED = 18f
        private const val GOLD_BUG_INTERVAL_MS = 20_000L
        private const val BONUS_LIFETIME_MS = 5_000L
    }

    interface BugGameListener {
        fun onBugHit()
        fun onMiss()
        fun onBonusCollected()
        fun onNaklonModeFinished()
        fun onGoldBugHit()
    }

    private val bugs = mutableListOf<Bug>()
    private val bugBitmaps = mutableListOf<Bitmap>()
    private var bonusBitmap: Bitmap? = null
    private var goldBugBitmap: Bitmap? = null
    private var goldBug: Bug? = null
    private var nextGoldBugAtMs = 0L
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
    private var bonusExpiresAtMs = 0L

    private var naklonMode = false
    private var naklonUntilMs = 0L
    private var naklonAx = 0f
    private var naklonAy = 0f

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
        goldBug = null
        bonusVisible = false
        bonusExpiresAtMs = 0L
        endNaklonMode(notify = false)
        val now = System.currentTimeMillis()
        nextBonusAtMs = now + bonusIntervalMs
        nextGoldBugAtMs = now + GOLD_BUG_INTERVAL_MS
        if (width == 0 || height == 0) {
            post { prepareField() }
            return
        }
        bonusRadius = (bonusBitmap?.width ?: 0) / 2f
        repeat(MIN_BUGS) { spawnBug() }
        invalidate()
    }

    fun setInputEnabled(enabled: Boolean) {
        inputEnabled = enabled
    }

    fun setNaklonAcceleration(ax: Float, ay: Float) {
        naklonAx = ax
        naklonAy = ay
    }

    fun isNaklonMode(): Boolean = naklonMode

    fun tick() {
        val now = System.currentTimeMillis()
        if (naklonMode && now >= naklonUntilMs) {
            endNaklonMode(notify = true)
        }
        updateBugs()
        maybeSpawnBug()
        maybeExpireBonus(now)
        maybeSpawnBonus(now)
        maybeSpawnGoldBug(now)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val now = System.currentTimeMillis()
        bugs.forEach { bug -> drawBug(canvas, bug, now) }
        goldBug?.let { drawBug(canvas, it, now) }

        if (bonusVisible) {
            bonusBitmap?.let { bmp ->
                canvas.drawBitmap(bmp, bonusX - bonusRadius, bonusY - bonusRadius, null)
            }
        }
    }

    private fun drawBug(canvas: Canvas, bug: Bug, now: Long) {
        canvas.save()
        canvas.translate(bug.x, bug.y)
        val angle =
            Math.toDegrees(kotlin.math.atan2(bug.vy.toDouble(), bug.vx.toDouble()))
                .toFloat() + 90f
        val wiggle = sin(now / 45.0).toFloat() * 12f
        canvas.rotate(angle + wiggle)
        canvas.drawBitmap(bug.bitmap, -bug.radius, -bug.radius, null)
        canvas.restore()
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

        val gold = goldBug
        if (gold != null && hypot(event.x - gold.x, event.y - gold.y) <= gold.radius) {
            goldBug = null
            nextGoldBugAtMs = System.currentTimeMillis() + GOLD_BUG_INTERVAL_MS
            listener?.onGoldBugHit()
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
        bonusExpiresAtMs = 0L
        naklonMode = true
        naklonUntilMs = System.currentTimeMillis() + NAKLON_DURATION_MS
        nextBonusAtMs = System.currentTimeMillis() + bonusIntervalMs
        listener?.onBonusCollected()
    }

    private fun maybeExpireBonus(now: Long) {
        if (!bonusVisible || now < bonusExpiresAtMs) return
        bonusVisible = false
        bonusExpiresAtMs = 0L
        nextBonusAtMs = now + bonusIntervalMs
    }

    private fun endNaklonMode(notify: Boolean) {
        val wasActive = naklonMode
        naklonMode = false
        naklonUntilMs = 0L
        naklonAx = 0f
        naklonAy = 0f
        if (notify && wasActive) {
            listener?.onNaklonModeFinished()
        }
    }

    private fun updateBugs() {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        bugs.forEach { stepBug(it, w, h) }
        goldBug?.let { stepBug(it, w, h) }
    }

    private fun stepBug(bug: Bug, w: Float, h: Float) {
        if (naklonMode) {
            bug.vx += -naklonAx * NAKLON_FORCE
            bug.vy += naklonAy * NAKLON_FORCE
            val speed = hypot(bug.vx, bug.vy)
            if (speed > MAX_NAKLON_SPEED) {
                bug.vx = bug.vx / speed * MAX_NAKLON_SPEED
                bug.vy = bug.vy / speed * MAX_NAKLON_SPEED
            }
        } else if (Random.nextFloat() < 0.05f) {
            val speed = hypot(bug.vx, bug.vy)
            val angle = kotlin.math.atan2(bug.vy.toDouble(), bug.vx.toDouble())
            val delta = (Random.nextFloat() - 0.5f) * Math.toRadians(60.0)
            val next = angle + delta
            bug.vx = (cos(next) * speed).toFloat()
            bug.vy = (sin(next) * speed).toFloat()
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

    private fun maybeSpawnBug() {
        if (bugs.size < maxBugs && Random.nextFloat() < 0.03f) {
            spawnBug()
        }
    }

    private fun maybeSpawnBonus(now: Long) {
        if (bonusVisible || naklonMode || width == 0 || height == 0) return
        if (now < nextBonusAtMs) return
        val padding = bonusRadius + 8f
        if (width <= padding * 2 || height <= padding * 2) return
        bonusX = Random.nextFloat() * (width - 2 * padding) + padding
        bonusY = Random.nextFloat() * (height - 2 * padding) + padding
        bonusVisible = true
        bonusExpiresAtMs = now + BONUS_LIFETIME_MS
    }

    private fun maybeSpawnGoldBug(now: Long) {
        if (goldBug != null || width == 0 || height == 0) return
        if (now < nextGoldBugAtMs) return
        goldBug = createBug(goldBugBitmap ?: return, isGold = true)
    }

    private fun spawnBug() {
        if (bugBitmaps.isEmpty() || width == 0 || height == 0) return
        val bug = createBug(bugBitmaps.random(), isGold = false) ?: return
        bugs += bug
    }

    private fun createBug(bitmap: Bitmap, isGold: Boolean): Bug? {
        val radius = bitmap.width / 2f
        if (radius * 2f >= width || radius * 2f >= height) return null

        val speed = (BASE_SPEED + Random.nextFloat() * BASE_SPEED) * speedFactor
        val angle = Random.nextFloat() * (Math.PI * 2).toFloat()
        return Bug(
            x = Random.nextFloat() * (width - 2 * radius) + radius,
            y = Random.nextFloat() * (height - 2 * radius) + radius,
            vx = cos(angle) * speed,
            vy = sin(angle) * speed,
            bitmap = bitmap,
            radius = radius,
            isGold = isGold
        )
    }

    private fun ensureBitmaps() {
        if (bugBitmaps.isNotEmpty() && bonusBitmap != null && goldBugBitmap != null) return
        val density = resources.displayMetrics.density
        val bugSize = (BUG_SIZE_DP * density).toInt().coerceAtLeast(48)
        if (bugBitmaps.isEmpty()) {
            listOf(R.drawable.beetle, R.drawable.beetle2, R.drawable.tarkan).forEach { resId ->
                val drawable = getDrawable(context, resId) ?: return@forEach
                bugBitmaps += drawable.toBitmap(bugSize, bugSize)
            }
        }
        if (goldBugBitmap == null) {
            val drawable = getDrawable(context, R.drawable.gold_tarakan)
            if (drawable != null) {
                goldBugBitmap = drawable.toBitmap(bugSize, bugSize)
            }
        }
        if (bonusBitmap == null) {
            val size = (BONUS_SIZE_DP * density).toInt().coerceAtLeast(40)
            val drawable = getDrawable(context, R.drawable.bonus) ?: return
            bonusBitmap = drawable.toBitmap(size, size)
        }
    }
}
