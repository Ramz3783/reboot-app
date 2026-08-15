package com.reboot.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.data.model.UserProfile
import com.reboot.app.ui.theme.*

private data class MenuRow(val label: String, val icon: ImageVector, val route: String)

private val menuItems = listOf(
    MenuRow("Достижения", Icons.Filled.EmojiEvents, "achievements"),
    MenuRow("Привычки", Icons.Filled.Repeat, "habits"),
    MenuRow("Цели", Icons.Filled.Flag, "plans"),
    MenuRow("Статистика", Icons.Filled.BarChart, "progress"),
    MenuRow("Настройки", Icons.Filled.Settings, "settings"),
    MenuRow("PRO подписка", Icons.Filled.WorkspacePremium, "pro"),
)

@Composable
fun ProfileScreen(
    profile: UserProfile,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().verticalScrollCompat().padding(20.dp)) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(AccentPurple, AccentCyan))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        profile.name.take(1).ifBlank { "R" }.uppercase(),
                        color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(profile.name.ifBlank { "Гость" }, color = TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text("Уровень ${profile.level} · ${profile.xp}/${profile.xpToNextLevel} XP", color = TextTertiary, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(24.dp))
            menuItems.forEach { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardDark)
                        .clickableNoRipple { onNavigate(item.route) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(item.icon, null, tint = AccentViolet)
                    Spacer(Modifier.width(14.dp))
                    Text(item.label, color = TextPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, null, tint = TextTertiary)
                }
                Spacer(Modifier.height(10.dp))
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentRed.copy(alpha = 0.12f))
                    .clickableNoRipple(onLogout)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Logout, null, tint = AccentRed)
                Spacer(Modifier.width(14.dp))
                Text("Выйти", color = AccentRed, fontSize = 15.sp)
            }
            Spacer(Modifier.height(90.dp))
        }
    }
}
