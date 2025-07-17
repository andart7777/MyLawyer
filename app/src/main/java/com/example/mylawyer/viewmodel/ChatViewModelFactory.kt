package com.example.mylawyer.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mylawyer.data.api.RetrofitInstance
import com.example.mylawyer.repository.ChatRepository
import com.example.mylawyer.ui.chatbot.ChatViewModel

class ChatViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            val retrofit = RetrofitInstance.api
            val repository = ChatRepository(retrofit, context) // Передаём context в ChatRepository
            return ChatViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}