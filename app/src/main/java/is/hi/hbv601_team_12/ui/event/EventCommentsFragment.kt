package `is`.hi.hbv601_team_12.ui.event

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import `is`.hi.hbv601_team_12.data.entities.Comment
import `is`.hi.hbv601_team_12.databinding.FragmentEventCommentsBinding
import `is`.hi.hbv601_team_12.ui.adapters.CommentAdapter
import java.time.LocalDateTime

class EventCommentsFragment : Fragment() {

    private var _binding: FragmentEventCommentsBinding? = null
    private val binding get() = _binding!!
    private var eventId: Long? = null
    private lateinit var commentAdapter: CommentAdapter

    // Example list of comments (replace this with actual data loading)
    private val comments = mutableListOf<Comment>()

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
            Toast.makeText(requireContext(), "Event ID: $eventId", Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    authorId = getCurrentUserId().toLong(),
                    commentData = commentText,
                    eventId = eventId!! // Replace with actual event ID
                )
                comments.add(newComment)
                commentAdapter.notifyItemInserted(comments.size - 1)

                binding.commentEditText.text.clear()
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
}