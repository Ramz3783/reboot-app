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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.data.remote.GroqApi
import com.reboot.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentApiKey: String,
    currentModel: String,
    notificationsEnabled: Boolean,
    silentMode: Boolean,
    onBack: () -> Unit,
    onSaveApiKey: (String) -> Unit,
    onSaveModel: (String) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleSilent: (Boolean) -> Unit,
) {
    var apiKey by remember { mutableStateOf(currentApiKey) }
    var model by remember { mutableStateOf(currentModel) }
    var testResult by remember { mutableStateOf<String?>(null) }
    var testing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().verticalScrollCompat().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary, modifier = Modifier.clickableNoRipple(onBack))
                Spacer(Modifier.width(12.dp))
                Text("Настройки", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))

            Text("Groq AI", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Ключ хранится только на этом устройстве и используется для прямых запросов к api.groq.com. Получить ключ можно на console.groq.com/keys",
                color = TextTertiary, fontSize = 11.sp
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                placeholder = { Text("gsk_...", color = TextTertiary) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardDark, unfocusedContainerColor = CardDark,
                    focusedBorderColor = AccentViolet, unfocusedBorderColor = CardLight,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                placeholder = { Text("Модель, напр. llama-3.3-70b-versatile", color = TextTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardDark, unfocusedContainerColor = CardDark,
                    focusedBorderColor = AccentViolet, unfocusedBorderColor = CardLight,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Row {
                OutlineButton(text = "Проверить ключ", modifier = Modifier.weight(1f)) {
                    testing = true
                    testResult = null
                    scope.launch {
                        val res = GroqApi.listModels(apiKey)
                        testResult = when (res) {
                            is GroqApi.Result.Success -> "OK. Доступные модели: ${res.text.take(120)}…"
                            is GroqApi.Result.Failure -> res.message
                        }
                        testing = false
                    }
                }
            }
            testResult?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = if (it.startsWith("OK")) AccentGreen else AccentRed, fontSize = 11.sp)
            }
            Spacer(Modifier.height(14.dp))
            GradientButton(text = "Сохранить") {
                onSaveApiKey(apiKey)
                onSaveModel(model)
            }

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
