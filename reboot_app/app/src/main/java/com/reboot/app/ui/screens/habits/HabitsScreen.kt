package com.reboot.app.ui.screens.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.data.model.HabitItem
import com.reboot.app.ui.theme.*

@Composable
fun HabitsScreen(
    habits: List<HabitItem>,
    onToggle: (String) -> Unit,
    onBack: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary, modifier = Modifier.clickableNoRipple(onBack))
                Spacer(Modifier.width(12.dp))
                Text("Привычки", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.Add, null, tint = AccentViolet)
            }
            Spacer(Modifier.height(16.dp))
            Column(Modifier.weight(1f).verticalScrollCompat()) {
                habits.forEach { habit ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CardDark)
                            .clickableNoRipple { onToggle(habit.id) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (habit.completedToday) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            null, tint = if (habit.completedToday) AccentGreen else TextTertiary
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(habit.title, color = TextPrimary, fontSize = 15.sp, modifier = Modifier.weight(1f))
                        Icon(Icons.Filled.LocalFireDepartment, null, tint = AccentGold, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${habit.streak}д", color = TextSecondary, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}
