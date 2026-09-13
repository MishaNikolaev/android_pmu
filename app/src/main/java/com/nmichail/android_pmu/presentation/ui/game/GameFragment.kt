package com.nmichail.android_pmu.presentation.ui.game

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

class GameFragment : Fragment() {

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
        })

        val settings = (requireActivity() as MainActivity).gameSettings
        gameView.configure(
            maxBugs = settings.maxTarakani,
            gameSpeed = settings.gameSpeed
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
        }
    }

    override fun onPause() {
        if (::gameView.isInitialized && !paused && remainingMs > 0) {
            stoppedByLifecycle = true
            stopGameLoop()
            gameView.setInputEnabled(false)
            timer?.cancel()
        }
        super.onPause()
    }

    override fun onDestroyView() {
        timer?.cancel()
        stopGameLoop()
        if (::gameView.isInitialized) {
            gameView.setInputEnabled(false)
            gameView.setBugGameListener(null)
        }
        super.onDestroyView()
    }

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
        } else {
            startGameLoop()
            startTimer(remainingMs)
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
        stopGameLoop()
        (activity as? MainActivity)?.openGameResult(score)
    }

    private fun leaveToTab(tabIndex: Int) {
        timer?.cancel()
        stopGameLoop()
        (activity as? MainActivity)?.showTabs(tabIndex)
    }

    private fun updateScoreLabel() {
        tvScore.text = getString(R.string.game_score, score)
    }
}