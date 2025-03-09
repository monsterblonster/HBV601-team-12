package `is`.hi.hbv601_team_12.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.entities.Event
import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineEventsRepository
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter
class EventFragment : Fragment() {

    private lateinit var eventsRepository: OfflineEventsRepository
    private lateinit var usersRepository: OfflineUsersRepository
    private var eventId: Int? = null
    private lateinit var participantAdapter: ParticipantAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event, container, false)

        val db = AppDatabase.getDatabase(requireContext())
        eventsRepository = OfflineEventsRepository(db.eventDao())
        usersRepository = OfflineUsersRepository(db.userDao()) 

        eventId = arguments?.getInt("eventId")

        val recyclerView = view.findViewById<RecyclerView>(R.id.participantsRecyclerView)
        participantAdapter = ParticipantAdapter()
        recyclerView.adapter = participantAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        if (eventId != null) {
            loadEventDetails(eventId!!)
        } else {
            Toast.makeText(requireContext(), "Invalid Event ID!", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun loadEventDetails(eventId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val event = eventsRepository.getEventById(eventId)
            val participants = eventsRepository.getParticipantsForEvent(eventId)

            withContext(Dispatchers.Main) {
                if (event != null) {
                    view?.findViewById<TextView>(R.id.eventNameTextView)?.text = event.name
                    view?.findViewById<TextView>(R.id.eventDescriptionTextView)?.text = event.description
                    view?.findViewById<TextView>(R.id.eventStartTimeTextView)?.text = "Start: ${event.startDateTime}"
                    view?.findViewById<TextView>(R.id.eventDurationTextView)?.text = "Duration: ${event.durationMinutes} minutes"
                    view?.findViewById<TextView>(R.id.eventLocationTextView)?.text = "Location: ${event.location}"

                    val participantWithStatusList = participants.map { participant ->
                        val user = usersRepository.getUserById(participant.userId) 
                        if (user != null) {
                            ParticipantWithStatus(user, participant.status)
                        } else {
                            null
                        }
                    }.filterNotNull()

                    participantAdapter.submitList(participantWithStatusList)
                } else {
                    Toast.makeText(requireContext(), "Event not found!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
        private fun navigateToEditEventFragment() {
        val eventId = eventId ?: return

        val bundle = Bundle().apply {
            putInt("eventId", eventId)
    }
}
}
