package com.example.mylawyer.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

// Модель для запроса к POST /chat
data class ChatRequest(
    @SerializedName("message") val message: String
)

// Модель для ответа от POST /chat
data class ChatResponse(
    @SerializedName("chat_id") val chatId: UUID,
    @SerializedName("message_id") val messageId: UUID,
    @SerializedName("message") val message: String,
    @SerializedName("response") val response: String
)
