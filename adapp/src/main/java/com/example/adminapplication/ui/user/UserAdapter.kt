package com.example.adminapplication.ui.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminapplication.R
import com.example.adminapplication.data.model.UserWithAvatarDTO

class UserAdapter(
    private var users: MutableList<UserWithAvatarDTO>,
    private val onEditClick: (UserWithAvatarDTO) -> Unit,
    private val onDeleteClick: (UserWithAvatarDTO) -> Unit,
    private val onActiveToggle: (UserWithAvatarDTO, Boolean) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val baseUrl = "http://10.0.2.2:8080" // nhớ có dấu "/" ở cuối

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAvatar: ImageView = view.findViewById(R.id.imgAvatar)
        val tvUserId: TextView = view.findViewById(R.id.tvUserId)
        val tvUsername: TextView = view.findViewById(R.id.tvUsername)
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val tvRole: TextView = view.findViewById(R.id.tvRole)
        val tvCreatedAt: TextView = view.findViewById(R.id.tvCreatedAt)
        val tvActive: TextView = view.findViewById(R.id.tvActive)
        val switchActive: Switch = view.findViewById(R.id.switchActive)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]

        holder.tvUserId.text = "ID: ${user.id}"
        holder.tvUsername.text = user.username
        holder.tvEmail.text = user.email
        holder.tvRole.text = user.role
        holder.tvCreatedAt.text = user.createdAt?.substringBefore("T") ?: ""

        // 🚫 Tạm thời gỡ listener trước khi setChecked để tránh gọi lại callback
        holder.switchActive.setOnCheckedChangeListener(null)

        holder.switchActive.isChecked = user.active
        holder.tvActive.text = if (user.active) "Active" else "Inactive"
        holder.tvActive.setTextColor(
            holder.itemView.context.getColor(
                if (user.active) R.color.green_500 else R.color.red_500
            )
        )

        // ✅ Load avatar
        val avatarUrl = user.avatar
        if (!avatarUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(baseUrl + avatarUrl)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(holder.imgAvatar)
        } else {
            holder.imgAvatar.setImageResource(R.drawable.image_placeholder)
        }

        // ✅ Gán lại listener sau khi setChecked xong
        holder.switchActive.setOnCheckedChangeListener { _, isChecked ->
            if (user.active != isChecked) {
                onActiveToggle(user, isChecked)
            }
        }

        holder.btnEdit.setOnClickListener { onEditClick(user) }
        holder.btnDelete.setOnClickListener { onDeleteClick(user) }
    }


    fun updateData(newUsers: List<UserWithAvatarDTO>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}
