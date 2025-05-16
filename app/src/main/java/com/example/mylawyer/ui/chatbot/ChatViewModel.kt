package com.example.mylawyer.ui.chatbot

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mylawyer.data.model.ChatCreateRequest
import com.example.mylawyer.data.model.ChatRequest
import com.example.mylawyer.data.model.ChatResponse
import com.example.mylawyer.data.model.NewChatResponse
import com.example.mylawyer.repository.ChatRepository
import com.example.mylawyer.utils.UserIdManager
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val repository: ChatRepository,
    private val context: Context
) : ViewModel() {
    private val _messages = MutableLiveData<List<ChatResponse>>()
    val messages: LiveData<List<ChatResponse>> get() = _messages

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _currentChatId = MutableLiveData<UUID?>()
    val currentChatId: LiveData<UUID?> get() = _currentChatId

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun sendMessage(message: String) {
        viewModelScope.launch {
            _isLoading.postValue(true) // Начинаем загрузку
            val userId = UserIdManager.getUserId(context)
            val result = repository.sendMessage(ChatRequest(userId, message))
            result.onSuccess { response ->
                Log.d("ChatViewModel", "Received response: $response")
                val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
                // Проверяем, нет ли уже этого ответа
                if (!currentMessages.any { it.response == response.response }) {
                    currentMessages.add(response)
                    _messages.postValue(currentMessages)
                }
                _currentChatId.postValue(response.chatId)
            }.onFailure { exception ->
                Log.e("ChatViewModel", "Error: ${exception.message}", exception)
                _error.postValue(exception.message)
            }
            _isLoading.postValue(false) // Завершаем загрузку
        }
    }

    fun createNewChat() {
        viewModelScope.launch {
            _isLoading.postValue(true) // Начинаем загрузку
            val userId = UserIdManager.getUserId(context)
            val request = ChatCreateRequest(userId = userId, title = null)
            val result = repository.createNewChat(request)
            result.onSuccess { response ->
                Log.d("ChatViewModel", "New chat created: ${response.chatId}")
                _currentChatId.postValue(response.chatId)
                _messages.postValue(emptyList()) // Очищаем сообщения для нового чата
            }.onFailure { exception ->
                Log.e("ChatViewModel", "Error: ${exception.message}", exception)
                _error.postValue(exception.message)
            }
            _isLoading.postValue(false) // Завершаем загрузку
        }
    }
}