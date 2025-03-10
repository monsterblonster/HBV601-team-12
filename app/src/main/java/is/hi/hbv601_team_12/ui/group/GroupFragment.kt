package `is`.hi.hbv601_team_12.ui.group

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.entities.Group
import `is`.hi.hbv601_team_12.data.entities.Event
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineEventsRepository
import `is`.hi.hbv601_team_12.data.repositories.GroupsRepository
import `is`.hi.hbv601_team_12.data.repositories.UsersRepository
import `is`.hi.hbv601_team_12.databinding.FragmentGroupBinding
import `is`.hi.hbv601_team_12.ui.events.EventsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GroupFragment : Fragment() {
    private var _binding: FragmentGroupBinding? = null
    private val binding get() = _binding!!
    private var groupId: Int? = null
    private lateinit var groupsRepository: GroupsRepository
    private lateinit var usersRepository: UsersRepository
    private lateinit var eventRepository: OfflineEventsRepository
    private lateinit var eventsAdapter: EventsAdapter
    private var isAdmin: Boolean = false
    private lateinit var currentGroup: Group

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()

        binding.fabCreateEvent.setOnClickListener {
            navigateToCreateEvent()
        }

        val db = `is`.hi.hbv601_team_12.data.AppDatabase.getDatabase(requireContext())
        groupsRepository =
            `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineGroupsRepository(db.groupDao())
        usersRepository =
            `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository(db.userDao())
        eventRepository =
            `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineEventsRepository(db.eventDao())

        binding.eventsRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        groupId = arguments?.getString("groupId")?.toIntOrNull()

        if (groupId != null) {
            fetchGroupDetails(groupId!!)
            loadEvents(groupId!!)
        } else {
            Toast.makeText(requireContext(), "Invalid Group ID!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchGroupDetails(groupId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            groupsRepository.getGroupStream(groupId).collect { group ->
                withContext(Dispatchers.Main) {
                    if (group != null) {
                        currentGroup = group
                        displayGroupDetails(group)
                        checkAdminPrivileges(group.adminId)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Group details not found!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun loadEvents(groupId: Int) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val eventsFlow = eventRepository.getEventsForGroupStream(groupId)

                eventsFlow.collect { events ->
                    val participantCounts = mutableMapOf<Int, Int>()
                    events.forEach { event ->
                        val count = eventRepository.getParticipantsForEvent(event.id).size
                        participantCounts[event.id] = count
                    }
                    withContext(Dispatchers.Main) {
                        if (!isAdded || _binding == null) return@withContext

                        if (events.isEmpty()) {
                            binding.noEventsTextView.visibility = View.VISIBLE
                            binding.eventsRecyclerView.visibility = View.GONE
                        } else {
                            binding.noEventsTextView.visibility = View.GONE
                            binding.eventsRecyclerView.visibility = View.VISIBLE

                            eventsAdapter = EventsAdapter(events, participantCounts) { eventId ->
                                onEventClicked(eventId)
                            }
                            binding.eventsRecyclerView.adapter = eventsAdapter
                            eventsAdapter.notifyDataSetChanged() // ui update
                        }
                    }

                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isAdded || _binding == null) return@withContext
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun checkAdminPrivileges(adminId: Int) {
        val currentUserID = getCurrentUserID()
        isAdmin = (currentUserID == adminId)
        requireActivity().invalidateOptionsMenu()
    }

    private fun getCurrentUserID(): Int {
        val sharedPref = requireActivity().getSharedPreferences(
            "VibeVaultPrefs",
            android.content.Context.MODE_PRIVATE
        )
        return sharedPref.getInt("loggedInUserId", -1)
    }

    private fun displayGroupDetails(group: Group) {
        binding.groupNameTextView.text = group.name
        binding.groupDescriptionTextView.text = group.description ?: "No description available"

        if (!group.tags.isNullOrBlank()) {
            binding.groupTagsTextView.visibility = View.VISIBLE
            val formattedTags = group.tags.split(",").joinToString(", ") { it.trim() }
            binding.groupTagsTextView.text = getString(R.string.group_tags, formattedTags)
        } else {
            binding.groupTagsTextView.visibility = View.GONE
        }

        group.groupPicture?.let {
            val file = File(it)
            if (file.exists()) {
                binding.groupImageView.load(file)
            } else {
                binding.groupImageView.setImageResource(R.drawable.default_group_image)
            }
        } ?: binding.groupImageView.setImageResource(R.drawable.default_group_image)

        val toolbar =
            requireActivity().findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar?.title = "Group: ${group.name}"
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.group_menu, menu)
                menu.findItem(R.id.action_edit_group)?.isVisible = isAdmin
                menu.findItem(R.id.action_delete_group)?.isVisible = isAdmin
                menu.findItem(R.id.action_leave_group)?.isVisible = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit_group -> {
                        val bundle = Bundle().apply {
                            putInt("groupId", groupId!!)
                        }
                        findNavController().navigate(
                            R.id.action_groupFragment_to_editGroupFragment,
                            bundle
                        )
                        true
                    }

                    R.id.action_delete_group -> {
                        showDeleteConfirmationDialog()
                        true
                    }

                    R.id.action_leave_group -> {
                        showLeaveConfirmationDialog()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showLeaveConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Leave Group")
            .setMessage("Are you sure you want to leave this group?")
            .setPositiveButton("Leave") { _, _ -> leaveGroup() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun leaveGroup() {
        lifecycleScope.launch(Dispatchers.IO) {
            val currentUserId = getCurrentUserID()
            val updatedMembers = currentGroup.getMemberList().filterNot { it == currentUserId }

            if (currentUserId == currentGroup.adminId) {
                withContext(Dispatchers.Main) {
                    if (updatedMembers.isEmpty()) {
                        showDeleteConfirmationDialog()
                    } else {
                        showAdminTransferDialog(updatedMembers)
                    }
                }
            } else {
                val updatedGroup = currentGroup.copy(members = updatedMembers.joinToString(","))
                groupsRepository.updateGroup(updatedGroup)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "You have left the group.", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun showAdminTransferDialog(otherMembers: List<Int>) {
        lifecycleScope.launch(Dispatchers.IO) {
            val memberNames = otherMembers.map { userId ->
                usersRepository.getUserById(userId)?.username ?: "Unknown"
            }

            withContext(Dispatchers.Main) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Select a new admin before leaving")
                    .setItems(memberNames.toTypedArray()) { _, which ->
                        val newAdminId = otherMembers[which]
                        transferAdminAndLeave(newAdminId)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun transferAdminAndLeave(newAdminId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val updatedMembers = currentGroup.getMemberList().filterNot { it == getCurrentUserID() }
            val updatedGroup =
                currentGroup.copy(adminId = newAdminId, members = updatedMembers.joinToString(","))

            groupsRepository.updateGroup(updatedGroup)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "Admin rights transferred. You have left the group.",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Group")
            .setMessage("Are you sure you want to delete this group? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteGroup() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteGroup() {
        lifecycleScope.launch(Dispatchers.IO) {
            groupsRepository.deleteGroup(currentGroup)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Group deleted successfully", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigateUp()
            }
        }
    }

    private fun navigateToCreateEvent() {
        val bundle = Bundle().apply {
            putInt("groupId", groupId ?: -1)
        }
        findNavController().navigate(R.id.createEventFragment, bundle)
    }

    private fun onEventClicked(eventId: Int) {
        val bundle = Bundle().apply {
            putInt("eventId", eventId)
        }
        findNavController().navigate(R.id.eventFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
