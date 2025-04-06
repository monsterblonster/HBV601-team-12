package `is`.hi.hbv601_team_12.ui.event

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import `is`.hi.hbv601_team_12.data.entities.Comment
import `is`.hi.hbv601_team_12.databinding.FragmentEventCommentsBinding
import `is`.hi.hbv601_team_12.ui.adapters.CommentAdapter
import java.time.LocalDateTime

class EventCommentsFragment : Fragment() {

    private var _binding: FragmentEventCommentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var commentAdapter: CommentAdapter

    // Example list of comments (replace this with actual data loading)
    private val comments = mutableListOf<Comment>(
        // ... add more comments here ...
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventCommentsBinding.inflate(inflater, container, false)
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
                    author = "currentUser", // Replace with current user data
                    commentData = commentText,
                    commentTime = LocalDateTime.now().toString()
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
}