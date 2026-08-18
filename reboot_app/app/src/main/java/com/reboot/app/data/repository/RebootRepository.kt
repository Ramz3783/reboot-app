package com.reboot.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.reboot.app.data.local.Keys
import com.reboot.app.data.local.PrefsStore
import com.reboot.app.data.model.Achievement
import com.reboot.app.data.model.ChatMessage
import com.reboot.app.data.model.DayLog
import com.reboot.app.data.model.HabitItem
import com.reboot.app.data.model.MentorMode
import com.reboot.app.data.model.OnboardingCatalog
import com.reboot.app.data.model.PlanItem
import com.reboot.app.data.model.PlanTemplate
import com.reboot.app.data.model.SkinCatalog
import com.reboot.app.data.model.TaskItem
import com.reboot.app.data.model.UserProfile
import com.reboot.app.data.model.VerificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

/** Streak lengths (in days) that trigger a celebration screen, Duolingo-style. */
val STREAK_MILESTONES = listOf(3, 7, 14, 30, 60, 100, 180, 365)
private const val PERFECT_WEEK_BONUS_XP = 200

/**
 * Single source of truth for app state. Everything is persisted locally on-device via
 * Jetpack DataStore, so the logged-in user, their tasks, habits, XP, streak, and chat
 * history all survive app restarts (i.e. "the app remembers the user").
 */
class RebootRepository(context: Context) {

    private val store = PrefsStore(context)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private fun today(): String = LocalDate.now().toString()
    private fun mondayOfThisWeek(): String =
        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()

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

    /** Overall XP/level, PLUS the matching per-category "skill branch" XP. */
    suspend fun addXp(amount: Int, category: String? = null) {
        updateProfile { p ->
            var xp = p.xp + amount
            var level = p.level
            var cap = p.xpToNextLevel
            var coins = p.coins + (amount / 5)
            var leveledUp = false
            while (xp >= cap) {
                xp -= cap
                level += 1
                cap = (cap * 1.15).toInt()
                leveledUp = true
            }
            val newCategoryXp = if (category != null) {
                p.categoryXp.toMutableMap().apply { this[category] = (this[category] ?: 0) + amount }
            } else p.categoryXp
            p.copy(
                xp = xp, level = level, xpToNextLevel = cap, coins = coins,
                categoryXp = newCategoryXp,
                pendingLevelUp = p.pendingLevelUp || leveledUp,
            )
        }
    }

    suspend fun clearPendingLevelUp() = updateProfile { it.copy(pendingLevelUp = false) }
    suspend fun clearPendingMilestone() = updateProfile { it.copy(pendingMilestone = 0, lastCelebratedMilestone = maxOf(it.lastCelebratedMilestone, it.pendingMilestone)) }

    suspend fun buyStreakFreeze(cost: Int = 100) {
        val p = getUserProfileOnce()
        if (p.coins >= cost) updateProfile { it.copy(coins = it.coins - cost, streakFreezes = it.streakFreezes + 1) }
    }

    suspend fun buySkin(skinId: String) {
        val p = getUserProfileOnce()
        val skin = SkinCatalog.byId(skinId)
        if (skinId in p.unlockedSkins) return
        if (p.coins >= skin.cost) {
            updateProfile { it.copy(coins = it.coins - skin.cost, unlockedSkins = it.unlockedSkins + skinId) }
        }
    }

    suspend fun setActiveSkin(skinId: String) {
        val p = getUserProfileOnce()
        if (skinId in p.unlockedSkins) updateProfile { it.copy(activeSkin = skinId) }
    }

    suspend fun logout() {
        updateProfile { it.copy(isLoggedIn = false) }
    }

    // ---------------- DAILY MAINTENANCE ("ударный режим" / streak mode) ----------------

    /**
     * Runs once whenever the app comes to the foreground (called from Splash). Handles:
     *  1. Streak freeze: if a full day passed with yesterday's tasks incomplete, a banked
     *     freeze auto-absorbs the miss.
     *  2. Streak repair: if no freeze is available, the streak isn't reset immediately —
     *     the person gets today to clear DOUBLE the normal task count to restore it.
     *     If yesterday's repair window was not fulfilled, THAT'S when it actually resets.
     *  3. Perfect-week bonus: full 7/7 days completed in the week that just ended awards bonus XP.
     *  4. Milestone detection (3/7/14/30/...) -> one-shot celebration flag.
     *  5. Daily task reset + light rotation + level-scaled difficulty + a fresh daily challenge.
     *  6. Logs yesterday's completed/missed task titles so the AI coach can spot real patterns.
     */
    suspend fun runDailyMaintenance() {
        val profile = getUserProfileOnce()
        val todayStr = today()
        if (profile.lastTaskResetDate == todayStr) return // already handled today

        val yesterdayTasks = tasks.first()
        logYesterday(profile, yesterdayTasks, todayStr)

        val daysSinceCredit = if (profile.lastStreakCreditDate.isBlank()) {
            null
        } else {
            ChronoUnit.DAYS.between(LocalDate.parse(profile.lastStreakCreditDate), LocalDate.parse(todayStr))
        }
        val aboutToBreak = daysSinceCredit != null && daysSinceCredit >= 2

        var newStreak = profile.streakDays
        var newFreezes = profile.streakFreezes
        var repairPending = profile.pendingStreakRepair
        var repairTarget = profile.streakRepairTarget
        var repairProgress = 0
        var streakBeforeBreak = profile.streakBeforeBreak

        if (profile.pendingStreakRepair) {
            // Yesterday's repair window already expired without being fulfilled -> real reset now.
            newStreak = 0
            repairPending = false
            repairTarget = 0
            streakBeforeBreak = 0
        } else if (aboutToBreak) {
            if (newFreezes > 0) {
                newFreezes -= 1 // freeze absorbs the miss silently, streak survives untouched
            } else {
                // Don't hard-reset yet — offer one make-up day at double the normal task count.
                val normalCount = yesterdayTasks.count { !it.isDailyChallenge }.coerceAtLeast(2)
                repairPending = true
                repairTarget = normalCount * 2
                streakBeforeBreak = profile.streakDays
                // newStreak intentionally left as-is (still shown) until repair window resolves
            }
        }

        val currentWeekAnchor = mondayOfThisWeek()
        val isNewWeek = profile.weekAnchorDate != currentWeekAnchor
        val perfectWeekJustCompleted = isNewWeek && profile.daysCompletedThisWeek >= 7

        val reachedMilestone = STREAK_MILESTONES.lastOrNull { it <= newStreak && it > profile.lastCelebratedMilestone } ?: 0

        updateProfile {
            it.copy(
                lastTaskResetDate = todayStr,
                streakDays = newStreak,
                streakFreezes = newFreezes,
                pendingStreakRepair = repairPending,
                streakRepairTarget = repairTarget,
                streakRepairProgress = repairProgress,
                streakBeforeBreak = streakBeforeBreak,
                weekAnchorDate = currentWeekAnchor,
                daysCompletedThisWeek = if (isNewWeek) 0 else it.daysCompletedThisWeek,
                perfectWeekCount = if (perfectWeekJustCompleted) it.perfectWeekCount + 1 else it.perfectWeekCount,
                pendingMilestone = if (reachedMilestone > 0) reachedMilestone else it.pendingMilestone,
            )
        }

        if (perfectWeekJustCompleted) {
            addXp(PERFECT_WEEK_BONUS_XP) // real bonus, not just a counter
        }

        rotateAndResetTasks(profile)
    }

    private suspend fun logYesterday(profile: UserProfile, yesterdayTasks: List<TaskItem>, todayStr: String) {
        if (profile.lastTaskResetDate.isBlank() || yesterdayTasks.isEmpty()) return
        val real = yesterdayTasks.filterNot { it.isDailyChallenge }
        val entry = DayLog(
            date = profile.lastTaskResetDate,
            completedTitles = real.filter { it.done }.map { it.title },
            missedTitles = real.filterNot { it.done }.map { it.title },
        )
        updateProfile { it.copy(recentDayLogs = (it.recentDayLogs + entry).takeLast(14)) }
    }

    /** Fresh day: un-check everything, lightly rotate for variety, scale difficulty with
     * level (both XP AND timer duration), and guarantee a daily challenge shows up every
     * few days (not literally every single day, so it stays a "surprise"). */
    private suspend fun rotateAndResetTasks(profile: UserProfile) {
        var current = tasks.first().map { it.copy(done = false) }.filterNot { it.isDailyChallenge }

        // Difficulty scaling with level: both reward AND timer duration grow, so e.g.
        // "no phone" starts at 30 min and stretches toward 2 hours as the person levels up.
        val xpScale = 1f + (profile.level - 1) * 0.06f
        val durationScale = 1f + (profile.level - 1) * 0.12f
        current = current.map { t ->
            val scaledXp = (t.xpReward * xpScale).toInt()
            if (t.verificationType == VerificationType.TIMER) {
                val scaledMinutes = (t.durationMinutes * durationScale).toInt().coerceAtMost(120)
                t.copy(xpReward = scaledXp, durationMinutes = scaledMinutes)
            } else {
                t.copy(xpReward = scaledXp)
            }
        }

        // Light rotation: every few days, swap one non-workout task for a fresh pool pick
        // tied to the person's own problems, so it's not the exact same list forever.
        if (profile.problems.isNotEmpty() && current.size > 1 && LocalDate.now().dayOfYear % 3 == 0) {
            val swappable = current.filter { it.workoutId == null }
            if (swappable.isNotEmpty()) {
                val toReplace = swappable.random()
                val pool = profile.problems.flatMap { OnboardingCatalog.taskPoolForProblem(it) }
                    .filter { it.title != toReplace.title }
                if (pool.isNotEmpty()) {
                    val replacement = pool.random()
                    current = current.map { if (it.id == toReplace.id) replacement.copy(id = "rot_${replacement.id}_${System.currentTimeMillis()}") else it }
                }
            }
        }

        // "Случайный вызов дня": a bonus x2-XP task every ~3 days, not literally daily.
        val challenge = if (LocalDate.now().dayOfYear % 3 == 0) listOf(OnboardingCatalog.randomDailyChallenge()) else emptyList()
        saveTasks(current + challenge)

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

        saveTasks((if (problemTasks.isNotEmpty()) problemTasks else defaultTasks()) + OnboardingCatalog.randomDailyChallenge())
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

    /** Simple tap-to-complete path, used for NONE-verification tasks. */
    suspend fun toggleTask(id: String) {
        val current = tasks.first().toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val task = current[idx]
            val nowDone = !task.done
            current[idx] = task.copy(done = nowDone)
            saveTasks(current)
            if (nowDone) {
                addXp(task.xpReward, task.category)
                onTaskCompleted(current)
            }
        }
    }

    /** Completion path for TIMER-verified tasks: only call this once the timer actually ran out. */
    suspend fun completeTaskWithTimer(id: String) = toggleTaskDoneDirect(id)

    /** Completion path for PHOTO-verified tasks: stores the local proof photo path. */
    suspend fun completeTaskWithPhoto(id: String, photoPath: String) {
        val current = tasks.first().toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx >= 0 && !current[idx].done) {
            val task = current[idx].copy(done = true, proofPhotoPath = photoPath)
            current[idx] = task
            saveTasks(current)
            addXp(task.xpReward, task.category)
            onTaskCompleted(current)
        }
    }

    private suspend fun toggleTaskDoneDirect(id: String) {
        val current = tasks.first().toMutableList()
        val idx = current.indexOfFirst { it.id == id }
        if (idx >= 0 && !current[idx].done) {
            val task = current[idx].copy(done = true)
            current[idx] = task
            saveTasks(current)
            addXp(task.xpReward, task.category)
            onTaskCompleted(current)
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

    /** Runs after ANY task is marked done. Handles two separate things:
     *  1. Streak-repair progress: if a repair is pending, count this toward the double target;
     *     once reached, the streak is restored (+1) and the repair clears.
     *  2. Normal streak credit once all of today's (non-bonus) tasks are complete. */
    private suspend fun onTaskCompleted(current: List<TaskItem>) {
        val profile = getUserProfileOnce()
        val todayStr = today()

        if (profile.pendingStreakRepair) {
            val doneToday = current.count { it.done && !it.isDailyChallenge }
            if (doneToday >= profile.streakRepairTarget) {
                updateProfile {
                    it.copy(
                        streakDays = it.streakBeforeBreak + 1,
                        lastStreakCreditDate = todayStr,
                        pendingStreakRepair = false,
                        streakRepairTarget = 0,
                        streakRepairProgress = 0,
                        streakBeforeBreak = 0,
                    )
                }
            } else {
                updateProfile { it.copy(streakRepairProgress = doneToday) }
            }
            return
        }

        val required = current.filterNot { it.isDailyChallenge }
        if (required.isEmpty() || !required.all { it.done }) return
        if (profile.lastStreakCreditDate == todayStr) return // already credited today, don't double count
        val reachedMilestone = STREAK_MILESTONES.lastOrNull {
            it <= profile.streakDays + 1 && it > profile.lastCelebratedMilestone
        } ?: 0
        updateProfile {
            it.copy(
                streakDays = it.streakDays + 1,
                lastStreakCreditDate = todayStr,
                daysCompletedThisWeek = it.daysCompletedThisWeek + 1,
                pendingMilestone = if (reachedMilestone > 0) reachedMilestone else it.pendingMilestone,
            )
        }
    }

    private fun defaultTasks() = listOf(
        TaskItem("t1", "Тренировка", xpReward = 80, category = "Спорт", timeLabel = "07:30", workoutId = "beginner_full_body"),
        TaskItem("t2", "Прочитать 20 страниц", xpReward = 40, category = "Развитие", timeLabel = "20:00", verificationType = VerificationType.TIMER, durationMinutes = 20),
        TaskItem("t3", "Медитация", xpReward = 30, category = "Психология", timeLabel = "21:00", verificationType = VerificationType.TIMER, durationMinutes = 10),
        TaskItem("t4", "Без телефона 1 час", xpReward = 50, category = "Дисциплина", timeLabel = "22:00", verificationType = VerificationType.TIMER, durationMinutes = 30),
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
    // Unlocked automatically based on real stats, not just static flags.

    val achievements: Flow<List<Achievement>> = store.stringFlow(Keys.ACHIEVEMENTS).map { raw ->
        if (raw.isBlank()) defaultAchievements() else runCatching { json.decodeFromString<List<Achievement>>(raw) }
            .getOrDefault(defaultAchievements())
    }

    /** Recomputes unlock state from the current profile/task stats. Call after XP/streak changes. */
    suspend fun refreshAchievements() {
        val profile = getUserProfileOnce()
        val allTasks = tasks.first()
        val completedWorkouts = allTasks.count { it.workoutId != null && it.done }
        val current = achievements.first().map { a ->
            val unlocked = when (a.id) {
                "a1" -> profile.xp > 0 || profile.level > 1
                "a2" -> profile.streakDays >= 7
                "a3" -> profile.streakDays >= 30
                "a7" -> completedWorkouts >= 10
                else -> a.unlocked
            }
            a.copy(unlocked = unlocked)
        }
        store.putString(Keys.ACHIEVEMENTS, json.encodeToString(current))
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

    // ---------------- PROACTIVE AI (shown right on the Home screen, not buried in chat) ----------------

    /** A short note the coach wants to show on the Home screen right now, or null if nothing
     * is due. Persisted so it survives navigation/recomposition until dismissed. */
    val coachNote: Flow<String?> = store.stringFlow(Keys.COACH_NOTE).map { it.ifBlank { null } }

    suspend fun setCoachNote(text: String) = store.putString(Keys.COACH_NOTE, text)
    suspend fun clearCoachNote() = store.putString(Keys.COACH_NOTE, "")

    suspend fun shouldShowMorningBriefing(): Boolean = getUserProfileOnce().lastMorningBriefingDate != today()
    suspend fun markMorningBriefingShown() = updateProfile { it.copy(lastMorningBriefingDate = today()) }

    suspend fun shouldShowEveningRecap(): Boolean {
        val p = getUserProfileOnce()
        if (p.lastEveningRecapDate == today()) return false
        return LocalTime.now().hour >= 17
    }

    suspend fun markEveningRecapShown() = updateProfile { it.copy(lastEveningRecapDate = today()) }

    /** Simple, honest pattern detection over the last two weeks' logs: a task title that was
     * missed 3+ days running. Used to give the AI real signal instead of guessing. */
    suspend fun detectSkipPattern(): String? {
        val logs = getUserProfileOnce().recentDayLogs
        if (logs.size < 3) return null
        val lastThree = logs.takeLast(3)
        val allTitles = lastThree.flatMap { it.missedTitles + it.completedTitles }.distinct()
        for (title in allTitles) {
            if (lastThree.all { title in it.missedTitles }) return title
        }
        return null
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
