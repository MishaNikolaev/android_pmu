package com.nmichail.android_pmu.presentation.records.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nmichail.android_pmu.R
import com.nmichail.android_pmu.presentation.records.RecordsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecordsFragment : Fragment() {

    private val viewModel: RecordsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listView = view.findViewById<ListView>(R.id.lvRecords)
        val emptyView = view.findViewById<TextView>(R.id.tvRecordsEmpty)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    emptyView.isVisible = state.records.isEmpty()
                    listView.isVisible = state.records.isNotEmpty()
                    listView.adapter = RecordsAdapter(requireContext(), state.records)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }
}
