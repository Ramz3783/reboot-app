package com.reboot.app.ui.screens.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.data.model.PlanItem
import com.reboot.app.data.model.PlanTemplate
import com.reboot.app.data.model.TemplateCatalog
import com.reboot.app.ui.theme.*

private enum class PlansTab { MINE, TEMPLATES }

@Composable
fun PlansScreen(
    plans: List<PlanItem>,
    onApplyTemplate: (PlanTemplate) -> Unit,
    onCreatePlan: () -> Unit,
) {
    var tab by remember { mutableStateOf(PlansTab.MINE) }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("Планы", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row {
                PlanTabChip("Мои планы", tab == PlansTab.MINE) { tab = PlansTab.MINE }
                Spacer(Modifier.width(10.dp))
                PlanTabChip("Шаблоны", tab == PlansTab.TEMPLATES) { tab = PlansTab.TEMPLATES }
            }
            Spacer(Modifier.height(16.dp))

            when (tab) {
                PlansTab.MINE -> MyPlansList(plans, onCreatePlan)
                PlansTab.TEMPLATES -> TemplatesList(onApplyTemplate)
            }
        }
    }
}

@Composable
private fun PlanTabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) AccentViolet else CardDark)
            .clickableNoRipple(onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(label, color = if (selected) Color.White else TextSecondary, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun MyPlansList(plans: List<PlanItem>, onCreatePlan: () -> Unit) {
    Column(Modifier.weight(1f).verticalScrollCompat()) {
        if (plans.isEmpty()) {
            Text("У тебя пока нет планов — возьми готовый шаблон или создай свой.", color = TextTertiary, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))
        }
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
        Spacer(Modifier.height(6.dp))
        OutlineButton(text = "+ Создать свой план", onClick = onCreatePlan)
        Spacer(Modifier.height(90.dp))
    }
}

@Composable
private fun TemplatesList(onApplyTemplate: (PlanTemplate) -> Unit) {
    var addedIds by remember { mutableStateOf(setOf<String>()) }
    Column(Modifier.weight(1f).verticalScrollCompat()) {
        TemplateCatalog.TEMPLATES.forEach { template ->
            val added = template.id in addedIds
            NeonCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(Modifier.fillMaxWidth()) {
                    Text(template.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(template.category, color = TextTertiary, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(template.taskTitles.joinToString(" · "), color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    if (added) {
                        OutlineButton(text = "Добавлено ✓") { }
                    } else {
                        GradientButton(text = "Добавить в мои планы") {
                            onApplyTemplate(template)
                            addedIds = addedIds + template.id
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(90.dp))
    }
}
