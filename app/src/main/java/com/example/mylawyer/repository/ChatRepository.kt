package com.example.mylawyer.repository

import android.content.Context
import android.util.Log
import com.example.mylawyer.data.api.ChatApi
import com.example.mylawyer.data.model.ChatCreateRequest
import com.example.mylawyer.data.model.ChatHistoryItem
import com.example.mylawyer.data.model.ChatRequest
import com.example.mylawyer.data.model.ChatResponse
import com.example.mylawyer.data.model.Message
import com.example.mylawyer.data.model.NewChatResponse
import com.example.mylawyer.data.model.ReactionRequest
import com.example.mylawyer.utils.UserIdManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository(private val apiService: ChatApi, private val context: Context) {
    private val messageCache = mutableMapOf<String, List<Message>>()
    private val lastRequestTimes = mutableMapOf<String, Long>()
    private val debounceTime = 1000L // 1 секунда
    private val cacheTTL = 5 * 60 * 1000L // 5 минут

    suspend fun sendMessage(request: ChatRequest): Result<ChatResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.sendMessage(request)
                Result.success(response)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 401) {
                    Log.e("ChatRepository", "Токен недействителен или отсутствует")
                    Result.failure(Exception("Требуется повторная авторизация"))
                } else {
                    Log.e("ChatRepository", "Ошибка отправки сообщения: ${e.message}", e)
                    Result.failure(e)
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Ошибка отправки сообщения: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun sendReaction(request: ReactionRequest): Result<Map<String, String>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ChatRepository", "Отправка реакции: $request")
                val response = apiService.sendReaction(request)
                Log.d("ChatRepository", "Ответ на реакцию: $response")
                Result.success(response)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Ошибка отправки реакции: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun createNewChat(): Result<NewChatResponse> {
        val userId = UserIdManager.getUserId(context)
        val request = ChatCreateRequest(userId = userId, title = null)
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ChatRepository", "Создание нового чата для user_id=$userId")
                val response = apiService.createNewChat(request)
                Log.d("ChatRepository", "Ответ на создание чата: $response")
                Result.success(response)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Ошибка создания чата: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getChats(): Result<List<ChatHistoryItem>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ChatRepository", "Получение чатов")
                val response = apiService.getChats()
                Log.d("ChatRepository", "Получено чатов: ${response.size}")
                Result.success(response)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Ошибка получения чатов: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getChatMessages(chatId: String): Result<List<Message>> {
        return withContext(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            val lastRequestTime = lastRequestTimes[chatId] ?: 0L
            // Проверяем кэш и его актуальность
            if (messageCache[chatId] != null && (currentTime - lastRequestTime) < cacheTTL) {
                Log.d("ChatRepository", "Используем кэшированные сообщения для chatId: $chatId, размер: ${messageCache[chatId]!!.size}")
                return@withContext Result.success(messageCache[chatId]!!)
            }
            try {
                Log.d("ChatRepository", "Получение сообщений для chat_id=$chatId")
                val response = apiService.getChatMessages(chatId)
                Log.d("ChatRepository", "Получено сообщений: ${response.size}")
                // Сохраняем в кэш
                messageCache[chatId] = response
                lastRequestTimes[chatId] = currentTime
                Result.success(response)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Ошибка получения сообщений: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    fun clearMessageCache(chatId: String? = null) {
        if (chatId != null) {
            messageCache.remove(chatId)
            lastRequestTimes.remove(chatId)
        } else {
            messageCache.clear()
            lastRequestTimes.clear()
        }
        Log.d("ChatRepository", "Кэш сообщений очищен для chatId: $chatId")
    }

    suspend fun deleteChat(chatId: String): Result<Map<String, String>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ChatRepository", "Удаление чата: chat_id=$chatId")
                val response = apiService.deleteChat(chatId)
                Log.d("ChatRepository", "Ответ на удаление чата: $response")
                Result.success(response)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Ошибка удаления чата: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}