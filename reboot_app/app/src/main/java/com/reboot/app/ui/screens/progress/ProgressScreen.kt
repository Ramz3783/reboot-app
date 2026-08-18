package com.reboot.app.ui.screens.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.data.model.DayLog
import com.reboot.app.data.model.UserProfile
import com.reboot.app.ui.theme.*

@Composable
fun ProgressScreen(profile: UserProfile) {
    val logs = profile.recentDayLogs
    val todayPercent = 0 // today's own % is already shown on Home; this screen is history-based
    val bestDayPercent = logs.maxOfOrNull { dayPercent(it) } ?: 0
    val totalCompleted = logs.sumOf { it.completedTitles.size }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().verticalScrollCompat().padding(20.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("Прогресс", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth()) {
                    Text("Последние ${logs.size} дней", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (logs.isEmpty()) "Пока нет данных" else "${logs.count { dayPercent(it) == 100 }} идеальных дней",
                        color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    if (logs.isEmpty()) {
                        Text(
                            "Статистика появится, как только пройдёт хотя бы один день с задачами",
                            color = TextTertiary, fontSize = 12.sp
                        )
                    } else {
                        Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                            val barWidth = size.width / (logs.size * 1.6f)
                            logs.forEachIndexed { i, log ->
                                val pct = dayPercent(log)
                                val barHeight = size.height * (pct / 100f).coerceIn(0.04f, 1f)
                                val x = i * (size.width / logs.size) + barWidth / 2
                                drawRoundRect(
                                    brush = Brush.verticalGradient(listOf(AccentCyan, AccentViolet)),
                                    topLeft = Offset(x, size.height - barHeight),
                                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                                )
                            }
                        }
                    }
                }
            }

            if (profile.categoryXp.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Text("Ветки развития", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("Опыт качается отдельно по категориям задач", color = TextTertiary, fontSize = 11.sp)
                Spacer(Modifier.height(12.dp))
                profile.categoryXp.entries.sortedByDescending { it.value }.forEach { (category, xp) ->
                    CategoryBranchRow(category, xp)
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Статистика", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            StatRow("Задач выполнено (история)", "$totalCompleted")
            StatRow("Серия дней", "${profile.streakDays}")
            StatRow("Лучший день", if (logs.isEmpty()) "—" else "$bestDayPercent%")
            StatRow("Опыт получено", "${profile.xp} XP")
            StatRow("Идеальных недель подряд", "${profile.perfectWeekCount}")
            Spacer(Modifier.height(90.dp))
        }
    }
}

private fun dayPercent(log: DayLog): Int {
    val total = log.completedTitles.size + log.missedTitles.size
    if (total == 0) return 0
    return (log.completedTitles.size * 100) / total
}

/** Simple level derived from category XP: every 150 XP is one level in that skill branch. */
private fun categoryLevel(xp: Int): Int = (xp / 150) + 1

@Composable
private fun CategoryBranchRow(category: String, xp: Int) {
    val level = categoryLevel(xp)
    val progressInLevel = xp % 150
    NeonCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(category, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("Ур. $level", color = AccentCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            val fraction = (progressInLevel / 150f).coerceIn(0f, 1f)
            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(CardLight)) {
                Box(
                    Modifier.fillMaxWidth(fraction).height(6.dp).clip(RoundedCornerShape(3.dp))
                        .background(Brush.horizontalGradient(listOf(AccentPurple, AccentCyan)))
                )
            }
            Spacer(Modifier.height(4.dp))
            Text("$progressInLevel / 150 XP до след. уровня", color = TextTertiary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(10.dp))
}
