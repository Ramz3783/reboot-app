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
import com.reboot.app.data.model.UserProfile
import com.reboot.app.ui.theme.*
import kotlin.random.Random

@Composable
fun ProgressScreen(profile: UserProfile) {
    val weeklyData = remember2()

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().verticalScrollCompat().padding(20.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("Прогресс", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth()) {
                    Text("Выполнено задач", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("78%", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                        val barWidth = size.width / (weeklyData.size * 1.6f)
                        val maxVal = weeklyData.max().coerceAtLeast(1)
                        weeklyData.forEachIndexed { i, v ->
                            val barHeight = size.height * (v / maxVal.toFloat())
                            val x = i * (size.width / weeklyData.size) + barWidth / 2
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

            Spacer(Modifier.height(20.dp))
            Text("Статистика", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            StatRow("Задачи выполнено", "${profile.level * 20 + profile.xp}")
            StatRow("Серия дней", "${profile.streakDays}")
            StatRow("Лучший день", "92%")
            StatRow("Опыт получено", "${profile.xp} XP")
            Spacer(Modifier.height(90.dp))
        }
    }
}

@Composable
private fun remember2(): List<Int> {
    return androidx.compose.runtime.remember {
        List(31) { Random.nextInt(20, 100) }
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
