package com.nmichail.android_pmu.presentation.main.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nmichail.android_pmu.presentation.authors.ui.AuthorsFragment
import com.nmichail.android_pmu.presentation.records.ui.RecordsFragment
import com.nmichail.android_pmu.presentation.registration.ui.RegistrationFragment
import com.nmichail.android_pmu.presentation.rules.ui.RulesFragment
import com.nmichail.android_pmu.presentation.settings.ui.SettingsFragment

class PagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RegistrationFragment()
            1 -> RulesFragment()
            2 -> RecordsFragment()
            3 -> AuthorsFragment()
            4 -> SettingsFragment()
            else -> error("Неизвестная вкладка: $position")
        }
    }
}
