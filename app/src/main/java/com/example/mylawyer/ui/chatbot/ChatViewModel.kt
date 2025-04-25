package com.example.mylawyer.ui.chatbot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mylawyer.repository.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()

    private val _botResponse = MutableLiveData<String>()
    val botResponse: LiveData<String> = _botResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun sendMessage(message: String) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = repository.sendMessage(userId = 1, message)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        _botResponse.value = body.response
                    } else {
                        _botResponse.value = "Ответ пуст"
                    }
                } else {
                    _botResponse.value = "Ошибка: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Ошибка при получении ответа", e)
                _botResponse.value = "Ошибка подключения: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
