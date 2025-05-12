package com.example.mylawyer.utils

import android.content.Context
import java.util.UUID

object UserIdManager {
    private const val PREFS_NAME = "MyLawyerPrefs"
    private const val KEY_USER_ID = "user_id"

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
}