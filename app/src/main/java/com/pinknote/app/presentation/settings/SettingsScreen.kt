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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.domain.model.AppLanguage
import com.pinknote.app.domain.model.ThemeMode
import com.pinknote.app.presentation.common.PinkCard
import com.pinknote.app.presentation.localization.AppStrings
import com.pinknote.app.presentation.localization.LocalAppStrings
import com.pinknote.app.utils.Constants

@Composable
fun SettingsScreen(
    onOpenAdmin: () -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val strings = LocalAppStrings.current
    val settings by viewModel.settings.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val isLoggingOut by viewModel.isLoggingOut.collectAsState()
    val isDeletingAccount by viewModel.isDeletingAccount.collectAsState()
    val deleteAccountMessage by viewModel.deleteAccountMessage.collectAsState()
    val logoutCompleted by viewModel.logoutCompleted.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteConfirmationText by remember { mutableStateOf("") }

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
        Text(strings.settings, style = MaterialTheme.typography.headlineMedium)
        Text(strings.theme, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = settings.themeMode == mode,
                    onClick = { viewModel.setTheme(mode) },
                    label = { Text(strings.themeModeLabel(mode)) }
                )
            }
        }
        Text(strings.language, style = MaterialTheme.typography.titleMedium)
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
            Text(strings.notifications)
            Switch(checked = settings.notificationsEnabled, onCheckedChange = viewModel::setNotificationsEnabled)
        }
        if (isAdmin) {
            Button(onClick = onOpenAdmin, modifier = Modifier.fillMaxWidth()) {
                Text(strings.adminConsole)
            }
        }
        AboutPinkNoteCard()
        Button(onClick = viewModel::sendChangePasswordEmail, modifier = Modifier.fillMaxWidth()) {
            Text(strings.sendPasswordEmail)
        }
        Button(
            onClick = viewModel::logout,
            enabled = !isLoggingOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoggingOut) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(strings.logout)
            }
        }
        deleteAccountMessage?.let { message ->
            PinkCard {
                Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
        }
        Button(
            onClick = {
                deleteConfirmationText = ""
                showDeleteDialog = true
            },
            enabled = !isDeletingAccount && !isLoggingOut,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            if (isDeletingAccount) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text(strings.deleteAccount)
            }
        }
    }

    if (showDeleteDialog) {
        DeleteAccountConfirmationDialog(
            confirmationText = deleteConfirmationText,
            isDeleting = isDeletingAccount,
            onConfirmationTextChange = { deleteConfirmationText = it },
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteAccount()
            }
        )
    }
}

@Composable
private fun DeleteAccountConfirmationDialog(
    confirmationText: String,
    isDeleting: Boolean,
    onConfirmationTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isDeleting) onDismiss()
        },
        title = { Text("Xác nhận xóa tài khoản") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Thao tác này sẽ xóa tài khoản, hồ sơ, chu kỳ, nhật ký, nhắc nhở và dữ liệu đã đồng bộ của bạn. Không thể hoàn tác.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Nhập $DELETE_ACCOUNT_CONFIRMATION_TEXT để xác nhận.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = confirmationText,
                    onValueChange = onConfirmationTextChange,
                    label = { Text(DELETE_ACCOUNT_CONFIRMATION_TEXT) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isDeleteAccountConfirmationValid(confirmationText) && !isDeleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Xóa vĩnh viễn")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text("Hủy")
            }
        }
    )
}

private fun AppStrings.themeModeLabel(mode: ThemeMode): String {
    return when (mode) {
        ThemeMode.SYSTEM -> themeSystem
        ThemeMode.LIGHT -> themeLight
        ThemeMode.DARK -> themeDark
    }
}

@Composable
private fun AboutPinkNoteCard() {
    val strings = LocalAppStrings.current
    PinkCard {
        Text(strings.about, style = MaterialTheme.typography.titleMedium)
        Text("${strings.version}: ${Constants.APP_VERSION}", style = MaterialTheme.typography.bodyMedium)
        Text("${strings.developer}: ${Constants.DEVELOPER_NAME}", style = MaterialTheme.typography.bodyMedium)
        Text("${strings.contact}: ${Constants.SUPPORT_EMAIL}", style = MaterialTheme.typography.bodyMedium)
        Text(Constants.PRIVACY_POLICY, style = MaterialTheme.typography.bodyMedium)
        Text(
            "Open-source libraries: Kotlin, Jetpack Compose, Hilt, Room, Firebase, WorkManager, MPAndroidChart.",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(strings.medicalDisclaimer, style = MaterialTheme.typography.titleSmall)
        Text(
            strings.medicalDisclaimerBody,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
