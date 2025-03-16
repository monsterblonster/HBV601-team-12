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

class CreateEventFragment : Fragment() {

    private lateinit var eventsRepository: OnlineEventsRepository
    private var groupId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_event, container, false)

        eventsRepository = OnlineEventsRepository()
        groupId = arguments?.getLong("groupId")

        view.findViewById<Button>(R.id.saveEventButton).setOnClickListener {
            val eventName = view.findViewById<EditText>(R.id.eventNameEditText).text.toString()
            val eventDescription = view.findViewById<EditText>(R.id.eventDescriptionEditText).text.toString()
            val startDateTime = LocalDateTime.now() // TODO: Replace with user input
            val durationMinutes = 120 // TODO: Replace with user input
            val location = "Some Location" // TODO: Replace with user input


            if (eventName.isBlank()) {
                Toast.makeText(requireContext(), "Event name cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val response = eventsRepository.createEvent(
                    userId = getCurrentUserId().toLong(),
                    groupId = groupId ?: return@launch,
                    event = Event(
                        name = eventName,
                        description = eventDescription,
                        startDateTime = startDateTime,
                        durationMinutes = durationMinutes,
                        creatorId = getCurrentUserId().toLong(),
                        location = location,
                        isPublic = true,
                        maxParticipants = null,
                        groupId = groupId!!
                    )
                )

                // Invite all users in the group to the event
            /*    if (groupId != null) {
                    val group = groupsRepository.getGroupById(groupId!!)
                    if (group != null) {
                        val userIds = group.getMembersList()
                        eventsRepository.inviteUsersToEvent(eventId.toInt(), userIds)
                    }
                }*/

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show()
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

    private fun getCurrentUserId(): Long {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Context.MODE_PRIVATE)
        return sharedPref.getLong("loggedInUserId", -1L)
    }

}
