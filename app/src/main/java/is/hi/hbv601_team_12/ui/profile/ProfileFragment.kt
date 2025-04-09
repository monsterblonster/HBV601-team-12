package `is`.hi.hbv601_team_12.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import `is`.hi.hbv601_team_12.MainActivity
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.defaultRepositories.*
import `is`.hi.hbv601_team_12.data.entities.*
import `is`.hi.hbv601_team_12.data.offlineRepositories.*
import `is`.hi.hbv601_team_12.data.onlineRepositories.*
import `is`.hi.hbv601_team_12.data.repositories.*
import `is`.hi.hbv601_team_12.databinding.FragmentProfileBinding
import `is`.hi.hbv601_team_12.ui.adapters.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var usersRepository: UsersRepository
    private lateinit var invitationsRepository: InvitationsRepository
    private lateinit var groupsRepository: GroupsRepository
    private lateinit var eventsRepository: OnlineEventsRepository

    private var currentUser: User? = null
    private lateinit var groupsAdapter: GroupsAdapter
    private lateinit var invitesAdapter: InvitesAdapter
    private lateinit var eventsAdapter: EventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())

        val offlineUsersRepo = OfflineUsersRepository(db.userDao())
        val onlineUsersRepo = OnlineUsersRepository(offlineUsersRepo)
        usersRepository = DefaultUsersRepository(offlineUsersRepo, onlineUsersRepo)

        val offlineInvitesRepo = OfflineInvitationsRepository(db.invitationDao())
        val onlineInvitesRepo = OnlineInvitationRepository()
        invitationsRepository = DefaultInvitationsRepository(offlineInvitesRepo, onlineInvitesRepo)

        val offlineGroupsRepo = OfflineGroupsRepository(db.groupDao())
        val onlineGroupsRepo = OnlineGroupsRepository()
        groupsRepository = DefaultGroupsRepository(
            offlineRepo = offlineGroupsRepo,
            onlineRepo = onlineGroupsRepo
        )

        eventsRepository = OnlineEventsRepository()

        invitesAdapter = InvitesAdapter(
            invites = mutableListOf(),
            onAccept = { invite -> acceptInvite(invite) },
            onDecline = { invite -> declineInvite(invite) }
        )
        binding.invitesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.invitesRecyclerView.adapter = invitesAdapter

        loadUserProfile()
        setupMenu()

    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.profile_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit_profile -> {
                        findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
                        true
                    }
                    R.id.action_create_group -> {
                        findNavController().navigate(R.id.action_profileFragment_to_createGroupFragment)
                        true
                    }
                    R.id.action_logout -> {
                        showLogoutConfirmation()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }



    private fun loadUserInvites() {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
        val userId = sharedPref.getLong("loggedInUserId", -1L)
        if (userId == -1L) {
            Toast.makeText(requireContext(), "No logged-in user found!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val response = invitationsRepository.getUserInvites(userId)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val inviteList = response.body().orEmpty()
                    if (inviteList.isEmpty()) {
                        binding.invitesContainer.visibility = View.GONE
                    } else {
                        binding.invitesContainer.visibility = View.VISIBLE
                        invitesAdapter.updateData(inviteList)
                    }
                } else {
                    Toast.makeText(requireContext(),
                        "Failed to fetch invites: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun acceptInvite(invite: Invitation) {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
        val userId = sharedPref.getLong("loggedInUserId", -1L)
        val serverId = invite.serverId
        Log.d("ProfileFragment", "acceptInvite() called with serverId=${invite.serverId}")

        if (userId == -1L || serverId == null) return

        lifecycleScope.launch(Dispatchers.IO) {
            val response = invitationsRepository.acceptInvite(userId, serverId)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Invite accepted!", Toast.LENGTH_SHORT).show()
                    (requireActivity() as? MainActivity)?.recreate() // svo eg þarf ekki að rrestarta til að sja groups
                    loadUserProfile()
                } else {
                    Toast.makeText(requireContext(),
                        "Error accepting invite: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun declineInvite(invite: Invitation) {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
        val userId = sharedPref.getLong("loggedInUserId", -1L)
        val serverId = invite.serverId
        Log.d("ProfileFragment", "acceptInvite() called with serverId=${invite.serverId}")

        if (userId == -1L || serverId == null) return

        lifecycleScope.launch(Dispatchers.IO) {
            val response = invitationsRepository.declineInvite(userId, serverId)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Invite declined!", Toast.LENGTH_SHORT).show()
                    loadUserInvites()
                } else {
                    Toast.makeText(requireContext(),
                        "Error declining invite: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ -> logoutUser() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logoutUser() {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
        sharedPref.edit { clear() }

        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        //requireActivity().finish()
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun loadUserProfile() {
        lifecycleScope.launch(Dispatchers.IO) {
            val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
            val userId = sharedPref.getLong("loggedInUserId", -1L)

            if (userId == -1L) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "No logged-in user found!", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            try {
                // Load user profile
                val userResponse = usersRepository.getUserById(userId)
                // Load events in parallel
                val eventsResponse = eventsRepository.getEventsForUser(userId)

                withContext(Dispatchers.Main) {
                    if (userResponse.isSuccessful) {
                        userResponse.body()?.let { user ->
                            currentUser = user
                            displayUserProfile(user)
                        }
                    }

                    if (eventsResponse.isSuccessful) {
                        val allEvents = eventsResponse.body().orEmpty()
                        displayUpcomingEvent(allEvents)
                    }

                    loadUserInvites()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayUserProfile(user: User) {
        binding.tvFullName.text = user.fullName
        binding.tvUsername.text = getString(R.string.username_format, user.userName)
        binding.tvEmail.text = user.emailAddress
        binding.tvPhoneNumber.text = user.phoneNumber
        binding.tvAddress.text = user.address

        if (!user.profilePicturePath.isNullOrEmpty()) {
            binding.ivProfilePicture.load(user.profilePicturePath) {
                crossfade(true)
                placeholder(R.drawable.default_profile)
                error(R.drawable.default_profile)
            }
        } else {
            binding.ivProfilePicture.setImageResource(R.drawable.default_profile)
        }
    }

    private fun displayUpcomingEvent(events: List<Event>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val userId = currentUser?.id ?: return@launch

            // Filter events to only show upcoming ones (not declined)
            val upcomingEvents = events.filter { event ->
                try {
                    val goingResponse = eventsRepository.getGoingUsers(event.id)
                    val maybeResponse = eventsRepository.getMaybeUsers(event.id)
                    val invitedResponse = eventsRepository.getInvitedUsers(event.id)
// TODO invited
                    val isGoing = goingResponse.body()?.any { it.id == userId } ?: false
                    val isMaybe = maybeResponse.body()?.any { it.id == userId } ?: false
                    val isInvited = invitedResponse.body()?.any { it.id == userId } ?: false


                    isGoing || isMaybe || isInvited
                } catch (e: Exception) {
                    false
                }
            }.sortedBy { it.date }

            withContext(Dispatchers.Main) {
                if (upcomingEvents.isNotEmpty()) {
                    val nextEvent = upcomingEvents.first()

                    binding.upcomingEventView.eventNameTextView.text = nextEvent.name
                    binding.upcomingEventView.eventDateTimeTextView.text =
                        nextEvent.date?.format(DateTimeFormatter.ofPattern("EEEE, MMM d 'at' h:mm a"))
                            ?: "Time not set"

                    binding.upcomingEventCard.setOnClickListener {
                        navigateToEvent(nextEvent.id)
                    }

                    binding.upcomingEventCard.visibility = View.VISIBLE
                    binding.noUpcomingEventText.visibility = View.GONE
                } else {
                    binding.upcomingEventCard.visibility = View.GONE
                    binding.noUpcomingEventText.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun navigateToEvent(eventId: Long) {
        val bundle = Bundle().apply {
            putLong("eventId", eventId)
        }
        findNavController().navigate(R.id.action_profileFragment_to_eventFragment, bundle)
    }

}
