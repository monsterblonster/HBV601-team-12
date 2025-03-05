package `is`.hi.hbv601_team_12.ui.login

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import `is`.hi.hbv601_team_12.databinding.FragmentLoginBinding
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: OfflineUsersRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        repository = OfflineUsersRepository(db.userDao())

        binding.btnLogin.setOnClickListener {
            val email = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            handleLogin(email, password)
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(`is`.hi.hbv601_team_12.R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun handleLogin(username: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val user = repository.loginUser(username, password)
            withContext(Dispatchers.Main) {
                if (user != null) {
                    val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("loggedInUsername", user.username)
                        putBoolean("isLoggedIn", true)
                        apply()
                    }

                    Toast.makeText(requireContext(), "Login Successful!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(`is`.hi.hbv601_team_12.R.id.action_loginFragment_to_profileFragment)
                } else {
                    Toast.makeText(requireContext(), "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
