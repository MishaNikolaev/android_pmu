package com.nmichail.android_pmu.presentation.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.nmichail.android_pmu.R
import com.nmichail.android_pmu.presentation.settings.SettingsState
import com.nmichail.android_pmu.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SettingsFragment : Fragment() {

    private val settingsViewModel: SettingsViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settings = (settingsViewModel.state.value as? SettingsState.Content)?.gameSettings
            ?: return

        val seekSpeed = view.findViewById<SeekBar>(R.id.seekGameSpeed)
        val seekMax = view.findViewById<SeekBar>(R.id.seekMaxTarakani)
        val seekBonus = view.findViewById<SeekBar>(R.id.seekBonusInterval)
        val seekRound = view.findViewById<SeekBar>(R.id.seekRoundDuration)

        seekSpeed.progress = settings.gameSpeed
        seekMax.progress = settings.maxTarakani
        seekBonus.progress = settings.bonusIntervalSec
        seekRound.progress = settings.roundDurationSec

        seekSpeed.bindTo(view.findViewById(R.id.tvGameSpeed), R.string.settings_game_speed) { progress ->
            settingsViewModel.updateSettings { it.copy(gameSpeed = progress) }
        }
        seekMax.bindTo(view.findViewById(R.id.tvMaxTarakani), R.string.settings_max_tarakani) { progress ->
            settingsViewModel.updateSettings { it.copy(maxTarakani = progress.coerceAtLeast(3)) }
        }
        seekBonus.bindTo(view.findViewById(R.id.tvBonusInterval), R.string.settings_bonus_interval) { progress ->
            settingsViewModel.updateSettings { it.copy(bonusIntervalSec = progress.coerceAtLeast(1)) }
        }
        seekRound.bindTo(view.findViewById(R.id.tvRoundDuration), R.string.settings_round_duration) { progress ->
            settingsViewModel.updateSettings { it.copy(roundDurationSec = progress.coerceAtLeast(1)) }
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
