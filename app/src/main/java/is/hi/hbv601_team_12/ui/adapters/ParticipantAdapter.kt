package `is`.hi.hbv601_team_12.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.entities.*
import `is`.hi.hbv601_team_12.databinding.ItemParticipantBinding

class ParticipantAdapter : ListAdapter<ParticipantWithStatus, ParticipantAdapter.ParticipantViewHolder>(
    ParticipantDiffCallback()
) {
    private var onStatusChangeListener: ((Long, ParticipantStatus) -> Unit)? = null

    fun setOnStatusChangeListener(listener: (Long, ParticipantStatus) -> Unit) {
        onStatusChangeListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val binding = ItemParticipantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ParticipantViewHolder(binding, onStatusChangeListener)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ParticipantViewHolder(
        private val binding: ItemParticipantBinding,
        private val statusChangeListener: ((Long, ParticipantStatus) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: ParticipantWithStatus) {
            with(binding) {
                tvParticipantName.text = participant.user.fullName

                participant.user.profilePicturePath?.let { path ->
                    ivParticipantPicture.load(path) {
                        crossfade(true)
                        placeholder(R.drawable.default_profile)
                        error(R.drawable.default_profile)
                    }
                } ?: run {
                    ivParticipantPicture.setImageResource(R.drawable.default_profile)
                }

                // Update button states based on current status
                updateButtonStates(participant.status)

                // Set click listeners for buttons
                btnGoing.setOnClickListener {
                    statusChangeListener?.invoke(participant.user.id, ParticipantStatus.GOING)
                }
                btnMaybe.setOnClickListener {
                    statusChangeListener?.invoke(participant.user.id, ParticipantStatus.MAYBE)
                }
                btnCantGo.setOnClickListener {
                    statusChangeListener?.invoke(participant.user.id, ParticipantStatus.DECLINED)
                }
            }
        }

        private fun updateButtonStates(currentStatus: ParticipantStatus) {
            with(binding) {
                // Reset all buttons first
                btnGoing.apply {
                    setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                    setTextColor(ContextCompat.getColor(context, R.color.purple_500))
                }
                btnMaybe.apply {
                    setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                    setTextColor(ContextCompat.getColor(context, R.color.purple_500))
                }
                btnCantGo.apply {
                    setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                    setTextColor(ContextCompat.getColor(context, R.color.purple_500))
                }

                // Highlight the current status button
                when (currentStatus) {
                    ParticipantStatus.GOING -> btnGoing.apply {
                        setBackgroundColor(ContextCompat.getColor(context, R.color.status_going_bg))
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                    }
                    ParticipantStatus.MAYBE -> btnMaybe.apply {
                        setBackgroundColor(ContextCompat.getColor(context, R.color.status_maybe_bg))
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                    }
                    ParticipantStatus.DECLINED -> btnCantGo.apply {
                        setBackgroundColor(ContextCompat.getColor(context, R.color.status_declined_bg))
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                    }
                    ParticipantStatus.INVITED -> {
                        // No special styling for invited
                    }
                }
            }
        }
    }
}

    private class ParticipantDiffCallback : DiffUtil.ItemCallback<ParticipantWithStatus>() {
        override fun areItemsTheSame(
            oldItem: ParticipantWithStatus,
            newItem: ParticipantWithStatus
        ): Boolean {
            return oldItem.user.id == newItem.user.id
        }

        override fun areContentsTheSame(
            oldItem: ParticipantWithStatus,
            newItem: ParticipantWithStatus
        ): Boolean {
            return oldItem == newItem
        }
    }

data class ParticipantWithStatus(
    val user: User,
    val status: ParticipantStatus
)