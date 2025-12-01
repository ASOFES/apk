package com.example.gestionvehicules.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestionvehicules.R
import com.example.gestionvehicules.data.api.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val messageSent: View = itemView.findViewById(R.id.messageSentLayout)
        private val messageReceived: View = itemView.findViewById(R.id.messageReceivedLayout)
        private val sentMessageText: TextView = itemView.findViewById(R.id.textViewSentMessage)
        private val sentMessageTime: TextView = itemView.findViewById(R.id.textViewSentTime)
        private val receivedMessageText: TextView = itemView.findViewById(R.id.textViewReceivedMessage)
        private val receivedMessageTime: TextView = itemView.findViewById(R.id.textViewReceivedTime)
        private val receivedMessageSender: TextView = itemView.findViewById(R.id.textViewReceivedSender)
        
        fun bind(message: ChatMessage) {
            // Format timestamp
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = try {
                val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    .parse(message.timestamp)
                timestamp?.let { timeFormat.format(it) } ?: message.timestamp.substring(11, 16)
            } catch (e: Exception) {
                message.timestamp.substring(11, 16)
            }
            
            if (message.is_read) {
                // Message sent by current user
                messageSent.visibility = View.VISIBLE
                messageReceived.visibility = View.GONE
                
                sentMessageText.text = message.content
                sentMessageTime.text = time
            } else {
                // Message received from other user
                messageSent.visibility = View.GONE
                messageReceived.visibility = View.VISIBLE
                
                receivedMessageText.text = message.content
                receivedMessageTime.text = time
                receivedMessageSender.text = message.sender_name
            }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}
