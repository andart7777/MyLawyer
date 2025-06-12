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

class MessageAdapter : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
    }

    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageContainer: ConstraintLayout = view.findViewById(R.id.messageContainer)
        val textMessage: TextView = view.findViewById(R.id.textMessage)
        val btnLike: ImageButton? = view.findViewById(R.id.imageButton)
        val btnDislike: ImageButton? = view.findViewById(R.id.imageButton2)
        val reactionContainer: View? = view.findViewById(R.id.reactionContainer)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_USER) R.layout.item_user_message else R.layout.item_message
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
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
            constraintSet.connect(
                R.id.textMessage,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                8
            )
            holder.textMessage.setBackgroundResource(R.drawable.bg_user_message)
            Log.d("MessageAdapter", "User message: ${message.text}")
            holder.reactionContainer?.visibility = View.GONE
        } else {
            // Бот – влево
            constraintSet.connect(
                R.id.textMessage,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                8
            )
            holder.textMessage.setBackgroundResource(R.drawable.bg_bot_message)
            Log.d("MessageAdapter", "Bot message: ${message.text}")
            holder.btnLike?.setOnClickListener {
                Log.d("MessageAdapter", "Лайк для сообщения: ${message.text}")
            }
            holder.btnDislike?.setOnClickListener {
                Log.d("MessageAdapter", "Дизлайк для сообщения: ${message.text}")
            }
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