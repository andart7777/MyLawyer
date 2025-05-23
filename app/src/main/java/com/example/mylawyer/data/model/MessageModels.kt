package com.example.mylawyer.data.model

import com.google.gson.annotations.SerializedName
import java.util.UUID

// Модель для запроса к POST /chat
data class ChatRequest(
    @SerializedName("user_id") val userId: UUID,
    @SerializedName("message") val message: String
)

// Модель для ответа от POST /chat
data class ChatResponse(
    @SerializedName("chat_id") val chatId: UUID,
    @SerializedName("message_id") val messageId: UUID? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("response") val response: String
)

data class NewChatResponse(
    @SerializedName("chat_id") val chatId: UUID
)

data class ChatCreateRequest(
    @SerializedName("user_id") val userId: UUID,
    @SerializedName("title") val title: String? = null
)

data class ChatListItem(
    @SerializedName("chat_id") val chatId: UUID,
    @SerializedName("title") val title: String,
    @SerializedName("last_message") val lastMessage: String?,
    @SerializedName("timestamp") val timestamp: String
)

data class ChatListResponse(
    @SerializedName("chats") val chats: List<ChatListItem>
)