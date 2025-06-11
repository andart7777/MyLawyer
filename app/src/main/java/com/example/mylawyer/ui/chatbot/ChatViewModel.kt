package com.example.mylawyer.ui.chatbot

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mylawyer.data.model.ChatCreateRequest
import com.example.mylawyer.data.model.ChatHistoryItem
import com.example.mylawyer.data.model.ChatRequest
import com.example.mylawyer.data.model.ChatResponse
import com.example.mylawyer.data.model.Message
import com.example.mylawyer.repository.ChatRepository
import com.example.mylawyer.utils.Event
import com.example.mylawyer.utils.UserIdManager
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val repository: ChatRepository,
    private val context: Context
) : ViewModel() {
    private val _messages = MutableLiveData<List<ChatResponse>>()
    val messages: LiveData<List<ChatResponse>> get() = _messages

    private val _chatMessages = MutableLiveData<Event<List<Message>>>()
    val chatMessages: LiveData<Event<List<Message>>> get() = _chatMessages

    private val _chats = MutableLiveData<List<ChatHistoryItem>>()
    val chats: LiveData<List<ChatHistoryItem>> get() = _chats

    private val _error = MutableLiveData<Event<String>>()
    val error: LiveData<Event<String>> get() = _error

    private val _currentChatId = MutableLiveData<String?>()
    val currentChatId: LiveData<String?> get() = _currentChatId

    private val _isLoadingMessages = MutableLiveData<Boolean>()
    val isLoadingMessages: LiveData<Boolean> get() = _isLoadingMessages

    private val _isWaitingForBotResponse = MutableLiveData<Boolean>()
    val isWaitingForBotResponse: LiveData<Boolean> get() = _isWaitingForBotResponse

    init {
        // Загружаем сохранённый chatId
        val savedChatId = UserIdManager.getCurrentChatId(context)
        if (savedChatId != null) {
            _currentChatId.postValue(savedChatId)
        }
        // Загружаем чаты без индикатора загрузки на старте
        loadChats(initialLoad = true)
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            _isWaitingForBotResponse.postValue(true)
            val userId = UserIdManager.getUserId(context).toString()
            val currentChatId = _currentChatId.value ?: UUID.randomUUID().toString().also {
                _currentChatId.postValue(it)
                UserIdManager.saveCurrentChatId(context, it)
            }
            val result = repository.sendMessage(ChatRequest(userId, message))
            result.onSuccess { response ->
                Log.d("ChatViewModel", "Получен ответ: $response")
                val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
                if (!currentMessages.any { it.response == response.response }) {
                    currentMessages.add(response)
                    _messages.postValue(currentMessages)
                }
                _currentChatId.postValue(response.chatId.toString())
                UserIdManager.saveCurrentChatId(context, response.chatId.toString())
                // Загружаем сообщения только если это новый чат
                if (currentChatId != response.chatId) {
                    loadChatMessages(response.chatId.toString())
                }
            }.onFailure { exception ->
                Log.e("ChatViewModel", "Ошибка отправки: ${exception.message}", exception)
                _error.postValue(Event("Не удалось отправить сообщение: ${exception.message}"))
            }
            _isWaitingForBotResponse.postValue(false)
        }
    }

    fun createNewChat() {
        viewModelScope.launch {
            _isLoadingMessages.postValue(true)
            val userId = UserIdManager.getUserId(context).toString()
            val request = ChatCreateRequest(userId = userId, title = null)
            val result = repository.createNewChat(request)
            result.onSuccess { response ->
                Log.d("ChatViewModel", "Создан новый чат: ${response.chatId}")
                _currentChatId.postValue(response.chatId.toString())
                UserIdManager.saveCurrentChatId(context, response.chatId.toString())
                _messages.postValue(emptyList())
                _chatMessages.postValue(Event(emptyList()))
            }.onFailure { exception ->
                Log.e("ChatViewModel", "Ошибка создания чата: ${exception.message}", exception)
                _error.postValue(Event("Не удалось создать чат: ${exception.message}"))
            }
            _isLoadingMessages.postValue(false)
        }
    }

    fun setCurrentChatId(chatId: String) {
        Log.d("ChatViewModel", "Устанавливаем chatId: $chatId")
        if (_currentChatId.value != chatId) {
            _currentChatId.postValue(chatId)
            UserIdManager.saveCurrentChatId(context, chatId)
            _messages.postValue(emptyList())
            loadChatMessages(chatId)
        }
    }

    fun loadChatMessages(chatId: String) {
        viewModelScope.launch {
            _isLoadingMessages.postValue(true)
            Log.d("ChatViewModel", "Загрузка сообщений для chatId: $chatId")
            val userId = UserIdManager.getUserId(context).toString()
            val result = repository.getChatMessages(chatId, userId)
            result.onSuccess { messages ->
                Log.d("ChatViewModel", "Получено сообщений: ${messages.size}")
                _chatMessages.postValue(Event(messages))
            }.onFailure { exception ->
                Log.e("ChatViewModel", "Ошибка загрузки: ${exception.message}", exception)
                _error.postValue(Event("Не удалось загрузить сообщения: ${exception.message}"))
            }
            _isLoadingMessages.postValue(false)
        }
    }

    fun loadChats(initialLoad: Boolean = false) {
        viewModelScope.launch {
            if (!initialLoad) {
                _isLoadingMessages.postValue(true)
            }
            val userId = UserIdManager.getUserId(context).toString()
            val result = repository.getChats(userId)
            result.onSuccess { chats ->
                Log.d("ChatViewModel", "Получено чатов: ${chats.size}")
                _chats.postValue(chats)
                if (_currentChatId.value == null && chats.isNotEmpty()) {
                    val lastChat = chats.maxByOrNull { it.timestamp }
                    lastChat?.chatId?.let { chatId ->
                        Log.d("ChatViewModel", "Автоматически установлен chatId: $chatId")
                        setCurrentChatId(chatId)
                    }
                }
            }.onFailure { exception ->
                Log.e("ChatViewModel", "Ошибка загрузки чатов: ${exception.message}", exception)
                _error.postValue(Event("Не удалось загрузить чаты: ${exception.message}"))
            }
            if (!initialLoad) {
                _isLoadingMessages.postValue(false)
            }
        }
    }

    fun initializeDefaultChat() {
        viewModelScope.launch {
            if (_currentChatId.value == null) {
                val userId = UserIdManager.getUserId(context).toString()
                val result = repository.getChats(userId)
                result.onSuccess { chats ->
                    if (chats.isNotEmpty()) {
                        val lastChat = chats.maxByOrNull { it.timestamp }
                        lastChat?.chatId?.let { chatId ->
                            Log.d("ChatViewModel", "Устанавливаем последний chatId: $chatId")
                            setCurrentChatId(chatId)
                        }
                    } else {
                        Log.d("ChatViewModel", "Чатов нет")
                        _chatMessages.postValue(Event(emptyList()))
                        _messages.postValue(emptyList())
                    }
                }.onFailure { exception ->
                    Log.e("ChatViewModel", "Ошибка загрузки: ${exception.message}", exception)
                    _error.postValue(Event("Не удалось загрузить чаты: ${exception.message}"))
                }
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            _isLoadingMessages.postValue(true)
            val userId = UserIdManager.getUserId(context).toString()
            val result = repository.deleteChat(chatId, userId)
            result.onSuccess { response ->
                Log.d("ChatViewModel", "Чат удален: $response")
                if (_currentChatId.value == chatId) {
                    _currentChatId.postValue(null)
                    UserIdManager.clearCurrentChatId(context)
                    _messages.postValue(emptyList())
                    _chatMessages.postValue(Event(emptyList()))
                }
                loadChats()
            }.onFailure { exception ->
                Log.e("ChatViewModel", "Ошибка удаления: ${exception.message}", exception)
                _error.postValue(Event("Не удалось удалить чат: ${exception.message}"))
            }
            _isLoadingMessages.postValue(false)
        }
    }
}