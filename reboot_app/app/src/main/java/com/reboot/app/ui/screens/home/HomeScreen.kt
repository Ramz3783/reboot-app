package com.reboot.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.data.model.TaskItem
import com.reboot.app.data.model.UserProfile
import com.reboot.app.data.model.VerificationType
import com.reboot.app.ui.theme.*

@Composable
fun HomeScreen(
    profile: UserProfile,
    tasks: List<TaskItem>,
    coachNote: String?,
    onDismissCoachNote: () -> Unit,
    onToggleTask: (String) -> Unit,
    onOpenWorkout: (String) -> Unit,
    onOpenTimerVerification: (String) -> Unit,
    onOpenPhotoVerification: (String) -> Unit,
    onSeeAllTasks: () -> Unit,
) {
    val regularTasks = tasks.filterNot { it.isDailyChallenge }
    val challenge = tasks.firstOrNull { it.isDailyChallenge }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScrollCompat()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("REBOOT", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Icon(Icons.Filled.Menu, null, tint = TextSecondary)
            }
            Spacer(Modifier.height(16.dp))

            if (coachNote != null) {
                CoachNoteBanner(coachNote, onDismissCoachNote)
                Spacer(Modifier.height(16.dp))
            }

            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(44.dp).clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(AccentPurple, AccentCyan)))
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Прогресс за сегодня", color = TextSecondary, fontSize = 13.sp)
                        }
                        Text("${progressPercent(regularTasks)}%", color = AccentGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("Уровень ${profile.level}", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("${profile.xp} / ${profile.xpToNextLevel} XP", color = TextTertiary, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    val fraction = (profile.xp.toFloat() / profile.xpToNextLevel.toFloat()).coerceIn(0f, 1f)
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(CardLight)) {
                        Box(
                            Modifier.fillMaxWidth(fraction).height(8.dp).clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(AccentPurple, AccentViolet)))
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.LocalFireDepartment, null,
                                tint = streakColor(profile.streakDays),
                                modifier = Modifier.size(streakIconSize(profile.streakDays))
                            )
                            Spacer(Modifier.width(4.dp))
                            Column {
                                Text("${profile.streakDays} дней", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    if (profile.streakFreezes > 0) "❄ заморозок: ${profile.streakFreezes}" else "серия",
                                    color = TextTertiary, fontSize = 10.sp
                                )
                            }
                        }
                        StatMini("Задачи", "${regularTasks.count { it.done }} / ${regularTasks.size}")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.MonetizationOn, null, tint = AccentGold, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${profile.coins}", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    WeekDots(daysCompleted = profile.daysCompletedThisWeek)
                }
            }

            if (profile.pendingStreakRepair) {
                Spacer(Modifier.height(16.dp))
                StreakRepairCard(profile.streakRepairProgress, profile.streakRepairTarget)
            }

            if (challenge != null) {
                Spacer(Modifier.height(16.dp))
                DailyChallengeCard(challenge) {
                    routeTaskTap(challenge, onToggleTask, onOpenWorkout, onOpenTimerVerification, onOpenPhotoVerification)
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Сегодняшние задачи", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text("Сегодня", color = AccentViolet, fontSize = 13.sp, modifier = Modifier.clickableNoRipple(onSeeAllTasks))
            }
            Spacer(Modifier.height(12.dp))
            if (regularTasks.isEmpty()) {
                Text(
                    "Пока нет задач — они появятся из выбранных тобой целей, или добавь свою через +",
                    color = TextTertiary, fontSize = 13.sp
                )
            }
            regularTasks.forEach { task ->
                TaskRow(task) {
                    routeTaskTap(task, onToggleTask, onOpenWorkout, onOpenTimerVerification, onOpenPhotoVerification)
                }
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(90.dp))
        }
    }
}

private fun routeTaskTap(
    task: TaskItem,
    onToggleTask: (String) -> Unit,
    onOpenWorkout: (String) -> Unit,
    onOpenTimerVerification: (String) -> Unit,
    onOpenPhotoVerification: (String) -> Unit,
) {
    if (task.done) return
    when {
        task.workoutId != null -> onOpenWorkout(task.workoutId)
        task.verificationType == VerificationType.TIMER -> onOpenTimerVerification(task.id)
        task.verificationType == VerificationType.PHOTO -> onOpenPhotoVerification(task.id)
        else -> onToggleTask(task.id)
    }
}

private fun progressPercent(tasks: List<TaskItem>): Int {
    if (tasks.isEmpty()) return 0
    return (tasks.count { it.done } * 100) / tasks.size
}

/** Duolingo-style: the flame gets a hotter color as the streak grows. */
fun streakColor(days: Int): Color = when {
    days >= 100 -> AccentPurple
    days >= 30 -> AccentPink
    days >= 7 -> AccentGold
    else -> AccentCyan
}

/** ...and it visibly grows too, not just changes color. */
fun streakIconSize(days: Int): androidx.compose.ui.unit.Dp = when {
    days >= 100 -> 28.dp
    days >= 30 -> 24.dp
    days >= 7 -> 20.dp
    else -> 18.dp
}

@Composable
private fun CoachNoteBanner(text: String, onDismiss: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(AccentPurple.copy(alpha = 0.22f), AccentCyan.copy(alpha = 0.16f))))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(Icons.Filled.AutoAwesome, null, tint = AccentCyan, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("AI-наставник", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(text, color = TextPrimary, fontSize = 13.sp)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.Close, null, tint = TextTertiary, modifier = Modifier.size(18.dp).clickableNoRipple(onDismiss))
        }
    }
}

@Composable
private fun StreakRepairCard(progress: Int, target: Int) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AccentRed.copy(alpha = 0.14f))
            .padding(16.dp)
    ) {
        Column {
            Text("⚠ Серия под угрозой", color = AccentRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Выполни $target задач сегодня (двойная норма), чтобы восстановить серию вместо обнуления",
                color = TextSecondary, fontSize = 12.sp
            )
            Spacer(Modifier.height(10.dp))
            val fraction = if (target > 0) (progress.toFloat() / target).coerceIn(0f, 1f) else 0f
            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(CardLight)) {
                Box(Modifier.fillMaxWidth(fraction).height(6.dp).clip(RoundedCornerShape(3.dp)).background(AccentRed))
            }
            Spacer(Modifier.height(4.dp))
            Text("$progress / $target", color = TextTertiary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun WeekDots(daysCompleted: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(7) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (i < daysCompleted) AccentGreen else CardLight)
            )
        }
    }
}

@Composable
private fun DailyChallengeCard(task: TaskItem, onTap: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(AccentGold.copy(alpha = 0.18f), AccentPink.copy(alpha = 0.18f))))
            .clickableNoRipple(onTap)
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎲 Челлендж дня", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text("x2 XP", color = TextTertiary, fontSize = 11.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                task.title,
                color = if (task.done) TextTertiary else TextPrimary,
                fontSize = 15.sp, fontWeight = FontWeight.SemiBold
            )
            if (task.done) {
                Spacer(Modifier.height(4.dp))
                Text("Выполнено ✓", color = AccentGreen, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun StatMini(label: String, value: String) {
    Column {
        Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(label, color = TextTertiary, fontSize = 11.sp)
    }
}

@Composable
fun TaskRow(task: TaskItem, onTap: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .clickableNoRipple(onTap)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = when {
            task.done -> Icons.Filled.CheckCircle
            task.workoutId != null -> Icons.Filled.FitnessCenter
            task.verificationType == VerificationType.TIMER -> Icons.Filled.Timer
            task.verificationType == VerificationType.PHOTO -> Icons.Filled.CameraAlt
            else -> Icons.Filled.RadioButtonUnchecked
        }
        val tint = when {
            task.done -> AccentGreen
            task.workoutId != null || task.verificationType != VerificationType.NONE -> AccentCyan
            else -> TextTertiary
        }
        Icon(icon, null, tint = tint)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                task.title,
                color = if (task.done) TextTertiary else TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            val hint = when {
                task.done -> null
                task.workoutId != null -> "Нажми, чтобы начать тренировку"
                task.verificationType == VerificationType.TIMER -> "Таймер ${task.durationMinutes} мин — нельзя просто отметить"
                task.verificationType == VerificationType.PHOTO -> "Нужно фото-доказательство"
                else -> null
            }
            val subtitle = listOfNotNull(task.timeLabel.takeIf { it.isNotBlank() }, hint).joinToString(" · ")
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = TextTertiary, fontSize = 11.sp)
            }
        }
        Text("+${task.xpReward} XP", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
