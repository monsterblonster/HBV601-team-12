package `is`.hi.hbv601_team_12.ui.group

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.entities.Group
import `is`.hi.hbv601_team_12.data.repositories.GroupsRepository
import `is`.hi.hbv601_team_12.databinding.FragmentEditGroupBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class EditGroupFragment : Fragment() {
    private var _binding: FragmentEditGroupBinding? = null
    private val binding get() = _binding!!
    private var groupId: Int? = null
    private lateinit var groupsRepository: GroupsRepository
    private var groupPicturePath: String? = null
    private lateinit var currentGroup: Group
    private var photoFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupsRepository = `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineGroupsRepository(
            `is`.hi.hbv601_team_12.data.AppDatabase.getDatabase(requireContext()).groupDao()
        )

        groupId = arguments?.getInt("groupId")

        if (groupId != null) {
            fetchGroupDetails(groupId!!)
        } else {
            Toast.makeText(requireContext(), "Invalid Group ID!", Toast.LENGTH_SHORT).show()
        }

        binding.btnSaveGroup.setOnClickListener { updateGroup() }
        binding.btnChangeGroupPicture.setOnClickListener { showImageSourceDialog() }
    }

    private fun fetchGroupDetails(groupId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            groupsRepository.getGroupStream(groupId).collect { group ->
                withContext(Dispatchers.Main) {
                    if (group != null) {
                        currentGroup = group
                        populateFields(group)

                        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
                        val loggedInUserId = sharedPref.getInt("loggedInUserId", -1)

                        val isAdmin = loggedInUserId == group.adminId
                        binding.etGroupTags.isEnabled = isAdmin

                        if (!isAdmin) {
                            binding.etGroupTags.hint = "Only admins can change tags"
                        }
                    } else {
                        Toast.makeText(requireContext(), "Group not found!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun populateFields(group: Group) {
        binding.etGroupName.setText(group.name)
        binding.etGroupDescription.setText(group.description ?: "")
        binding.etGroupTags.setText(group.tags?.split(",")?.joinToString(", ") ?: "")


        group.groupPicture?.let {
            val file = File(it)
            if (file.exists()) {
                binding.ivGroupPicture.load(file)
                groupPicturePath = it
            } else {
                binding.ivGroupPicture.setImageResource(R.drawable.default_group_image)
            }
        } ?: binding.ivGroupPicture.setImageResource(R.drawable.default_group_image)
    }


    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery")

        AlertDialog.Builder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> pickImageFromGallery()
                }
            }
            .show()
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { saveImageLocally(it) }
        }

    private fun pickImageFromGallery() {
        imagePickerLauncher.launch("image/*")
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = createImageFile()

        photoFile?.let {
            val photoURI = FileProvider.getUriForFile(
                requireContext(),
                "is.hi.hbv601_team_12.fileprovider",
                it
            )

            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            takePictureLauncher.launch(intent)
        }
    }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                photoFile?.let {
                    groupPicturePath = it.absolutePath
                    binding.ivGroupPicture.load(it)
                }
            }
        }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) // Fixed typo
        val fileName = "group_${groupId}_$timeStamp.jpg"
        return File(requireContext().filesDir, fileName)
    }


    private fun saveImageLocally(imageUri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            val inputStream: InputStream? = requireActivity().contentResolver.openInputStream(imageUri)
            val file = File(requireContext().filesDir, "group_${groupId}.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            groupPicturePath = file.absolutePath

            withContext(Dispatchers.Main) {
                binding.ivGroupPicture.load(file)
            }
        }
    }

    private fun updateGroup() {
        val newName = binding.etGroupName.text.toString().trim()
        val newDescription = binding.etGroupDescription.text.toString().trim()
        val newTags = binding.etGroupTags.text.toString().trim()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(",")

        if (newName.isEmpty()) {
            Toast.makeText(requireContext(), "Group name cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedGroup = currentGroup.copy(
            name = newName,
            description = newDescription.ifEmpty { null },
            groupPicture = groupPicturePath,
            tags = newTags
        )

        lifecycleScope.launch(Dispatchers.IO) {
            groupsRepository.updateGroup(updatedGroup)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Group updated successfully!", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
