package com.example.mylawyer.data.api

import com.example.mylawyer.data.model.ChatCreateRequest
import com.example.mylawyer.data.model.ChatHistoryItem
import com.example.mylawyer.data.model.ChatRequest
import com.example.mylawyer.data.model.ChatResponse
import com.example.mylawyer.data.model.Message
import com.example.mylawyer.data.model.NewChatResponse
import com.example.mylawyer.data.model.ReactionRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApi {
    @POST("chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse

    @POST("chats/new")
    suspend fun createNewChat(@Body request: ChatCreateRequest): NewChatResponse

    @GET("chats")
    suspend fun getChats(): List<ChatHistoryItem>

    @GET("chats/{chat_id}/messages")
    suspend fun getChatMessages(@Path("chat_id") chatId: String): List<Message>

    @DELETE("chats/{chat_id}")
    suspend fun deleteChat(@Path("chat_id") chatId: String): Map<String, String>

    @POST("like")
    suspend fun sendReaction(@Body request: ReactionRequest): Map<String, String>
}