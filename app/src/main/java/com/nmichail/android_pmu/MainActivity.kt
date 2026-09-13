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
import com.nmichail.android_pmu.presentation.ui.game.GameFragment

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAB_RULES = 1
    }

    var gameSettings: GameSettings = GameSettings(
        gameSpeed = 50,
        maxTarakani = 5,
        bonusIntervalSec = 10,
        roundDurationSec = 60 // 60 поменять и протестить ее изменение в настройках
    )

    private lateinit var tabsContainer: View
    private lateinit var gameContainer: View
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tabsContainer = findViewById(R.id.tabsContainer)
        gameContainer = findViewById(R.id.gameContainer)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

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

        val hasGameScreen = supportFragmentManager.findFragmentById(R.id.gameContainer) != null

        if (savedInstanceState != null && hasGameScreen) {
            tabsContainer.isVisible = false
            viewPager.isUserInputEnabled = false
            gameContainer.isVisible = true
            gameContainer.bringToFront()
        } else {
            gameContainer.isVisible = false
            tabsContainer.isVisible = true
            viewPager.isUserInputEnabled = true
            tabsContainer.bringToFront()
        }
    }

    fun openGame() {
        tabsContainer.isVisible = false
        viewPager.isUserInputEnabled = false
        gameContainer.isVisible = true
        gameContainer.bringToFront()

        supportFragmentManager.beginTransaction()
            .replace(R.id.gameContainer, GameFragment())
            .commit()
    }

    fun showTabs(tabIndex: Int = TAB_RULES) {
        supportFragmentManager.findFragmentById(R.id.gameContainer)?.let { fragment ->
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }

        gameContainer.isVisible = false
        tabsContainer.isVisible = true
        viewPager.isUserInputEnabled = true
        tabsContainer.bringToFront()
        viewPager.setCurrentItem(tabIndex.coerceIn(0, 3), false)
    }
}