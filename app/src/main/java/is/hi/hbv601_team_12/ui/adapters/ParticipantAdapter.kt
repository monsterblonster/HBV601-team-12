package `is`.hi.hbv601_team_12.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.entities.*
import `is`.hi.hbv601_team_12.databinding.ItemParticipantBinding
import java.io.File

class ParticipantAdapter : ListAdapter<ParticipantWithStatus, ParticipantAdapter.ParticipantViewHolder>(
    ParticipantDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val binding = ItemParticipantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ParticipantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ParticipantViewHolder(private val binding: ItemParticipantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(participantWithStatus: ParticipantWithStatus) {
            val user = participantWithStatus.user

            binding.tvParticipantName.text = user.fullName

            binding.tvParticipantStatus.text = when (participantWithStatus.status) {
                ParticipantStatus.GOING -> "Going"
                ParticipantStatus.MAYBE -> "Maybe"
                ParticipantStatus.INVITED -> "Invited"
                ParticipantStatus.DECLINED -> "Declined"
            }

            user.profilePicturePath?.let {
                binding.ivParticipantPicture.load(it) {
                    crossfade(true)
                    placeholder(R.drawable.default_profile)
                    error(R.drawable.default_profile)
                }
            } ?: binding.ivParticipantPicture.setImageResource(R.drawable.default_profile)
        }
    }

    private class ParticipantDiffCallback : DiffUtil.ItemCallback<ParticipantWithStatus>() {
        override fun areItemsTheSame(oldItem: ParticipantWithStatus, newItem: ParticipantWithStatus): Boolean {
            return oldItem.user.id == newItem.user.id
        }

        override fun areContentsTheSame(oldItem: ParticipantWithStatus, newItem: ParticipantWithStatus): Boolean {
            return oldItem == newItem
        }
    }
}

data class ParticipantWithStatus(
    val user: User,
    val status: ParticipantStatus
)
