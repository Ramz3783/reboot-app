package com.reboot.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.reboot.app.data.local.Keys
import com.reboot.app.data.local.PrefsStore
import com.reboot.app.data.model.Achievement
import com.reboot.app.data.model.ChatMessage
import com.reboot.app.data.model.HabitItem
import com.reboot.app.data.model.MentorMode
import com.reboot.app.data.model.OnboardingCatalog
import com.reboot.app.data.model.PlanItem
import com.reboot.app.data.model.PlanTemplate
import com.reboot.app.data.model.TaskItem
import com.reboot.app.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Single source of truth for app state. Everything is persisted locally on-device via
 * Jetpack DataStore, so the logged-in user, their tasks, habits, XP, streak, and chat
 * history all survive app restarts (i.e. "the app remembers the user").
 */
class RebootRepository(context: Context) {

    private val store = PrefsStore(context)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun today(): String = LocalDate.now().toString()

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

    /**
     * Runs once whenever the app comes to the foreground (called from Splash). Handles two
     * things tied to real calendar dates, not app-session state:
     *  1. "Ударный режим" (streak mode): if the person let a full day pass with the previous
     *     day's tasks incomplete, the streak resets to 0. Completing all of today's tasks keeps
     *     extending it — forever, as long as no day is skipped.
     *  2. Daily task reset: yesterday's checked-off tasks un-check themselves for the new day,
     *     so the task list is actually usable day after day instead of staying "done" forever.
     */
    suspend fun runDailyMaintenance() {
        val profile = getUserProfileOnce()
        val todayStr = today()
        if (profile.lastTaskResetDate == todayStr) return // already handled today

        val daysSinceCredit = if (profile.lastStreakCreditDate.isBlank()) {
            null
        } else {
            ChronoUnit.DAYS.between(LocalDate.parse(profile.lastStreakCreditDate), LocalDate.parse(todayStr))
        }
        val brokeStreak = daysSinceCredit != null && daysSinceCredit >= 2

        updateProfile {
            it.copy(
                lastTaskResetDate = todayStr,
                streakDays = if (brokeStreak) 0 else it.streakDays,
            )
        }

        // Fresh unchecked tasks for the new day.
        val current = tasks.first()
        if (current.isNotEmpty()) {
            saveTasks(current.map { it.copy(done = false) })
        }
        val currentHabits = habits.first()
        if (currentHabits.isNotEmpty()) {
            saveHabits(currentHabits.map { it.copy(completedToday = false) })
        }
    }

    /**
     * Called once, right when onboarding finishes. Builds the person's actual task/habit list
     * from what they picked in the "problems" and "goals" steps, instead of handing everyone
     * the same 4 generic tasks no matter what they answered.
     */
    suspend fun seedFromOnboarding(problems: List<String>, goals: List<String>) {
        val problemTasks = problems.flatMap { OnboardingCatalog.tasksForProblem(it) }.distinctBy { it.id }
        val goalHabits = goals.mapNotNull { OnboardingCatalog.habitForGoal(it) }.distinctBy { it.id }

        saveTasks(if (problemTasks.isNotEmpty()) problemTasks else defaultTasks())
        saveHabits(if (goalHabits.isNotEmpty()) goalHabits else defaultHabits())
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
                creditStreakIfAllDone(current)
            }
        }
    }

    suspend fun addTask(task: TaskItem) {
        val current = tasks.first().toMutableList()
        current.add(0, task)
        saveTasks(current)
    }

    /** Adds every task from a template plan (skips titles the person already has). */
    suspend fun addTasksFromTitles(titles: List<String>, category: String) {
        val current = tasks.first().toMutableList()
        val existingTitles = current.map { it.title }.toSet()
        titles.filter { it !in existingTitles }.forEach { title ->
            current.add(0, TaskItem(id = "tpl_${title.hashCode()}_${System.currentTimeMillis()}", title = title, xpReward = 35, category = category))
        }
        saveTasks(current)
    }

    private suspend fun creditStreakIfAllDone(current: List<TaskItem>) {
        if (current.isEmpty() || !current.all { it.done }) return
        val profile = getUserProfileOnce()
        val todayStr = today()
        if (profile.lastStreakCreditDate == todayStr) return // already credited today, don't double count
        updateProfile { it.copy(streakDays = it.streakDays + 1, lastStreakCreditDate = todayStr) }
    }

    private fun defaultTasks() = listOf(
        TaskItem("t1", "Тренировка", xpReward = 80, category = "Спорт", timeLabel = "07:30", workoutId = "beginner_full_body"),
        TaskItem("t2", "Прочитать 20 страниц", xpReward = 40, category = "Развитие", timeLabel = "20:00"),
        TaskItem("t3", "Медитация", xpReward = 30, category = "Психология", timeLabel = "21:00"),
        TaskItem("t4", "Без телефона 1 час", xpReward = 50, category = "Дисциплина", timeLabel = "22:00"),
        TaskItem("t5", "Выпей 2 литра воды", xpReward = 25, category = "Здоровье", timeLabel = "весь день"),
        TaskItem("t6", "Составь план на завтра", xpReward = 30, category = "Планирование", timeLabel = "22:30"),
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

    suspend fun addHabit(habit: HabitItem) {
        val current = habits.first().toMutableList()
        current.add(0, habit)
        saveHabits(current)
    }

    private fun defaultHabits() = listOf(
        HabitItem("h1", "Тренировка", streak = 0),
        HabitItem("h2", "Чтение", streak = 0),
        HabitItem("h3", "Медитация", streak = 0),
        HabitItem("h4", "Ранний подъём", streak = 0),
        HabitItem("h5", "Без сахара", streak = 0),
        HabitItem("h6", "2 литра воды", streak = 0),
        HabitItem("h7", "Без соцсетей после 22:00", streak = 0),
    )

    // ---------------- PLANS ----------------

    val plans: Flow<List<PlanItem>> = store.stringFlow(Keys.PLANS).map { raw ->
        if (raw.isBlank()) defaultPlans() else runCatching { json.decodeFromString<List<PlanItem>>(raw) }
            .getOrDefault(defaultPlans())
    }

    suspend fun savePlans(items: List<PlanItem>) {
        store.putString(Keys.PLANS, json.encodeToString(items))
    }

    /** "Шаблоны" tab action: adds a template to "Мои планы" and seeds its tasks. */
    suspend fun applyTemplate(template: PlanTemplate) {
        val current = plans.first().toMutableList()
        if (current.none { it.id == template.id }) {
            current.add(
                0,
                PlanItem(
                    id = template.id,
                    title = template.title,
                    category = template.category,
                    progressPercent = 0,
                    totalTasks = template.taskTitles.size,
                    icon = template.icon,
                )
            )
            savePlans(current)
        }
        addTasksFromTitles(template.taskTitles, template.title)
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
        Achievement("a1", "Первый шаг", "Заверши свою первую задачу", false),
        Achievement("a2", "7 дней подряд", "Выполняй все задачи 7 дней подряд", false),
        Achievement("a3", "30 дней подряд", "Держи серию месяц без пропусков", false),
        Achievement("a4", "Ранняя пташка", "Просыпайся до 7:00 5 дней", false),
        Achievement("a5", "Читатель", "Прочитай 5 книг", false),
        Achievement("a6", "Без телефона", "Продержись 3 часа без телефона", false),
        Achievement("a7", "Спортсмен", "Заверши 10 тренировок", false),
        Achievement("a8", "Мастер фокуса", "Заверши 10 фокус-сессий", false),
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
    // Note: the Groq API key itself is baked in at build time (BuildConfig, see GroqApi.kt) —
    // it is intentionally NOT stored here or editable from Settings.

    val groqModel: Flow<String> = store.stringFlow(Keys.GROQ_MODEL, default = "llama-3.3-70b-versatile")
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
