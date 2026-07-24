package com.pinknote.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.domain.model.AppLanguage
import com.pinknote.app.domain.model.ThemeMode
import com.pinknote.app.presentation.common.PinkCard
import com.pinknote.app.utils.Constants

@Composable
fun SettingsScreen(
    onOpenAdmin: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val isLoggingOut by viewModel.isLoggingOut.collectAsState()
    val logoutCompleted by viewModel.logoutCompleted.collectAsState()

    LaunchedEffect(logoutCompleted) {
        if (logoutCompleted) {
            viewModel.consumeLogoutNavigation()
            onLoggedOut()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Cài đặt", style = MaterialTheme.typography.headlineMedium)
        Text("Giao diện", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = settings.themeMode == mode,
                    onClick = { viewModel.setTheme(mode) },
                    label = { Text(mode.name) }
                )
            }
        }
        Text("Ngôn ngữ", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AppLanguage.entries.forEach { language ->
                FilterChip(
                    selected = settings.language == language,
                    onClick = { viewModel.setLanguage(language) },
                    label = { Text(if (language == AppLanguage.VI) "Tiếng Việt" else "English") }
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Thông báo")
            Switch(checked = settings.notificationsEnabled, onCheckedChange = viewModel::setNotificationsEnabled)
        }
        if (isAdmin) {
            Button(onClick = onOpenAdmin, modifier = Modifier.fillMaxWidth()) {
                Text("Quản trị ứng dụng")
            }
        }
        AboutPinkNoteCard()
        Button(onClick = viewModel::sendChangePasswordEmail, modifier = Modifier.fillMaxWidth()) {
            Text("Gửi email đổi mật khẩu")
        }
        Button(
            onClick = viewModel::logout,
            enabled = !isLoggingOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoggingOut) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text("Đăng xuất")
            }
        }
        Button(onClick = viewModel::deleteAccount, modifier = Modifier.fillMaxWidth()) {
            Text("Xóa tài khoản")
        }
    }
}

@Composable
private fun AboutPinkNoteCard() {
    PinkCard {
        Text("About PinkNote", style = MaterialTheme.typography.titleMedium)
        Text("Version: ${Constants.APP_VERSION}", style = MaterialTheme.typography.bodyMedium)
        Text("Developer: ${Constants.DEVELOPER_NAME}", style = MaterialTheme.typography.bodyMedium)
        Text("Contact: ${Constants.SUPPORT_EMAIL}", style = MaterialTheme.typography.bodyMedium)
        Text(Constants.PRIVACY_POLICY, style = MaterialTheme.typography.bodyMedium)
        Text(
            "Open-source libraries: Kotlin, Jetpack Compose, Hilt, Room, Firebase, WorkManager, MPAndroidChart.",
            style = MaterialTheme.typography.bodyMedium
        )
        Text("Medical Disclaimer", style = MaterialTheme.typography.titleSmall)
        Text(
            "This application is intended for educational and personal health tracking purposes only.\n\n" +
                "It does not provide medical diagnosis, treatment, or professional medical advice.\n\n" +
                "Predictions of menstruation, ovulation, and fertility are estimates based on user-entered data and statistical models.\n\n" +
                "The application should not be used as a method of contraception or pregnancy planning.\n\n" +
                "Please consult a qualified healthcare professional for any medical concerns.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
