package com.reboot.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.reboot.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RebootTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextTertiary) },
        leadingIcon = { Icon(leadingIcon, null, tint = TextTertiary) },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { visible = !visible }) {
                    Icon(if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = TextTertiary)
                }
            }
        },
        visualTransformation = if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CardDark,
            unfocusedContainerColor = CardDark,
            focusedBorderColor = AccentViolet,
            unfocusedBorderColor = CardLight,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = AccentViolet,
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun LoginScreen(
    onLogin: (email: String) -> Unit,
    onGoRegister: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            Box(
                Modifier
                    .size(84.dp)
                    .background(Brush.linearGradient(listOf(AccentPurple, AccentCyan)), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PowerSettingsNew, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("С возвращением", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Войди, чтобы продолжить свой путь", color = TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(32.dp))

            RebootTextField(email, { email = it }, "Email", Icons.Filled.Email, keyboardType = KeyboardType.Email)
            Spacer(Modifier.height(14.dp))
            RebootTextField(password, { password = it }, "Пароль", Icons.Filled.Lock, isPassword = true)

            Spacer(Modifier.height(24.dp))
            GradientButton(
                text = "Войти",
                enabled = email.isNotBlank() && password.isNotBlank(),
            ) { onLogin(email) }

            Spacer(Modifier.height(14.dp))
            OutlineButton(text = "Войти без регистрации") { onLogin("guest@reboot.app") }

            Spacer(Modifier.height(24.dp))
            Row {
                Text("Нет аккаунта? ", color = TextSecondary)
                Text(
                    "Создать",
                    color = AccentViolet,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickableNoRipple(onGoRegister)
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegister: (name: String, email: String) -> Unit,
    onGoLogin: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val valid = name.isNotBlank() && email.isNotBlank() && password.length >= 4 && password == confirm

    Box(Modifier.fillMaxSize().background(BgDeep)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScrollCompat()
                .padding(24.dp)
        ) {
            Spacer(Modifier.height(32.dp))
            Text("Регистрация", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(96.dp)
                    .background(Color.Transparent, androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(
                    Icons.Filled.Person, null, tint = AccentViolet,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            RebootTextField(name, { name = it }, "Имя", Icons.Filled.Person)
            Spacer(Modifier.height(14.dp))
            RebootTextField(email, { email = it }, "Email", Icons.Filled.Email, keyboardType = KeyboardType.Email)
            Spacer(Modifier.height(14.dp))
            RebootTextField(password, { password = it }, "Пароль", Icons.Filled.Lock, isPassword = true)
            Spacer(Modifier.height(14.dp))
            RebootTextField(confirm, { confirm = it }, "Подтвердите пароль", Icons.Filled.Lock, isPassword = true)

            Spacer(Modifier.height(24.dp))
            GradientButton(text = "Создать аккаунт", enabled = valid) { onRegister(name, email) }

            Spacer(Modifier.height(16.dp))
            Text(
                "Продолжая, ты соглашаешься с Условиями использования и Политикой конфиденциальности",
                color = TextTertiary,
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            Row(Modifier.align(Alignment.CenterHorizontally)) {
                Text("Уже есть аккаунт? ", color = TextSecondary)
                Text("Войти", color = AccentViolet, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickableNoRipple(onGoLogin))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
