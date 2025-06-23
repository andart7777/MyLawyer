package com.example.mylawyer.utils

import android.content.Context
import android.content.SharedPreferences

object ReactionManager {
    private const val PREFS_NAME = "ReactionPrefs"
    private const val KEY_REACTION_PREFIX = "reaction_"

    fun saveReaction(context: Context, messageId: Int, reaction: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_REACTION_PREFIX + messageId, reaction).apply()
    }

    fun getReaction(context: Context, messageId: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_REACTION_PREFIX + messageId, 0)
    }

    fun clearReactions(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}