package com.example.mylawyer.ui.chatbot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mylawyer.repository.ChatRepository
import com.example.mylawyer.utils.Event
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()

    private val _botResponse = MutableLiveData<Event<String>>()
    val botResponse: LiveData<Event<String>> = _botResponse

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
                        _botResponse.value = Event(body.response)
                    } else {
                        _botResponse.value = Event("Ответ пуст")
                    }
                } else {
                    _botResponse.value = Event("Ошибка: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Ошибка при получении ответа", e)
                _botResponse.value = Event("Ошибка подключения: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
