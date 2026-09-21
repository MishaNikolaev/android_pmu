package com.nmichail.android_pmu.presentation.ui.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.nmichail.android_pmu.MainActivity
import com.nmichail.android_pmu.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameFragment : Fragment(), SensorEventListener {

    companion object {
        private const val FRAME_DELAY_MS = 16L
        private const val HIT_POINTS = 10
        private const val MISS_PENALTY = 5
    }

    private var score = 0
    private var paused = false
    private var stoppedByLifecycle = false
    private var timer: CountDownTimer? = null
    private var remainingMs = 0L
    private var gameJob: Job? = null

    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnPause: MaterialButton
    private lateinit var gameView: BugsGameView

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var tiltListening = false

    private var soundPool: SoundPool? = null
    private var screamSoundId = 0
    private var screamLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvScore = view.findViewById(R.id.tvScore)
        tvTimer = view.findViewById(R.id.tvTimer)
        btnPause = view.findViewById(R.id.btnPause)
        gameView = view.findViewById(R.id.bugsGameView)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        initSound()

        updateScoreLabel()
        updatePauseIcon()

        gameView.setBugGameListener(object : BugsGameView.BugGameListener {
            override fun onBugHit() {
                score += HIT_POINTS
                updateScoreLabel()
            }

            override fun onMiss() {
                score = (score - MISS_PENALTY).coerceAtLeast(0)
                updateScoreLabel()
            }

            override fun onBonusCollected() {
                playScream()
                startTiltListening()
            }

            override fun onTiltEnded() {
                stopTiltListening()
            }
        })

        val settings = (requireActivity() as MainActivity).gameSettings
        gameView.configure(
            maxBugs = settings.maxTarakani,
            gameSpeed = settings.gameSpeed,
            bonusIntervalSec = settings.bonusIntervalSec
        )

        btnPause.setOnClickListener { togglePause() }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    leaveToTab(MainActivity.TAB_RULES)
                }
            }
        )

        remainingMs = settings.roundDurationSec * 1000L
        startRound()
    }

    override fun onResume() {
        super.onResume()
        if (stoppedByLifecycle && remainingMs > 0) {
            stoppedByLifecycle = false
            startGameLoop()
            startTimer(remainingMs)
            if (gameView.isTiltMode()) {
                startTiltListening()
            }
        }
    }

    override fun onPause() {
        if (::gameView.isInitialized && !paused && remainingMs > 0) {
            stoppedByLifecycle = true
            stopGameLoop()
            gameView.setInputEnabled(false)
            timer?.cancel()
        }
        stopTiltListening()
        super.onPause()
    }

    override fun onDestroyView() {
        timer?.cancel()
        stopGameLoop()
        stopTiltListening()
        releaseSound()
        if (::gameView.isInitialized) {
            gameView.setInputEnabled(false)
            gameView.setBugGameListener(null)
        }
        super.onDestroyView()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        if (!::gameView.isInitialized || !gameView.isTiltMode()) return
        gameView.setTiltAcceleration(event.values[0], event.values[1])
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun startRound() {
        gameView.prepareField()
        startGameLoop()
        startTimer(remainingMs)
    }

    private fun startGameLoop() {
        stopGameLoop()
        gameView.setInputEnabled(true)
        gameJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                gameView.tick()
                delay(FRAME_DELAY_MS)
            }
        }
    }

    private fun stopGameLoop() {
        gameJob?.cancel()
        gameJob = null
        if (::gameView.isInitialized) {
            gameView.setInputEnabled(false)
        }
    }

    private fun startTimer(durationMs: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(durationMs, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMs = millisUntilFinished
                tvTimer.text = getString(
                    R.string.game_timer,
                    (millisUntilFinished / 1000L).toInt()
                )
            }

            override fun onFinish() {
                remainingMs = 0
                tvTimer.text = getString(R.string.game_timer, 0)
                finishRound()
            }
        }.start()
    }

    private fun togglePause() {
        paused = !paused
        if (paused) {
            stopGameLoop()
            timer?.cancel()
            stopTiltListening()
        } else {
            startGameLoop()
            startTimer(remainingMs)
            if (gameView.isTiltMode()) {
                startTiltListening()
            }
        }
        updatePauseIcon()
    }

    private fun updatePauseIcon() {
        if (paused) {
            btnPause.setIconResource(R.drawable.ic_play)
            btnPause.contentDescription = getString(R.string.game_resume)
        } else {
            btnPause.setIconResource(R.drawable.ic_pause)
            btnPause.contentDescription = getString(R.string.game_pause)
        }
    }

    private fun finishRound() {
        stopTiltListening()
        stopGameLoop()
        (activity as? MainActivity)?.openGameResult(score)
    }

    private fun leaveToTab(tabIndex: Int) {
        timer?.cancel()
        stopTiltListening()
        stopGameLoop()
        (activity as? MainActivity)?.showTabs(tabIndex)
    }

    private fun updateScoreLabel() {
        tvScore.text = getString(R.string.game_score, score)
    }

    private fun startTiltListening() {
        if (tiltListening) return
        val manager = sensorManager ?: return
        val sensor = accelerometer ?: return
        manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        tiltListening = true
    }

    private fun stopTiltListening() {
        if (!tiltListening) return
        sensorManager?.unregisterListener(this)
        tiltListening = false
        if (::gameView.isInitialized) {
            gameView.setTiltAcceleration(0f, 0f)
        }
    }

    private fun initSound() {
        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
            .also { pool ->
                pool.setOnLoadCompleteListener { _, sampleId, status ->
                    if (status == 0 && sampleId == screamSoundId) {
                        screamLoaded = true
                    }
                }
                screamSoundId = pool.load(requireContext(), R.raw.bug_scream, 1)
            }
    }

    private fun playScream() {
        val pool = soundPool ?: return
        if (!screamLoaded || screamSoundId == 0) return
        pool.play(screamSoundId, 1f, 1f, 1, 0, 1f)
    }

    private fun releaseSound() {
        soundPool?.release()
        soundPool = null
        screamSoundId = 0
        screamLoaded = false
    }
}