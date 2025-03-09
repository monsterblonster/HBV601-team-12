package `is`.hi.hbv601_team_12.ui.event

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineEventsRepository
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import `is`.hi.hbv601_team_12.databinding.FragmentEditEventBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class EditEventFragment : Fragment() {
    private var _binding: FragmentEditEventBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var eventsRepository: OfflineEventsRepository
    private lateinit var usersRepository: OfflineUsersRepository
    
    private val args: EditEventFragmentArgs by navArgs()
    
    private var selectedDate: LocalDate = LocalDate.now()
    private var selectedTime: LocalTime = LocalTime.now().plusHours(1)
    
    private var currentUserId: Int = -1
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditEventBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val db = AppDatabase.getDatabase(requireContext())
        eventsRepository = OfflineEventsRepository(db.eventDao())
        usersRepository = OfflineUsersRepository(db.userDao())
        
        getCurrentUserId()
        
        setupClickListeners()
        
        loadEventData()
    }
    
    private fun getCurrentUserId() {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUsername", null)
        
        if (username != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val user = usersRepository.getUserByUsername(username)
                if (user != null) {
                    currentUserId = user.id
                    
                    verifyEventOwnership()
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "User not found. Please log in again.", Toast.LENGTH_LONG).show()
                        findNavController().navigateUp()
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Please log in to edit an event", Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
        }
    }
    
    private fun verifyEventOwnership() {
        lifecycleScope.launch(Dispatchers.IO) {
            val event = eventsRepository.getEventById(args.eventId)
            
            if (event == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                return@launch
            }
            
            if (event.creatorId != currentUserId) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "You can only edit events you created", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
            }
        }
    }
    
    private fun loadEventData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val event = eventsRepository.getEventById(args.eventId)
            
            if (event != null) {
                selectedDate = event.startDateTime.toLocalDate()
                selectedTime = event.startDateTime.toLocalTime()
                
                withContext(Dispatchers.Main) {
                    binding.etEventName.setText(event.name)
                    binding.etDescription.setText(event.description ?: "")
                    binding.etLocation.setText(event.location ?: "")
                    binding.etDuration.setText(event.durationMinutes.toString())
                    binding.etMaxParticipants.setText(event.maxParticipants?.toString() ?: "")
                    binding.switchPublic.isChecked = event.isPublic
                    
                    updateDateTimeDisplay()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }
        
        binding.btnSelectTime.setOnClickListener {
            showTimePicker()
        }
        
        binding.btnSaveEvent.setOnClickListener {
            saveEventChanges()
        }
        
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun showDatePicker() {
        val year = selectedDate.year
        val month = selectedDate.monthValue - 1 // DatePickerDialog uses 0-based months
        val day = selectedDate.dayOfMonth
        
        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
            updateDateTimeDisplay()
        }, year, month, day).show()
    }
    
    private fun showTimePicker() {
        val hour = selectedTime.hour
        val minute = selectedTime.minute
        
        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            selectedTime = LocalTime.of(selectedHour, selectedMinute)
            updateDateTimeDisplay()
        }, hour, minute, true).show()
    }
    
    private fun updateDateTimeDisplay() {
        val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        
        binding.tvSelectedDate.text = selectedDate.format(dateFormatter)
        binding.tvSelectedTime.text = selectedTime.format(timeFormatter)
    }
    
    private fun saveEventChanges() {
        val eventName = binding.etEventName.text.toString().trim()
        if (eventName.isEmpty()) {
            binding.etEventName.error = "Event name is required"
            return
        }
        
        val durationText = binding.etDuration.text.toString().trim()
        if (durationText.isEmpty()) {
            binding.etDuration.error = "Duration is required"
            return
        }
        
        lifecycleScope.launch(Dispatchers.IO) {
            val event = eventsRepository.getEventById(args.eventId)
            
            if (event != null) {
                event.name = eventName
                event.description = binding.etDescription.text.toString().trim().let { 
                    if (it.isEmpty()) null else it 
                }
                event.location = binding.etLocation.text.toString().trim().let { 
                    if (it.isEmpty()) null else it 
                }
                event.durationMinutes = durationText.toIntOrNull() ?: 60
                event.startDateTime = LocalDateTime.of(selectedDate, selectedTime)
                event.isPublic = binding.switchPublic.isChecked
                
                val maxParticipantsText = binding.etMaxParticipants.text.toString().trim()
                event.maxParticipants = if (maxParticipantsText.isEmpty()) null else maxParticipantsText.toIntOrNull()
                
                eventsRepository.updateEvent(event)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Event updated successfully!", Toast.LENGTH_SHORT).show()
                    
                    val action = EditEventFragmentDirections.actionEditEventFragmentToEventFragment(event.id)
                    findNavController().navigate(action)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
