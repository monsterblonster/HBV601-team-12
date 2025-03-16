package `is`.hi.hbv601_team_12.ui.event

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.entities.*
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import `is`.hi.hbv601_team_12.data.onlineRepositories.*
import `is`.hi.hbv601_team_12.ui.adapters.ParticipantAdapter
import `is`.hi.hbv601_team_12.ui.adapters.ParticipantWithStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

class EventFragment : Fragment() {

    private lateinit var eventsRepository: OnlineEventsRepository
    private lateinit var usersRepository: OnlineUsersRepository
    private var eventId: Long? = null
    private var creatorId: Long? = null
    private lateinit var participantAdapter: ParticipantAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event, container, false)

        val db = `is`.hi.hbv601_team_12.data.AppDatabase.getDatabase(requireContext())
        val offlineUsersRepo = OfflineUsersRepository(db.userDao())

        eventsRepository = OnlineEventsRepository()
        usersRepository = OnlineUsersRepository(offlineUsersRepo)
        eventId = arguments?.getLong("eventId")

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

    private fun loadEventDetails(eventId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val response = eventsRepository.getEvent(eventId)

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val event = response.body()
                    if (event != null) {
                        creatorId = event.creatorId
                        updateUI(event)
                        setupMenu()
                        loadParticipants(eventId)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load event!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun loadParticipants(eventId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Each call returns List<User>
            val goingResponse = eventsRepository.getGoingUsers(eventId)
            val maybeResponse = eventsRepository.getMaybeUsers(eventId)
            val cantGoResponse = eventsRepository.getCantGoUsers(eventId)

            if (goingResponse.isSuccessful && maybeResponse.isSuccessful && cantGoResponse.isSuccessful) {
                val goingUsers = goingResponse.body().orEmpty()
                val maybeUsers = maybeResponse.body().orEmpty()
                val cantGoUsers = cantGoResponse.body().orEmpty()

                // Combine them:
                // We get a distinct list of all Users from going + maybe + cantGo
                val allUsers = (goingUsers + maybeUsers + cantGoUsers).distinctBy { it.id }

                val participants = mutableListOf<ParticipantWithStatus>()

                // Determine each user's status
                allUsers.forEach { user ->
                    val status = when (user) {
                        in goingUsers -> ParticipantStatus.GOING
                        in maybeUsers -> ParticipantStatus.MAYBE
                        in cantGoUsers -> ParticipantStatus.DECLINED
                        else -> ParticipantStatus.INVITED
                    }
                    participants.add(ParticipantWithStatus(user, status))
                }

                withContext(Dispatchers.Main) {
                    participantAdapter.submitList(participants)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to load participants!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun updateUI(event: Event) {
        view?.findViewById<TextView>(R.id.eventNameTextView)?.text = event.name
        view?.findViewById<TextView>(R.id.eventDescriptionTextView)?.text = event.description

        val formattedStartTime = event.startDateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) ?: "Start time unavailable"
        view?.findViewById<TextView>(R.id.eventStartTimeTextView)?.text = "Start: $formattedStartTime"

        view?.findViewById<TextView>(R.id.eventDurationTextView)?.text = "Duration: ${event.durationMinutes} minutes"
        view?.findViewById<TextView>(R.id.eventLocationTextView)?.text = "Location: ${event.location}"
    }

    private fun setupMenu() {
        if (creatorId != getCurrentUserId().toLong()) return // Only show menu for event creator

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.event_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit_event -> {
                        val bundle = Bundle().apply {
                            putLong("eventId", eventId!!)
                        }
                        findNavController().navigate(R.id.action_eventFragment_to_editEventFragment, bundle)
                        true
                    }

                    R.id.action_delete_event -> {
                        showDeleteConfirmationDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteEvent() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteEvent() {
        lifecycleScope.launch(Dispatchers.IO) {
            val response = eventsRepository.deleteEvent(eventId!!, getCurrentUserId().toLong())

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Event deleted successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete event!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getCurrentUserId(): Long {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Context.MODE_PRIVATE)
        return sharedPref.getLong("loggedInUserId", -1L)
    }

}
