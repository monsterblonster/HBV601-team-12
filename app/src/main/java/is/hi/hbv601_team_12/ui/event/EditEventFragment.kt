package `is`.hi.hbv601_team_12.ui.event

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
import `is`.hi.hbv601_team_12.data.repositories.EventsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class EditEventFragment : Fragment() {

    private lateinit var eventsRepository: EventsRepository
    private var event: Event? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_event, container, false)

        // Initialize the EventsRepository (you might need to pass this via dependency injection)
        // eventsRepository = ...

        // Get the event ID from the arguments
        val eventId = arguments?.getInt("eventId") ?: 0

        // Load the event details
        lifecycleScope.launch(Dispatchers.IO) {
            event = eventsRepository.getEventStream(eventId).first()
            withContext(Dispatchers.Main) {
                view.findViewById<EditText>(R.id.eventNameEditText).setText(event?.name)
                view.findViewById<EditText>(R.id.eventDescriptionEditText).setText(event?.description)
                // Set the date and time pickers with the event's startDateTime and durationMinutes
            }
        }

        // Set up the save event button
        view.findViewById<Button>(R.id.saveEventButton).setOnClickListener {
            val eventName = view.findViewById<EditText>(R.id.eventNameEditText).text.toString()
            val eventDescription = view.findViewById<EditText>(R.id.eventDescriptionEditText).text.toString()
            val startDateTime = LocalDateTime.now() // Placeholder, replace with actual date picker logic
            val durationMinutes = 120 // Placeholder, replace with actual logic
            val location = "Some Location" // Placeholder, replace with actual logic

            event?.let {
                it.name = eventName
                it.description = eventDescription
                it.startDateTime = startDateTime
                it.durationMinutes = durationMinutes
                it.location = location

                // Update the event in the database
                lifecycleScope.launch(Dispatchers.IO) {
                    eventsRepository.updateEvent(it)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show()
                        
                        // Navigate back to the EventFragment
                        findNavController().navigateUp()
                    }
                }
            }
        }

        return view
    }
}
