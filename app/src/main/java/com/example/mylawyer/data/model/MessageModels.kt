package com.example.mylawyer.data.model

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("message") val message: String
)

data class ChatResponse(
    @SerializedName("chat_id") val chatId: String,
    @SerializedName("message_id") val messageId: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("response") val response: String
)

data class NewChatResponse(
    @SerializedName("chat_id") val chatId: String
)

data class ChatCreateRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("title") val title: String? = null
)

data class ChatListItem(
    @SerializedName("chat_id") val chatId: String,
    @SerializedName("title") val title: String,
    @SerializedName("last_message") val lastMessage: String?,
    @SerializedName("timestamp") val timestamp: String
)

data class ChatListResponse(
    @SerializedName("chats") val chats: List<ChatListItem>
)