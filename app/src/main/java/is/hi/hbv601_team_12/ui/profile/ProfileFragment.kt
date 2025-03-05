package `is`.hi.hbv601_team_12.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import `is`.hi.hbv601_team_12.databinding.FragmentProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: OfflineUsersRepository
    private var currentUser: User? = null

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
        repository = OfflineUsersRepository(db.userDao())

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
            val username = sharedPref.getString("loggedInUsername", null)

            if (username != null) {
                val user = repository.getUserByUsername(username)
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        currentUser = user
                        binding.tvFullName.text = user.fullName
                        binding.tvUsername.text = getString(R.string.username_format, user.username)
                        binding.tvEmail.text = user.email
                        binding.tvPhoneNumber.text = user.phoneNumber
                        binding.tvAddress.text = user.address

                        user.profilePicture?.let {
                            val file = File(it)
                            if (file.exists()) {
                                binding.ivProfilePicture.load(file)
                            } else {
                                binding.ivProfilePicture.setImageResource(R.drawable.default_profile)
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "User not found!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "No logged-in user found!", Toast.LENGTH_SHORT).show()
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
        sharedPref.edit().clear().apply()

        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
