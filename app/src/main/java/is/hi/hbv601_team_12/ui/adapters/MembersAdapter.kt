package `is`.hi.hbv601_team_12.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import `is`.hi.hbv601_team_12.R
import `is`.hi.hbv601_team_12.data.entities.User
import `is`.hi.hbv601_team_12.databinding.ItemMemberBinding

class MembersAdapter(
    private val members: List<User>
) : RecyclerView.Adapter<MembersAdapter.MemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount() = members.size

    class MemberViewHolder(private val binding: ItemMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.userNameTextView.text = user.userName

            binding.memberImageView.load(user.profilePicturePath) {
                crossfade(true)
                placeholder(R.drawable.default_profile)
                error(R.drawable.default_profile)
                transformations(CircleCropTransformation())
            }
        }
    }
}