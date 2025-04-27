package com.example.mylawyer.data.model

import java.util.Date

data class ChatHistoryItem(
    val id: Int,
    val title: String,
    val lastMessage: String,
    val timestamp: Date
)
