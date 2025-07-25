package com.example.mylawyer.utils

import android.content.Context
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object UserIdManager {
    private const val PREFS_NAME = "MyLawyerPrefs"
    private const val KEY_CURRENT_CHAT_ID = "current_chat_id"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_ID_TOKEN = "id_token"

    fun getUserId(context: Context): String {
        val auth = Firebase.auth
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    suspend fun getIdToken(context: Context): String? {
        return try {
            Firebase.auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }

    fun saveUserId(context: Context, userId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun saveIdToken(context: Context, idToken: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ID_TOKEN, idToken).apply()
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