package com.reboot.app.ui.screens.createtask

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.data.model.TaskItem
import com.reboot.app.ui.theme.*
import java.util.UUID

private val colorOptions = listOf(AccentGold, AccentPurple, AccentBlue, AccentGreen, AccentCyan, AccentPink)

@Composable
fun CreateTaskScreen(onBack: () -> Unit, onCreate: (TaskItem) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("30") }
    var selectedColorIdx by remember { mutableStateOf(0) }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary, modifier = Modifier.clickableNoRipple(onBack))
                Spacer(Modifier.width(12.dp))
                Text("Создать задачу", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))

            Text("Название задачи", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            RebootField(title) { title = it }

            Spacer(Modifier.height(16.dp))
            Text("Описание", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            RebootField(description) { description = it }

            Spacer(Modifier.height(16.dp))
            Text("Время (минут)", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            RebootField(minutes) { minutes = it.filter { c -> c.isDigit() } }

            Spacer(Modifier.height(16.dp))
            Text("Цвет", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            Row {
                colorOptions.forEachIndexed { idx, color ->
                    Box(
                        Modifier
                            .padding(end = 10.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (idx == selectedColorIdx)
                                    Modifier.background(color)
                                else Modifier
                            )
                            .clickableNoRipple { selectedColorIdx = idx }
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            GradientButton(text = "Создать", enabled = title.isNotBlank()) {
                onCreate(
                    TaskItem(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        description = description,
                        xpReward = ((minutes.toIntOrNull() ?: 30) / 2).coerceAtLeast(10),
                        timeLabel = "$minutes мин",
                    )
                )
                onBack()
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RebootField(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardDark, unfocusedContainerColor = CardDark,
            focusedBorderColor = AccentViolet, unfocusedBorderColor = CardLight,
            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
