package com.reboot.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.reboot.app.data.local.Keys
import com.reboot.app.data.local.PrefsStore
import com.reboot.app.data.model.Achievement
import com.reboot.app.data.model.ChatMessage
import com.reboot.app.data.model.HabitItem
import com.reboot.app.data.model.MentorMode
import com.reboot.app.data.model.PlanItem
import com.reboot.app.data.model.TaskItem
import com.reboot.app.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Single source of truth for app state. Everything is persisted locally on-device via
 * Jetpack DataStore, so the logged-in user, their tasks, habits, XP, streak, and chat
 * history all survive app restarts (i.e. "the app remembers the user").
 */
class RebootRepository(context: Context) {

    private val store = PrefsStore(context)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // ---------------- USER PROFILE ----------------

    val userProfile: Flow<UserProfile> = store.stringFlow(Keys.USER_PROFILE).map { raw ->
        if (raw.isBlank()) UserProfile() else runCatching { json.decodeFromString<UserProfile>(raw) }
            .getOrDefault(UserProfile())
    }

    suspend fun getUserProfileOnce(): UserProfile = userProfile.first()

    suspend fun saveUserProfile(profile: UserProfile) {
        store.putString(Keys.USER_PROFILE, json.encodeToString(profile))
    }

    suspend fun updateProfile(transform: (UserProfile) -> UserProfile) {
        val current = getUserProfileOnce()
        saveUserProfile(transform(current))
    }

    suspend fun addXp(amount: Int) {
        updateProfile { p ->
            var xp = p.xp + amount
            var level = p.level
            var cap = p.xpToNextLevel
            var coins = p.coins + (amount / 5)
            while (xp >= cap) {
                xp -= cap
                level += 1
                cap = (cap * 1.15).toInt()
            }
            p.copy(xp = xp, level = level, xpToNextLevel = cap, coins = coins)
        }
    }

    suspend fun logout() {
        updateProfile { it.copy(isLoggedIn = false) }
    }

    // ---------------- TASKS ----------------

    val tasks: Flow<List<TaskItem>> = store.stringFlow(Keys.TASKS).map { raw ->
        if (raw.isBlank()) defaultTasks() else runCatching { json.decodeFromString<List<TaskItem>>(raw) }
            .getOrDefault(defaultTasks())
    }

    suspend fun saveTasks(items: List<TaskItem>) {
        store.putString(Keys.TASKS, json.encodeToString(items))
    }

    suspend fun toggleTask(id: String) {
        val current = tasks.first().toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val task = current[idx]
            val nowDone = !task.done
            current[idx] = task.copy(done = nowDone)
            saveTasks(current)
            if (nowDone) {
                addXp(task.xpReward)
                bumpStreakIfAllDone(current)
            }
        }
    }

    suspend fun addTask(task: TaskItem) {
        val current = tasks.first().toMutableList()
        current.add(0, task)
        saveTasks(current)
    }

    private suspend fun bumpStreakIfAllDone(current: List<TaskItem>) {
        if (current.isNotEmpty() && current.all { it.done }) {
            updateProfile { it.copy(streakDays = it.streakDays + 1) }
        }
    }

    private fun defaultTasks() = listOf(
        TaskItem("t1", "Тренировка", xpReward = 80, category = "Спорт", timeLabel = "07:30"),
        TaskItem("t2", "Прочитать 20 страниц", xpReward = 40, category = "Развитие", timeLabel = "20:00"),
        TaskItem("t3", "Медитация", xpReward = 30, category = "Психология", timeLabel = "21:00"),
        TaskItem("t4", "Без телефона 1 час", xpReward = 50, category = "Дисциплина", timeLabel = "22:00"),
    )

    // ---------------- HABITS ----------------

    val habits: Flow<List<HabitItem>> = store.stringFlow(Keys.HABITS).map { raw ->
        if (raw.isBlank()) defaultHabits() else runCatching { json.decodeFromString<List<HabitItem>>(raw) }
            .getOrDefault(defaultHabits())
    }

    suspend fun saveHabits(items: List<HabitItem>) {
        store.putString(Keys.HABITS, json.encodeToString(items))
    }

    suspend fun toggleHabit(id: String) {
        val current = habits.first().toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val h = current[idx]
            val done = !h.completedToday
            current[idx] = h.copy(completedToday = done, streak = if (done) h.streak + 1 else maxOf(0, h.streak - 1))
            saveHabits(current)
            if (done) addXp(15)
        }
    }

    private fun defaultHabits() = listOf(
        HabitItem("h1", "Тренировка", streak = 5),
        HabitItem("h2", "Чтение", streak = 5),
        HabitItem("h3", "Медитация", streak = 3),
        HabitItem("h4", "Ранний подъём", streak = 2),
        HabitItem("h5", "Без сахара", streak = 0),
    )

    // ---------------- PLANS ----------------

    val plans: Flow<List<PlanItem>> = store.stringFlow(Keys.PLANS).map { raw ->
        if (raw.isBlank()) defaultPlans() else runCatching { json.decodeFromString<List<PlanItem>>(raw) }
            .getOrDefault(defaultPlans())
    }

    private fun defaultPlans() = listOf(
        PlanItem("p1", "Тренировки", "4 недели / 12 задач", 65, 12),
        PlanItem("p2", "Саморазвитие", "33 задачи", 40, 33),
        PlanItem("p3", "Психология", "21 задача", 30, 21),
        PlanItem("p4", "Питание", "24 задачи", 50, 24),
        PlanItem("p5", "Карьера", "18 задач", 25, 18),
    )

    // ---------------- ACHIEVEMENTS ----------------

    val achievements: Flow<List<Achievement>> = store.stringFlow(Keys.ACHIEVEMENTS).map { raw ->
        if (raw.isBlank()) defaultAchievements() else runCatching { json.decodeFromString<List<Achievement>>(raw) }
            .getOrDefault(defaultAchievements())
    }

    private fun defaultAchievements() = listOf(
        Achievement("a1", "7 дней подряд", "Выполняй задачи 7 дней подряд", true),
        Achievement("a2", "Ранняя пташка", "Просыпайся до 7:00 5 дней", true),
        Achievement("a3", "Читатель", "Прочитай 5 книг", true),
        Achievement("a4", "Без телефона", "3 часа без телефона", false),
    )

    // ---------------- CHAT HISTORY (per mentor mode) ----------------

    fun chatHistory(mode: MentorMode): Flow<List<ChatMessage>> =
        store.stringFlow(stringPreferencesKey(Keys.CHAT_PREFIX + mode.name)).map { raw ->
            if (raw.isBlank()) emptyList() else runCatching { json.decodeFromString<List<ChatMessage>>(raw) }
                .getOrDefault(emptyList())
        }

    suspend fun appendChatMessage(mode: MentorMode, message: ChatMessage) {
        val key = stringPreferencesKey(Keys.CHAT_PREFIX + mode.name)
        val current = chatHistory(mode).first().toMutableList()
        current.add(message)
        store.putString(key, json.encodeToString(current))
    }

    // ---------------- SETTINGS ----------------

    val groqApiKey: Flow<String> = store.stringFlow(Keys.GROQ_API_KEY)
    suspend fun setGroqApiKey(key: String) = store.putString(Keys.GROQ_API_KEY, key)

    val groqModel: Flow<String> = store.stringFlow(Keys.GROQ_MODEL, default = "openai/gpt-oss-20b")
    suspend fun setGroqModel(model: String) = store.putString(Keys.GROQ_MODEL, model)

    val silentMode: Flow<Boolean> = store.boolFlow(Keys.SILENT_MODE)
    suspend fun setSilentMode(enabled: Boolean) = store.putBool(Keys.SILENT_MODE, enabled)

    val notificationsEnabled: Flow<Boolean> = store.boolFlow(Keys.NOTIFICATIONS, default = true)
    suspend fun setNotifications(enabled: Boolean) = store.putBool(Keys.NOTIFICATIONS, enabled)

    companion object {
        @Volatile private var INSTANCE: RebootRepository? = null
        fun getInstance(context: Context): RebootRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: RebootRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
