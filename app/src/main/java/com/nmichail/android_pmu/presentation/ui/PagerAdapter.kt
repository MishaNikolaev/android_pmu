package com.nmichail.android_pmu.presentation.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nmichail.android_pmu.presentation.ui.authors.AuthorsFragment
import com.nmichail.android_pmu.presentation.ui.registartion.RegistrationFragment
import com.nmichail.android_pmu.presentation.ui.rules.RulesFragment
import com.nmichail.android_pmu.presentation.ui.settings.SettingsFragment

class PagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RegistrationFragment()
            1 -> RulesFragment()
            2 -> AuthorsFragment()
            3 -> SettingsFragment()
            else -> error("Неизвестная вкладка: $position")
        }
    }
}
