package `is`.hi.hbv601_team_12.ui.profile

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
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineGroupsRepository
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import `is`.hi.hbv601_team_12.databinding.FragmentProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var usersRepository: OfflineUsersRepository
    private lateinit var groupsRepository: OfflineGroupsRepository
    private var currentUser: User? = null
    private lateinit var groupsAdapter: GroupsAdapter

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
        usersRepository = OfflineUsersRepository(db.userDao())
        groupsRepository = OfflineGroupsRepository(db.groupDao())

        binding.groupsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

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
            val userId = sharedPref.getInt("loggedInUserId", -1)

            if (userId == -1) {
                withContext(Dispatchers.Main) {
                    if (isAdded && _binding != null) {
                        Toast.makeText(requireContext(), "No logged-in user found!", Toast.LENGTH_SHORT).show()
                    }
                }
                return@launch
            }

            val userFlow = usersRepository.getUserStream(userId)

            withContext(Dispatchers.Main) {
                userFlow.collect { profile ->
                    if (!isAdded || _binding == null) return@collect

                    if (profile != null) {
                        currentUser = profile
                        binding.tvFullName.text = profile.fullName
                        binding.tvUsername.text = getString(R.string.username_format, profile.username)
                        binding.tvEmail.text = profile.email
                        binding.tvPhoneNumber.text = profile.phoneNumber
                        binding.tvAddress.text = profile.address

                        profile.profilePicture?.let {
                            val file = File(it)
                            if (file.exists()) {
                                binding.ivProfilePicture.load(file)
                            } else {
                                binding.ivProfilePicture.setImageResource(R.drawable.default_profile)
                            }
                        }

                        loadUserGroups(userId)
                    } else {
                        Toast.makeText(requireContext(), "User not found!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadUserGroups(userId: Int) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
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
        sharedPref.edit().clear().apply()

        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
