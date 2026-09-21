package com.nmichail.android_pmu.presentation.registration.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nmichail.android_pmu.R
import com.nmichail.android_pmu.domain.model.Player
import com.nmichail.android_pmu.domain.model.User
import com.nmichail.android_pmu.presentation.main.MainState
import com.nmichail.android_pmu.presentation.main.MainViewModel
import com.nmichail.android_pmu.presentation.registration.RegistrationState
import com.nmichail.android_pmu.presentation.registration.RegistrationViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Calendar

class RegistrationFragment : Fragment() {

    private val viewModel: RegistrationViewModel by viewModel()
    private val mainViewModel: MainViewModel by activityViewModel()

    private var updatingSpinner = false
    private var restoringUi = false

    private lateinit var formContainer: View
    private lateinit var progressLoading: View
    private lateinit var errorContainer: View
    private lateinit var tvError: TextView
    private lateinit var btnRetry: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.registration_form, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        formContainer = view.findViewById(R.id.formContainer)
        progressLoading = view.findViewById(R.id.progressLoading)
        errorContainer = view.findViewById(R.id.errorContainer)
        tvError = view.findViewById(R.id.tvError)
        btnRetry = view.findViewById(R.id.btnRetry)

        val name = view.findViewById<EditText>(R.id.name)
        val surname = view.findViewById<EditText>(R.id.surname)
        val otchestvo = view.findViewById<EditText>(R.id.othcestvo)
        val genderGroup = view.findViewById<RadioGroup>(R.id.myRadioGroup)
        val courseSpinner = view.findViewById<Spinner>(R.id.combobox)
        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val buttonShowZadiak = view.findViewById<Button>(R.id.showZnak)
        val buttonRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvResult = view.findViewById<TextView>(R.id.tvResult)
        val ivZodiac = view.findViewById<ImageView>(R.id.ivZodiac)
        val spinnerPlayers = view.findViewById<Spinner>(R.id.spinnerPlayers)
        val tvCurrentPlayer = view.findViewById<TextView>(R.id.tvCurrentPlayer)

        name.bindText { viewModel.updateName(it) }
        surname.bindText { viewModel.updateSurname(it) }
        otchestvo.bindText { viewModel.updateOtchestvo(it) }

        genderGroup.setOnCheckedChangeListener { _, checkedId ->
            if (restoringUi) return@setOnCheckedChangeListener
            val gender = when (checkedId) {
                R.id.male -> "Мужчина"
                R.id.female -> "Женщина"
                else -> ""
            }
            viewModel.updateGender(gender)
        }

        courseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (restoringUi) return
                viewModel.updateCourse(position + 1)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) viewModel.updateDifficulty(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            if (restoringUi) return@setOnDateChangeListener
            viewModel.updateBirthDate(dayOfMonth, month + 1, year)
        }

        spinnerPlayers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (updatingSpinner) return
                if (position == 0) {
                    mainViewModel.setCurrentUser(null)
                    viewModel.clearForm()
                    updateCurrentPlayerLabel(tvCurrentPlayer, null)
                    return
                }
                val content = viewModel.state.value as? RegistrationState.Content ?: return
                val user = content.users.getOrNull(position - 1) ?: return
                mainViewModel.setCurrentUser(user)
                viewModel.applyUser(user)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        buttonShowZadiak.setOnClickListener { viewModel.showPreview() }
        btnRetry.setOnClickListener { viewModel.retry() }

        buttonRegister.setOnClickListener {
            viewModel.register(
                onSuccess = { saved ->
                    mainViewModel.setCurrentUser(saved)
                    Toast.makeText(requireContext(), R.string.player_registered, Toast.LENGTH_SHORT).show()
                },
                onError = {
                    Toast.makeText(requireContext(), R.string.fill_name_surname, Toast.LENGTH_SHORT).show()
                }
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var lastUsers: List<User> = emptyList()
                viewModel.state.collect { state ->
                    when (state) {
                        RegistrationState.Initial -> Unit
                        RegistrationState.Loading -> {
                            formContainer.isVisible = false
                            errorContainer.isVisible = false
                            progressLoading.isVisible = true
                        }
                        is RegistrationState.Content -> {
                            progressLoading.isVisible = false
                            errorContainer.isVisible = false
                            formContainer.isVisible = true
                            restoreForm(
                                state = state,
                                name = name,
                                surname = surname,
                                otchestvo = otchestvo,
                                genderGroup = genderGroup,
                                courseSpinner = courseSpinner,
                                seekBar = seekBar,
                                calendarView = calendarView
                            )
                            if (state.users != lastUsers) {
                                lastUsers = state.users
                                bindPlayersSpinner(
                                    spinnerPlayers,
                                    state.users,
                                    (mainViewModel.state.value as? MainState.Content)?.currentUser
                                )
                            }
                            updateCurrentPlayerLabel(
                                tvCurrentPlayer,
                                (mainViewModel.state.value as? MainState.Content)?.currentUser
                            )
                            if (state.preview != null) {
                                showPlayerPreview(state.preview, tvResult, ivZodiac)
                            } else {
                                tvResult.text = ""
                                ivZodiac.setImageDrawable(null)
                            }
                        }
                        is RegistrationState.Error -> {
                            formContainer.isVisible = false
                            progressLoading.isVisible = false
                            errorContainer.isVisible = true
                            tvError.text = state.message
                        }
                    }
                }
            }
        }

        if (viewModel.state.value is RegistrationState.Initial) {
            viewModel.loadUsers()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val content = viewModel.state.value as? RegistrationState.Content ?: return
        val root = view ?: return
        restoreForm(
            state = content,
            name = root.findViewById(R.id.name),
            surname = root.findViewById(R.id.surname),
            otchestvo = root.findViewById(R.id.othcestvo),
            genderGroup = root.findViewById(R.id.myRadioGroup),
            courseSpinner = root.findViewById(R.id.combobox),
            seekBar = root.findViewById(R.id.seekBar),
            calendarView = root.findViewById(R.id.calendarView)
        )
        content.preview?.let { preview ->
            showPlayerPreview(
                preview,
                root.findViewById(R.id.tvResult),
                root.findViewById(R.id.ivZodiac)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.state.value is RegistrationState.Content) {
            viewModel.loadUsers()
        }
    }

    private fun restoreForm(
        state: RegistrationState.Content,
        name: EditText,
        surname: EditText,
        otchestvo: EditText,
        genderGroup: RadioGroup,
        courseSpinner: Spinner,
        seekBar: SeekBar,
        calendarView: CalendarView
    ) {
        restoringUi = true
        if (name.text.toString() != state.name) name.setText(state.name)
        if (surname.text.toString() != state.surname) surname.setText(state.surname)
        if (otchestvo.text.toString() != state.otchestvo) otchestvo.setText(state.otchestvo)
        when (state.gender) {
            "Мужчина" -> genderGroup.check(R.id.male)
            "Женщина" -> genderGroup.check(R.id.female)
            else -> genderGroup.clearCheck()
        }
        val courseIndex = (state.course - 1).coerceIn(0, 3)
        if (courseSpinner.selectedItemPosition != courseIndex) {
            courseSpinner.setSelection(courseIndex, false)
        }
        if (seekBar.progress != state.difficulty) {
            seekBar.progress = state.difficulty
        }
        val calendar = Calendar.getInstance().apply {
            set(state.birthYear, state.birthMonth - 1, state.birthDay)
        }
        calendarView.date = calendar.timeInMillis
        restoringUi = false
    }

    private fun bindPlayersSpinner(spinner: Spinner, users: List<User>, currentUser: User?) {
        val labels = mutableListOf(getString(R.string.select_player_hint))
        labels.addAll(users.map { formatPlayerName(it) })
        updatingSpinner = true
        spinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            labels
        )
        val selectedIndex = users.indexOfFirst { it.id == currentUser?.id }
        spinner.setSelection(if (selectedIndex >= 0) selectedIndex + 1 else 0, false)
        spinner.post { updatingSpinner = false }
    }

    private fun updateCurrentPlayerLabel(label: TextView, user: User?) {
        label.text = if (user == null) {
            getString(R.string.current_player_none)
        } else {
            getString(R.string.current_player, formatPlayerName(user))
        }
    }

    private fun formatPlayerName(user: User): String {
        return listOf(user.surname, user.name, user.otchestvo)
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }

    private fun showPlayerPreview(player: Player, tvResult: TextView, ivZodiac: ImageView) {
        tvResult.text =
            "${player.surname} ${player.name} ${player.otchestvo}\n" +
            "Пол: ${player.gender}\n" +
            "Курс: ${player.course}\n" +
            "Сложность: ${player.difficulty}\n" +
            "Дата: ${player.birthDay}.${player.birthMonth}.${player.birthYear}\n" +
            "Зодиак: ${player.zodiac}"
        ivZodiac.setImageResource(zodiacImage(player.zodiac))
    }

    private fun EditText.bindText(onChanged: (String) -> Unit) {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (restoringUi) return
                onChanged(s?.toString().orEmpty())
            }
        })
    }

    private fun zodiacImage(zodiac: String): Int {
        return when (zodiac) {
            "Овен" -> R.drawable.oven
            "Телец" -> R.drawable.telec
            "Близнецы" -> R.drawable.blizneci
            "Рак" -> R.drawable.rak
            "Лев" -> R.drawable.lev
            "Дева" -> R.drawable.deva
            "Весы" -> R.drawable.vesi
            "Скорпион" -> R.drawable.scorpion
            "Стрелец" -> R.drawable.strelec
            "Козерог" -> R.drawable.kozerog
            "Водолей" -> R.drawable.vodoley
            "Рыбы" -> R.drawable.ribi
            else -> R.drawable.ic_launcher_foreground
        }
    }
}
