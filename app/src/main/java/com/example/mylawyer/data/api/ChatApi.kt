package com.example.mylawyer.data.api

import com.example.mylawyer.data.model.MessageRequest
import com.example.mylawyer.data.model.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApi {
    @POST("chat")
    suspend fun sendMessage(
        @Body request: MessageRequest
    ): Response<MessageResponse>
}
