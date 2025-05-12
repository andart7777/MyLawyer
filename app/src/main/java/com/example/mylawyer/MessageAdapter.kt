package com.example.mylawyer

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

data class Message(val text: String, val isUser: Boolean)

class MessageAdapter : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageContainer: ConstraintLayout = view.findViewById(R.id.messageContainer)
        val textMessage: TextView = view.findViewById(R.id.textMessage)
        val btnLike: ImageButton = view.findViewById(R.id.imageButton)
        val btnDislike: ImageButton = view.findViewById(R.id.imageButton2)
        val reactionContainer: View = view.findViewById(R.id.reactionContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.textMessage.text = message.text

        // Применяем ConstraintSet для изменения выравнивания
        val constraintSet = ConstraintSet()
        constraintSet.clone(holder.messageContainer)

        // Сброс выравнивания
        constraintSet.clear(R.id.textMessage, ConstraintSet.START)
        constraintSet.clear(R.id.textMessage, ConstraintSet.END)

        if (message.isUser) {
            // Пользовательское сообщение – вправо
            Log.d("Adapter", "User message: ${message.text}")
            constraintSet.connect(
                R.id.textMessage,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                0
            )
            holder.textMessage.setBackgroundResource(R.drawable.bg_user_message)
            holder.reactionContainer.visibility = View.GONE
        } else {
            // Бот – влево
            Log.d("Adapter", "BOT message: ${message.text}")
            constraintSet.connect(
                R.id.textMessage,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                0
            )
            holder.textMessage.setBackgroundResource(R.drawable.bg_bot_message)
            holder.reactionContainer.visibility = View.VISIBLE
        }

        constraintSet.applyTo(holder.messageContainer)
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.text == newItem.text && oldItem.isUser == newItem.isUser
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}

//package com.example.mylawyer
//
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageButton
//import android.widget.TextView
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.constraintlayout.widget.ConstraintSet
//import androidx.recyclerview.widget.RecyclerView
//
//class MessageAdapter(private val messages: List<Message>) :
//    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
//
//    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val messageContainer: ConstraintLayout = view.findViewById(R.id.messageContainer)
//        val textMessage: TextView = view.findViewById(R.id.textMessage)
//        val btnLike: ImageButton = view.findViewById(R.id.imageButton)
//        val btnDislike: ImageButton = view.findViewById(R.id.imageButton2)
//        val reactionContainer: View = view.findViewById(R.id.reactionContainer)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_message, parent, false)
//        return MessageViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
//        val message = messages[position]
//        holder.textMessage.text = message.text
//
//        // Применяем ConstraintSet для изменения выравнивания
//        val constraintSet = ConstraintSet()
//        constraintSet.clone(holder.messageContainer)
//
//        // Сброс выравнивания (на всякий случай)
//        constraintSet.clear(R.id.textMessage, ConstraintSet.START)
//        constraintSet.clear(R.id.textMessage, ConstraintSet.END)
//
//        if (message.isUser) {
//            // Пользовательское сообщение – вправо
//            Log.d("Adapter", "User message: ${message.text}")
//            constraintSet.connect(
//                R.id.textMessage,
//                ConstraintSet.END,
//                ConstraintSet.PARENT_ID,
//                ConstraintSet.END,
//                0
//            )
//            holder.textMessage.setBackgroundResource(R.drawable.bg_user_message)
//            holder.reactionContainer.visibility = View.GONE
//        } else {
//            // Бот – влево
//            Log.d("Adapter", "BOT message: ${message.text}")
//            constraintSet.connect(
//                R.id.textMessage,
//                ConstraintSet.START,
//                ConstraintSet.PARENT_ID,
//                ConstraintSet.START,
//                0
//            )
//            holder.textMessage.setBackgroundResource(R.drawable.bg_bot_message)
//            holder.reactionContainer.visibility = View.VISIBLE
//        }
//
//        constraintSet.applyTo(holder.messageContainer)
//    }
//
//
//    override fun getItemCount() = messages.size
//}
