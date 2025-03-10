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
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineEventsRepository
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineGroupsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class CreateEventFragment : Fragment() {

    private lateinit var eventsRepository: OfflineEventsRepository
    private lateinit var groupsRepository: OfflineGroupsRepository
    private var groupId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_event, container, false)

        val db = AppDatabase.getDatabase(requireContext())
        eventsRepository = OfflineEventsRepository(db.eventDao())
        groupsRepository = OfflineGroupsRepository(db.groupDao())

        groupId = arguments?.getInt("groupId")

        view.findViewById<Button>(R.id.saveEventButton).setOnClickListener {
            val eventName = view.findViewById<EditText>(R.id.eventNameEditText).text.toString()
            val eventDescription = view.findViewById<EditText>(R.id.eventDescriptionEditText).text.toString()
            val startDateTime = LocalDateTime.now() // Placeholder
            val durationMinutes = 120 // Placeholder
            val location = "Some Location" // Placeholder

            lifecycleScope.launch(Dispatchers.IO) {
                val eventId = eventsRepository.createEvent(
                    name = eventName,
                    description = eventDescription,
                    startDateTime = startDateTime,
                    durationMinutes = durationMinutes,
                    creatorId = getCurrentUserId(),
                    location = location,
                    isPublic = true,
                    maxParticipants = null,
                    groupId = groupId!!
                )

                // Invite all users in the group to the event
                if (groupId != null) {
                    val group = groupsRepository.getGroupById(groupId!!)
                    if (group != null) {
                        val userIds = group.getMemberList()
                        eventsRepository.inviteUsersToEvent(eventId.toInt(), userIds)
                    }
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show()

                    val bundle = Bundle().apply {
                        putInt("eventId", eventId.toInt())
                    }
                    findNavController().navigate(R.id.eventFragment, bundle)
                }
            }
        }

        return view
    }

    private fun getCurrentUserId(): Int {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("loggedInUserId", -1)
    }
}
