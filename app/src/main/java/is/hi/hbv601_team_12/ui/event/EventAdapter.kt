package `is`.hi.hbv601_team_12.ui.group

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.entities.Event
import `is`.hi.hbv601_team_12.databinding.ItemEventBinding
import java.time.format.DateTimeFormatter

class EventAdapter : ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.eventNameTextView.text = event.name
            binding.eventTimeTextView.text = event.startDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
            binding.eventDurationTextView.text = "${event.durationMinutes} minutes"
            binding.eventMembersGoingTextView.text = "5 members going" // TODO Placeholder
        }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}
