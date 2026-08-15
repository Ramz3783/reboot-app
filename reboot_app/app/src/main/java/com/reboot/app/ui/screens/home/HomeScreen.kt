package com.reboot.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import com.reboot.app.ui.theme.*

@Composable
fun HomeScreen(
    profile: UserProfile,
    tasks: List<TaskItem>,
    onToggleTask: (String) -> Unit,
    onSeeAllTasks: () -> Unit,
) {
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
            Spacer(Modifier.height(20.dp))

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
                        Text("${progressPercent(tasks)}%", color = AccentGreen, fontWeight = FontWeight.Bold)
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
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatMini("Серия", "${profile.streakDays} дней")
                        StatMini("Задачи", "${tasks.count { it.done }} / ${tasks.size}")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.MonetizationOn, null, tint = AccentGold, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("${profile.coins}", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Сегодняшние задачи", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text("Сегодня", color = AccentViolet, fontSize = 13.sp, modifier = Modifier.clickableNoRipple(onSeeAllTasks))
            }
            Spacer(Modifier.height(12.dp))
            tasks.forEach { task ->
                TaskRow(task) { onToggleTask(task.id) }
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(90.dp))
        }
    }
}

private fun progressPercent(tasks: List<TaskItem>): Int {
    if (tasks.isEmpty()) return 0
    return (tasks.count { it.done } * 100) / tasks.size
}

@Composable
private fun StatMini(label: String, value: String) {
    Column {
        Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(label, color = TextTertiary, fontSize = 11.sp)
    }
}

@Composable
fun TaskRow(task: TaskItem, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .clickableNoRipple(onToggle)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (task.done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            null,
            tint = if (task.done) AccentGreen else TextTertiary
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                task.title,
                color = if (task.done) TextTertiary else TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            if (task.timeLabel.isNotBlank()) {
                Text(task.timeLabel, color = TextTertiary, fontSize = 11.sp)
            }
        }
        Text("+${task.xpReward} XP", color = AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
