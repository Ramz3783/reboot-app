package com.reboot.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "reboot_prefs")

object Keys {
    val USER_PROFILE = stringPreferencesKey("user_profile_json")
    val TASKS = stringPreferencesKey("tasks_json")
    val HABITS = stringPreferencesKey("habits_json")
    val PLANS = stringPreferencesKey("plans_json")
    val ACHIEVEMENTS = stringPreferencesKey("achievements_json")
    val CHAT_PREFIX = "chat_history_" // + mode name
    val GROQ_API_KEY = stringPreferencesKey("groq_api_key")
    val GROQ_MODEL = stringPreferencesKey("groq_model")
    val SILENT_MODE = booleanPreferencesKey("silent_mode")
    val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
    val LANGUAGE = stringPreferencesKey("language")
    val COACH_NOTE = stringPreferencesKey("coach_note")
}

class PrefsStore(private val context: Context) {

    fun stringFlow(key: Preferences.Key<String>, default: String = ""): Flow<String> =
        context.dataStore.data.map { it[key] ?: default }

    fun boolFlow(key: Preferences.Key<Boolean>, default: Boolean = false): Flow<Boolean> =
        context.dataStore.data.map { it[key] ?: default }

    suspend fun putString(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { it[key] = value }
    }

    suspend fun putBool(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { it[key] = value }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
