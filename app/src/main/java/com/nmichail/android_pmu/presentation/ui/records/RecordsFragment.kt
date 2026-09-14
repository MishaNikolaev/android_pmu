package com.nmichail.android_pmu.presentation.ui.records

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nmichail.android_pmu.MyApplication
import com.nmichail.android_pmu.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecordsFragment : Fragment() {

    private val scoreRepository get() = (requireActivity().application as MyApplication).scoreRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onResume() {
        super.onResume()
        loadRecords()
    }

    private fun loadRecords() {
        val listView = view?.findViewById<ListView>(R.id.lvRecords) ?: return
        val emptyView = view?.findViewById<TextView>(R.id.tvRecordsEmpty) ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            val records = withContext(Dispatchers.IO) {
                scoreRepository.getRecords()
            }

            emptyView.isVisible = records.isEmpty()
            listView.isVisible = records.isNotEmpty()
            listView.adapter = RecordsAdapter(requireContext(), records)
        }
    }
}