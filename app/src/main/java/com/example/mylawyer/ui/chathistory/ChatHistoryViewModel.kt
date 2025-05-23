package com.example.mylawyer.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mylawyer.data.model.ChatListResponse
import com.example.mylawyer.repository.ChatRepository
import com.example.mylawyer.utils.UserIdManager
import kotlinx.coroutines.launch
import java.util.UUID


class ChatHistoryViewModel(
    private val repository: ChatRepository,
    private val context: Context
) : ViewModel() {
    private val _chats = MutableLiveData<List<ChatListResponse.ChatListItem>>()
    val chats: LiveData<List<ChatListResponse.ChatListItem>> get() = _chats

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchChats() {
        viewModelScope.launch {
            val userId = UserIdManager.getUserId(context)
            val result = repository.getChats(userId)
            result.onSuccess { response ->
                Log.d("ChatHistoryViewModel", "Received ${response.chats.size} chats")
                _chats.postValue(response.chats)
            }.onFailure { exception ->
                Log.e("ChatHistoryViewModel", "Error: ${exception.message}", exception)
                _error.postValue("Failed to load chats: ${exception.message}")
            }
        }
    }
}