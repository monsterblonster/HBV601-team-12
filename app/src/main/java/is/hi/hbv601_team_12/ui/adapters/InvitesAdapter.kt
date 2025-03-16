package `is`.hi.hbv601_team_12.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import `is`.hi.hbv601_team_12.data.entities.Invitation
import `is`.hi.hbv601_team_12.databinding.ItemInviteBinding

class InvitesAdapter(
    private val invites: MutableList<Invitation>,
    private val onAccept: (Invitation) -> Unit,
    private val onDecline: (Invitation) -> Unit
) : RecyclerView.Adapter<InvitesAdapter.InviteViewHolder>() {

    inner class InviteViewHolder(val binding: ItemInviteBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InviteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemInviteBinding.inflate(inflater, parent, false)
        return InviteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InviteViewHolder, position: Int) {
        val invite = invites[position]

        with(holder.binding) {
            val nameToDisplay = invite.groupName ?: "Unknown Group"
            inviteInfoText.text = "Group: $nameToDisplay"

            btnAccept.setOnClickListener {
                Log.d("InvitesAdapter", "Accept button clicked for invite: ${invite.serverId}")
                onAccept(invite)
            }
            btnDecline.setOnClickListener {
                Log.d("InvitesAdapter", "Decline button clicked for invite: ${invite.serverId}")
                onDecline(invite)
            }

        }
    }

    override fun getItemCount(): Int = invites.size

    fun updateData(newInvites: List<Invitation>) {
        invites.clear()
        invites.addAll(newInvites)
        notifyDataSetChanged()
    }
}
