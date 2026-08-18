package com.reboot.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val birthDate: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val avatarUri: String? = null,
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNextLevel: Int = 2000,
    val streakDays: Int = 0,
    val coins: Int = 0,
    val problems: List<String> = emptyList(),
    val goals: List<String> = emptyList(),
    val isOnboarded: Boolean = false,
    val isLoggedIn: Boolean = false,
    // "Ударный режим" (streak mode) bookkeeping — dates stored as yyyy-MM-dd.
    val lastTaskResetDate: String = "",
    val lastStreakCreditDate: String = "",
    // Duolingo-style streak extras.
    val streakFreezes: Int = 1, // everyone starts with one free freeze
    val perfectWeekCount: Int = 0,
    val daysCompletedThisWeek: Int = 0,
    val weekAnchorDate: String = "", // Monday of the week we're currently counting
    val lastCelebratedMilestone: Int = 0,
    // "Восстановление серии": if a day was fully missed and no freeze was available, instead
    // of hard-resetting immediately, the person gets ONE day to clear double the normal task
    // count to restore the streak instead of losing it.
    val pendingStreakRepair: Boolean = false,
    val streakRepairTarget: Int = 0,
    val streakRepairProgress: Int = 0,
    val streakBeforeBreak: Int = 0,
    // Per-category "skill branch" XP/levels, e.g. {"Спорт": 340, "Дисциплина": 120}.
    val categoryXp: Map<String, Int> = emptyMap(),
    // Pending one-shot UI events the app should show once, then clear.
    val pendingMilestone: Int = 0,
    val pendingLevelUp: Boolean = false,
    val lastMorningBriefingDate: String = "",
    val lastEveningRecapDate: String = "",
    // Profile accent colors, unlockable with coins ("скины").
    val unlockedSkins: List<String> = listOf("violet"),
    val activeSkin: String = "violet",
    // Rolling log of recently completed task titles per day, used so the AI coach can spot
    // real patterns ("3 дня подряд пропускаешь тренировку") instead of guessing.
    val recentDayLogs: List<DayLog> = emptyList(),
)

@Serializable
data class DayLog(val date: String, val completedTitles: List<String>, val missedTitles: List<String>)

enum class VerificationType { NONE, TIMER, PHOTO, STEPS }

@Serializable
data class TaskItem(
    val id: String,
    val title: String,
    val description: String = "",
    val done: Boolean = false,
    val xpReward: Int = 50,
    val category: String = "General",
    val timeLabel: String = "",
    // If set, tapping this task opens a guided workout session (exercise list + timers)
    // instead of just toggling a checkbox.
    val workoutId: String? = null,
    // How the person actually proves they did it, instead of a bare tap.
    val verificationType: VerificationType = VerificationType.NONE,
    val durationMinutes: Int = 0, // used when verificationType == TIMER
    val proofPhotoPath: String? = null, // local file path once completed with a photo
    val isDailyChallenge: Boolean = false, // bonus task, 2x XP, rotates daily
)

@Serializable
data class HabitItem(
    val id: String,
    val title: String,
    val icon: String = "bolt",
    val streak: Int = 0,
    val completedToday: Boolean = false,
)

@Serializable
data class PlanItem(
    val id: String,
    val title: String,
    val category: String,
    val progressPercent: Int,
    val totalTasks: Int,
    val icon: String = "target",
)

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean,
    val icon: String = "trophy",
)

@Serializable
data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
)

enum class ExerciseAnimation { SQUAT, PUSHUP, PLANK, LUNGE, SITUP, JUMP, STRETCH, GENERIC }

@Serializable
data class WorkoutExercise(
    val id: String,
    val name: String,
    val reps: String,      // e.g. "15 повторений" or "30 секунд"
    val restSeconds: Int = 45,
    val done: Boolean = false,
    val animation: ExerciseAnimation = ExerciseAnimation.GENERIC,
)

@Serializable
data class WorkoutPlan(
    val id: String,
    val title: String,
    val exercises: List<WorkoutExercise>,
)

// Short, chat-style tone is enforced for every mode: this is a phone chat screen, replies
// should read like real texting, not essays.
private const val SHORT_STYLE =
    " Отвечай КОРОТКО: максимум 2-4 предложения, как в живой переписке, без длинных вступлений " +
        "и списков, если не попросили список отдельно. Пиши просто и по-человечески."

enum class MentorMode(val displayName: String, val subtitle: String, val systemPrompt: String) {
    MOTIVATOR(
        "Мотиватор",
        "Поддержка и мотивация каждый день",
        "Ты — тёплый, поддерживающий AI-наставник по имени REBOOT. Мотивируй пользователя, " +
            "хвали прогресс, давай мягкие и безопасные советы по привычкам и дисциплине, учитывая " +
            "его цели и проблемы, которые он указал при регистрации. Никогда не давай медицинских " +
            "советов, не оценивай тело пользователя, не предлагай опасных заданий." + SHORT_STYLE
    ),
    STRATEGIST(
        "Стратег",
        "Планы, цели и анализ",
        "Ты — рациональный AI-стратег REBOOT. Помогай выстраивать чёткие планы, разбивать цели " +
            "на шаги, анализировать прогресс, отталкиваясь от целей и проблем пользователя. " +
            "Отвечай структурно и по делу. Не давай медицинских советов." + SHORT_STYLE
    ),
    PROVOCATEUR(
        "Провокатор",
        "Жёсткая правда без фильтров",
        "Ты — прямой AI-наставник REBOOT, говорящий жёсткую правду без прикрас, но уважительно. " +
            "Дави на слабые места и оправдания пользователя, опираясь на его же цели. " +
            "Не оскорбляй пользователя лично, не унижай, не используй травлю и не давай опасных " +
            "советов. Жёсткость должна быть в тоне и требовательности, а не в оскорблениях." + SHORT_STYLE
    ),
    TURBO(
        "Турбо",
        "Максимальный разгон",
        "Ты — энергичный, быстрый AI-наставник REBOOT в режиме 'турбо'. Давай короткие, " +
            "конкретные и энергичные ответы-действия. Не давай опасных или экстремальных заданий." + SHORT_STYLE
    ),
    FRIEND(
        "Друг",
        "Просто поговорить",
        "Ты — дружелюбный AI-собеседник REBOOT. Общайся тепло, неформально, слушай пользователя. " +
            "Не давай медицинских или психиатрических диагнозов, при серьёзных переживаниях мягко " +
            "предложи обратиться к специалисту." + SHORT_STYLE
    ),
}

/**
 * The full taxonomy shown during onboarding. Each problem/goal maps to a POOL of possible
 * tasks/habits (not just one), so the app can rotate what it shows day to day instead of
 * repeating the exact same four things forever, and so tasks actually reflect what the
 * person picked instead of everyone getting an identical starter list.
 */
object OnboardingCatalog {

    val PROBLEMS = listOf(
        "Прокрастинация", "Нет мотивации", "Зависимость от телефона",
        "Плохие привычки", "Низкая уверенность", "Тревожность / Стресс",
        "Лень", "Нерегулярный сон", "Переедание", "Отсутствие спорта",
        "Разбросанность в делах", "Финансовый хаос",
    )

    val GOALS = listOf(
        "Тренировки", "Чтение", "Медитация", "Ранний подъём",
        "Правильное питание", "Обучение", "Дисциплина", "Фокус",
        "Больше воды", "Меньше соцсетей", "Ведение дневника", "Планирование дня",
    )

    /** Pool of possible starter tasks unlocked by a chosen PROBLEM — one is picked to start,
     * the rest are candidates the daily rotation can swap in later so it doesn't repeat. */
    fun taskPoolForProblem(problem: String): List<TaskItem> = when (problem) {
        "Прокрастинация" -> listOf(
            taskOf("Сделай самую сложную задачу первой", 60, "Дисциплина", "09:00", VerificationType.TIMER, 15),
            taskOf("Разбей одну большую задачу на 3 шага", 35, "Дисциплина", "09:30"),
            taskOf("Работай 25 минут без отвлечений", 50, "Дисциплина", "10:00", VerificationType.TIMER, 25),
        )
        "Нет мотивации" -> listOf(
            taskOf("Запиши 3 причины, зачем тебе цель", 30, "Психология", "08:00"),
            taskOf("Посмотри на свой прогресс за неделю", 25, "Психология", "20:00"),
            taskOf("Сделай одно маленькое действие к цели", 30, "Психология", "12:00"),
        )
        "Зависимость от телефона" -> listOf(
            taskOf("Без телефона 1 час", 50, "Дисциплина", "21:00", VerificationType.TIMER, 60),
            taskOf("Убери соцсети с главного экрана", 30, "Дисциплина", "18:00"),
            taskOf("Не бери телефон первые 20 минут утра", 40, "Дисциплина", "07:00", VerificationType.TIMER, 20),
        )
        "Плохие привычки" -> listOf(
            taskOf("Замени плохую привычку на полезную", 40, "Дисциплина", "18:00"),
            taskOf("Отследи, когда сработал триггер привычки", 30, "Психология", "любое время"),
        )
        "Низкая уверенность" -> listOf(
            taskOf("Похвали себя за 1 маленькую победу", 25, "Психология", "22:00"),
            taskOf("Сделай то, что откладывал из-за страха", 45, "Психология", "любое время"),
        )
        "Тревожность / Стресс" -> listOf(
            taskOf("5 минут дыхательной практики", 30, "Психология", "20:30", VerificationType.TIMER, 5),
            taskOf("Прогулка на свежем воздухе", 35, "Здоровье", "любое время", VerificationType.TIMER, 15),
        )
        "Лень" -> listOf(
            taskOf("10-минутное усилие без отговорок", 35, "Дисциплина", "10:00", VerificationType.TIMER, 10),
            taskOf("Сделай дело сразу, как подумал о нём", 30, "Дисциплина", "любое время"),
        )
        "Нерегулярный сон" -> listOf(
            taskOf("Ложись спать до 23:00", 40, "Здоровье", "23:00"),
            taskOf("Без экранов за 30 минут до сна", 35, "Здоровье", "22:30", VerificationType.TIMER, 30),
        )
        "Переедание" -> listOf(
            taskOf("Приготовь один здоровый приём пищи", 40, "Питание", "13:00"),
            taskOf("Ешь медленно, без телефона и экрана", 25, "Питание", "любое время"),
        )
        "Отсутствие спорта" -> listOf(workoutTask(), workoutTask("cardio_burn", "Кардио-тренировка"))
        "Разбросанность в делах" -> listOf(
            taskOf("Составь список из 3 главных дел на день", 30, "Планирование", "08:30"),
            taskOf("Разбери один беспорядок (стол/папка/шкаф)", 30, "Планирование", "любое время"),
        )
        "Финансовый хаос" -> listOf(
            taskOf("Запиши все траты за сегодня", 25, "Финансы", "21:30"),
            taskOf("Проверь одну ненужную подписку", 25, "Финансы", "любое время"),
        )
        else -> emptyList()
    }

    /** Backwards-compatible single pick (first item from the pool) used at onboarding time. */
    fun tasksForProblem(problem: String): List<TaskItem> = taskPoolForProblem(problem).take(1)

    /** Starter habits unlocked by a chosen GOAL. */
    fun habitForGoal(goal: String): HabitItem? = when (goal) {
        "Тренировки" -> HabitItem("goal_workout", "Тренировки")
        "Чтение" -> HabitItem("goal_reading", "Чтение")
        "Медитация" -> HabitItem("goal_meditation", "Медитация")
        "Ранний подъём" -> HabitItem("goal_early", "Ранний подъём")
        "Правильное питание" -> HabitItem("goal_nutrition", "Правильное питание")
        "Обучение" -> HabitItem("goal_learning", "Обучение 20 минут")
        "Дисциплина" -> HabitItem("goal_discipline", "Час дисциплины")
        "Фокус" -> HabitItem("goal_focus", "Фокус-сессия")
        "Больше воды" -> HabitItem("goal_water", "2 литра воды")
        "Меньше соцсетей" -> HabitItem("goal_social", "Меньше соцсетей")
        "Ведение дневника" -> HabitItem("goal_journal", "Дневник")
        "Планирование дня" -> HabitItem("goal_planning", "План на день")
        else -> null
    }

    /** Pool for "случайный челлендж дня" — bonus task, always worth 2x its listed XP. */
    val DAILY_CHALLENGE_POOL = listOf(
        taskOf("Сделай 50 приседаний за день", 40, "Спорт", "в любое время"),
        taskOf("Напиши что-то, чем гордишься за этот месяц", 30, "Психология", "в любое время"),
        taskOf("Позвони близкому человеку просто так", 25, "Психология", "в любое время"),
        taskOf("Проведи час полностью в тишине, без экранов", 45, "Дисциплина", "в любое время", VerificationType.TIMER, 60),
        taskOf("Убери свою комнату/стол до блеска", 35, "Дисциплина", "в любое время"),
        taskOf("Попробуй холодный душ", 35, "Здоровье", "в любое время"),
        taskOf("Прочитай 30 страниц одним махом", 40, "Развитие", "в любое время", VerificationType.TIMER, 25),
    )

    fun randomDailyChallenge(): TaskItem {
        val picked = DAILY_CHALLENGE_POOL.random()
        return picked.copy(
            id = "daily_${System.currentTimeMillis()}",
            xpReward = picked.xpReward * 2,
            isDailyChallenge = true,
        )
    }

    private fun taskOf(
        title: String,
        xp: Int,
        category: String,
        time: String,
        verification: VerificationType = VerificationType.NONE,
        durationMinutes: Int = 0,
    ) = TaskItem(
        id = "task_${title.hashCode()}",
        title = title,
        xpReward = xp,
        category = category,
        timeLabel = time,
        verificationType = verification,
        durationMinutes = durationMinutes,
    )

    private fun workoutTask(workoutId: String = "beginner_full_body", title: String = "Тренировка") = TaskItem(
        id = "task_workout_${workoutId}",
        title = title,
        xpReward = 80,
        category = "Спорт",
        timeLabel = "07:30",
        workoutId = workoutId,
    )
}

@Serializable
data class PlanTemplate(
    val id: String,
    val title: String,
    val category: String,
    val icon: String = "target",
    val taskTitles: List<String>,
)

object TemplateCatalog {
    val TEMPLATES = listOf(
        PlanTemplate(
            "tpl_sport", "Спорт с нуля", "12 задач", "dumbbell",
            listOf("Тренировка 20 минут", "10 000 шагов", "Растяжка перед сном", "Пей воду каждый час")
        ),
        PlanTemplate(
            "tpl_focus", "Глубокий фокус", "10 задач", "focus",
            listOf("90 минут без телефона", "Один Pomodoro-блок", "Убери отвлекающие уведомления")
        ),
        PlanTemplate(
            "tpl_sleep", "Здоровый сон", "8 задач", "moon",
            listOf("Ложись до 23:00", "Без экранов за час до сна", "Проветри комнату")
        ),
        PlanTemplate(
            "tpl_mind", "Ясность ума", "9 задач", "brain",
            listOf("10 минут медитации", "Дневник благодарности", "5 минут тишины утром")
        ),
        PlanTemplate(
            "tpl_money", "Финансовый порядок", "7 задач", "coin",
            listOf("Запиши все траты", "Отложи 10% от дохода", "Проверь подписки")
        ),
        PlanTemplate(
            "tpl_social", "Меньше соцсетей", "6 задач", "phone-off",
            listOf("Лимит соцсетей 30 минут", "Убери приложения с главного экрана", "Час без телефона")
        ),
    )
}

object SkinCatalog {
    data class Skin(val id: String, val label: String, val color: Long, val cost: Int)

    val SKINS = listOf(
        Skin("violet", "Фиолетовый", 0xFF8B5CF6, 0),
        Skin("cyan", "Бирюзовый", 0xFF00D2D3, 150),
        Skin("gold", "Золотой", 0xFFFFC857, 250),
        Skin("pink", "Розовый", 0xFFFF6B9D, 250),
        Skin("green", "Изумрудный", 0xFF35E58A, 350),
        Skin("red", "Огненный", 0xFFFF4D67, 450),
    )

    fun byId(id: String): Skin = SKINS.firstOrNull { it.id == id } ?: SKINS.first()
}

object WorkoutCatalog {
    val WORKOUTS: Map<String, WorkoutPlan> = mapOf(
        "beginner_full_body" to WorkoutPlan(
            id = "beginner_full_body",
            title = "Тренировка на всё тело",
            exercises = listOf(
                WorkoutExercise("e1", "Приседания", "15 повторений", 30, animation = ExerciseAnimation.SQUAT),
                WorkoutExercise("e2", "Отжимания", "10 повторений", 40, animation = ExerciseAnimation.PUSHUP),
                WorkoutExercise("e3", "Планка", "30 секунд", 30, animation = ExerciseAnimation.PLANK),
                WorkoutExercise("e4", "Выпады", "12 на каждую ногу", 40, animation = ExerciseAnimation.LUNGE),
                WorkoutExercise("e5", "Скручивания на пресс", "15 повторений", 30, animation = ExerciseAnimation.SITUP),
            )
        ),
        "cardio_burn" to WorkoutPlan(
            id = "cardio_burn",
            title = "Кардио-разгон",
            exercises = listOf(
                WorkoutExercise("c1", "Прыжки на месте", "40 секунд", 20, animation = ExerciseAnimation.JUMP),
                WorkoutExercise("c2", "Берпи", "8 повторений", 45, animation = ExerciseAnimation.SQUAT),
                WorkoutExercise("c3", "Высокие колени", "40 секунд", 20, animation = ExerciseAnimation.JUMP),
                WorkoutExercise("c4", "Планка с касанием плеч", "30 секунд", 30, animation = ExerciseAnimation.PLANK),
            )
        ),
        "morning_stretch" to WorkoutPlan(
            id = "morning_stretch",
            title = "Утренняя растяжка",
            exercises = listOf(
                WorkoutExercise("m1", "Растяжка шеи", "30 секунд", 15, animation = ExerciseAnimation.STRETCH),
                WorkoutExercise("m2", "Наклоны к полу", "30 секунд", 15, animation = ExerciseAnimation.STRETCH),
                WorkoutExercise("m3", "Растяжка спины кошка-корова", "40 секунд", 15, animation = ExerciseAnimation.STRETCH),
                WorkoutExercise("m4", "Растяжка ног сидя", "40 секунд", 15, animation = ExerciseAnimation.STRETCH),
            )
        ),
    )

    fun forId(id: String?): WorkoutPlan? = WORKOUTS[id]
    fun randomWorkoutId(): String = WORKOUTS.keys.random()
}
