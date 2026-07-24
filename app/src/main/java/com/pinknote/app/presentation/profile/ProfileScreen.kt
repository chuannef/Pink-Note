package com.pinknote.app.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.utils.DateUtils.ageFromBirthday

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Hồ sơ", style = MaterialTheme.typography.headlineMedium)
        Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(profile?.name?.ifBlank { "Chưa cập nhật tên" } ?: "Chưa cập nhật hồ sơ", style = MaterialTheme.typography.titleLarge)
                Text("Email: ${profile?.email.orEmpty()}")
                Text("Tuổi: ${ageFromBirthday(profile?.birthday)}")
                Text("Chiều cao: ${profile?.heightCm ?: 0f} cm")
                Text("Cân nặng: ${profile?.weightKg ?: 0f} kg")
                Text("Mục tiêu: ${profile?.healthGoal.orEmpty()}")
                Text("Chu kỳ TB: ${profile?.averageCycleLength ?: 28} ngày")
                Text("Số ngày hành kinh: ${profile?.periodLength ?: 5} ngày")
            }
        }
        Button(onClick = onEditProfile, modifier = Modifier.fillMaxWidth()) {
            Text("Chỉnh sửa")
        }
    }
}
