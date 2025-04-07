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
            binding.commentAuthor.text = comment.authorName
            binding.commentContent.text = comment.commentData
            binding.commentDate.text = comment.commentTime?.format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")) ?: "No date set"
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