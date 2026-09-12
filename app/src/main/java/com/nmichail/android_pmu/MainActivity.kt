package com.nmichail.android_pmu

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nmichail.android_pmu.domain.GameSettings
import com.nmichail.android_pmu.presentation.ui.PagerAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var tabsContainer: View
    private lateinit var gameContainer: View
    private lateinit var viewPager: ViewPager2

    var gameSettings: GameSettings = GameSettings(
        gameSpeed = 50,
        maxTarakani = 5,
        bonusIntervalSec = 10,
        roundDurationSec = 60
    )

    fun openGameResult(score: Int) {
        tabsContainer.isVisible = false
        viewPager.isUserInputEnabled = false
        gameContainer.isVisible = true
        gameContainer.bringToFront()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        viewPager.adapter = PagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_registration)
                1 -> getString(R.string.tab_rules)
                2 -> getString(R.string.tab_authors)
                3 -> getString(R.string.tab_settings)
                else -> ""
            }
        }.attach()
    }

    fun openGame() {
        TODO("Not yet implemented")
    }
}