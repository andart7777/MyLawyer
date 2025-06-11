package com.example.mylawyer.data.model

import com.google.gson.annotations.SerializedName

data class ChatHistoryItem(
    @SerializedName("id") val chatId: String,
    @SerializedName("title") val title: String,
    @SerializedName("last_message") val lastMessage: String?,
    @SerializedName("timestamp") val timestamp: String
)