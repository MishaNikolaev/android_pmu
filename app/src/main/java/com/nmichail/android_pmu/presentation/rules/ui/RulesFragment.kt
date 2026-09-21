package com.nmichail.android_pmu.presentation.rules.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.nmichail.android_pmu.MainActivity
import com.nmichail.android_pmu.R

class RulesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_rules, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(bottom = bars.bottom)
            insets
        }

        val webView = view.findViewById<WebView>(R.id.webViewRules)
        val html = resources.openRawResource(R.raw.rules)
            .bufferedReader()
            .use { it.readText() }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        view.findViewById<Button>(R.id.btnStartGame).setOnClickListener {
            (activity as? MainActivity)?.openGame()
        }
    }
}