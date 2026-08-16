package com.reboot.app.ui.screens.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.ui.theme.*
import kotlinx.coroutines.delay

private val THINKING_LINES = listOf(
    "Анализирую твои проблемы…",
    "Подбираю подходящие задачи…",
    "Настраиваю AI-наставника под тебя…",
    "Собираю твой первый план…",
)

/**
 * Purely local "AI is thinking about you" moment between goal selection and profile setup.
 * It doesn't call the network (keeps onboarding fast and reliable even with no connection) —
 * the personalization itself is real and happens right after via OnboardingCatalog, this
 * screen just gives that moment weight instead of an instant, jarring screen swap.
 */
@Composable
fun AiThinkingScreen(onDone: () -> Unit) {
    var lineIndex by remember { mutableStateOf(0) }
    val callback = rememberUpdatedState(onDone)

    LaunchedEffect(Unit) {
        for (i in THINKING_LINES.indices) {
            lineIndex = i
            delay(700)
        }
        delay(300)
        callback.value()
    }

    val transition = rememberInfiniteTransition(label = "ai-thinking")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .size(120.dp)
                    .rotate(rotation)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(listOf(AccentPurple, AccentCyan, AccentViolet, AccentPurple))
                    )
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .clip(CircleShape)
                        .background(BgDeep)
                )
            }
            Spacer(Modifier.height(32.dp))
            Text(
                THINKING_LINES[lineIndex],
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "REBOOT AI составляет твой первый план",
                color = TextTertiary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
