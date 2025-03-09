package `is`.hi.hbv601_team_12.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.activity.result.contract.ActivityResultContracts
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import `is`.hi.hbv601_team_12.databinding.FragmentRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream


class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: OfflineUsersRepository
    private var profilePicturePath: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = AppDatabase.getDatabase(requireContext())
        repository = OfflineUsersRepository(db.userDao())

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            handleRegistration(username, email, password)
        }

        binding.tvBackToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.btnUploadPicture.setOnClickListener {
            openGallery()
        }

        binding.btnTakePicture.setOnClickListener {
            openCamera()
        }

    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: androidx.activity.result.ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri: Uri? = result.data?.data
                imageUri?.let {
                    val savedPath = saveImageToInternalStorage(it)
                    binding.ivProfilePicture.setImageURI(it)
                    profilePicturePath = savedPath
                }
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: androidx.activity.result.ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra("data", Bitmap::class.java)
                } else {
                    result.data?.extras?.get("data") as? Bitmap
                }
                bitmap?.let {
                    val savedPath = saveBitmapToInternalStorage(it)
                    binding.ivProfilePicture.setImageBitmap(it)
                    profilePicturePath = savedPath
                }
            }
        }


    private fun saveImageToInternalStorage(imageUri: Uri): String {
        val file = File(requireContext().filesDir, "profile_picture_${System.currentTimeMillis()}.jpg")
        requireContext().contentResolver.openInputStream(imageUri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap): String {
        val file = File(requireContext().filesDir, "profile_picture_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
        return file.absolutePath
    }


    private fun handleRegistration(username: String, email: String, password: String) {
        val fullName = binding.etFullName.text.toString()
        val address = binding.etAddress.text.toString()
        val phoneNumber = binding.etPhoneNumber.text.toString()

        if (username.isBlank() || email.isBlank() || password.isBlank() ||
            fullName.isBlank() || address.isBlank() || phoneNumber.isBlank()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val existingUser = repository.getUserByEmail(email)

            withContext(Dispatchers.Main) {
                if (existingUser != null) {
                    Toast.makeText(requireContext(), "Email already in use", Toast.LENGTH_SHORT).show()
                } else {
                    val newUser = User(
                        username = username,
                        email = email,
                        password = password,
                        fullName = fullName,
                        address = address,
                        phoneNumber = phoneNumber,
                        profilePicture = profilePicturePath
                    )

                    repository.insertUser(newUser)

                    val storedUser = repository.getUserByEmail(email)
                    if (storedUser != null) {
                        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putInt("loggedInUserId", storedUser.id)
                            putString("loggedInUsername", storedUser.username)
                            putBoolean("isLoggedIn", true)
                            apply()
                        }

                        Toast.makeText(requireContext(), "Registration Successful!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    } else {
                        Toast.makeText(requireContext(), "Error: Could not retrieve user after registration", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
