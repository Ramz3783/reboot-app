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
)

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

@Serializable
data class WorkoutExercise(
    val id: String,
    val name: String,
    val reps: String,      // e.g. "15 повторений" or "30 секунд"
    val restSeconds: Int = 45,
    val done: Boolean = false,
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
 * The full taxonomy shown during onboarding. Each problem/goal maps to a concrete starter
 * task and/or habit, so the plan a person gets on day one actually reflects what they picked —
 * instead of everyone getting the same four generic tasks regardless of their answers.
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

    /** Starter tasks unlocked by a chosen PROBLEM (things that directly counter it). */
    fun tasksForProblem(problem: String): List<TaskItem> = when (problem) {
        "Прокрастинация" -> listOf(taskOf("Сделай самую сложную задачу первой", 60, "Дисциплина", "09:00"))
        "Нет мотивации" -> listOf(taskOf("Запиши 3 причины, зачем тебе цель", 30, "Психология", "08:00"))
        "Зависимость от телефона" -> listOf(taskOf("Без телефона 1 час", 50, "Дисциплина", "21:00"))
        "Плохие привычки" -> listOf(taskOf("Замени плохую привычку на полезную", 40, "Дисциплина", "18:00"))
        "Низкая уверенность" -> listOf(taskOf("Похвали себя за 1 маленькую победу", 25, "Психология", "22:00"))
        "Тревожность / Стресс" -> listOf(taskOf("5 минут дыхательной практики", 30, "Психология", "20:30"))
        "Лень" -> listOf(taskOf("10-минутное усилие без отговорок", 35, "Дисциплина", "10:00"))
        "Нерегулярный сон" -> listOf(taskOf("Ложись спать до 23:00", 40, "Здоровье", "23:00"))
        "Переедание" -> listOf(taskOf("Приготовь один здоровый приём пищи", 40, "Питание", "13:00"))
        "Отсутствие спорта" -> listOf(workoutTask())
        "Разбросанность в делах" -> listOf(taskOf("Составь список из 3 главных дел на день", 30, "Планирование", "08:30"))
        "Финансовый хаос" -> listOf(taskOf("Запиши все траты за сегодня", 25, "Финансы", "21:30"))
        else -> emptyList()
    }

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

    private fun taskOf(title: String, xp: Int, category: String, time: String) =
        TaskItem(id = "task_${title.hashCode()}", title = title, xpReward = xp, category = category, timeLabel = time)

    private fun workoutTask() = TaskItem(
        id = "task_workout_intro",
        title = "Тренировка",
        xpReward = 80,
        category = "Спорт",
        timeLabel = "07:30",
        workoutId = "beginner_full_body",
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

object WorkoutCatalog {
    val WORKOUTS: Map<String, WorkoutPlan> = mapOf(
        "beginner_full_body" to WorkoutPlan(
            id = "beginner_full_body",
            title = "Тренировка на всё тело",
            exercises = listOf(
                WorkoutExercise("e1", "Приседания", "15 повторений", 30),
                WorkoutExercise("e2", "Отжимания", "10 повторений", 40),
                WorkoutExercise("e3", "Планка", "30 секунд", 30),
                WorkoutExercise("e4", "Выпады", "12 на каждую ногу", 40),
                WorkoutExercise("e5", "Скручивания на пресс", "15 повторений", 30),
            )
        ),
        "cardio_burn" to WorkoutPlan(
            id = "cardio_burn",
            title = "Кардио-разгон",
            exercises = listOf(
                WorkoutExercise("c1", "Прыжки на месте", "40 секунд", 20),
                WorkoutExercise("c2", "Берпи", "8 повторений", 45),
                WorkoutExercise("c3", "Высокие колени", "40 секунд", 20),
                WorkoutExercise("c4", "Планка с касанием плеч", "30 секунд", 30),
            )
        ),
        "morning_stretch" to WorkoutPlan(
            id = "morning_stretch",
            title = "Утренняя растяжка",
            exercises = listOf(
                WorkoutExercise("m1", "Растяжка шеи", "30 секунд", 15),
                WorkoutExercise("m2", "Наклоны к полу", "30 секунд", 15),
                WorkoutExercise("m3", "Растяжка спины кошка-корова", "40 секунд", 15),
                WorkoutExercise("m4", "Растяжка ног сидя", "40 секунд", 15),
            )
        ),
    )

    fun forId(id: String?): WorkoutPlan? = WORKOUTS[id]
    fun randomWorkoutId(): String = WORKOUTS.keys.random()
}
