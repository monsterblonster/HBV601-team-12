package `is`.hi.hbv601_team_12.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.entities.Event
import java.time.format.DateTimeFormatter

class EventsAdapter(
    private val events: List<Event>,
    private val onEventClick: (Long) -> Unit
) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view, onEventClick)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event, dateTimeFormatter)
    }

    override fun getItemCount() = events.size

        class EventViewHolder(itemView: View, private val onEventClick: (Long) -> Unit) :
            RecyclerView.ViewHolder(itemView) {
            private val eventNameTextView: TextView = itemView.findViewById(R.id.eventNameTextView)
            private val eventDescriptionTextView: TextView = itemView.findViewById(R.id.eventDescriptionTextView)
            private val eventDateTimeTextView: TextView = itemView.findViewById(R.id.eventDateTimeTextView)
            private val eventParticipantsTextView: TextView = itemView.findViewById(R.id.eventParticipantsTextView)

            fun bind(event: Event, formatter: DateTimeFormatter) {
                eventNameTextView.text = event.name

                // Handle description
                eventDescriptionTextView.text = event.description
                eventDescriptionTextView.visibility = if (event.description.isNullOrEmpty()) View.GONE else View.VISIBLE

                // Handle nullable startDateTime
                eventDateTimeTextView.text = event.startDateTime?.format(formatter) ?: "No date set"

                // Handle participants
                val participantCount = event.going?.size ?: 0
                eventParticipantsTextView.text = "$participantCount going"

                itemView.setOnClickListener {
                    event.id?.let { onEventClick(it) }
                }
            }
        }
    }