package com.reboot.app.ui.screens.workout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
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
import com.reboot.app.data.model.WorkoutPlan
import com.reboot.app.ui.theme.*
import kotlinx.coroutines.delay

/**
 * A real "coach day" flow, not just a checkbox: each exercise is shown one at a time, and
 * once the person taps Start, a rest timer counts down automatically before moving to the
 * next exercise — like the workout screens in the reference mockups.
 */
@Composable
fun WorkoutScreen(
    workout: WorkoutPlan,
    onBack: () -> Unit,
    onCompleted: () -> Unit,
) {
    var exerciseIndex by remember { mutableStateOf(0) }
    var phase by remember { mutableStateOf("ready") } // ready, resting, done
    var secondsLeft by remember { mutableStateOf(0) }

    val exercise = workout.exercises.getOrNull(exerciseIndex)

    LaunchedEffect(phase, exerciseIndex) {
        if (phase == "resting") {
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft -= 1
            }
            if (exerciseIndex < workout.exercises.lastIndex) {
                exerciseIndex += 1
                phase = "ready"
            } else {
                phase = "done"
            }
        }
    }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary, modifier = Modifier.clickableNoRipple(onBack))
                Spacer(Modifier.width(12.dp))
                Text(workout.title, color = TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Упражнение ${(exerciseIndex + 1).coerceAtMost(workout.exercises.size)} из ${workout.exercises.size}",
                color = TextTertiary, fontSize = 12.sp
            )
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(CardLight)) {
                Box(
                    Modifier
                        .fillMaxWidth(((exerciseIndex + if (phase == "done") 1 else 0).toFloat() / workout.exercises.size).coerceIn(0f, 1f))
                        .height(4.dp).clip(RoundedCornerShape(2.dp)).background(AccentViolet)
                )
            }

            Spacer(Modifier.weight(1f))

            if (phase == "done" || exercise == null) {
                Icon(Icons.Filled.CheckCircle, null, tint = AccentGreen, modifier = Modifier.size(72.dp).align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(16.dp))
                Text("Тренировка завершена!", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(24.dp))
                GradientButton(text = "Готово") { onCompleted() }
            } else {
                ExerciseAnimationView(
                    animation = exercise.animation,
                    modifier = Modifier.align(Alignment.CenterHorizontally).size(140.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    exercise.name, color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(8.dp))
                Text(exercise.reps, color = AccentCyan, fontSize = 16.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(32.dp))

                if (phase == "resting") {
                    Box(Modifier.align(Alignment.CenterHorizontally).size(180.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stroke = 12f
                            drawArc(
                                color = CardLight, startAngle = -90f, sweepAngle = 360f, useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = StrokeCap.Round)
                            )
                            val fraction = secondsLeft / exercise.restSeconds.toFloat().coerceAtLeast(1f)
                            drawArc(
                                color = AccentCyan, startAngle = -90f, sweepAngle = 360f * fraction, useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = StrokeCap.Round)
                            )
                        }
                        Text("$secondsLeft", color = TextPrimary, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Отдых…", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Box(
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(AccentViolet)
                            .clickableNoRipple {
                                secondsLeft = exercise.restSeconds
                                phase = "resting"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            if (phase != "done" && exercise != null) {
                Row(
                    Modifier.align(Alignment.CenterHorizontally).clickableNoRipple {
                        if (exerciseIndex < workout.exercises.lastIndex) {
                            exerciseIndex += 1
                            phase = "ready"
                        } else {
                            phase = "done"
                        }
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Пропустить упражнение", color = TextTertiary, fontSize = 13.sp)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.SkipNext, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
