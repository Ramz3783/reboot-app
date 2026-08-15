package com.reboot.app.ui.screens.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.ui.theme.*

private val perks = listOf(
    "Безлимитный AI-чат",
    "Голосовой AI без ограничений",
    "Расширенная аналитика",
    "Приоритетная поддержка",
    "Эксклюзивные режимы",
)

@Composable
fun ProScreen(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary, modifier = Modifier.clickableNoRipple(onBack))
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(AccentPurple, AccentViolet, AccentCyan)))
                    .padding(24.dp)
            ) {
                Column {
                    Text("PRO", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Text("Раскрой свой максимум", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    Spacer(Modifier.height(20.dp))
                    perks.forEach { perk ->
                        Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(perk, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            GradientButton(text = "Попробовать 3 дня") { }
            Spacer(Modifier.height(10.dp))
            Text("299 ₽ / месяц после пробного периода", color = TextTertiary, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
        }
    }
}
