package com.nmichail.android_pmu.presentation.records.ui
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.nmichail.android_pmu.R
import com.nmichail.android_pmu.domain.model.ScoreRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordsAdapter(
	context: Context,
	private val records: List<ScoreRecord>
) : ArrayAdapter<ScoreRecord>(context, 0, records) {

	private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val view = convertView ?: LayoutInflater.from(context)
			.inflate(R.layout.item_record, parent, false)

		val record = records[position]
		view.findViewById<TextView>(R.id.tvRecordPlayer).text = record.playerName
		view.findViewById<TextView>(R.id.tvRecordDetails).text = context.getString(
			R.string.record_details,
			record.score,
			record.difficulty,
			dateFormat.format(Date(record.createdAt))
		)

		return view
	}
}