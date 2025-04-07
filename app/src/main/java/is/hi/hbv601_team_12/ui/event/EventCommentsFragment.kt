package `is`.hi.hbv601_team_12.ui.event

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import `is`.hi.hbv601_team_12.data.AppDatabase
import `is`.hi.hbv601_team_12.data.defaultRepositories.DefaultUsersRepository
import `is`.hi.hbv601_team_12.data.entities.Comment
import `is`.hi.hbv601_team_12.data.offlineRepositories.OfflineUsersRepository
import `is`.hi.hbv601_team_12.data.onlineRepositories.OnlineEventsRepository
import `is`.hi.hbv601_team_12.data.onlineRepositories.OnlineUsersRepository
import `is`.hi.hbv601_team_12.data.repositories.UsersRepository
import `is`.hi.hbv601_team_12.databinding.FragmentEventCommentsBinding
import `is`.hi.hbv601_team_12.ui.adapters.CommentAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException

class EventCommentsFragment : Fragment() {

    private lateinit var eventsRepository: OnlineEventsRepository
    private lateinit var usersRepository: UsersRepository
    private var _binding: FragmentEventCommentsBinding? = null
    private val binding get() = _binding!!
    private var eventId: Long? = null
    private lateinit var commentAdapter: CommentAdapter

    // Example list of comments (replace this with actual data loading)
    private val comments: MutableList<Comment> = mutableListOf<Comment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventCommentsBinding.inflate(inflater, container, false)
        eventId = arguments?.getLong("eventId")

        if (eventId == null) {
            Toast.makeText(requireContext(), "Invalid Event ID!", Toast.LENGTH_SHORT).show()
        } else {
            fetchEventComments(eventId!!)
        }

        eventsRepository = OnlineEventsRepository()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())

        val offlineUsersRepo = OfflineUsersRepository(db.userDao())
        val onlineUsersRepo = OnlineUsersRepository(offlineUsersRepo)
        usersRepository = DefaultUsersRepository(offlineUsersRepo, onlineUsersRepo)

        // Set up the RecyclerView
        commentAdapter = CommentAdapter(comments)
        binding.commentsRecyclerView.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.postCommentButton.setOnClickListener {
            val commentText = binding.commentEditText.text.toString()
            if (commentText.isNotEmpty()) {
                // Add new comment to list and update
                val newComment = Comment(
                    authorName = getCurrentUserName(),
                    commentData = commentText,
                    eventId = eventId!! // Replace with actual event ID
                )
                comments.add(newComment)
                commentAdapter.notifyItemInserted(comments.size - 1)

                binding.commentEditText.text.clear()
            }
        }

    }

    private fun fetchEventComments(eventId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = eventsRepository.getEventComments(eventId)
                println("getEventComments response: $response")
                println("getEventComments body: ${response.body()}")
                if (response.isSuccessful) {
                    val comments = response.body().orEmpty()
                    withContext(Dispatchers.Main) {
                        if (comments.isEmpty()) {
                            binding.noCommentsTextView.visibility = View.VISIBLE
                            binding.commentsRecyclerView.visibility = View.GONE
                        } else {
                            binding.noCommentsTextView.visibility = View.GONE
                            binding.commentsRecyclerView.visibility = View.VISIBLE
                        }
                        binding.commentsRecyclerView.adapter = CommentAdapter(comments)
                    }
                }
                } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error fetching event comments: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getCurrentUserId(): Long {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Context.MODE_PRIVATE)
        return sharedPref.getLong("loggedInUserId", -1L)
    }

    private fun getCurrentUserName(): String {
        val sharedPref = requireActivity().getSharedPreferences("VibeVaultPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("loggedInUserName", "") ?: ""
    }
}