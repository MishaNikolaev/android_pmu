package com.nmichail.android_pmu.presentation.game.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.nmichail.android_pmu.MainActivity
import com.nmichail.android_pmu.R

class GameResultFragment : Fragment() {

	companion object {
		private const val ARG_SCORE = "score"
		private const val ARG_HITS = "hits"
		private const val ARG_MISSES = "misses"
		private const val ARG_ACCURACY = "accuracy"

		fun newInstance(score: Int, hits: Int, misses: Int, accuracy: Float): GameResultFragment {
			return GameResultFragment().apply {
				arguments = Bundle().apply {
					putInt(ARG_SCORE, score)
					putInt(ARG_HITS, hits)
					putInt(ARG_MISSES, misses)
					putFloat(ARG_ACCURACY, accuracy)
				}
			}
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return inflater.inflate(R.layout.fragment_game_result, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
			val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.updatePadding(top = bars.top, bottom = bars.bottom)
			insets
		}

		val score = arguments?.getInt(ARG_SCORE) ?: 0
		val hits = arguments?.getInt(ARG_HITS) ?: 0
		val misses = arguments?.getInt(ARG_MISSES) ?: 0
		val accuracy = arguments?.getFloat(ARG_ACCURACY) ?: 0f

		view.findViewById<TextView>(R.id.tvResultScore).text = getString(R.string.game_result_score, score)
		view.findViewById<TextView>(R.id.tvResultHits).text = getString(R.string.game_result_hits, hits)
		view.findViewById<TextView>(R.id.tvResultMisses).text = getString(R.string.game_result_misses, misses)
		view.findViewById<TextView>(R.id.tvResultAccuracy).text = getString(R.string.game_result_accuracy, accuracy)

		view.findViewById<Button>(R.id.btnNewGame).setOnClickListener {
			(activity as? MainActivity)?.openGame()
		}
		view.findViewById<Button>(R.id.btnExitToMenu).setOnClickListener {
			(activity as? MainActivity)?.showTabs(MainActivity.TAB_RULES)
		}

		requireActivity().onBackPressedDispatcher.addCallback(
			viewLifecycleOwner,
			object : OnBackPressedCallback(true) {
				override fun handleOnBackPressed() {
					(activity as? MainActivity)?.showTabs(MainActivity.TAB_RULES)
				}
			}
		)
	}
}