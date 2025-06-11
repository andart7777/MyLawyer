//package com.example.mylawyer.viewmodel
//
//import android.content.Context
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import com.example.mylawyer.repository.ChatRepository
//
//class ChatHistoryViewModelFactory(
//    private val repository: ChatRepository,
//    private val context: Context
//) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(ChatHistoryViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return ChatHistoryViewModel(repository, context) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}