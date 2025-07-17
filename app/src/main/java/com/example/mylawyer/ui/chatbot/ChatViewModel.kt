package com.example.mylawyer.ui.chatbot

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mylawyer.data.model.ChatHistoryItem
import com.example.mylawyer.data.model.ChatRequest
import com.example.mylawyer.data.model.ChatResponse
import com.example.mylawyer.data.model.Message
import com.example.mylawyer.data.model.ReactionRequest
import com.example.mylawyer.repository.ChatRepository
import com.example.mylawyer.utils.Event
import com.example.mylawyer.utils.ReactionManager
import com.example.mylawyer.utils.UserIdManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class ReactionUpdate(val messageId: Int, val reaction: Int)

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

    // Новое LiveData для обновления реакции
    private val _reactionUpdate = MutableLiveData<Event<ReactionUpdate>>()
    val reactionUpdate: LiveData<Event<ReactionUpdate>> get() = _reactionUpdate

    private var syncChatsRetryAttempts = 0
    private val maxSyncChatsRetryAttempts = 3

    init {
        // Загружаем сохранённый chatId
        val savedChatId = UserIdManager.getCurrentChatId(context)
        if (savedChatId != null) {
            _currentChatId.postValue(savedChatId)
        }
        syncChats(isManualRetry = false)
    }

private fun handleError(exception: Throwable?, defaultMessage: String) {
    Log.e("ChatViewModel", "Ошибка: ${exception?.message}", exception)
    when (exception) {
        is HttpException -> {
            when (exception.code()) {
                400 -> {
                    val errorBody = exception.response()?.errorBody()?.string()
                    if (errorBody?.contains("Invalid UUID format") == true) {
                        _error.postValue(Event("Некорректный формат идентификатора чата"))
                    } else {
                        _error.postValue(Event("Некорректный запрос: проверьте данные чата"))
                    }
                }
                401 -> _error.postValue(Event("Требуется повторная авторизация"))
                403 -> _error.postValue(Event("Доступ запрещён: этот чат не принадлежит вам"))
                404 -> _error.postValue(Event("Чат не найден"))
                429 -> _error.postValue(Event("Слишком много запросов, попробуйте позже"))
                500 -> _error.postValue(Event("Ошибка сервера, попробуйте позже"))
                else -> _error.postValue(Event("$defaultMessage: ${exception.message}"))
            }
        }
        is IllegalStateException -> {
            if (exception.message?.contains("closed") == true) {
                _error.postValue(Event("Ошибка обработки ответа сервера. Попробуйте снова."))
            } else {
                _error.postValue(Event("$defaultMessage: ${exception.message}"))
            }
        }
        is SocketTimeoutException -> {
            _error.postValue(Event("Время ожидания истекло: $defaultMessage"))
        }
        is UnknownHostException, is ConnectException -> {
            _error.postValue(Event("Нет соединения с интернетом, проверьте подключение"))
        }
        else -> _error.postValue(Event("$defaultMessage: ${exception?.message}"))
    }
}

    fun sendMessage(message: String) {
        viewModelScope.launch {
            _isWaitingForBotResponse.postValue(true)
            val userId = UserIdManager.getUserId(context)
            val currentChatId = _currentChatId.value ?: run {
                val newChatId = createNewChatSync()
                newChatId ?: return@launch
            }
            val result = repository.sendMessage(ChatRequest(userId, message))
            result.onSuccess { response ->
                Log.d("ChatViewModel", "Получен ответ: $response")
                val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
                if (!currentMessages.any { it.response == response.response }) {
                    currentMessages.add(response)
                    _messages.postValue(currentMessages)
                }
                _currentChatId.postValue(response.chatId)
                UserIdManager.saveCurrentChatId(context, response.chatId)
                if (currentChatId != response.chatId) {
                    loadChatMessages(response.chatId)
                }
            }.onFailure { exception ->
                handleError(exception, "Не удалось отправить сообщение")
            }
            _isWaitingForBotResponse.postValue(false)
        }
    }

    fun createNewChat() {
        viewModelScope.launch {
            _isLoadingMessages.postValue(true)
            val result = repository.createNewChat()
            result.onSuccess { response ->
                Log.d("ChatViewModel", "Создан новый чат: ${response.chatId}")
                _currentChatId.postValue(response.chatId)
                UserIdManager.saveCurrentChatId(context, response.chatId)
                _messages.postValue(emptyList())
                _chatMessages.postValue(Event(emptyList()))
            }.onFailure { exception ->
                handleError(exception, "Не удалось создать чат")
            }
            _isLoadingMessages.postValue(false)
        }
    }

    private suspend fun createNewChatSync(): String? {
        val result = repository.createNewChat()
        return result.getOrNull()?.chatId?.also {
            _currentChatId.postValue(it)
            UserIdManager.saveCurrentChatId(context, it)
        }.also { chatId ->
            if (result.isFailure) {
                handleError(result.exceptionOrNull(), "Не удалось создать чат")
            }
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

    fun sendReaction(messageId: Int, reaction: Int) {
        viewModelScope.launch {
            Log.d("ChatViewModel", "Начало отправки реакции: messageId=$messageId, reaction=$reaction")
            val userId = UserIdManager.getUserId(context)
            val request = ReactionRequest(messageId, userId, reaction)
            try {
                val response = repository.sendReaction(request)
                Log.d("ChatViewModel", "Реакция отправлена успешно: $response")
                ReactionManager.saveReaction(context, messageId, reaction)
                // Отправляем событие обновления реакции
                _reactionUpdate.postValue(Event(ReactionUpdate(messageId, reaction)))
            } catch (e: Exception) {
                handleError(e, "Не удалось отправить реакцию")
            }
        }
    }

fun loadChatMessages(chatId: String) {
    viewModelScope.launch {
        _isLoadingMessages.postValue(true)
        Log.d("ChatViewModel", "Загрузка сообщений для chatId: $chatId")
        // Очищаем chatId от пробелов и проверяем формат
        val cleanedChatId = chatId.trim()
        if (!cleanedChatId.matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))) {
            Log.e("ChatViewModel", "Некорректный формат chatId: $cleanedChatId")
            _error.postValue(Event("Некорректный формат идентификатора чата"))
            _isLoadingMessages.postValue(false)
            return@launch
        }
        val result = repository.getChatMessages(cleanedChatId)
        result.onSuccess { messages ->
            Log.d("ChatViewModel", "Получено сообщений: ${messages.size}")
            val updatedMessages = messages.map { message ->
                message.copy(reaction = ReactionManager.getReaction(context, message.id ?: 0))
            }
            _chatMessages.postValue(Event(updatedMessages))
        }.onFailure { exception ->
            handleError(exception, "Не удалось загрузить сообщения")
        }
        _isLoadingMessages.postValue(false)
    }
}

    fun syncChats(isManualRetry: Boolean = false) {
        viewModelScope.launch {
            if (Firebase.auth.currentUser == null) return@launch
            if (isManualRetry) {
                syncChatsRetryAttempts = 0 // Сбрасываем счетчик при ручном вызове
                Log.d("ChatViewModel", "Ручной вызов syncChats, сброс попыток")
            }
            _isLoadingMessages.postValue(true)
            val result = repository.getChats()
            result.onSuccess { chats ->
                Log.d("ChatViewModel", "Получено чатов: ${chats.size}")
                _chats.postValue(chats)
                syncChatsRetryAttempts = 0 // Сбрасываем счетчик при успехе
                if (_currentChatId.value == null && chats.isNotEmpty()) {
                    val lastChat = chats.maxByOrNull { it.timestamp }
                    lastChat?.chatId?.let { chatId ->
                        Log.d("ChatViewModel", "Автоматически установлен chatId: $chatId")
                        setCurrentChatId(chatId)
                    }
                }
            }.onFailure { exception ->
                if (syncChatsRetryAttempts < maxSyncChatsRetryAttempts && !isManualRetry) {
                    syncChatsRetryAttempts++
                    Log.d("ChatViewModel", "Попытка повторной загрузки чатов: $syncChatsRetryAttempts/$maxSyncChatsRetryAttempts")
                    delay(5000) // Задержка 5 секунд перед повторной попыткой
                    syncChats()
                } else {
                    handleError(exception, "Не удалось загрузить чаты")
                }
            }
            _isLoadingMessages.postValue(false)
        }
    }

    fun initializeDefaultChat() {
        viewModelScope.launch {
            if (_currentChatId.value == null) {
                _isLoadingMessages.postValue(true)
                val result = repository.getChats()
                result.onSuccess { chats ->
                    Log.d("ChatViewModel", "Получено чатов: ${chats.size}")
                    syncChatsRetryAttempts = 0 // Сбрасываем счетчик при успехе
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
                    if (syncChatsRetryAttempts < maxSyncChatsRetryAttempts) {
                        syncChatsRetryAttempts++
                        Log.d("ChatViewModel", "Попытка повторной загрузки чатов (initializeDefaultChat): $syncChatsRetryAttempts/$maxSyncChatsRetryAttempts")
                        delay(5000) // Задержка 5 секунд перед повторной попыткой
                        initializeDefaultChat()
                    } else {
                        handleError(exception, "Не удалось загрузить чаты")
                    }
                }
                _isLoadingMessages.postValue(false)
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            _isLoadingMessages.postValue(true)
            val result = repository.deleteChat(chatId)
            result.onSuccess { response ->
                Log.d("ChatViewModel", "Чат удален: $response")
                if (_currentChatId.value == chatId) {
                    _currentChatId.postValue(null)
                    UserIdManager.clearCurrentChatId(context)
                    _messages.postValue(emptyList())
                    _chatMessages.postValue(Event(emptyList()))
                }
                syncChats()
            }.onFailure { exception ->
                handleError(exception, "Не удалось удалить чат")
            }
            _isLoadingMessages.postValue(false)
        }
    }
}