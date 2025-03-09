package `is`.hi.hbv601_team_12.ui.event

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.entities.Event
import `is`.hi.hbv601_team_12.data.entities.EventParticipant
import `is`.hi.hbv601_team_12.data.entities.ParticipantStatus
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineEventsRepository
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import `is`.hi.hbv601_team_12.databinding.FragmentCreateEventBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class CreateEventFragment : Fragment() {
    private var _binding: FragmentCreateEventBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var eventsRepository: OfflineEventsRepository
    private lateinit var usersRepository: OfflineUsersRepository
    
    private var selectedDate: LocalDate = LocalDate.now()
    private var selectedTime: LocalTime = LocalTime.now().plusHours(1).withMinute(0)
    
    private var currentUserId: Int = -1
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val db = AppDatabase.getDatabase(requireContext())
        eventsRepository = OfflineEventsRepository(db.eventDao())
        usersRepository = OfflineUsersRepository(db.userDao())
        
        updateDateTimeDisplay()
        
        getCurrentUserId()
        
        setupClickListeners()
    }
    
    private fun getCurrentUserId() {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Activity.MODE_PRIVATE)
        val username = sharedPref.getString("loggedInUsername", null)
        
        if (username != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val user = usersRepository.getUserByUsername(username)
                if (user != null) {
                    currentUserId = user.id
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "User not found. Please log in again.", Toast.LENGTH_LONG).show()
                        findNavController().navigateUp()
                    }
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
        
        binding.btnCreateEvent.setOnClickListener {
            createEvent()
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
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDateTimeDisplay() {
        val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        
        binding.tvSelectedDate.text = selectedDate.format(dateFormatter)
        binding.tvSelectedTime.text = selectedTime.format(timeFormatter)
    }
    
    private fun createEvent() {
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
        
        val description = binding.etDescription.text.toString().trim().let { 
            if (it.isEmpty()) null else it 
        }
        val location = binding.etLocation.text.toString().trim().let { 
            if (it.isEmpty()) null else it 
        }
        val durationMinutes = durationText.toIntOrNull() ?: 60 
        
        val startDateTime = LocalDateTime.of(selectedDate, selectedTime)
        
        if (startDateTime.isBefore(LocalDateTime.now())) {
            Toast.makeText(requireContext(), "Event cannot be scheduled in the past", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "User not found. Please log in again.", Toast.LENGTH_LONG).show()
            return
        }
        
        val isPublic = binding.switchPublic.isChecked
        val maxParticipantsText = binding.etMaxParticipants.text.toString().trim()
        val maxParticipants = if (maxParticipantsText.isEmpty()) null else maxParticipantsText.toIntOrNull()
        
        val newEvent = Event(
            name = eventName,
            description = description,
            startDateTime = startDateTime,
            durationMinutes = durationMinutes,
            creatorId = currentUserId,
            location = location,
            isPublic = isPublic,
            maxParticipants = maxParticipants
        )
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val eventId = eventsRepository.insertEvent(newEvent).toInt()
                
                val creatorParticipant = EventParticipant(
                    eventId = eventId,
                    userId = currentUserId,
                    status = ParticipantStatus.GOING
                )
                eventsRepository.insertParticipant(creatorParticipant)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Event created successfully!", Toast.LENGTH_SHORT).show()
                    
                    val action = CreateEventFragmentDirections.actionCreateEventFragmentToEventFragment(eventId)
                    findNavController().navigate(action)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error creating event: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

