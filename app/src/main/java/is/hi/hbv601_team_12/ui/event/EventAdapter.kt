package `is`.hi.hbv601_team_12.ui.events

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
    private val participantCounts: Map<Int, Int>, // eventId to participant count
    private val onEventClick: (Int) -> Unit
) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view, onEventClick)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        val participantCount = participantCounts[event.id] ?: 0
        holder.bind(event, participantCount, dateTimeFormatter)
    }

    override fun getItemCount() = events.size

    class EventViewHolder(itemView: View, private val onEventClick: (Int) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val eventNameTextView: TextView = itemView.findViewById(R.id.eventNameTextView)
        private val eventDescriptionTextView: TextView = itemView.findViewById(R.id.eventDescriptionTextView)
        private val eventDateTimeTextView: TextView = itemView.findViewById(R.id.eventDateTimeTextView)
        private val eventParticipantsTextView: TextView = itemView.findViewById(R.id.eventParticipantsTextView)

        fun bind(event: Event, participantCount: Int, formatter: DateTimeFormatter) {
            eventNameTextView.text = event.name
            
            if (!event.description.isNullOrEmpty()) {
                eventDescriptionTextView.text = event.description
                eventDescriptionTextView.visibility = View.VISIBLE
            } else {
                eventDescriptionTextView.visibility = View.GONE
            }
            
            eventDateTimeTextView.text = event.startDateTime.format(formatter)
            
            eventParticipantsTextView.text = "$participantCount going"

            itemView.setOnClickListener {
                onEventClick(event.id)
            }
        }
    }
}
