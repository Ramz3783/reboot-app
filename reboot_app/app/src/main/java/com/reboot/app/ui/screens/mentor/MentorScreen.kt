package com.reboot.app.ui.screens.mentor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.data.model.ChatMessage
import com.reboot.app.data.model.MentorMode
import com.reboot.app.data.remote.GroqApi
import com.reboot.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MentorScreen(
    model: String,
    onOpenVoice: () -> Unit,
    getHistory: (MentorMode) -> kotlinx.coroutines.flow.Flow<List<ChatMessage>>,
    onSendMessage: suspend (MentorMode, ChatMessage) -> Unit,
    onEnterChat: suspend (MentorMode) -> Unit,
) {
    var selectedMode by remember { mutableStateOf<MentorMode?>(null) }

    if (selectedMode == null) {
        ModeSelectScreen(onSelect = { selectedMode = it })
    } else {
        val mode = selectedMode!!
        ChatScreen(
            mode = mode,
            model = model,
            history = getHistory(mode),
            onBack = { selectedMode = null },
            onOpenVoice = onOpenVoice,
            onSendMessage = { msg -> onSendMessage(mode, msg) },
            onEnterChat = { onEnterChat(mode) },
        )
    }
}

@Composable
private fun ModeSelectScreen(onSelect: (MentorMode) -> Unit) {
    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("AI Наставник", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Выбери режим", color = TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            Column(Modifier.weight(1f).verticalScrollCompat()) {
                MentorMode.entries.forEach { mode ->
                    NeonCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickableNoRipple { onSelect(mode) }) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(CardLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(mode.displayName.first().toString(), color = AccentViolet, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text(mode.displayName, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text(mode.subtitle, color = TextTertiary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatScreen(
    mode: MentorMode,
    model: String,
    history: kotlinx.coroutines.flow.Flow<List<ChatMessage>>,
    onBack: () -> Unit,
    onOpenVoice: () -> Unit,
    onSendMessage: suspend (ChatMessage) -> Unit,
    onEnterChat: suspend () -> Unit,
) {
    val messages by history.collectAsState(initial = emptyList())
    var input by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(mode) { onEnterChat() }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary, modifier = Modifier.clickableNoRipple(onBack))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(mode.displayName, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text(mode.subtitle, color = TextTertiary, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Filled.Mic, null, tint = AccentCyan, modifier = Modifier.clickableNoRipple(onOpenVoice))
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg -> ChatBubble(msg) }
                if (isSending) {
                    item { ChatBubble(ChatMessage("assistant", "…печатает…")) }
                }
                errorText?.let { err ->
                    item {
                        Text(err, color = AccentRed, fontSize = 12.sp, modifier = Modifier.padding(vertical = 6.dp))
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Напиши сообщение…", color = TextTertiary) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardDark, unfocusedContainerColor = CardDark,
                        focusedBorderColor = AccentViolet, unfocusedBorderColor = CardLight,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                    )
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(AccentViolet)
                        .clickableNoRipple {
                            val text = input.trim()
                            if (text.isNotEmpty() && !isSending) {
                                input = ""
                                errorText = null
                                scope.launch {
                                    isSending = true
                                    val userMsg = ChatMessage("user", text)
                                    onSendMessage(userMsg)
                                    val historyPairs = (messages + userMsg).takeLast(20).map { it.role to it.content }
                                    val result = GroqApi.chatCompletion(model, mode.systemPrompt, historyPairs)
                                    when (result) {
                                        is GroqApi.Result.Success -> onSendMessage(ChatMessage("assistant", result.text))
                                        is GroqApi.Result.Failure -> errorText = result.message
                                    }
                                    isSending = false
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Send, null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
        Box(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isUser) AccentViolet else CardDark)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(msg.content, color = if (isUser) Color.White else TextPrimary, fontSize = 14.sp)
        }
    }
}
