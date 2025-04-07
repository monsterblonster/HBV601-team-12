package `is`.hi.hbv601_team_12.ui.event

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import java.time.temporal.ChronoUnit
import java.util.Calendar

class CreateEventFragment : Fragment() {

    private lateinit var eventsRepository: OnlineEventsRepository
    private var groupId: Long? = null
    private var startDateTime: LocalDateTime? = null
    private var durationHours: Int = 1 // Default duration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_event, container, false)

        eventsRepository = OnlineEventsRepository()
        groupId = arguments?.getLong("groupId")
        println("DEBUG: Creating event for group $groupId")

        // Setup start time picker
        view.findViewById<TextInputEditText>(R.id.startDateTimeEditText).setOnClickListener {
            showDateTimePicker()
        }

        // Setup duration input
        view.findViewById<TextInputEditText>(R.id.durationEditText).setOnClickListener {
            showDurationPicker()
        }

        view.findViewById<Button>(R.id.saveEventButton).setOnClickListener {
            val eventName = view.findViewById<TextInputEditText>(R.id.eventNameEditText).text.toString()
            val eventDescription = view.findViewById<TextInputEditText>(R.id.eventDescriptionEditText).text.toString()
            val location = view.findViewById<TextInputEditText>(R.id.locationEditText).text.toString()

            if (eventName.isBlank()) {
                Toast.makeText(requireContext(), "Event name cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (startDateTime == null) {
                Toast.makeText(requireContext(), "Please select start time!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startDateTime = startDateTime!!.truncatedTo(ChronoUnit.MINUTES).withSecond(1)

            lifecycleScope.launch(Dispatchers.IO) {

                val response = eventsRepository.createEvent(
                    userId = getCurrentUserId().toLong(),
                    groupId = groupId ?: return@launch,
                    event = Event(
                        name = eventName,
                        description = eventDescription,
                        date = startDateTime!!,
                        durationMinutes = durationHours * 60, // Convert hours to minutes
                        creatorId = getCurrentUserId().toLong(),
                        location = location,
                        isPublic = true,
                        maxParticipants = null,
                        groupId = groupId!!,
                        comments = emptyList(),
                    )
                )


                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Time: ${startDateTime.toString()}", Toast.LENGTH_LONG).show()
                        val eventId = response.body()?.id ?: -1L
                        val bundle = Bundle().apply { putLong("eventId", eventId) }
                        findNavController().navigate(R.id.eventFragment, bundle)
                    } else {
                        Toast.makeText(requireContext(), "Failed to create event: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return view
    }


    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val timePicker = TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        startDateTime = LocalDateTime.of(
                            LocalDate.of(year, month + 1, day),
                            LocalTime.of(hour, minute))
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

    private fun showDurationPicker() {
        val durations = arrayOf("1 hour", "2 hours", "3 hours", "4 hours", "Custom")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Duration")
            .setItems(durations) { _, which ->
                durationHours = when (which) {
                    0 -> 1
                    1 -> 2
                    2 -> 3
                    3 -> 4
                    else -> showCustomDurationPicker()
                }
                updateDurationField()
            }
            .show()
    }

    private fun showCustomDurationPicker(): Int {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Enter hours"
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Custom Duration")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                durationHours = input.text.toString().toIntOrNull() ?: 1
                updateDurationField()
            }
            .setNegativeButton("Cancel", null)
            .show()
        return 1 // Default if cancelled
    }

    private fun updateDateTimeFields() {
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")
        view?.findViewById<TextInputEditText>(R.id.startDateTimeEditText)?.setText(
            startDateTime?.format(formatter) ?: ""
        )
    }

    private fun updateDurationField() {
        view?.findViewById<TextInputEditText>(R.id.durationEditText)?.setText(
            "$durationHours ${if (durationHours == 1) "hour" else "hours"}"
        )
    }

    private fun getCurrentUserId(): Long {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Context.MODE_PRIVATE)
        return sharedPref.getLong("loggedInUserId", -1L)
    }
}