package com.example.mylawyer.utils

import android.content.Context
import java.util.UUID

object UserIdManager {
    private const val PREFS_NAME = "MyLawyerPrefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_CURRENT_CHAT_ID = "current_chat_id"

    fun getUserId(context: Context): UUID {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userIdString = prefs.getString(KEY_USER_ID, null)
        return if (userIdString != null) {
            UUID.fromString(userIdString)
        } else {
            val newUserId = UUID.randomUUID()
            prefs.edit().putString(KEY_USER_ID, newUserId.toString()).apply()
            newUserId
        }
    }

    fun saveCurrentChatId(context: Context, chatId: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENT_CHAT_ID, chatId).apply()
    }

    fun getCurrentChatId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENT_CHAT_ID, null)
    }

    fun clearCurrentChatId(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_CURRENT_CHAT_ID).apply()
    }
}