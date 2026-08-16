package com.reboot.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.ui.theme.*

@Composable
fun SettingsScreen(
    currentModel: String,
    notificationsEnabled: Boolean,
    silentMode: Boolean,
    onBack: () -> Unit,
    onSaveModel: (String) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleSilent: (Boolean) -> Unit,
) {
    var model by remember { mutableStateOf(currentModel) }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().verticalScrollCompat().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary, modifier = Modifier.clickableNoRipple(onBack))
                Spacer(Modifier.width(12.dp))
                Text("Настройки", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))

            Text("AI-модель", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                "AI уже подключен и готов к работе. Здесь можно поменять модель, если нужно.",
                color = TextTertiary, fontSize = 11.sp
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardDark, unfocusedContainerColor = CardDark,
                    focusedBorderColor = AccentViolet, unfocusedBorderColor = CardLight,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
            GradientButton(text = "Сохранить") { onSaveModel(model) }

            Spacer(Modifier.height(28.dp))
            SettingsToggleRow("Уведомления", notificationsEnabled, onToggleNotifications)
            SettingsToggleRow("Режим тишины", silentMode, onToggleSilent)
            Spacer(Modifier.height(90.dp))
        }
    }
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontSize = 15.sp)
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = androidx.compose.ui.graphics.Color.White, checkedTrackColor = AccentViolet)
        )
    }
    Spacer(Modifier.height(10.dp))
}
