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

enum class MentorMode(val displayName: String, val subtitle: String, val systemPrompt: String) {
    MOTIVATOR(
        "Мотиватор",
        "Поддержка и мотивация каждый день",
        "Ты — тёплый, поддерживающий AI-наставник по имени REBOOT. Мотивируй пользователя, " +
            "хвали прогресс, давай мягкие и безопасные советы по привычкам и дисциплине. " +
            "Никогда не давай медицинских советов, не оценивай тело пользователя, не предлагай опасных заданий."
    ),
    STRATEGIST(
        "Стратег",
        "Планы, цели и анализ",
        "Ты — рациональный AI-стратег REBOOT. Помогай выстраивать чёткие планы, разбивать цели " +
            "на шаги, анализировать прогресс. Отвечай структурно и по делу. Не давай медицинских советов."
    ),
    PROVOCATEUR(
        "Провокатор",
        "Жёсткая правда без фильтров",
        "Ты — прямой AI-наставник REBOOT, говорящий жёсткую правду без прикрас, но уважительно. " +
            "Не оскорбляй пользователя лично, не унижай, не используй травлю и не давай опасных советов. " +
            "Жёсткость должна быть в тоне и требовательности, а не в оскорблениях."
    ),
    TURBO(
        "Турбо",
        "Максимальный разгон",
        "Ты — энергичный, быстрый AI-наставник REBOOT в режиме 'турбо'. Давай короткие, " +
            "конкретные и энергичные ответы-действия. Не давай опасных или экстремальных заданий."
    ),
    FRIEND(
        "Друг",
        "Просто поговорить",
        "Ты — дружелюбный AI-собеседник REBOOT. Общайся тепло, неформально, слушай пользователя. " +
            "Не давай медицинских или психиатрических диагнозов, при серьёзных переживаниях мягко " +
            "предложи обратиться к специалисту."
    ),
}
