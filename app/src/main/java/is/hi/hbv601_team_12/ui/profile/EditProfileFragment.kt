package `is`.hi.hbv601_team_12.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import `is`.hi.hbv601_team_12.data.onlineRepositories.OnlineUsersRepository
import `is`.hi.hbv601_team_12.databinding.FragmentEditProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var onlineUsersRepository: OnlineUsersRepository
    private lateinit var offlineCache: OfflineUsersRepository
    private var currentUser: User? = null
    private var profilePicturePath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = `is`.hi.hbv601_team_12.data.AppDatabase.getDatabase(requireContext())
        offlineCache = OfflineUsersRepository(db.userDao())
        onlineUsersRepository = OnlineUsersRepository(offlineCache)

        loadUserProfile()

        binding.btnSaveChanges.setOnClickListener {
            saveUserProfile()
        }

        binding.btnUploadPicture.setOnClickListener {
            openGallery()
        }

        binding.btnTakePicture.setOnClickListener {
            openCamera()
        }
    }

    private fun loadUserProfile() {
        lifecycleScope.launch(Dispatchers.IO) {
            val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
            val userId = sharedPref.getLong("loggedInUserId", -1L)
            Log.d("EditProfileFragment", "Loading user profile for userId: $userId")
            if (userId == -1L) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "No logged-in user found!", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            try {
                val response = onlineUsersRepository.getUserById(userId)
                Log.d("EditProfileFragment", "getUserById response code: ${response.code()}")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { user ->
                            currentUser = user
                            cacheUserLocally(user)

                            binding.etFullName.setText(user.fullName)
                            binding.etUsername.setText(user.userName)
                            binding.etEmail.setText(user.emailAddress)
                            binding.etPhoneNumber.setText(user.phoneNumber)
                            binding.etAddress.setText(user.address)

                            user.profilePicturePath?.let { urlOrPath ->
                                binding.ivProfilePicture.setImageResource(R.drawable.default_profile)
                                if (urlOrPath.startsWith("http")) {
                                    binding.ivProfilePicture.load(urlOrPath) {
                                        crossfade(true)
                                        placeholder(R.drawable.default_profile)
                                        error(R.drawable.default_profile)
                                    }
                                } else {
                                    val file = File(urlOrPath)
                                    if (file.exists()) {
                                        binding.ivProfilePicture.setImageURI(Uri.fromFile(file))
                                    } else {
                                        binding.ivProfilePicture.setImageResource(R.drawable.default_profile)
                                    }
                                }
                            }


                            Log.d("EditProfileFragment", "Loaded user: $user")
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to load profile: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun saveUserProfile() {
        val fullName = binding.etFullName.text.toString()
        val username = binding.etUsername.text.toString()
        val email = binding.etEmail.text.toString()
        val phoneNumber = binding.etPhoneNumber.text.toString()
        val address = binding.etAddress.text.toString()

        if (fullName.isBlank() || username.isBlank() || email.isBlank() || phoneNumber.isBlank() || address.isBlank()) {
            Toast.makeText(requireContext(), "All fields are required!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            currentUser?.let { user ->
                user.fullName = fullName
                user.userName = username
                user.emailAddress = email
                user.phoneNumber = phoneNumber
                user.address = address

                if (profilePicturePath != null) {
                    val file = File(profilePicturePath)
                    val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val imagePart = MultipartBody.Part.createFormData("picture", file.name, requestBody)

                    val uploadResponse = onlineUsersRepository.uploadProfilePicture(user.id, imagePart)
                    if (uploadResponse.isSuccessful) {
                        val imageUrl = uploadResponse.body()?.imageUrl
                        user.profilePicturePath = imageUrl
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Image upload failed: ${uploadResponse.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                user.confirmPassword = user.userPW

                try {
                    val response = onlineUsersRepository.updateUser(user.id, user)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            response.body()?.let { updatedUser ->
                                cacheUserLocally(updatedUser)
                                val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    putString("loggedInUsername", updatedUser.userName)
                                    apply()
                                }
                                Toast.makeText(requireContext(), "Profile Updated!", Toast.LENGTH_SHORT).show()
                                findNavController().navigateUp()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Update failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "No user available to update.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }





    private suspend fun cacheUserLocally(user: User) {
        offlineCache.cacheUser(user)
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


}
