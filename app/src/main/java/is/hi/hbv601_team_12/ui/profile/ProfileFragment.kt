package `is`.hi.hbv601_team_12.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        val offlineEventsRepo = OfflineEventsRepository(db.eventDao())
        val onlineEventsRepository = OnlineEventsRepository()
        eventsRepository = OnlineEventsRepository()

        binding.groupsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

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
                val response = usersRepository.getUserById(userId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { user ->
                            currentUser = user

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

                            loadUserGroups(userId)
                            loadUserEvents(userId)
                            loadUserInvites()
                        } ?: run {
                            Toast.makeText(requireContext(), "User not found!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to load profile: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun loadUserGroups(userId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = groupsRepository.pullUserGroupsOnline(userId)
                if (!response.isSuccessful) {
                    Log.e("ProfileFragment", "pullUserGroupsOnline failed: ${response.message()}")
                }

                val userGroupsFlow = groupsRepository.getAllGroupsStream()
                userGroupsFlow.collect { groups ->
                    withContext(Dispatchers.Main) {
                        if (!isAdded || _binding == null) return@withContext

                        if (groups.isEmpty()) {
                            binding.noGroupsTextView.visibility = View.VISIBLE
                            binding.groupsRecyclerView.visibility = View.GONE
                        } else {
                            binding.noGroupsTextView.visibility = View.GONE
                            binding.groupsRecyclerView.visibility = View.VISIBLE
                            groupsAdapter = GroupsAdapter(groups, userId) { groupId ->
                                onGroupClicked(groupId)
                            }
                            binding.groupsRecyclerView.adapter = groupsAdapter
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
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
                    loadUserInvites()
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

    private fun onGroupClicked(groupId: String) {
        val bundle = Bundle().apply {
            putString("groupId", groupId)
        }
        findNavController().navigate(R.id.action_profileFragment_to_groupFragment, bundle)
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
        requireActivity().finish()
    }

    private fun loadUserEvents(userId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = eventsRepository.getEventsForUser(userId)
                if (response.isSuccessful) {
                    val events = response.body().orEmpty()
                    withContext(Dispatchers.Main) {
                        if (events.isEmpty()) {
                            binding.noEventsTextView.visibility = View.VISIBLE
                            binding.eventsRecyclerView.visibility = View.GONE
                        } else {
                            binding.noEventsTextView.visibility = View.GONE
                            binding.eventsRecyclerView.visibility = View.VISIBLE
                            eventsAdapter = EventsAdapter(events) { eventId ->
                                navigateToEvent(eventId)
                            }
                            binding.eventsRecyclerView.adapter = eventsAdapter
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error fetching events: ${e.message}", Toast.LENGTH_LONG).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
