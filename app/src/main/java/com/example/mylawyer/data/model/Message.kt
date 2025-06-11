package com.example.mylawyer.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Message(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("chat_id") val chatId: String? = null,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("user_message") val userMessage: String? = null,
    @SerializedName("bot_response") val botResponse: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("metadata") val metadata: Map<String, Any>? = null,
    val text: String? = null,
    val isUser: Boolean = false,
    val tempId: String = UUID.randomUUID().toString()
)