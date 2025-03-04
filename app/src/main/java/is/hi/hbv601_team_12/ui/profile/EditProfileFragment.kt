package `is`.hi.hbv601_team_12.ui.profile

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
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import `is`.hi.hbv601_team_12.databinding.FragmentEditProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: OfflineUsersRepository
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

        val db = AppDatabase.getDatabase(requireContext())
        repository = OfflineUsersRepository(db.userDao())

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
            val username = sharedPref.getString("loggedInUsername", null)

            if (username != null) {
                val user = repository.getUserByUsername(username)
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        currentUser = user
                        binding.etFullName.setText(user.fullName)
                        binding.etUsername.setText(user.username)
                        binding.etEmail.setText(user.email)
                        binding.etPhoneNumber.setText(user.phoneNumber)
                        binding.etAddress.setText(user.address)

                        user.profilePicture?.let {
                            val file = File(it)
                            if (file.exists()) {
                                binding.ivProfilePicture.setImageURI(Uri.fromFile(file))
                                profilePicturePath = it
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "User not found!", Toast.LENGTH_SHORT).show()
                    }
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
            currentUser?.let {
                val oldUsername = it.username

                it.fullName = fullName
                it.username = username
                it.email = email
                it.phoneNumber = phoneNumber
                it.address = address
                it.profilePicture = profilePicturePath

                repository.updateUser(it)

                val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    if (oldUsername != username) putString("loggedInUsername", username)
                    apply()
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Profile Updated!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
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
}
