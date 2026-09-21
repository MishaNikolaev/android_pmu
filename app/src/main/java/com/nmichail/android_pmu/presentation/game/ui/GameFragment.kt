package com.nmichail.android_pmu.presentation.game.ui

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
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.nmichail.android_pmu.MainActivity
import com.nmichail.android_pmu.R
import com.nmichail.android_pmu.presentation.game.GameState
import com.nmichail.android_pmu.presentation.game.GameViewModel
import com.nmichail.android_pmu.presentation.settings.SettingsState
import com.nmichail.android_pmu.presentation.settings.SettingsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameFragment : Fragment(), SensorEventListener {

    companion object {
        private const val FRAME_DELAY_MS = 16L
    }

    private val viewModel: GameViewModel by viewModel()
    private val settingsViewModel: SettingsViewModel by activityViewModel()

    private var stoppedByLifecycle = false
    private var timer: CountDownTimer? = null
    private var gameJob: Job? = null
    private var contentStarted = false

    private lateinit var contentContainer: View
    private lateinit var progressLoading: ProgressBar
    private lateinit var errorContainer: View
    private lateinit var tvError: TextView
    private lateinit var btnRetry: MaterialButton
    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnPause: MaterialButton
    private lateinit var gameView: BugsGameView

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var naklonListening = false

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

        contentContainer = view.findViewById(R.id.contentContainer)
        progressLoading = view.findViewById(R.id.progressLoading)
        errorContainer = view.findViewById(R.id.errorContainer)
        tvError = view.findViewById(R.id.tvError)
        btnRetry = view.findViewById(R.id.btnRetry)
        tvScore = view.findViewById(R.id.tvScore)
        tvTimer = view.findViewById(R.id.tvTimer)
        btnPause = view.findViewById(R.id.btnPause)
        gameView = view.findViewById(R.id.bugsGameView)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        initSound()

        val settings = (settingsViewModel.state.value as? SettingsState.Content)?.gameSettings
            ?: return
        gameView.configure(
            maxBugs = settings.maxTarakani,
            gameSpeed = settings.gameSpeed,
            bonusIntervalSec = settings.bonusIntervalSec
        )

        gameView.setBugGameListener(object : BugsGameView.BugGameListener {
            override fun onBugHit() {
                viewModel.onBugHit()
            }

            override fun onMiss() {
                viewModel.onMiss()
            }

            override fun onBonusCollected() {
                playScream()
                startNaklonListening()
            }

            override fun onNaklonModeFinished() {
                stopNaklonListening()
            }

            override fun onGoldBugHit() {
                viewModel.onGoldBugHit()
            }
        })

        btnPause.setOnClickListener { togglePause() }
        btnRetry.setOnClickListener { viewModel.retry() }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    leaveToTab(MainActivity.TAB_RULES)
                }
            }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    render(state)
                }
            }
        }

        viewModel.startRound(settings.roundDurationSec * 1000L)
    }

    private fun render(state: GameState) {
        when (state) {
            GameState.Initial -> Unit
            is GameState.Loading -> {
                contentStarted = false
                stopGameLoop()
                timer?.cancel()
                contentContainer.isVisible = false
                errorContainer.isVisible = false
                progressLoading.isVisible = true
            }
            is GameState.Content -> {
                progressLoading.isVisible = false
                errorContainer.isVisible = false
                contentContainer.isVisible = true
                tvScore.text = getString(R.string.game_score, state.score)
                tvTimer.text = getString(R.string.game_timer, (state.remainingMs / 1000L).toInt())
                if (state.paused) {
                    btnPause.setIconResource(R.drawable.ic_play)
                    btnPause.contentDescription = getString(R.string.game_resume)
                } else {
                    btnPause.setIconResource(R.drawable.ic_pause)
                    btnPause.contentDescription = getString(R.string.game_pause)
                }
                if (!contentStarted) {
                    contentStarted = true
                    startContent(state)
                }
            }
            is GameState.Error -> {
                contentStarted = false
                stopGameLoop()
                timer?.cancel()
                contentContainer.isVisible = false
                progressLoading.isVisible = false
                errorContainer.isVisible = true
                tvError.text = state.message
            }
        }
    }

    private fun startContent(state: GameState.Content) {
        gameView.prepareField()
        if (state.paused) {
            gameView.setInputEnabled(false)
            return
        }
        startGameLoop()
        startTimer(state.remainingMs)
    }

    override fun onResume() {
        super.onResume()
        val content = viewModel.state.value as? GameState.Content ?: return
        if (stoppedByLifecycle && content.remainingMs > 0 && !content.paused) {
            stoppedByLifecycle = false
            startGameLoop()
            startTimer(content.remainingMs)
            if (gameView.isNaklonMode()) {
                startNaklonListening()
            }
        }
    }

    override fun onPause() {
        val content = viewModel.state.value as? GameState.Content
        if (::gameView.isInitialized && content != null && !content.paused && content.remainingMs > 0) {
            stoppedByLifecycle = true
            stopGameLoop()
            gameView.setInputEnabled(false)
            timer?.cancel()
        }
        stopNaklonListening()
        super.onPause()
    }

    override fun onDestroyView() {
        timer?.cancel()
        stopGameLoop()
        stopNaklonListening()
        destroySound()
        if (::gameView.isInitialized) {
            gameView.setInputEnabled(false)
            gameView.setBugGameListener(null)
        }
        super.onDestroyView()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        if (!::gameView.isInitialized || !gameView.isNaklonMode()) return
        gameView.setNaklonAcceleration(event.values[0], event.values[1])
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

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
                viewModel.setRemainingMs(millisUntilFinished)
            }

            override fun onFinish() {
                viewModel.setRemainingMs(0)
                finishRound()
            }
        }.start()
    }

    private fun togglePause() {
        viewModel.togglePause()
        val content = viewModel.state.value as? GameState.Content ?: return
        if (content.paused) {
            stopGameLoop()
            timer?.cancel()
            stopNaklonListening()
        } else {
            startGameLoop()
            startTimer(content.remainingMs)
            if (gameView.isNaklonMode()) {
                startNaklonListening()
            }
        }
    }

    private fun finishRound() {
        val finalScore = (viewModel.state.value as? GameState.Content)?.score ?: 0
        viewModel.finishRound()
        stopNaklonListening()
        stopGameLoop()
        (activity as? MainActivity)?.openGameResult(finalScore)
    }

    private fun leaveToTab(tabIndex: Int) {
        timer?.cancel()
        viewModel.finishRound()
        stopNaklonListening()
        stopGameLoop()
        (activity as? MainActivity)?.showTabs(tabIndex)
    }

    private fun startNaklonListening() {
        if (naklonListening) return
        val manager = sensorManager ?: return
        val sensor = accelerometer ?: return
        manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        naklonListening = true
    }

    private fun stopNaklonListening() {
        if (!naklonListening) return
        sensorManager?.unregisterListener(this)
        naklonListening = false
        if (::gameView.isInitialized) {
            gameView.setNaklonAcceleration(0f, 0f)
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
                screamSoundId = pool.load(requireContext(), R.raw.bugs_scream, 1)
            }
    }

    private fun playScream() {
        val pool = soundPool ?: return
        if (!screamLoaded || screamSoundId == 0) return
        pool.play(screamSoundId, 1f, 1f, 1, 0, 1f)
    }

    private fun destroySound() {
        soundPool?.release()
        soundPool = null
        screamSoundId = 0
        screamLoaded = false
    }
}