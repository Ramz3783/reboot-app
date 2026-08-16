package com.reboot.app.ui.screens.celebration

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import com.reboot.app.ui.theme.*

/** Full-screen celebration shown once when a streak milestone (7, 30, 100... days) is hit. */
@Composable
fun StreakMilestoneOverlay(days: Int, onDismiss: () -> Unit) {
    val scale = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, tween(400)) }

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)).clickableNoRipple(onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.scale(scale.value).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(120.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(streakColor(days), Color.Transparent))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.LocalFireDepartment, null, tint = streakColor(days), modifier = Modifier.size(64.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text("$days ${daysWord(days)} подряд!", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(milestoneMessage(days), color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            GradientButton(text = "Продолжаем!", modifier = Modifier.width(220.dp), onClick = onDismiss)
        }
    }
}

/** Full-screen celebration shown when the person levels up. */
@Composable
fun LevelUpOverlay(newLevel: Int, onDismiss: () -> Unit) {
    val scale = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, tween(400)) }

    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)).clickableNoRipple(onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.scale(scale.value).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(120.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AccentPurple, AccentCyan))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Star, null, tint = Color.White, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text("Уровень $newLevel!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text("Ты стал ещё на шаг ближе к лучшей версии себя", color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            GradientButton(text = "Красота!", modifier = Modifier.width(220.dp), onClick = onDismiss)
        }
    }
}

private fun streakColor(days: Int): Color = when {
    days >= 100 -> AccentPurple
    days >= 30 -> AccentPink
    days >= 7 -> AccentGold
    else -> AccentCyan
}

private fun daysWord(n: Int): String {
    val mod100 = n % 100
    val mod10 = n % 10
    return when {
        mod100 in 11..14 -> "дней"
        mod10 == 1 -> "день"
        mod10 in 2..4 -> "дня"
        else -> "дней"
    }
}

private fun milestoneMessage(days: Int): String = when {
    days >= 365 -> "Целый год дисциплины. Ты не тот, кем был раньше."
    days >= 180 -> "Полгода без пропусков — это уже характер, а не мотивация."
    days >= 100 -> "100 дней! Это уже не привычка, это часть тебя."
    days >= 60 -> "Два месяца подряд — серьёзная серия."
    days >= 30 -> "Месяц без единого пропуска. Так держать."
    days >= 14 -> "Две недели дисциплины — привычка закрепляется."
    days >= 7 -> "Неделя без пропусков — отличное начало."
    else -> "Отличный старт, не останавливайся!"
}
