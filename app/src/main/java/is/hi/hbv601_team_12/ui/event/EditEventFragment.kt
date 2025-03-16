package `is`.hi.hbv601_team_12.ui.event

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.entities.Event
import `is`.hi.hbv601_team_12.data.onlineRepositories.OnlineEventsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class EditEventFragment : Fragment() {

    private lateinit var eventsRepository: OnlineEventsRepository
    private var event: Event? = null
    private var eventId: Long = -1

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

        lifecycleScope.launch(Dispatchers.IO) {
            val response = eventsRepository.getEvent(eventId)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    event = response.body()
                    view.findViewById<EditText>(R.id.eventNameEditText).setText(event?.name)
                    view.findViewById<EditText>(R.id.eventDescriptionEditText).setText(event?.description)
                    // TODO: date time stuff
                } else {
                    Toast.makeText(requireContext(), "Failed to load event details", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }

        view.findViewById<Button>(R.id.saveEventButton).setOnClickListener {
            val eventName = view.findViewById<EditText>(R.id.eventNameEditText).text.toString()
            val eventDescription = view.findViewById<EditText>(R.id.eventDescriptionEditText).text.toString()
            val startDateTime = LocalDateTime.now() // placeholder
            val durationMinutes = 120 // placeholder
            val location = "Some Location" // placeholder

            if (eventName.isBlank()) {
                Toast.makeText(requireContext(), "Event name cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            event?.let {
                it.name = eventName
                it.description = eventDescription
                it.startDateTime = startDateTime
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

        return view
    }

    private fun getCurrentUserId(): Long {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Context.MODE_PRIVATE)
        return sharedPref.getLong("loggedInUserId", -1L)
    }

}
