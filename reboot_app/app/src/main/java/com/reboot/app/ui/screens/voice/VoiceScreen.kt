package com.reboot.app.ui.screens.voice

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.reboot.app.data.model.MentorMode
import com.reboot.app.data.remote.GroqApi
import com.reboot.app.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

private enum class VoiceState { IDLE, RECORDING, PROCESSING, DONE }

@Composable
fun VoiceScreen(
    apiKey: String,
    model: String,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var state by remember { mutableStateOf(VoiceState.IDLE) }
    var transcript by remember { mutableStateOf("") }
    var aiReply by remember { mutableStateOf("") }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    val scope = rememberCoroutineScope()

    val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    var permissionGranted by remember { mutableStateOf(hasPermission) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> permissionGranted = granted }

    fun startRecording() {
        val file = File(context.cacheDir, "reboot_voice_${System.currentTimeMillis()}.m4a")
        val mr = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = mr
        audioFile = file
        state = VoiceState.RECORDING
        transcript = ""
        aiReply = ""
    }

    fun stopAndProcess() {
        val mr = recorder ?: return
        runCatching { mr.stop(); mr.release() }
        recorder = null
        state = VoiceState.PROCESSING
        val file = audioFile ?: return
        scope.launch {
            when (val sttResult = GroqApi.transcribeAudio(apiKey, file)) {
                is GroqApi.Result.Success -> {
                    transcript = sttResult.text
                    val chatResult = GroqApi.chatCompletion(
                        apiKey, model, MentorMode.MOTIVATOR.systemPrompt,
                        listOf("user" to sttResult.text)
                    )
                    aiReply = when (chatResult) {
                        is GroqApi.Result.Success -> chatResult.text
                        is GroqApi.Result.Failure -> chatResult.message
                    }
                }
                is GroqApi.Result.Failure -> {
                    transcript = ""
                    aiReply = sttResult.message
                }
            }
            state = VoiceState.DONE
        }
    }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Голосовой AI", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Filled.Close, null, tint = TextSecondary, modifier = Modifier.clickableNoRipple(onClose))
            }
            Spacer(Modifier.weight(1f))

            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(AccentViolet.copy(alpha = 0.35f), AccentCyan.copy(alpha = 0.15f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.size(140.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(AccentPurple, AccentCyan))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Mic, null, tint = Color.White, modifier = Modifier.size(48.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                when (state) {
                    VoiceState.IDLE -> "Нажми и говори"
                    VoiceState.RECORDING -> "Слушаю…"
                    VoiceState.PROCESSING -> "Обрабатываю…"
                    VoiceState.DONE -> "Готово"
                },
                color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            if (transcript.isNotBlank() || aiReply.isNotBlank()) {
                Spacer(Modifier.height(20.dp))
                NeonCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        if (transcript.isNotBlank()) {
                            Text("Ты: $transcript", color = TextSecondary, fontSize = 13.sp)
                            Spacer(Modifier.height(8.dp))
                        }
                        if (aiReply.isNotBlank()) {
                            Text(aiReply, color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Box(
                    Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (state == VoiceState.RECORDING) AccentRed else AccentViolet)
                        .clickableNoRipple {
                            if (!permissionGranted) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                return@clickableNoRipple
                            }
                            when (state) {
                                VoiceState.IDLE, VoiceState.DONE -> startRecording()
                                VoiceState.RECORDING -> stopAndProcess()
                                VoiceState.PROCESSING -> {}
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (state == VoiceState.RECORDING) Icons.Filled.Close else Icons.Filled.Mic,
                        null, tint = Color.White
                    )
                }
            }
            if (!permissionGranted) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Нажми на кнопку, чтобы разрешить доступ к микрофону",
                    color = AccentRed, fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
