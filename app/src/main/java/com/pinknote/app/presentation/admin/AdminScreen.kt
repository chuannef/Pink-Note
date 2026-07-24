package com.pinknote.app.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.domain.model.UserProfile
import com.pinknote.app.presentation.common.PinkCard
import com.pinknote.app.presentation.common.PinkPage
import com.pinknote.app.presentation.theme.BlushSurface
import com.pinknote.app.presentation.theme.CreamWhite
import com.pinknote.app.presentation.theme.RoseDeep
import com.pinknote.app.utils.AdminPolicy
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AdminScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    PinkPage {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AdminHeader(onBack = onBack)

            if (!state.isAllowed) {
                PinkCard(containerColor = CreamWhite) {
                    Text("Không có quyền truy cập", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Chỉ tài khoản có role admin mới được mở khu vực quản trị.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                return@Column
            }

            state.errorMessage?.let { message ->
                PinkCard(containerColor = CreamWhite) {
                    Text("Lỗi tải dữ liệu", style = MaterialTheme.typography.titleMedium)
                    Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
            }

            AdminSummary(state = state)
            UserManagementCard(
                users = state.users,
                onSetAdmin = { viewModel.setRole(it, AdminPolicy.ROLE_ADMIN) },
                onSetUser = { viewModel.setRole(it, AdminPolicy.ROLE_USER) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AdminHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Admin Console", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Quản lý người dùng PinkNote",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(RoseDeep.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = RoseDeep)
        }
    }
}

@Composable
private fun AdminSummary(state: AdminUiState) {
    PinkCard(containerColor = CreamWhite) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.People, contentDescription = null, tint = RoseDeep)
            Text("Tổng quan", style = MaterialTheme.typography.titleMedium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            AdminMetric("Người dùng", state.users.size.toString(), Modifier.weight(1f))
            AdminMetric("Admin", state.adminCount.toString(), Modifier.weight(1f))
            AdminMetric("User", state.standardUserCount.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun AdminMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(BlushSurface.copy(alpha = 0.75f), RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge, color = RoseDeep)
    }
}

@Composable
private fun UserManagementCard(
    users: List<UserProfile>,
    onSetAdmin: (String) -> Unit,
    onSetUser: (String) -> Unit
) {
    PinkCard(containerColor = CreamWhite) {
        Text("Quản lý tài khoản", style = MaterialTheme.typography.titleMedium)
        if (users.isEmpty()) {
            Text(
                "Chưa có user nào trong Firestore users.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            users.forEach { user ->
                UserRow(user = user, onSetAdmin = onSetAdmin, onSetUser = onSetUser)
            }
        }
    }
}

@Composable
private fun UserRow(
    user: UserProfile,
    onSetAdmin: (String) -> Unit,
    onSetUser: (String) -> Unit
) {
    val isAdmin = AdminPolicy.isAdmin(user.role)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BlushSurface.copy(alpha = 0.44f), RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(RoseDeep.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = RoseDeep)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(user.name.ifBlank { "Chưa có tên" }, style = MaterialTheme.typography.titleSmall)
            Text(user.email.ifBlank { user.uid }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Tạo: ${formatCreatedAt(user.createdAtEpochMillis)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AssistChip(
                onClick = {},
                label = { Text(if (isAdmin) "admin" else "user") }
            )
        }
        if (isAdmin) {
            TextButton(onClick = { onSetUser(user.uid) }) {
                Text("Gỡ admin")
            }
        } else {
            FilledTonalButton(onClick = { onSetAdmin(user.uid) }) {
                Text("Set admin")
            }
        }
    }
}

private fun formatCreatedAt(epochMillis: Long): String {
    return Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}
