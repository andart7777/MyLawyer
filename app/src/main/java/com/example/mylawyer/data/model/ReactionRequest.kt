package com.example.mylawyer.data.model

data class ReactionRequest(
    val message_id: Int,
    val user_id: String,
    val reaction: Int
)