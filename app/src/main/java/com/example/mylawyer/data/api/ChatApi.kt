package com.example.mylawyer.data.api

import com.example.mylawyer.data.model.ChatResponse
import com.example.mylawyer.data.model.ChatRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApi {
    @POST("chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse
}
