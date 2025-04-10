package `is`.hi.hbv601_team_12.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import `is`.hi.hbv601_team_12.data.entities.Comment
import `is`.hi.hbv601_team_12.databinding.ItemCommentBinding
import java.time.format.DateTimeFormatter

class CommentAdapter(
    private val comments: List<Comment>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
    // View Holder
    class CommentViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            val safeAuthor = comment.authorName?.takeIf { it.isNotBlank() } ?: "Anonymous"
            val safeContent = comment.commentData?.takeIf { it.isNotBlank() } ?: "No comment provided"
            val safeDate = comment.commentTime?.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")) ?: "No date set"

            binding.commentAuthor.text = safeAuthor
            binding.commentContent.text = safeContent
            binding.commentDate.text = safeDate
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        // Inflate the layout for each comment item
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCommentBinding.inflate(inflater, parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        // Bind the data to the view holder
        val comment = comments[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int = comments.size
}