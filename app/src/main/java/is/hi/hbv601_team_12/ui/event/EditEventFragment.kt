package `is`.hi.hbv601_team_12.ui.event

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.entities.Event
import `is`.hi.hbv601_team_12.data.onlineRepositories.OnlineEventsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class EditEventFragment : Fragment() {

    private lateinit var eventsRepository: OnlineEventsRepository
    private var event: Event? = null
    private var eventId: Long = -1
    private var startDateTime: LocalDateTime? = null
    private var endDateTime: LocalDateTime? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_event, container, false)

        eventsRepository = OnlineEventsRepository()
        eventId = arguments?.getLong("eventId") ?: -1L

        if (eventId == -1L) {
            Toast.makeText(requireContext(), "Invalid event ID", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return view
        }

        // Setup date/time pickers
        view.findViewById<TextInputEditText>(R.id.startDateTimeEditText).setOnClickListener {
            showDateTimePicker(true)
        }

        view.findViewById<TextInputEditText>(R.id.endDateTimeEditText).setOnClickListener {
            showDateTimePicker(false)
        }

        // Load event data
        lifecycleScope.launch(Dispatchers.IO) {
            val response = eventsRepository.getEvent(eventId)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    event = response.body()
                    event?.let {
                        view.findViewById<TextInputEditText>(R.id.eventNameEditText).setText(it.name)
                        view.findViewById<TextInputEditText>(R.id.eventDescriptionEditText).setText(it.description)
                        view.findViewById<TextInputEditText>(R.id.locationEditText).setText(it.location)

                        startDateTime = it.date
                        endDateTime = it.date?.plusMinutes(it.durationMinutes.toLong())

                        updateDateTimeFields()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load event details", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }

        view.findViewById<Button>(R.id.saveEventButton).setOnClickListener {
            saveEvent(view)
        }

        return view
    }

    private fun saveEvent(view: View) {
        val eventName = view.findViewById<TextInputEditText>(R.id.eventNameEditText).text.toString()
        val eventDescription = view.findViewById<TextInputEditText>(R.id.eventDescriptionEditText).text.toString()
        val location = view.findViewById<TextInputEditText>(R.id.locationEditText).text.toString()

        if (eventName.isBlank()) {
            Toast.makeText(requireContext(), "Event name cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        if (startDateTime == null || endDateTime == null) {
            Toast.makeText(requireContext(), "Please select start and end times!", Toast.LENGTH_SHORT).show()
            return
        }

        val durationMinutes = java.time.Duration.between(startDateTime, endDateTime).toMinutes().toInt()
        if (durationMinutes <= 0) {
            Toast.makeText(requireContext(), "End time must be after start time!", Toast.LENGTH_SHORT).show()
            return
        }

        event?.let {
            it.name = eventName
            it.description = eventDescription
            it.date = startDateTime as LocalDateTime
            it.durationMinutes = durationMinutes
            it.location = location

            lifecycleScope.launch(Dispatchers.IO) {
                val response = eventsRepository.editEvent(eventId, getCurrentUserId().toLong(), it)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update event: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showDateTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val timePicker = TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        val selectedDateTime = LocalDateTime.of(
                            LocalDate.of(year, month + 1, day),
                            LocalTime.of(hour, minute))

                            if (isStartTime) {
                                startDateTime = selectedDateTime
                            } else {
                                endDateTime = selectedDateTime
                            }
                                    updateDateTimeFields()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                )
                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun updateDateTimeFields() {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")
        view?.findViewById<TextInputEditText>(R.id.startDateTimeEditText)?.setText(
            startDateTime?.format(formatter) ?: ""
        )
        view?.findViewById<TextInputEditText>(R.id.endDateTimeEditText)?.setText(
            endDateTime?.format(formatter) ?: ""
        )
    }

    private fun getCurrentUserId(): Long {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Context.MODE_PRIVATE)
        return sharedPref.getLong("loggedInUserId", -1L)
    }
}