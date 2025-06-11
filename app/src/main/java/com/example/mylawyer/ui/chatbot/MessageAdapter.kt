package com.example.mylawyer.ui.chatbot

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
import com.example.mylawyer.R
import java.util.UUID

data class Message(val text: String, val isUser: Boolean, val tempId: String = UUID.randomUUID().toString())

class MessageAdapter :
    ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

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
        Log.d(
            "MessageAdapter",
            "Привязка сообщения: tempId=${message.tempId}, text=${message.text}, isUser=${message.isUser}"
        )
        holder.textMessage.text = message.text
        holder.textMessage.visibility = if (message.text.isEmpty()) View.GONE else View.VISIBLE

        // Применяем ConstraintSet для изменения выравнивания
        val constraintSet = ConstraintSet()
        constraintSet.clone(holder.messageContainer)

        // Сброс выравнивания
        constraintSet.clear(R.id.textMessage, ConstraintSet.START)
        constraintSet.clear(R.id.textMessage, ConstraintSet.END)

        if (message.isUser) {
            // Пользовательское сообщение – вправо
            Log.d("MessageAdapter", "User message: ${message.text}")
            constraintSet.connect(
                R.id.textMessage,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                8
            )
            holder.textMessage.setBackgroundResource(R.drawable.bg_user_message)
            holder.reactionContainer.visibility = View.GONE
        } else {
            // Бот – влево
            Log.d("MessageAdapter", "Bot message: ${message.text}")
            constraintSet.connect(
                R.id.textMessage,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                8
            )
            holder.textMessage.setBackgroundResource(R.drawable.bg_bot_message)
            holder.reactionContainer.visibility = View.VISIBLE
        }

        constraintSet.applyTo(holder.messageContainer)
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.tempId == newItem.tempId
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.text == newItem.text && oldItem.isUser == newItem.isUser
        }
    }
}