package `is`.hi.hbv601_team_12.ui.event

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.entities.ParticipantStatus
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineEventsRepository
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import `is`.hi.hbv601_team_12.databinding.FragmentEventBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class EventFragment : Fragment() {
    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var eventsRepository: OfflineEventsRepository
    private lateinit var usersRepository: OfflineUsersRepository
    private val participantAdapter = ParticipantAdapter()
    
    private val args: EventFragmentArgs by navArgs()
    private var currentUserId: Int = -1
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val db = AppDatabase.getDatabase(requireContext())
        eventsRepository = OfflineEventsRepository(db.eventDao())
        usersRepository = OfflineUsersRepository(db.userDao())
        
        binding.rvParticipants.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = participantAdapter
        }
        
        getCurrentUserId()
        
        loadEventDetails()
        
        binding.btnJoinLeave.setOnClickListener {
            handleJoinLeaveEvent()
        }
        
        binding.fabEditEvent.setOnClickListener {
            val action = EventFragmentDirections.actionEventFragmentToEditEventFragment(args.eventId)
            findNavController().navigate(action)
        }
        
        setupMenu()
    }
    
    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // only inflate the menu if current user is the creator
                lifecycleScope.launch {
                    val event = eventsRepository.getEventById(args.eventId)
                    if (event != null && event.creatorId == currentUserId) {
                        menuInflater.inflate(R.menu.event_menu, menu)
                    }
                }
            }
            
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete_event -> {
                        showDeleteConfirmation()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    
    private fun getCurrentUserId() {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUsername", null)
        
        if (username != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val user = usersRepository.getUserByUsername(username)
                if (user != null) {
                    currentUserId = user.id
                    
                    // Update UI elements that depend on user ID
                    withContext(Dispatchers.Main) {
                        updateJoinLeaveButton()
                        checkCreatorPermissions()
                    }
                }
            }
        }
    }
    
    private fun loadEventDetails() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Get event details
            val event = eventsRepository.getEventById(args.eventId)
            
            if (event != null) {
                // Get participants
                val participants = eventsRepository.getUsersForEvent(event.id)
                val participantStatuses = eventsRepository.getParticipantsForEvent(event.id)
                
                // Create list of participants with their statuses
                val participantsWithStatus = participants.map { user ->
                    val participantEntry = participantStatuses.find { it.userId == user.id }
                    ParticipantWithStatus(
                        user = user,
                        status = participantEntry?.status ?: ParticipantStatus.GOING
                    )
                }
                
                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    // Set event details
                    binding.tvEventName.text = event.name
                    
                    // Format date and time
                    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    binding.tvDateTime.text = event.startDateTime.format(formatter)
                    
                    // Format duration
                    val hours = event.durationMinutes / 60
                    val minutes = event.durationMinutes % 60
                    binding.tvDuration.text = when {
                        hours > 0 && minutes > 0 -> "$hours hr $minutes min"
                        hours > 0 -> "$hours hr"
                        else -> "$minutes min"
                    }
                    
                    // Set location
                    binding.tvLocation.text = event.location ?: "No location specified"
                    
                    // Set description
                    binding.tvDescription.text = event.description ?: "No description provided"
                    
                    // Update participants list
                    participantAdapter.submitList(participantsWithStatus)
                    
                    // Update join/leave button
                    updateJoinLeaveButton()
                    
                    // Check if current user is the creator
                    checkCreatorPermissions()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Event not found!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }
    }
    
    private fun updateJoinLeaveButton() {
        if (currentUserId == -1) return
        
        lifecycleScope.launch(Dispatchers.IO) {
            val isParticipating = eventsRepository.isUserParticipating(args.eventId, currentUserId)
            
            withContext(Dispatchers.Main) {
                binding.btnJoinLeave.text = if (isParticipating) "Leave Event" else "Join Event"
            }
        }
    }
    
    private fun checkCreatorPermissions() {
        lifecycleScope.launch(Dispatchers.IO) {
            val event = eventsRepository.getEventById(args.eventId)
            
            withContext(Dispatchers.Main) {
                // Only show edit FAB if current user is the creator
                binding.fabEditEvent.visibility = if (event != null && event.creatorId == currentUserId) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }
    
    private fun handleJoinLeaveEvent() {
        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "Please log in to join events", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch(Dispatchers.IO) {
            val isParticipating = eventsRepository.isUserParticipating(args.eventId, currentUserId)
            
            if (isParticipating) {
                // Leave event
                eventsRepository.removeParticipant(args.eventId, currentUserId)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Left event successfully", Toast.LENGTH_SHORT).show()
                    // Refresh UI
                    loadEventDetails()
                }
            } else {
                // Join event
                eventsRepository.addParticipant(args.eventId, currentUserId)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Joined event successfully", Toast.LENGTH_SHORT).show()
                    // Refresh UI
                    loadEventDetails()
                }
            }
        }
    }
    
    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteEvent() }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteEvent() {
        lifecycleScope.launch(Dispatchers.IO) {
            val event = eventsRepository.getEventById(args.eventId)
            
            if (event != null && event.creatorId == currentUserId) {
                eventsRepository.deleteEvent(event)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Event deleted successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "You don't have permission to delete this event", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
