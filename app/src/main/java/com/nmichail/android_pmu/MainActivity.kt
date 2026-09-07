package com.nmichail.android_pmu

import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.nmichail.android_pmu.domain.Player

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.registration_form)

        val name = findViewById<EditText>(R.id.name)
        val surname = findViewById<EditText>(R.id.surname)
        val otchestvo = findViewById<EditText>(R.id.othcestvo)
        val genderGroup = findViewById<RadioGroup>(R.id.myRadioGroup)
        val courseSpinner = findViewById<Spinner>(R.id.combobox)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val buttonShowZadiak = findViewById<Button>(R.id.showZnak)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val ivZodiac = findViewById<ImageView>(R.id.ivZodiac)

        var selectedDay = 1
        var selectedMonth = 1
        var selectedYear = 2000

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDay = dayOfMonth
            selectedMonth = month + 1
            selectedYear = year
        }

        buttonShowZadiak.setOnClickListener {
            val day = selectedDay
            val month = selectedMonth
            val birthYear = selectedYear

            val gender = when (genderGroup.checkedRadioButtonId) {
                R.id.male -> "Мужчина"
                R.id.female -> "Женщина"
                else -> "Вы не выбрали пол"
            }

            val player = Player(
                name = name.text.toString(),
                surname = surname.text.toString(),
                otchestvo = otchestvo.text.toString(),
                course = courseSpinner.selectedItemPosition + 1,
                difficulty = seekBar.progress,
                birthDay = day,
                birthMonth = month,
                birthYear = birthYear,
                zodiac = "",
                gender = gender
            )
        }
    }

}