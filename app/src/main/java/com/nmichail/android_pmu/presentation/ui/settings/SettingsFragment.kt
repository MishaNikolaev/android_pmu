package com.nmichail.android_pmu.presentation.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.nmichail.android_pmu.MainActivity
import com.nmichail.android_pmu.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        val seekSpeed = view.findViewById<SeekBar>(R.id.seekGameSpeed)
        val seekMax = view.findViewById<SeekBar>(R.id.seekMaxTarakani)
        val seekBonus = view.findViewById<SeekBar>(R.id.seekBonusInterval)
        val seekRound = view.findViewById<SeekBar>(R.id.seekRoundDuration)

        val settings = activity.gameSettings
        seekSpeed.progress = settings.gameSpeed
        seekMax.progress = settings.maxTarakani
        seekBonus.progress = settings.bonusIntervalSec
        seekRound.progress = settings.roundDurationSec

        seekSpeed.bindTo(view.findViewById(R.id.tvGameSpeed), R.string.settings_game_speed) {
            activity.gameSettings = activity.gameSettings.copy(gameSpeed = it)
        }
        seekMax.bindTo(view.findViewById(R.id.tvMaxTarakani), R.string.settings_max_tarakani) {
            activity.gameSettings = activity.gameSettings.copy(maxTarakani = it.coerceAtLeast(3))
        }
        seekBonus.bindTo(view.findViewById(R.id.tvBonusInterval), R.string.settings_bonus_interval) {
            activity.gameSettings = activity.gameSettings.copy(bonusIntervalSec = it.coerceAtLeast(1))
        }
        seekRound.bindTo(view.findViewById(R.id.tvRoundDuration), R.string.settings_round_duration) {
            activity.gameSettings = activity.gameSettings.copy(roundDurationSec = it.coerceAtLeast(1))
        }
    }

    private fun SeekBar.bindTo(
        label: TextView,
        labelRes: Int,
        onChanged: (Int) -> Unit
    ) {
        fun update() {
            label.text = context.getString(labelRes, progress)
        }

        update()

        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                update()
                if (fromUser) onChanged(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }
}