package com.reboot.app.ui.screens.workout

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.reboot.app.data.model.ExerciseAnimation
import com.reboot.app.ui.theme.AccentCyan
import com.reboot.app.ui.theme.AccentViolet
import kotlin.math.sin

/**
 * Simple, original stick-figure animations drawn on a Canvas — no third-party artwork or
 * copyrighted images involved, just geometry that loops in sync with the exercise's rhythm.
 */
@Composable
fun ExerciseAnimationView(animation: ExerciseAnimation, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "exercise")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Reverse),
        label = "phase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val stroke = Stroke(width = w * 0.035f, cap = StrokeCap.Round)
        val limbColor = AccentCyan
        val bodyColor = AccentViolet

        fun line(x1: Float, y1: Float, x2: Float, y2: Float, color: Color = limbColor) {
            drawLine(color, Offset(x1, y1), Offset(x2, y2), strokeWidth = stroke.width, cap = StrokeCap.Round)
        }

        when (animation) {
            ExerciseAnimation.SQUAT -> {
                val bend = phase // 0 standing .. 1 squatting
                val headY = h * (0.18f + 0.18f * bend)
                val hipY = h * (0.55f + 0.15f * bend)
                val kneeY = h * (0.55f + 0.30f * bend)
                val footY = h * 0.92f
                drawCircle(bodyColor, radius = w * 0.09f, center = Offset(cx, headY))
                line(cx, headY + w * 0.09f, cx, hipY, bodyColor)
                line(cx, hipY, cx - w * 0.14f, kneeY)
                line(cx - w * 0.14f, kneeY, cx - w * 0.12f, footY)
                line(cx, hipY, cx + w * 0.14f, kneeY)
                line(cx + w * 0.14f, kneeY, cx + w * 0.12f, footY)
                line(cx, hipY - h * 0.05f, cx - w * 0.2f, hipY - h * 0.15f - bend * h * 0.05f)
                line(cx, hipY - h * 0.05f, cx + w * 0.2f, hipY - h * 0.15f - bend * h * 0.05f)
            }
            ExerciseAnimation.PUSHUP -> {
                val dip = phase
                val bodyY = h * (0.5f + 0.12f * dip)
                drawCircle(bodyColor, radius = w * 0.08f, center = Offset(cx - w * 0.32f, bodyY - h * 0.03f))
                line(cx - w * 0.24f, bodyY, cx + w * 0.28f, bodyY, bodyColor)
                line(cx - w * 0.2f, bodyY, cx - w * 0.22f, bodyY + h * (0.25f - dip * 0.08f))
                line(cx + w * 0.2f, bodyY, cx + w * 0.22f, bodyY + h * (0.25f - dip * 0.08f))
                line(cx - w * 0.05f, bodyY, cx - w * 0.02f, h * 0.88f)
                line(cx + w * 0.2f, bodyY, cx + w * 0.24f, h * 0.88f)
            }
            ExerciseAnimation.PLANK -> {
                val shake = sin(phase * Math.PI * 2).toFloat() * h * 0.01f
                val bodyY = h * 0.55f + shake
                drawCircle(bodyColor, radius = w * 0.08f, center = Offset(cx - w * 0.3f, bodyY - h * 0.02f))
                line(cx - w * 0.22f, bodyY, cx + w * 0.28f, bodyY, bodyColor)
                line(cx - w * 0.18f, bodyY, cx - w * 0.2f, h * 0.86f)
                line(cx + w * 0.24f, bodyY, cx + w * 0.28f, h * 0.86f)
                line(cx + w * 0.15f, bodyY, cx + w * 0.1f, h * 0.86f)
            }
            ExerciseAnimation.LUNGE -> {
                val dip = phase
                val hipY = h * (0.5f + 0.12f * dip)
                drawCircle(bodyColor, radius = w * 0.09f, center = Offset(cx, hipY - h * 0.22f))
                line(cx, hipY - h * 0.13f, cx, hipY, bodyColor)
                line(cx, hipY, cx - w * 0.18f, hipY + h * (0.1f + dip * 0.1f))
                line(cx - w * 0.18f, hipY + h * (0.1f + dip * 0.1f), cx - w * 0.22f, h * 0.9f)
                line(cx, hipY, cx + w * 0.2f, hipY + h * (0.05f - dip * 0.02f))
                line(cx + w * 0.2f, hipY + h * (0.05f - dip * 0.02f), cx + w * 0.1f, h * 0.9f)
            }
            ExerciseAnimation.SITUP -> {
                val lift = phase
                val headX = cx - w * 0.05f - w * 0.18f * lift
                val headY = h * (0.55f - 0.2f * lift)
                drawCircle(bodyColor, radius = w * 0.08f, center = Offset(headX, headY))
                line(headX, headY + w * 0.08f, cx + w * 0.1f, h * 0.62f, bodyColor)
                line(cx + w * 0.1f, h * 0.62f, cx + w * 0.28f, h * 0.5f)
                line(cx + w * 0.28f, h * 0.5f, cx + w * 0.28f, h * 0.86f)
                line(cx + w * 0.1f, h * 0.62f, cx - w * 0.05f, h * 0.86f)
            }
            ExerciseAnimation.JUMP -> {
                val jump = sin(phase * Math.PI).toFloat()
                val bodyY = h * (0.5f - 0.15f * jump)
                drawCircle(bodyColor, radius = w * 0.09f, center = Offset(cx, bodyY - h * 0.16f))
                line(cx, bodyY - h * 0.07f, cx, bodyY + h * 0.12f, bodyColor)
                line(cx, bodyY + h * 0.12f, cx - w * (0.1f + 0.1f * jump), h * 0.9f)
                line(cx, bodyY + h * 0.12f, cx + w * (0.1f + 0.1f * jump), h * 0.9f)
                line(cx, bodyY - h * 0.02f, cx - w * (0.15f + 0.1f * jump), bodyY - h * 0.1f)
                line(cx, bodyY - h * 0.02f, cx + w * (0.15f + 0.1f * jump), bodyY - h * 0.1f)
            }
            ExerciseAnimation.STRETCH -> {
                val sway = sin(phase * Math.PI * 2).toFloat() * w * 0.06f
                drawCircle(bodyColor, radius = w * 0.09f, center = Offset(cx + sway, h * 0.28f))
                line(cx + sway, h * 0.37f, cx, h * 0.62f, bodyColor)
                line(cx, h * 0.62f, cx - w * 0.14f, h * 0.9f)
                line(cx, h * 0.62f, cx + w * 0.14f, h * 0.9f)
                line(cx, h * 0.45f, cx - w * 0.22f - sway, h * 0.35f)
                line(cx, h * 0.45f, cx + w * 0.22f + sway, h * 0.35f)
            }
            ExerciseAnimation.GENERIC -> {
                val bob = sin(phase * Math.PI * 2).toFloat() * h * 0.02f
                drawCircle(bodyColor, radius = w * 0.09f, center = Offset(cx, h * 0.3f + bob))
                line(cx, h * 0.39f + bob, cx, h * 0.62f + bob, bodyColor)
                line(cx, h * 0.62f + bob, cx - w * 0.15f, h * 0.9f)
                line(cx, h * 0.62f + bob, cx + w * 0.15f, h * 0.9f)
                line(cx, h * 0.45f + bob, cx - w * 0.2f, h * 0.35f + bob)
                line(cx, h * 0.45f + bob, cx + w * 0.2f, h * 0.35f + bob)
            }
        }
    }
}
