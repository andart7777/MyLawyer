package com.example.mylawyer

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mylawyer.data.model.ChatHistoryItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChatHistoryAdapter(
    private val onChatClick: (ChatHistoryItem) -> Unit,
    private val onDeleteClick: (ChatHistoryItem) -> Unit
) : ListAdapter<ChatHistoryItem, ChatHistoryAdapter.ChatViewHolder>(ChatDiffCallback()) {

    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val lastMessageTextView: TextView = view.findViewById(R.id.lastMessageTextView)
        val timestampTextView: TextView = view.findViewById(R.id.timestampTextView)
        val deleteButton: ImageButton = view.findViewById(R.id.ibDeleteItemHistory)

        fun bind(chat: ChatHistoryItem) {
            Log.d(
                "ChatAdapter",
                "Привязка чата: chatId=${chat.chatId}, title=${chat.title}, lastMessage=${chat.lastMessage}"
            )
            titleTextView.text = chat.title
            lastMessageTextView.text =
                chat.lastMessage?.takeIf { it.isNotBlank() } ?: "Нет сообщений"
            timestampTextView.text = formatTimestamp(chat.timestamp)
            itemView.setOnClickListener {
                Log.d("ChatAdapter", "Клик по чату: chatId=${chat.chatId}")
                onChatClick(chat)
            }
            deleteButton.setOnClickListener {
                Log.d("ChatAdapter", "Клик по кнопке удаления: chatId=${chat.chatId}")
                onDeleteClick(chat)
            }
        }

        private fun formatTimestamp(timestamp: String): String {
            return try {
                // Парсинг ISO-временной метки (например, "2025-06-05T11:13:00.000000")
                val isoFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
                val date = isoFormat.parse(timestamp) ?: return timestamp

                val now = Calendar.getInstance()
                val messageCal = Calendar.getInstance().apply { time = date }

                when {
                    isSameDay(messageCal, now) -> {
                        // Сегодня: только время (например, "12:00")
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
                    }

                    isYesterday(messageCal, now) -> {
                        // Вчера: отображаем "Вчера"
                        "Вчера"
                    }

                    else -> {
                        // Ранее вчера: дата в формате "dd.MM.yyyy" (например, "09.05.2025")
                        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatAdapter", "Ошибка форматирования времени: ${e.message}", e)
                timestamp // Возврат исходной строки при ошибке
            }
        }

        private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        }

        private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
            val yesterday = cal2.clone() as Calendar
            yesterday.add(Calendar.DAY_OF_YEAR, -1)
            return isSameDay(cal1, yesterday)
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

    class ChatDiffCallback : DiffUtil.ItemCallback<ChatHistoryItem>() {
        override fun areItemsTheSame(oldItem: ChatHistoryItem, newItem: ChatHistoryItem): Boolean {
            return oldItem.chatId == newItem.chatId
        }

        override fun areContentsTheSame(
            oldItem: ChatHistoryItem,
            newItem: ChatHistoryItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}