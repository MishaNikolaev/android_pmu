package com.nmichail.android_pmu

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nmichail.android_pmu.domain.repository.ScoreRepository
import com.nmichail.android_pmu.presentation.game.ui.GameFragment
import com.nmichail.android_pmu.presentation.game.ui.GameResultFragment
import com.nmichail.android_pmu.presentation.main.MainState
import com.nmichail.android_pmu.presentation.main.MainViewModel
import com.nmichail.android_pmu.presentation.main.ui.PagerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAB_REGISTRATION = 0
        const val TAB_RULES = 1
        private const val TAB_COUNT = 5
        private const val KEY_CURRENT_USER_ID = "current_user_id"
    }

    private val mainViewModel: MainViewModel by viewModel()
    private val scoreRepository: ScoreRepository by inject()

    private lateinit var tabsContainer: View
    private lateinit var gameContainer: View
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private val currentUser
        get() = (mainViewModel.state.value as? MainState.Content)?.currentUser

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
                2 -> getString(R.string.tab_records)
                3 -> getString(R.string.tab_authors)
                4 -> getString(R.string.tab_settings)
                else -> ""
            }
        }.attach()

        val savedUserId = savedInstanceState?.getLong(KEY_CURRENT_USER_ID, -1L) ?: -1L
        if (savedUserId > 0 && currentUser == null) {
            mainViewModel.restoreUser(savedUserId)
        }

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_CURRENT_USER_ID, currentUser?.id ?: -1L)
    }

    fun openGame() {
        if (currentUser == null) {
            Toast.makeText(this, R.string.select_player_first, Toast.LENGTH_SHORT).show()
            showTabs(TAB_REGISTRATION)
            return
        }

        tabsContainer.isVisible = false
        viewPager.isUserInputEnabled = false
        gameContainer.isVisible = true
        gameContainer.bringToFront()

        supportFragmentManager.beginTransaction()
            .replace(R.id.gameContainer, GameFragment())
            .commit()
    }

    fun openGameResult(score: Int) {
        currentUser?.let { user ->
            lifecycleScope.launch(Dispatchers.IO) {
                scoreRepository.save(
                    userId = user.id,
                    score = score,
                    difficulty = user.difficulty
                )
            }
        }

        tabsContainer.isVisible = false
        viewPager.isUserInputEnabled = false
        gameContainer.isVisible = true
        gameContainer.bringToFront()

        supportFragmentManager.beginTransaction()
            .replace(R.id.gameContainer, GameResultFragment.newInstance(score))
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
        viewPager.setCurrentItem(tabIndex.coerceIn(0, TAB_COUNT - 1), false)
    }
}