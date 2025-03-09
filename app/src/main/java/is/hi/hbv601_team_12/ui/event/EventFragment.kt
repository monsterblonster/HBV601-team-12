package `is`.hi.hbv601_team_12.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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
import java.time.format.DateTimeFormatter

class EventFragment : Fragment() {

    private lateinit var eventsRepository: EventsRepository
    private var event: Event? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event, container, false)

        // Initialize the EventsRepository (you might need to pass this via dependency injection)
        // eventsRepository = ...

        // Get the event ID from the arguments
        val eventId = arguments?.getInt("eventId") ?: 0

        // Load the event details
        lifecycleScope.launch(Dispatchers.IO) {
            event = eventsRepository.getEventStream(eventId).first()
            withContext(Dispatchers.Main) {
                view.findViewById<TextView>(R.id.eventNameTextView).text = event?.name
                view.findViewById<TextView>(R.id.eventDescriptionTextView).text = event?.description
                view.findViewById<TextView>(R.id.eventStartTimeTextView).text = "Start: ${event?.startDateTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}"
                view.findViewById<TextView>(R.id.eventDurationTextView).text = "Duration: ${event?.durationMinutes} minutes"
                view.findViewById<TextView>(R.id.eventLocationTextView).text = "Location: ${event?.location}"
            }
        }

        // Set up the edit event button
        view.findViewById<Button>(R.id.editEventButton).setOnClickListener {
            // Navigate to the EditEventFragment
            val bundle = Bundle().apply {
                putInt("eventId", eventId)
            }
            findNavController().navigate(R.id.action_eventFragment_to_editEventFragment, bundle)
        }

        return view
    }
}
