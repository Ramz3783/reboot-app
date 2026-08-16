package com.reboot.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.data.model.OnboardingCatalog
import com.reboot.app.ui.theme.*

private data class GoalOption(val label: String, val icon: ImageVector)

private val GOAL_ICONS: Map<String, ImageVector> = mapOf(
    "Тренировки" to Icons.Filled.FitnessCenter,
    "Чтение" to Icons.Filled.MenuBook,
    "Медитация" to Icons.Filled.SelfImprovement,
    "Ранний подъём" to Icons.Filled.WbSunny,
    "Правильное питание" to Icons.Filled.Restaurant,
    "Обучение" to Icons.Filled.School,
    "Дисциплина" to Icons.Filled.CheckCircle,
    "Фокус" to Icons.Filled.CenterFocusStrong,
    "Больше воды" to Icons.Filled.WaterDrop,
    "Меньше соцсетей" to Icons.Filled.PhonelinkErase,
    "Ведение дневника" to Icons.Filled.Edit,
    "Планирование дня" to Icons.Filled.EventNote,
)

private val GOALS = OnboardingCatalog.GOALS.map { GoalOption(it, GOAL_ICONS[it] ?: Icons.Filled.Star) }

@Composable
fun OnboardingGoalsScreen(
    initialSelected: List<String>,
    onNext: (List<String>) -> Unit,
) {
    val selected = remember { mutableStateListOf<String>().apply { addAll(initialSelected) } }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Text("Шаг 2 из 3", color = TextTertiary, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(CardLight)) {
                Box(Modifier.fillMaxWidth(2 / 3f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(AccentViolet))
            }
            Spacer(Modifier.height(20.dp))
            Text("Выбери свои цели", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Какие привычки хочешь развить?", color = TextSecondary, fontSize = 15.sp)
            Spacer(Modifier.height(20.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(GOALS) { goal ->
                    val isSelected = selected.contains(goal.label)
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AccentViolet.copy(alpha = 0.18f) else CardDark)
                            .clickableNoRipple {
                                if (isSelected) selected.remove(goal.label) else selected.add(goal.label)
                            }
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(goal.icon, null, tint = if (isSelected) AccentViolet else TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Text(goal.label, color = TextPrimary, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            GradientButton(text = "Далее", enabled = selected.isNotEmpty()) { onNext(selected.toList()) }
        }
    }
}

@Composable
fun OnboardingProfileScreen(
    initialName: String,
    onDone: (name: String, birthDate: String, height: String, weight: String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var birth by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Text("Шаг 3 из 3", color = TextTertiary, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(CardLight)) {
                Box(Modifier.fillMaxWidth(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(AccentViolet))
            }
            Spacer(Modifier.height(20.dp))
            Text("Создай профиль", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Расскажи немного о себе", color = TextSecondary, fontSize = 15.sp)
            Spacer(Modifier.height(24.dp))

            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(CardDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PersonOutline, null, tint = TextTertiary, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(24.dp))

            listOf(
                Triple("Твоё имя", name) { v: String -> name = v },
                Triple("Дата рождения", birth) { v: String -> birth = v },
            ).forEach { (label, value, setter) ->
                OutlinedTextField(
                    value = value,
                    onValueChange = setter,
                    placeholder = { Text(label, color = TextTertiary) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardDark, unfocusedContainerColor = CardDark,
                        focusedBorderColor = AccentViolet, unfocusedBorderColor = CardLight,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    placeholder = { Text("Рост (см)", color = TextTertiary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardDark, unfocusedContainerColor = CardDark,
                        focusedBorderColor = AccentViolet, unfocusedBorderColor = CardLight,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                    ),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    placeholder = { Text("Вес (кг)", color = TextTertiary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardDark, unfocusedContainerColor = CardDark,
                        focusedBorderColor = AccentViolet, unfocusedBorderColor = CardLight,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.weight(1f))
            GradientButton(text = "Готово", enabled = name.isNotBlank()) {
                onDone(name, birth, height, weight)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
