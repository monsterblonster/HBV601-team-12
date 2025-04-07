package `is`.hi.hbv601_team_12.ui.login

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import `is`.hi.hbv601_team_12.data.onlineRepositories.OnlineUsersRepository
import `is`.hi.hbv601_team_12.databinding.FragmentRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import kotlin.io.copyTo

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var onlineUsersRepository: OnlineUsersRepository
    private lateinit var offlineCache: OfflineUsersRepository

    private var profilePicturePath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        offlineCache = OfflineUsersRepository(db.userDao())
        onlineUsersRepository = OnlineUsersRepository(offlineCache)

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val fullName = binding.etFullName.text.toString()
            val address = binding.etAddress.text.toString()
            val phoneNumber = binding.etPhoneNumber.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (username.isBlank() || email.isBlank() || password.isBlank() ||
                fullName.isBlank() || address.isBlank() || phoneNumber.isBlank() || confirmPassword.isBlank()
            ) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            handleRegistration(username, email, password, confirmPassword)
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
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                imageUri?.let {
                    val savedPath = saveImageToInternalStorage(it)
                    binding.ivProfilePicture.setImageURI(it)
                    profilePicturePath = savedPath
                }
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleRegistration(username: String, email: String, password: String, confirmPassword: String) {
        val fullName = binding.etFullName.text.toString()
        val address = binding.etAddress.text.toString()
        val phoneNumber = binding.etPhoneNumber.text.toString()

        val newUser = User(
            userName = username,
            emailAddress = email,
            userPW = password,
            fullName = fullName,
            address = address,
            phoneNumber = phoneNumber,
            profilePicturePath = null
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = onlineUsersRepository.registerUser(newUser)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { registeredUser ->
                            cacheUserLocally(registeredUser)
                            val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putLong("loggedInUserId", registeredUser.id)
                                putString("loggedInUsername", registeredUser.userName)
                                putBoolean("isLoggedIn", true)
                                apply()
                            }

                            if (profilePicturePath != null) {
                                val file = File(profilePicturePath)
                                val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                                val imagePart = MultipartBody.Part.createFormData("picture", file.name, requestBody)

                                lifecycleScope.launch(Dispatchers.IO) {
                                    val uploadResponse = onlineUsersRepository.uploadProfilePicture(registeredUser.id, imagePart)
                                    if (uploadResponse.isSuccessful) {
                                        val imageUrl = uploadResponse.body()?.imageUrl
                                        registeredUser.profilePicturePath = imageUrl
                                        onlineUsersRepository.updateUser(registeredUser.id, registeredUser)
                                    } else {
                                        println("Image upload failed: ${uploadResponse.errorBody()?.string()}")
                                    }
                                }
                            }

                            Toast.makeText(requireContext(), "Registration Successful!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Registration failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun cacheUserLocally(user: User) {
        offlineCache.cacheUser(user)
    }
}
