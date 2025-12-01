package com.example.gestionvehicules.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionvehicules.R
import com.example.gestionvehicules.data.api.ChatUser

class ChatUserAdapter(
    private val onUserClick: (ChatUser) -> Unit
) : ListAdapter<ChatUser, ChatUserAdapter.ChatUserViewHolder>(ChatUserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_user, parent, false)
        return ChatUserViewHolder(view, onUserClick)
    }

    override fun onBindViewHolder(holder: ChatUserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatUserViewHolder(
        itemView: View,
        private val onUserClick: (ChatUser) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        
        private val userName: TextView = itemView.findViewById(R.id.textViewUserName)
        private val userRole: TextView = itemView.findViewById(R.id.textViewUserRole)
        private val unreadCount: TextView = itemView.findViewById(R.id.textViewUnreadCount)
        private val unreadBadge: View = itemView.findViewById(R.id.unreadBadge)
        
        fun bind(user: ChatUser) {
            userName.text = user.name
            userRole.text = getRoleDisplayName(user.role)
            
            if (user.unread_count > 0) {
                unreadCount.text = user.unread_count.toString()
                unreadBadge.visibility = View.VISIBLE
            } else {
                unreadBadge.visibility = View.GONE
            }
            
            itemView.setOnClickListener {
                onUserClick(user)
            }
        }
        
        private fun getRoleDisplayName(role: String): String {
            return when (role) {
                "chauffeur" -> "Chauffeur"
                "demandeur" -> "Demandeur"
                "dispatch" -> "Dispatcher"
                "admin" -> "Administrateur"
                else -> role
            }
        }
    }

    class ChatUserDiffCallback : DiffUtil.ItemCallback<ChatUser>() {
        override fun areItemsTheSame(oldItem: ChatUser, newItem: ChatUser): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatUser, newItem: ChatUser): Boolean {
            return oldItem == newItem
        }
    }
}
