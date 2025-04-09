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
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.entities.Group
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineGroupsRepository
import `is`.hi.hbv601_team_12.data.onlineRepositories.OnlineGroupsRepository
import `is`.hi.hbv601_team_12.data.defaultRepositories.DefaultGroupsRepository
import `is`.hi.hbv601_team_12.data.repositories.GroupsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class CreateGroupFragment : Fragment() {

    private lateinit var groupNameInput: EditText
    private lateinit var groupDescriptionInput: EditText
    private lateinit var groupTagsInput: EditText
    private lateinit var groupMaxMembersInput: EditText
    private lateinit var createGroupButton: Button
    private lateinit var groupsRepository: GroupsRepository
    private lateinit var allowUserInvites: SwitchCompat

    private lateinit var groupImageView: ImageView
    private lateinit var uploadGroupPictureButton: Button
    private lateinit var takeGroupPictureButton: Button
    private var groupPicturePath: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_create_group, container, false)

        groupNameInput = view.findViewById(R.id.groupNameInput)
        groupDescriptionInput = view.findViewById(R.id.groupDescriptionInput)
        groupTagsInput = view.findViewById(R.id.groupTagsInput)
        groupMaxMembersInput = view.findViewById(R.id.groupMaxMembersInput)
        createGroupButton = view.findViewById(R.id.createGroupButton)
        allowUserInvites = view.findViewById(R.id.switchAllowUserInvites)

        groupImageView = view.findViewById(R.id.groupImageView)
        uploadGroupPictureButton = view.findViewById(R.id.btnUploadGroupPicture)
        takeGroupPictureButton = view.findViewById(R.id.btnTakeGroupPicture)

        val db = AppDatabase.getDatabase(requireContext())
        val offlineGroupsRepo = OfflineGroupsRepository(db.groupDao())
        val onlineGroupsRepo = OnlineGroupsRepository()
        groupsRepository = DefaultGroupsRepository(offlineGroupsRepo, onlineGroupsRepo)

        createGroupButton.setOnClickListener {
            createGroup()
        }

        uploadGroupPictureButton.setOnClickListener {
            openGallery()
        }

        takeGroupPictureButton.setOnClickListener {
            openCamera()
        }

        return view
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
                val imageUri: Uri? = result.data?.data
                imageUri?.let {
                    val savedPath = saveImageToInternalStorage(it)
                    groupImageView.setImageURI(it)
                    groupPicturePath = savedPath
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
                    groupImageView.setImageBitmap(it)
                    groupPicturePath = savedPath
                }
            }
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

    private fun saveBitmapToInternalStorage(bitmap: Bitmap): String {
        val file = File(requireContext().filesDir, "group_picture_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
        return file.absolutePath
    }

    private fun createGroup() {
        val groupName = groupNameInput.text.toString().trim()
        val groupDescription = groupDescriptionInput.text.toString().trim()
        val groupTags = groupTagsInput.text.toString().trim()
        val maxMembersText = groupMaxMembersInput.text.toString().trim()
        val allowInvites = allowUserInvites.isChecked

        if (groupName.isEmpty()) {
            Toast.makeText(requireContext(), "Group Name is required", Toast.LENGTH_SHORT).show()
            return
        }

        val maxMembers = maxMembersText.toIntOrNull() ?: 5

        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
        val adminId = sharedPref.getLong("loggedInUserId", -1L)
        val adminUsername = sharedPref.getString("loggedInUsername", null)

        if (adminId == -1L || adminUsername.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Error: No logged-in user found", Toast.LENGTH_SHORT).show()
            return
        }

        val tagsList = if (groupTags.isEmpty()) emptyList()
        else groupTags.split(",").map { it.trim() }

        val newGroup = Group(
            groupName = groupName,
            description = groupDescription.ifEmpty { null },
            tags = tagsList,
            maxMembers = maxMembers,
            adminId = adminId,
            profilePicturePath = groupPicturePath,
            allowUserInvites = allowInvites
        )

        lifecycleScope.launch(Dispatchers.IO) {
            val response = groupsRepository.createGroup(newGroup, adminUsername)
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    groupsRepository.pullUserGroupsOnline(adminId)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Group Created Successfully!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_createGroupFragment_to_profileFragment)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Server Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
}
