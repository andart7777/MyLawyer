package com.example.mylawyer.repository

import android.util.Log
import com.example.mylawyer.data.api.ChatApi
import com.example.mylawyer.data.model.ChatCreateRequest
import com.example.mylawyer.data.model.ChatHistoryItem
import com.example.mylawyer.data.model.ChatRequest
import com.example.mylawyer.data.model.ChatResponse
import com.example.mylawyer.data.model.NewChatResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

// ChatRepository обрабатывает сетевые запросы и предоставляет данные для ViewModel.
class ChatRepository(private val apiService: ChatApi) {
    suspend fun sendMessage(request: ChatRequest): Result<ChatResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ChatRepository", "Sending request: user_id=${request.userId}, message=${request.message}")
                val response = apiService.sendMessage(request)
                Log.d("ChatRepository", "Received response: $response")
                Result.success(response)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error sending message: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun createNewChat(request: ChatCreateRequest): Result<NewChatResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ChatRepository", "Creating new chat for user_id=${request.userId}")
                val response = apiService.createNewChat(request)
                Log.d("ChatRepository", "New chat response: $response")
                Result.success(response)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error creating chat: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun getChats(userId: UUID): Result<List<ChatHistoryItem>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("ChatRepository", "Fetching chats for user_id=$userId")
                val response = apiService.getChats(userId)
                Log.d("ChatRepository", "Received chats: ${response.size}")
                Result.success(response)
            } catch (e: Exception) {
                Log.e("ChatRepository", "Error fetching chats: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
}