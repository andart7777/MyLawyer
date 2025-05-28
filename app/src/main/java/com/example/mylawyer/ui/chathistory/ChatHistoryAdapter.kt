//package com.example.mylawyer.ui.chathistory
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.example.mylawyer.data.model.ChatHistoryItem
//import com.example.mylawyer.databinding.ItemChatHistoryBinding
//import java.text.SimpleDateFormat
//import java.util.*
//
//class ChatHistoryAdapter(
//    private val onItemClick: (ChatHistoryItem) -> Unit
//) : ListAdapter<ChatHistoryItem, ChatHistoryAdapter.HistoryViewHolder>(DiffCallback) {
//
//    inner class HistoryViewHolder(val binding: ItemChatHistoryBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(item: ChatHistoryItem) {
//            binding.titleTextView.text = item.title
//            binding.lastMessageTextView.text = item.lastMessage
//            binding.timestampTextView.text = formatDate(item.timestamp)
//
//            binding.root.setOnClickListener {
//                onItemClick(item)
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
//        val binding = ItemChatHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return HistoryViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
//        holder.bind(getItem(position))
//    }
//
//    private fun formatDate(date: Date): String {
//        val now = Calendar.getInstance()
//        val cal = Calendar.getInstance().apply { time = date }
//
//        return when {
//            isSameDay(cal, now) -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
//            isYesterday(cal, now) -> "Вчера"
//            else -> SimpleDateFormat("d MMMM", Locale("ru")).format(date)
//        }
//    }
//
//    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
//        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
//                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
//    }
//
//    private fun isYesterday(cal1: Calendar, cal2: Calendar): Boolean {
//        val yesterday = cal2.clone() as Calendar
//        yesterday.add(Calendar.DAY_OF_YEAR, -1)
//        return isSameDay(cal1, yesterday)
//    }
//
//    companion object {
//        val DiffCallback = object : DiffUtil.ItemCallback<ChatHistoryItem>() {
//            override fun areItemsTheSame(oldItem: ChatHistoryItem, newItem: ChatHistoryItem): Boolean =
//                oldItem.chatId == newItem.chatId
//
//            override fun areContentsTheSame(oldItem: ChatHistoryItem, newItem: ChatHistoryItem): Boolean =
//                oldItem == newItem
//        }
//    }
//}
