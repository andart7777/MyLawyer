package com.example.mylawyer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mylawyer.data.model.ChatListItem
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter(
    private val onChatClick: (ChatListItem) -> Unit
) : ListAdapter<ChatListItem, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val lastMessageTextView: TextView = view.findViewById(R.id.lastMessageTextView)
        val timestampTextView: TextView = view.findViewById(R.id.timestampTextView)

        fun bind(chat: ChatListItem) {
            titleTextView.text = chat.title
            lastMessageTextView.text = chat.lastMessage ?: "No messages"
            timestampTextView.text = formatTimestamp(chat.timestamp)
            itemView.setOnClickListener { onChatClick(chat) }
        }

        private fun formatTimestamp(timestamp: String): String {
            return try {
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
                val displayFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val date = isoFormat.parse(timestamp)
                displayFormat.format(date)
            } catch (e: Exception) {
                timestamp
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_history, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<ChatListItem>() {
        override fun areItemsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
            return oldItem.chatId == newItem.chatId
        }

        override fun areContentsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
            return oldItem == newItem
        }
    }
}