package com.example.mylawyer.repository

import android.util.Log
import com.example.mylawyer.data.api.ChatApi
import com.example.mylawyer.data.model.ChatCreateRequest
import com.example.mylawyer.data.model.ChatHistoryItem
import com.example.mylawyer.data.model.ChatRequest
import com.example.mylawyer.data.model.ChatResponse
import com.example.mylawyer.data.model.Message
import com.example.mylawyer.data.model.NewChatResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.mylawyer.data.model.ReactionRequest

class ChatRepository(private val apiService: ChatApi) {
    suspend fun sendMessage(request: ChatRequest): Result<ChatResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(
                    "ChatRepository",
                    "Отправка запроса: user_id=${request.userId}, message=${request.message}"
                )
                val response = apiService.sendMessage(request)
                Log.d("ChatRepository", "Получен ответ: $response")
                Result.success(response)
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

    suspend fun createNewChat(request: ChatCreateRequest): Result<NewChatResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ChatRepository", "Создание нового чата для user_id=${request.userId}")
                val response = apiService.createNewChat(request)
                Log.d("ChatRepository", "Ответ на создание чата: $response")
                Result.success(response)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Ошибка создания чата: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getChats(userId: String): Result<List<ChatHistoryItem>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ChatRepository", "Получение чатов для user_id=$userId")
                val response = apiService.getChats(userId)
                Log.d("ChatRepository", "Получено чатов: ${response.size}")
                Result.success(response)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Ошибка получения чатов: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getChatMessages(chatId: String, userId: String): Result<List<Message>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ChatRepository", "Получение сообщений для chat_id=$chatId, user_id=$userId")
                val response = apiService.getChatMessages(chatId, userId)
                Log.d("ChatRepository", "Получено сообщений: ${response.size}")
                Result.success(response)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Ошибка получения сообщений: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteChat(chatId: String, userId: String): Result<Map<String, String>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ChatRepository", "Удаление чата: chat_id=$chatId, user_id=$userId")
                val response = apiService.deleteChat(chatId, userId)
                Log.d("ChatRepository", "Ответ на удаление чата: $response")
                Result.success(response)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Ошибка удаления чата: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}