package com.example.mylawyer.data.api

import com.example.mylawyer.data.model.ChatCreateRequest
import com.example.mylawyer.data.model.ChatResponse
import com.example.mylawyer.data.model.ChatRequest
import com.example.mylawyer.data.model.ChatHistoryItem
import com.example.mylawyer.data.model.NewChatResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.UUID

interface ChatApi {
    @POST("chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse

    @POST("chats/new")
    suspend fun createNewChat(@Body request: ChatCreateRequest): NewChatResponse

    @GET("chats")
    suspend fun getChats(@Query("user_id") userId: UUID): List<ChatHistoryItem>
}