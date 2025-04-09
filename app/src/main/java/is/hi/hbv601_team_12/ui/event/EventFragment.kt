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
import kotlinx.coroutines.async
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
        participantAdapter = ParticipantAdapter().apply {
            setOnStatusChangeListener { userId, status ->
                updateParticipationStatus(userId, status)
            }
        }

        recyclerView.adapter = participantAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        val commentsButton = view.findViewById<Button>(R.id.viewCommentsButton)
        commentsButton.setOnClickListener {
            navigateToEventComments(eventId!!)
        }

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

    private fun updateUI(event: Event) {
            view?.apply {
                findViewById<TextView>(R.id.eventNameTextView)?.text = event.name
                findViewById<TextView>(R.id.eventDescriptionTextView)?.text = event.description

                val dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d 'at' h:mm a")
                val formattedDateTime = event.date?.format(dateTimeFormatter) ?: "Time not set"
                findViewById<TextView>(R.id.eventDateTimeTextView)?.text = formattedDateTime

                val hours = event.durationMinutes / 60
                val minutes = event.durationMinutes % 60
                val durationText = when {
                    hours > 0 && minutes > 0 -> "$hours hr ${minutes} min"
                    hours > 0 -> "$hours hr"
                    else -> "$minutes min"
                }
                findViewById<TextView>(R.id.eventDurationTextView)?.text = durationText

                findViewById<TextView>(R.id.eventLocationTextView)?.text = event.location ?: "Location not specified"

                val creationDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
                val formattedCreationDate = event.timeCreated?.format(creationDateFormatter) ?: "Date not set"
                findViewById<TextView>(R.id.eventCreationDateTextView)?.text = "Created on $formattedCreationDate"
            }
        }
    private fun setupMenu() {
        if (creatorId != getCurrentUserId().toLong()) return

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

    private fun navigateToEventComments(eventId: Long) {
        val bundle = Bundle().apply {
            putLong("eventId", eventId)
        }
        findNavController().navigate(R.id.action_eventFragment_to_eventCommentsFragment, bundle)
    }

    private fun getCurrentUserId(): Long {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Context.MODE_PRIVATE)
        return sharedPref.getLong("loggedInUserId", -1L)
    }

    private fun loadParticipants(eventId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Fetch all participant lists in parallel
                val goingDeferred = async { eventsRepository.getGoingUsers(eventId) }
                val maybeDeferred = async { eventsRepository.getMaybeUsers(eventId) }
                val cantGoDeferred = async { eventsRepository.getCantGoUsers(eventId) }
                val invitedDeferred = async { eventsRepository.getInvitedUsers(eventId) }

                val goingResponse = goingDeferred.await()
                val maybeResponse = maybeDeferred.await()
                val cantGoResponse = cantGoDeferred.await()
                val invitedResponse = invitedDeferred.await()

                // Debug logging
                println("""
                API Responses:
                Going: ${goingResponse.body()?.size} users
                Maybe: ${maybeResponse.body()?.size} users
                Can't Go: ${cantGoResponse.body()?.size} users
                Invited: ${invitedResponse.body()?.size} users
            """.trimIndent())

                if (goingResponse.isSuccessful && maybeResponse.isSuccessful &&
                    cantGoResponse.isSuccessful && invitedResponse.isSuccessful
                ) {
                    val goingUsers = goingResponse.body().orEmpty().toSet()
                    val maybeUsers = maybeResponse.body().orEmpty().toSet()
                    val cantGoUsers = cantGoResponse.body().orEmpty().toSet()
                    val invitedUsers = invitedResponse.body().orEmpty()
                        .filterNot { user ->
                            // Exclude users who are in other statuses
                            user in goingUsers || user in maybeUsers || user in cantGoUsers
                        }
                        .toSet()

                    val participants = mutableListOf<ParticipantWithStatus>().apply {
                        addAll(goingUsers.map { ParticipantWithStatus(it, ParticipantStatus.GOING) })
                        addAll(maybeUsers.map { ParticipantWithStatus(it, ParticipantStatus.MAYBE) })
                        addAll(cantGoUsers.map { ParticipantWithStatus(it, ParticipantStatus.DECLINED) })
                        addAll(invitedUsers.map { ParticipantWithStatus(it, ParticipantStatus.INVITED) })
                    }

                    // Debug logging
                    println("""
                    Final Participants:
                    ${participants.groupBy { it.status }.mapValues { it.value.size }}
                """.trimIndent())

                    withContext(Dispatchers.Main) {
                        participantAdapter.submitList(participants)
                    }
                } else {
                    // Handle error responses
                    val errorMsg = buildString {
                        if (!goingResponse.isSuccessful) append("Going list failed. ")
                        if (!maybeResponse.isSuccessful) append("Maybe list failed. ")
                        if (!cantGoResponse.isSuccessful) append("Can't Go list failed. ")
                        if (!invitedResponse.isSuccessful) append("Invited list failed.")
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error loading participants: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateParticipationStatus(userId: Long, status: ParticipantStatus) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = when (status) {
                    ParticipantStatus.GOING -> eventsRepository.addUserToGoing(eventId!!, userId)
                    ParticipantStatus.MAYBE -> eventsRepository.addUserToMaybe(eventId!!, userId)
                    ParticipantStatus.DECLINED -> eventsRepository.addUserToCantGo(eventId!!, userId)
                    else -> return@launch
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        loadParticipants(eventId!!) // Refresh the list
                        Toast.makeText(
                            requireContext(),
                            "Status updated to ${status.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to update status",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }




}
