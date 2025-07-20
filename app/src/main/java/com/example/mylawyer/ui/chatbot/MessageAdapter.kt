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
import com.example.mylawyer.data.model.Message


class MessageAdapter(
    private val onLikeClick: (Message) -> Unit = {},
    private val onDislikeClick: (Message) -> Unit = {}
) : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
    }

    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageContainer: ConstraintLayout = view.findViewById(R.id.messageContainer)
        val textMessage: TextView = view.findViewById(R.id.textMessage)
        val btnLike: ImageButton? = view.findViewById(R.id.imageButton)
        val btnDislike: ImageButton? = view.findViewById(R.id.imageButton2)

        fun bind(message: Message) {
            Log.d(
                "MessageViewHolder",
                "Привязка: id=${message.id}, text=${message.text}, isUser=${message.isUser}, reaction=${message.reaction}"
            )
            textMessage.text = message.text
            textMessage.visibility = if (message.text.isNullOrEmpty()) View.GONE else View.VISIBLE

            if (message.isUser) {
                btnLike?.visibility = View.GONE
                btnDislike?.visibility = View.GONE
            } else {
                btnLike?.visibility = View.VISIBLE
                btnDislike?.visibility = View.VISIBLE
                btnLike?.setImageResource(
                    if (message.reaction == 1) R.drawable.ic_like_message_filled
                    else R.drawable.ic_like_message
                )
                btnDislike?.setImageResource(
                    if (message.reaction == 2) R.drawable.ic_dislike_message_filled
                    else R.drawable.ic_dislike_message
                )
                btnLike?.setOnClickListener {
                    Log.d(
                        "MessageViewHolder",
                        "Кнопка ЛАЙК нажата: id=${message.id}, text=${message.text}"
                    )
                    onLikeClick(message)
                }
                btnDislike?.setOnClickListener {
                    Log.d(
                        "MessageViewHolder",
                        "Кнопка ДИЗЛАЙК нажата: id=${message.id}, text=${message.text}"
                    )
                    onDislikeClick(message)
                }
            }

            // Применяем ConstraintSet для изменения выравнивания
            val constraintSet = ConstraintSet()
            constraintSet.clone(messageContainer)
            constraintSet.clear(R.id.textMessage, ConstraintSet.START)
            constraintSet.clear(R.id.textMessage, ConstraintSet.END)

            if (message.isUser) {
                constraintSet.connect(
                    R.id.textMessage,
                    ConstraintSet.END,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.END,
                    8
                )
                textMessage.setBackgroundResource(R.drawable.bg_user_message)
                Log.d("MessageViewHolder", "User message: ${message.text}")
            } else {
                constraintSet.connect(
                    R.id.textMessage,
                    ConstraintSet.START,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.START,
                    8
                )
                textMessage.setBackgroundResource(R.drawable.bg_bot_message)
                Log.d("MessageViewHolder", "Bot message: ${message.text}")
            }
            constraintSet.applyTo(messageContainer)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId =
            if (viewType == VIEW_TYPE_USER) R.layout.item_user_message else R.layout.item_message
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        Log.d(
            "MessageAdapter",
            "onBindViewHolder: position=$position, id=${message.id}, text=${message.text}, isUser=${message.isUser}, reaction=${message.reaction}"
        )
        holder.bind(message)
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            // Сравниваем по id и isUser для уникальности
            return if (oldItem.id != null && newItem.id != null) {
                oldItem.id == newItem.id && oldItem.isUser == newItem.isUser
            } else {
                // Для локальных сообщений без id используем tempId
                oldItem.tempId == newItem.tempId
            }
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}