package `is`.hi.hbv601_team_12.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.entities.Group

class GroupsAdapter(
    private val groups: List<Group>,
    private val userId: Int,
    private val onGroupClick: (String) -> Unit
) : RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(view, onGroupClick)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groups[position]
        holder.bind(group, userId)
    }

    override fun getItemCount() = groups.size

    class GroupViewHolder(itemView: View, private val onGroupClick: (String) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val groupNameTextView: TextView = itemView.findViewById(R.id.groupNameTextView)
        private val adminStatusTextView: TextView = itemView.findViewById(R.id.adminStatusTextView)

        fun bind(group: Group, userId: Int) {
            groupNameTextView.text = group.name
            adminStatusTextView.visibility = if (group.adminId == userId) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onGroupClick(group.id.toString())
            }
        }
    }
}
