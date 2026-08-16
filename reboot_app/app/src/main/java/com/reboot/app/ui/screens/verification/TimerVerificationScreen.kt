package com.reboot.app.ui.screens.verification

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.ui.theme.*
import kotlinx.coroutines.delay

/**
 * "Real" verification for tasks like "no phone for 1 hour" or "read for 20 minutes": you
 * can't just tap a checkbox, you have to actually keep this countdown running for the full
 * duration before it lets you complete the task.
 */
@Composable
fun TimerVerificationScreen(
    taskTitle: String,
    durationMinutes: Int,
    onBack: () -> Unit,
    onVerified: () -> Unit,
) {
    val totalSeconds = (durationMinutes.coerceAtLeast(1)) * 60
    var remaining by remember { mutableStateOf(totalSeconds) }
    var running by remember { mutableStateOf(true) }

    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000)
            remaining -= 1
        }
    }

    val minutes = remaining / 60
    val secs = remaining % 60
    val fraction = remaining / totalSeconds.toFloat()

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary, modifier = Modifier.clickableNoRipple(onBack))
                Spacer(Modifier.width(12.dp))
                Text(taskTitle, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.align(Alignment.CenterHorizontally).size(260.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 16f
                    drawArc(
                        color = CardLight, startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = AccentCyan, startAngle = -90f, sweepAngle = 360f * fraction, useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
                Text(String.format("%02d:%02d", minutes, secs), color = TextPrimary, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                if (remaining > 0) "Держи приложение открытым, пока идёт таймер" else "Готово! Задача засчитана",
                color = TextSecondary, fontSize = 13.sp, modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.weight(1f))

            if (remaining > 0) {
                Box(
                    Modifier.align(Alignment.CenterHorizontally).size(72.dp).background(AccentViolet, CircleShape)
                        .clickableNoRipple { running = !running },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = Color.White)
                }
            } else {
                GradientButton(text = "Засчитать выполнение") { onVerified() }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
