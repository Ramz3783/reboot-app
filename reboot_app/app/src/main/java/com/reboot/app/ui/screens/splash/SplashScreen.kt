package com.reboot.app.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.ui.theme.*
import kotlinx.coroutines.delay

/**
 * onCheckStatus is a suspend function that goes straight to the repository (DataStore) and
 * returns the REAL, up-to-date (loggedIn, onboarded) state. We deliberately do NOT read this
 * from a Composable state / collectAsState default here, because that default value races
 * against the async DataStore read on cold start and can incorrectly look "logged out" right
 * after a fresh app launch (the bug where registration seemed to reset after closing the app).
 */
@Composable
fun SplashScreen(
    onCheckStatus: suspend () -> Pair<Boolean, Boolean>,
    onNavigate: (loggedIn: Boolean, onboarded: Boolean) -> Unit,
) {
    LaunchedEffect(Unit) {
        val (loggedIn, onboarded) = onCheckStatus()
        delay(700) // small delay purely so the splash branding is visible, not for correctness
        onNavigate(loggedIn, onboarded)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, Color(0xFF190E2E), BgDeep))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Brush.linearGradient(listOf(AccentPurple, AccentCyan))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PowerSettingsNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text("REBOOT", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text(
                "СТАНЬ ЛУЧШЕЙ ВЕРСИЕЙ СЕБЯ",
                color = TextSecondary,
                fontSize = 13.sp,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
