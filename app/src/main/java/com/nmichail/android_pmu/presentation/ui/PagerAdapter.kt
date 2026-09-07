package com.nmichail.android_pmu.presentation.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class PagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RegistrationFragment()
            1 -> RulesFragment()
            2 -> AuthorsFragment()
            else -> error("Неизвестная вкладка: $position")
        }
    }
}