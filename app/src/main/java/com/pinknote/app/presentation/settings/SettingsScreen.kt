package com.pinknote.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.domain.model.AppLanguage
import com.pinknote.app.domain.model.ThemeMode

@Composable
fun SettingsScreen(
    onOpenAdmin: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
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
        Button(onClick = viewModel::sendChangePasswordEmail, modifier = Modifier.fillMaxWidth()) {
            Text("Gửi email đổi mật khẩu")
        }
        Button(onClick = viewModel::logout, modifier = Modifier.fillMaxWidth()) {
            Text("Đăng xuất")
        }
        Button(onClick = viewModel::deleteAccount, modifier = Modifier.fillMaxWidth()) {
            Text("Xóa tài khoản")
        }
    }
}
