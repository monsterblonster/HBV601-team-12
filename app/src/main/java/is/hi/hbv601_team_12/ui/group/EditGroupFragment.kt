package `is`.hi.hbv601_team_12.ui.group

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
import coil.load
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.entities.Group
import `is`.hi.hbv601_team_12.data.models.ImageUploadResponse
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineGroupsRepository
import `is`.hi.hbv601_team_12.data.onlineRepositories.OnlineGroupsRepository
import `is`.hi.hbv601_team_12.data.defaultRepositories.DefaultGroupsRepository
import `is`.hi.hbv601_team_12.data.repositories.GroupsRepository
import `is`.hi.hbv601_team_12.databinding.FragmentEditGroupBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class EditGroupFragment : Fragment() {

    private var _binding: FragmentEditGroupBinding? = null
    private val binding get() = _binding!!

    private lateinit var groupsRepository: GroupsRepository

    private lateinit var currentGroup: Group
    private var groupId: Long = -1L
    private var groupPicturePath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        val offlineGroupsRepo = OfflineGroupsRepository(db.groupDao())
        val onlineGroupsRepo = OnlineGroupsRepository()
        groupsRepository = DefaultGroupsRepository(offlineGroupsRepo, onlineGroupsRepo)

        groupId = arguments?.getLong("groupId", -1L) ?: -1L
        if (groupId == -1L) {
            Toast.makeText(requireContext(), "Invalid Group ID!", Toast.LENGTH_SHORT).show()
            return
        }

        loadGroupDetails()

        binding.btnSaveGroup.setOnClickListener { updateGroup() }
        binding.btnChangeGroupPicture.setOnClickListener { showImageSourceDialog() }
    }

    private fun loadGroupDetails() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = groupsRepository.getGroupById(groupId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { fetchedGroup ->
                            groupsRepository.insertGroup(fetchedGroup)
                            currentGroup = fetchedGroup
                            populateFields(fetchedGroup)
                        }
                    } else {
                        loadOfflineGroup()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loadOfflineGroup()
                }
            }
        }
    }

    private fun loadOfflineGroup() {
        lifecycleScope.launch(Dispatchers.IO) {
            val group = groupsRepository.getGroupStream(groupId).first()
            withContext(Dispatchers.Main) {
                if (group != null) {
                    currentGroup = group
                    populateFields(group)
                } else {
                    Toast.makeText(requireContext(), "Group not found offline!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun populateFields(group: Group) {
        binding.etGroupName.setText(group.groupName)
        binding.etGroupDescription.setText(group.description ?: "")
        binding.etGroupTags.setText(group.tags.joinToString(", "))

        if (!group.profilePicturePath.isNullOrEmpty()) {
            if (group.profilePicturePath!!.startsWith("http")) {
                binding.ivGroupPicture.load(group.profilePicturePath) {
                    crossfade(true)
                    placeholder(R.drawable.default_group_image)
                    error(R.drawable.default_group_image)
                }
            } else {
                val file = File(group.profilePicturePath!!)
                if (file.exists()) {
                    binding.ivGroupPicture.load(file)
                } else {
                    binding.ivGroupPicture.setImageResource(R.drawable.default_group_image)
                }
            }
        } else {
            binding.ivGroupPicture.setImageResource(R.drawable.default_group_image)
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Change Group Picture")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getParcelableExtra("data", Bitmap::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.extras?.get("data") as? Bitmap
                }
                bitmap?.let {
                    val savedPath = saveBitmapToInternalStorage(it)
                    binding.ivGroupPicture.setImageBitmap(it)
                    groupPicturePath = savedPath
                }
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                imageUri?.let {
                    val savedPath = saveImageToInternalStorage(it)
                    binding.ivGroupPicture.setImageURI(it)
                    groupPicturePath = savedPath
                }
            }
        }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap): String {
        val file = File(requireContext().filesDir, "group_picture_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
        return file.absolutePath
    }

    private fun saveImageToInternalStorage(imageUri: Uri): String {
        val file = File(requireContext().filesDir, "group_picture_${System.currentTimeMillis()}.jpg")
        requireContext().contentResolver.openInputStream(imageUri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    private fun updateGroup() {
        val newName = binding.etGroupName.text.toString().trim()
        val newDescription = binding.etGroupDescription.text.toString().trim()
        val newTags = binding.etGroupTags.text.toString().trim().split(",").map { it.trim() }

        if (newName.isEmpty()) {
            Toast.makeText(requireContext(), "Group name cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            var finalPictureUrl: String? = currentGroup.profilePicturePath
            if (!groupPicturePath.isNullOrEmpty()) {
                val uploadResponse = uploadGroupPicture(groupId, groupPicturePath!!)
                if (uploadResponse.isSuccessful) {
                    val imageUrl = uploadResponse.body()?.imageUrl
                    if (!imageUrl.isNullOrEmpty()) {
                        finalPictureUrl = imageUrl
                    } else {
                        finalPictureUrl = groupPicturePath
                    }
                } else {
                    println("Group photo upload failed: ${uploadResponse.errorBody()?.string()}")
                }
            }

            val updatedGroup = currentGroup.copy(
                groupName = newName,
                description = newDescription.ifEmpty { null },
                profilePicturePath = finalPictureUrl,
                tags = newTags
            )

            try {
                val updateResponse = groupsRepository.editGroupOnline(groupId, updatedGroup)
                withContext(Dispatchers.Main) {
                    if (updateResponse.isSuccessful) {
                        updateResponse.body()?.let { newGroup ->
                            lifecycleScope.launch(Dispatchers.IO) {
                                groupsRepository.insertGroup(newGroup)
                            }
                        }
                        Toast.makeText(requireContext(), "Group updated successfully!", Toast.LENGTH_SHORT).show()
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    } else {
                        Toast.makeText(requireContext(), "Update failed: ${updateResponse.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun uploadGroupPicture(groupId: Long, imagePath: String): Response<ImageUploadResponse> {
        val file = File(imagePath)
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("picture", file.name, requestBody)
        return groupsRepository.uploadGroupPicture(groupId, imagePart)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
