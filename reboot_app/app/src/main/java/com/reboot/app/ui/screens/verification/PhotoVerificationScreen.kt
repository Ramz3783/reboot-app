package com.reboot.app.ui.screens.verification

import android.content.Context
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.reboot.app.ui.theme.*
import java.io.File

/**
 * Proof-of-completion via camera: for tasks like "clean your desk" or "cook a healthy meal",
 * a photo is a much better signal that something actually happened than a bare tap.
 */
@Composable
fun PhotoVerificationScreen(
    taskTitle: String,
    onBack: () -> Unit,
    onVerified: (photoPath: String) -> Unit,
) {
    val context = LocalContext.current
    var pendingFile by remember { mutableStateOf<File?>(null) }
    var capturedPath by remember { mutableStateOf<String?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingFile?.let { capturedPath = it.absolutePath }
        }
    }

    fun launchCamera() {
        val file = createProofPhotoFile(context)
        pendingFile = file
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        takePicture.launch(uri)
    }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary, modifier = Modifier.clickableNoRipple(onBack))
                Spacer(Modifier.width(12.dp))
                Text(taskTitle, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text("Сделай фото как доказательство, что задача реально выполнена", color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))

            val path = capturedPath
            if (path != null) {
                Image(
                    painter = rememberFilePainter(path),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.height(20.dp))
                GradientButton(text = "Засчитать выполнение") { onVerified(path) }
                Spacer(Modifier.height(10.dp))
                OutlineButton(text = "Переснять") { launchCamera() }
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardDark)
                        .clickableNoRipple { launchCamera() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CameraAlt, null, tint = AccentCyan, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(10.dp))
                        Text("Нажми, чтобы сделать фото", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
        }
    }
}

private fun createProofPhotoFile(context: Context): File {
    val dir = File(context.cacheDir, "proof_photos").apply { mkdirs() }
    return File(dir, "proof_${System.currentTimeMillis()}.jpg")
}

@Composable
private fun rememberFilePainter(path: String): Painter {
    val bitmap = remember(path) { BitmapFactory.decodeFile(path) }
    return if (bitmap != null) BitmapPainter(bitmap.asImageBitmap()) else ColorPainter(CardDark)
}
