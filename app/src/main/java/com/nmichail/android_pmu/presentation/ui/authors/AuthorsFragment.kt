package com.nmichail.android_pmu.presentation.ui.authors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.nmichail.android_pmu.R
import com.nmichail.android_pmu.domain.model.Author

class AuthorsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_authors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authors = listOf(
            Author(getString(R.string.author_nikolaev), R.drawable.nikolaev),
            Author(getString(R.string.author_terentyev), R.drawable.terentev)
        )

        val listView = view.findViewById<ListView>(R.id.lvAuthors)
        listView.adapter = AuthorsAdapter(requireContext(), authors)
    }
}