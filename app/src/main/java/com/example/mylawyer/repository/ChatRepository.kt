package com.example.mylawyer.repository

import com.example.mylawyer.data.api.RetrofitInstance
import com.example.mylawyer.data.model.MessageRequest


// ChatRepository обрабатывает сетевые запросы и предоставляет данные для ViewModel.
class ChatRepository {
    suspend fun sendMessage(userId: Int, message: String) =
        RetrofitInstance.api.sendMessage(MessageRequest(user_id = userId, message = message))
}