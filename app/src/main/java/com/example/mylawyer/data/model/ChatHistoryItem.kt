package com.example.mylawyer.data.model

import java.util.UUID

data class ChatHistoryItem(
    val chatId: UUID,
    val title: String,
    val lastMessage: String,
    val timestamp: String
)
