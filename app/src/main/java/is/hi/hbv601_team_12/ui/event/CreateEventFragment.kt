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
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineEventsRepository
import `is`.hi.hbv601_team_12.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class CreateEventFragment : Fragment() {

    private lateinit var eventsRepository: OfflineEventsRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_event, container, false)

        // Initialize the OfflineEventsRepository
        val db = AppDatabase.getDatabase(requireContext()) // Assuming you have an AppDatabase class
        eventsRepository = OfflineEventsRepository(db.eventDao()) // Pass the DAO to the repository

        view.findViewById<Button>(R.id.saveEventButton).setOnClickListener {
            val eventName = view.findViewById<EditText>(R.id.eventNameEditText).text.toString()
            val eventDescription = view.findViewById<EditText>(R.id.eventDescriptionEditText).text.toString()
            val startDateTime = LocalDateTime.now() // Placeholder, replace with actual date picker logic
            val durationMinutes = 120 // Placeholder, replace with actual logic
            val location = "Some Location" // Placeholder, replace with actual logic

            lifecycleScope.launch(Dispatchers.IO) {
                val eventId = eventsRepository.createEvent(
                    name = eventName,
                    description = eventDescription,
                    startDateTime = startDateTime,
                    durationMinutes = durationMinutes,
                    creatorId = 1, // Placeholder, replace with actual user ID
                    location = location,
                    isPublic = true,
                    maxParticipants = null
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show()
                    
                    // Navigate to the EventFragment with the new event's ID
                    val bundle = Bundle().apply {
                        putInt("eventId", eventId.toInt()) // Convert Long to Int
                    }
                    findNavController().navigate(R.id.action_createEventFragment_to_eventFragment, bundle)
                }
            }
        }

        return view
    }
}
