package com.reboot.app.ui.screens.focus

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun FocusScreen(onBack: () -> Unit) {
    val totalSeconds = 25 * 60
    var remaining by remember { mutableStateOf(totalSeconds) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        while (running && remaining > 0) {
            delay(1000)
            remaining -= 1
        }
        if (remaining == 0) running = false
    }

    val minutes = remaining / 60
    val secs = remaining % 60
    val fraction = remaining / totalSeconds.toFloat()

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary, modifier = Modifier.clickableNoRipple(onBack))
                Spacer(Modifier.width(12.dp))
                Text("Фокус режим", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.align(Alignment.CenterHorizontally).size(260.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 16f
                    drawArc(
                        color = CardLight,
                        startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = AccentViolet,
                        startAngle = -90f, sweepAngle = 360f * fraction, useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
                Text(
                    String.format("%02d:%02d", minutes, secs),
                    color = TextPrimary, fontSize = 40.sp, fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("Фокус на задаче", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.weight(1f))
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AccentViolet)
                    .clickableNoRipple {
                        if (remaining == 0) remaining = totalSeconds
                        running = !running
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = Color.White)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
