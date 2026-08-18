package com.reboot.app.ui.screens.skins

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.data.model.SkinCatalog
import com.reboot.app.data.model.UserProfile
import com.reboot.app.ui.theme.*

/** Something real to spend the coins earned from tasks/habits/streak on — profile accent colors. */
@Composable
fun SkinsScreen(
    profile: UserProfile,
    onBack: () -> Unit,
    onBuy: (String) -> Unit,
    onSelect: (String) -> Unit,
) {
    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary, modifier = Modifier.clickableNoRipple(onBack))
                Spacer(Modifier.width(12.dp))
                Text("Скины", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Filled.MonetizationOn, null, tint = AccentGold, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("${profile.coins}", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(6.dp))
            Text("Цвет профиля, разблокируется за монеты, которые ты зарабатываешь задачами", color = TextTertiary, fontSize = 12.sp)
            Spacer(Modifier.height(20.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                items(SkinCatalog.SKINS) { skin ->
                    val unlocked = skin.id in profile.unlockedSkins
                    val active = profile.activeSkin == skin.id
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(skin.color))
                                .clickableNoRipple {
                                    if (unlocked) onSelect(skin.id) else onBuy(skin.id)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                active -> Icon(Icons.Filled.Check, null, tint = Color.White)
                                !unlocked -> Icon(Icons.Filled.Lock, null, tint = Color.White.copy(alpha = 0.85f))
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(skin.label, color = TextPrimary, fontSize = 11.sp)
                        if (!unlocked) {
                            Text("${skin.cost} монет", color = TextTertiary, fontSize = 10.sp)
                        } else if (active) {
                            Text("выбран", color = AccentGreen, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
