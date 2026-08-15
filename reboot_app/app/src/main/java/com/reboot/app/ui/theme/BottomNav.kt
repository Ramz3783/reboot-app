package com.reboot.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BottomTab(val route: String, val label: String, val icon: ImageVector)

val bottomTabs = listOf(
    BottomTab("home", "Главная", Icons.Filled.Home),
    BottomTab("plans", "Планы", Icons.Filled.ListAlt),
    BottomTab("mentor", "AI", Icons.Filled.AutoAwesome),
    BottomTab("profile", "Профиль", Icons.Filled.Person),
)

@Composable
fun RebootBottomBar(
    currentRoute: String,
    onTabSelected: (String) -> Unit,
    onFabClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(CardDark)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomTabs.take(2).forEach { tab -> TabItem(tab, currentRoute == tab.route) { onTabSelected(tab.route) } }

            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AccentPurple, AccentViolet)))
                    .clickableNoRipple(onFabClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, null, tint = Color.White)
            }

            bottomTabs.drop(2).forEach { tab -> TabItem(tab, currentRoute == tab.route) { onTabSelected(tab.route) } }
        }
    }
}

@Composable
private fun TabItem(tab: BottomTab, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickableNoRipple(onClick).padding(horizontal = 12.dp)
    ) {
        Icon(tab.icon, null, tint = if (selected) AccentViolet else TextTertiary, modifier = Modifier.size(24.dp))
        Text(tab.label, fontSize = 10.sp, color = if (selected) AccentViolet else TextTertiary)
    }
}
