package `is`.hi.hbv601_team_12.ui.group

import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
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
import `is`.hi.hbv601_team_12.data.entities.Group
import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.offlineRepositories.*
import `is`.hi.hbv601_team_12.data.onlineRepositories.*
import `is`.hi.hbv601_team_12.data.defaultRepositories.*
import `is`.hi.hbv601_team_12.data.repositories.*
import `is`.hi.hbv601_team_12.databinding.FragmentGroupBinding
import `is`.hi.hbv601_team_12.ui.adapters.EventsAdapter
import `is`.hi.hbv601_team_12.ui.adapters.MembersAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GroupFragment : Fragment() {

    private var _binding: FragmentGroupBinding? = null
    private val binding get() = _binding!!

    private var groupId: Long? = null
    private lateinit var groupsRepository: GroupsRepository
    private lateinit var usersRepository: UsersRepository
    private lateinit var invitationsRepository: InvitationsRepository
    private lateinit var eventsRepository: OnlineEventsRepository
    private lateinit var eventsAdapter: EventsAdapter

    private var isAdmin: Boolean = false
    private lateinit var currentGroup: Group

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())

        val offlineGroupsRepo = OfflineGroupsRepository(db.groupDao())
        val onlineGroupsRepo = OnlineGroupsRepository()
        groupsRepository = DefaultGroupsRepository(offlineGroupsRepo, onlineGroupsRepo)

        val offlineUsersRepo = OfflineUsersRepository(db.userDao())
        val onlineUsersRepo = OnlineUsersRepository(offlineUsersRepo)
        usersRepository = DefaultUsersRepository(offlineUsersRepo, onlineUsersRepo)

        val offlineInvitesRepo = OfflineInvitationsRepository(db.invitationDao())
        val onlineInvitesRepo = OnlineInvitationRepository()
        invitationsRepository = DefaultInvitationsRepository(offlineInvitesRepo, onlineInvitesRepo)

        eventsRepository = OnlineEventsRepository()

        setupMenu()

        binding.fabCreateEvent.setOnClickListener {
            navigateToCreateEvent()
        }

        binding.eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        groupId = arguments?.getString("groupId")?.toLongOrNull()
        if (groupId == null) {
            Toast.makeText(requireContext(), "Invalid Group ID!", Toast.LENGTH_SHORT).show()
        } else {
            fetchLatestGroupDetails(groupId!!)
            loadGroupEvents(groupId!!)
        }
    }

    private fun fetchLatestGroupDetails(gId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = groupsRepository.getGroupById(gId)
                if (response.isSuccessful) {
                    response.body()?.let { fetchedGroup ->
                        withContext(Dispatchers.Main) {
                            currentGroup = fetchedGroup
                            displayGroupDetails(fetchedGroup)
                            checkAdminPrivileges(fetchedGroup.adminId)
                        }
                    } ?: run {
                        loadGroupOffline(gId)
                    }
                } else {
                    loadGroupOffline(gId)
                }
            } catch (e: Exception) {
                loadGroupOffline(gId)
            }
        }
    }

    private fun loadGroupOffline(gId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            val offlineGroup = groupsRepository.getGroupStream(gId).first()
            withContext(Dispatchers.Main) {
                if (offlineGroup != null) {
                    currentGroup = offlineGroup
                    displayGroupDetails(offlineGroup)
                    checkAdminPrivileges(offlineGroup.adminId)
                } else {
                    Toast.makeText(requireContext(), "Group details not found offline!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadGroupEvents(groupId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = eventsRepository.getEventsByGroupId(groupId)
                println("getEventsByGroupId response: $response")
                println("getEventsByGroupId body: ${response.body()}")
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
                    Toast.makeText(requireContext(), "Error fetching group events: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToEvent(eventId: Long) {
        val bundle = bundleOf("eventId" to eventId)
        findNavController().navigate(R.id.action_groupFragment_to_eventFragment, bundle)
    }

    private fun checkAdminPrivileges(adminId: Long) {
        val currentUserID = getCurrentUserID()
        isAdmin = (currentUserID == adminId)
        requireActivity().invalidateOptionsMenu()
    }

    private fun getCurrentUserID(): Long {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs",
            android.content.Context.MODE_PRIVATE
        )
        return sharedPref.getLong("loggedInUserId", -1L)
    }

    private fun displayGroupDetails(group: Group) {
        binding.groupNameTextView.text = group.groupName
        binding.groupDescriptionTextView.text = group.description ?: "No description available"
        binding.groupTagsTextView.text = group.tags.joinToString(", ")
        binding.groupTagsTextView.visibility = if (group.tags.isNotEmpty()) View.VISIBLE else View.GONE

        group.profilePicturePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                binding.groupImageView.load(file)
            } else {
                binding.groupImageView.setImageResource(R.drawable.default_group_image)
            }
        } ?: binding.groupImageView.setImageResource(R.drawable.default_group_image)

        loadGroupMembers(group.members)
    }

    private fun navigateToCreateEvent() {
        val bundle = bundleOf("groupId" to groupId)
        findNavController().navigate(R.id.action_groupFragment_to_createEventFragment, bundle)
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.group_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit_group -> {
                        if (isAdmin) {
                            navigateToEditGroup()
                        } else {
                            Toast.makeText(requireContext(), "Only admins can edit this group.", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    R.id.action_delete_group -> {
                        if (isAdmin) {
                            confirmDeleteGroup()
                        } else {
                            Toast.makeText(requireContext(), "Only admins can delete this group.", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    R.id.action_leave_group -> {
                        confirmLeaveGroup()
                        true
                    }
                    R.id.action_invite_user -> {
                        if (isAdmin) {
                            showInviteDialog()
                        } else {
                            Toast.makeText(requireContext(), "Only admins can invite users.", Toast.LENGTH_SHORT).show()
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showInviteDialog() {
        val editText = EditText(requireContext())
        editText.hint = "Enter username"

        AlertDialog.Builder(requireContext())
            .setTitle("Invite User")
            .setView(editText)
            .setPositiveButton("Invite") { _, _ ->
                val username = editText.text.toString().trim()
                if (username.isNotEmpty()) {
                    createInvitation(username)
                } else {
                    Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createInvitation(username: String) {
        if (groupId == null) return

        lifecycleScope.launch(Dispatchers.IO) {
            val response = invitationsRepository.createInvitation(groupId!!, username)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Invite sent!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to invite user: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun navigateToEditGroup() {
        if (groupId == null) return
        val bundle = bundleOf("groupId" to groupId)
        findNavController().navigate(R.id.action_groupFragment_to_editGroupFragment, bundle)
    }

    private fun confirmDeleteGroup() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Group")
            .setMessage("Are you sure you want to delete this group?")
            .setPositiveButton("Yes") { _, _ -> deleteGroup() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteGroup() {
        if (groupId == null) return
        lifecycleScope.launch(Dispatchers.IO) {
            val response = groupsRepository.deleteGroupOnline(groupId!!)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Group deleted!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Failed to delete group: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun confirmLeaveGroup() {
        AlertDialog.Builder(requireContext())
            .setTitle("Leave Group")
            .setMessage("Are you sure you want to leave this group?")
            .setPositiveButton("Yes") { _, _ -> leaveGroup() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun leaveGroup() {
        val userId = getCurrentUserID()
        println("Leaving group with groupId: $groupId and userId: $userId")
        if (groupId == null || userId == -1L) return

        lifecycleScope.launch(Dispatchers.IO) {
            println("Leaving group with groupId: $groupId and userId: $userId")
            val response = groupsRepository.removeUserFromGroup(groupId!!, userId, userId)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "You have left the group!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Failed to leave group: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadGroupMembers(memberIds: List<Long>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val memberList = mutableListOf<User>()
            for (id in memberIds) {
                val response = usersRepository.getUserById(id)
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        memberList.add(user)
                    }
                } else {
                    println("Failed to fetch user $id: ${response.message()}")
                }
            }

            withContext(Dispatchers.Main) {
                if (memberList.isNotEmpty()) {
                    binding.membersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    binding.membersRecyclerView.adapter = MembersAdapter(memberList)
                } else {
                    Toast.makeText(requireContext(), "No members to display", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
