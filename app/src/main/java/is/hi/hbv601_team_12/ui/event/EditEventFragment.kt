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
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineEventsRepository
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineGroupsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
class EditEventFragment : Fragment() {

    private lateinit var eventsRepository: OfflineEventsRepository
    private var event: Event? = null
    private lateinit var groupsRepository: OfflineGroupsRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_event, container, false)

        val db = `is`.hi.hbv601_team_12.data.AppDatabase.getDatabase(requireContext())
        eventsRepository = OfflineEventsRepository(db.eventDao())


        val eventId = arguments?.getInt("eventId") ?: 0

        lifecycleScope.launch(Dispatchers.IO) {
            event = eventsRepository.getEventStream(eventId).first()
            withContext(Dispatchers.Main) {
                view.findViewById<EditText>(R.id.eventNameEditText).setText(event?.name)
                view.findViewById<EditText>(R.id.eventDescriptionEditText).setText(event?.description)
                // TODO vantar meira event info
            }
        }

        view.findViewById<Button>(R.id.saveEventButton).setOnClickListener {
            val eventName = view.findViewById<EditText>(R.id.eventNameEditText).text.toString()
            val eventDescription = view.findViewById<EditText>(R.id.eventDescriptionEditText).text.toString()
            val startDateTime = LocalDateTime.now() // Placeholder
            val durationMinutes = 120 // Placeholder
            val location = "Some Location" // Placeholder

            event?.let {
                it.name = eventName
                it.description = eventDescription
                it.startDateTime = startDateTime
                it.durationMinutes = durationMinutes
                it.location = location

                lifecycleScope.launch(Dispatchers.IO) {
                    eventsRepository.updateEvent(it)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                }
            }
        }

        return view
    }
}
