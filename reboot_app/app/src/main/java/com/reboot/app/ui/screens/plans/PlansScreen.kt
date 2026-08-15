package com.reboot.app.ui.screens.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.data.model.PlanItem
import com.reboot.app.ui.theme.*

@Composable
fun PlansScreen(plans: List<PlanItem>, onCreatePlan: () -> Unit) {
    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("Планы", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row {
                Box(
                    Modifier.clip(RoundedCornerShape(12.dp)).background(AccentViolet).padding(horizontal = 18.dp, vertical = 10.dp)
                ) { Text("Мои планы", color = androidx.compose.ui.graphics.Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier.clip(RoundedCornerShape(12.dp)).background(CardDark).padding(horizontal = 18.dp, vertical = 10.dp)
                ) { Text("Шаблоны", color = TextSecondary, fontSize = 13.sp) }
            }
            Spacer(Modifier.height(16.dp))
            Column(Modifier.weight(1f).verticalScrollCompat()) {
                plans.forEach { plan ->
                    NeonCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Column(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(plan.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text("${plan.progressPercent}%", color = AccentGreen, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(plan.category, color = TextTertiary, fontSize = 12.sp)
                            Spacer(Modifier.height(10.dp))
                            val fraction = plan.progressPercent / 100f
                            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(CardLight)) {
                                Box(
                                    Modifier.fillMaxWidth(fraction).height(6.dp).clip(RoundedCornerShape(3.dp))
                                        .background(Brush.horizontalGradient(listOf(AccentPurple, AccentCyan)))
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(90.dp))
            }
        }
    }
}
