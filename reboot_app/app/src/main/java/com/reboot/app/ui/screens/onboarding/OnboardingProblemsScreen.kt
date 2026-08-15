package com.reboot.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.ui.theme.*

private val PROBLEMS = listOf(
    "Прокрастинация", "Нет мотивации", "Зависимость от телефона",
    "Плохие привычки", "Низкая уверенность", "Тревожность / Стресс",
    "Лень", "Нерегулярный сон"
)

@Composable
fun OnboardingProblemsScreen(
    initialSelected: List<String>,
    onNext: (List<String>) -> Unit,
) {
    val selected = remember { mutableStateListOf<String>().apply { addAll(initialSelected) } }

    OnboardingScaffold(
        step = 1,
        title = "Выбери свои цели",
        subtitle = "С какими проблемами ты хочешь справиться?",
        hint = "Выбери всё, что актуально",
        buttonText = "Далее",
        buttonEnabled = selected.isNotEmpty(),
        onButtonClick = { onNext(selected.toList()) }
    ) {
        PROBLEMS.forEach { label ->
            val isSelected = selected.contains(label)
            SelectableRow(label, isSelected) {
                if (isSelected) selected.remove(label) else selected.add(label)
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun SelectableRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) AccentViolet.copy(alpha = 0.18f) else CardDark)
            .then(
                Modifier.background(Color.Transparent)
            )
            .clickableNoRipple(onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (selected) AccentViolet else CardLight)
        )
        Spacer(Modifier.width(14.dp))
        Text(label, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun OnboardingScaffold(
    step: Int,
    title: String,
    subtitle: String,
    hint: String,
    buttonText: String,
    buttonEnabled: Boolean,
    onButtonClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Spacer(Modifier.height(8.dp))
            Text("Шаг $step из 3", color = TextTertiary, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CardLight)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(step / 3f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AccentViolet)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(title, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = TextSecondary, fontSize = 15.sp)
            Spacer(Modifier.height(4.dp))
            Text(hint, color = TextTertiary, fontSize = 12.sp)
            Spacer(Modifier.height(20.dp))
            Column(Modifier.weight(1f).verticalScrollCompat()) {
                content()
            }
            Spacer(Modifier.height(12.dp))
            GradientButton(text = buttonText, enabled = buttonEnabled, onClick = onButtonClick)
            Spacer(Modifier.height(8.dp))
        }
    }
}
