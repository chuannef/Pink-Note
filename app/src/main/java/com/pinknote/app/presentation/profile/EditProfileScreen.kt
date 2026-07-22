package com.pinknote.app.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate

@Composable
fun EditProfileScreen(
    onSaved: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()
    var name by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var cycleLength by remember { mutableStateOf("28") }
    var periodLength by remember { mutableStateOf("5") }

    LaunchedEffect(profile) {
        profile?.let {
            name = it.name
            birthday = it.birthday?.toString().orEmpty()
            height = it.heightCm?.toString().orEmpty()
            weight = it.weightKg?.toString().orEmpty()
            goal = it.healthGoal
            cycleLength = it.averageCycleLength.toString()
            periodLength = it.periodLength.toString()
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Chỉnh sửa hồ sơ", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = birthday, onValueChange = { birthday = it }, label = { Text("Ngày sinh yyyy-MM-dd") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Chiều cao") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Cân nặng") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
        }
        OutlinedTextField(value = goal, onValueChange = { goal = it }, label = { Text("Mục tiêu sức khỏe") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = cycleLength, onValueChange = { cycleLength = it }, label = { Text("Chu kỳ TB") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = periodLength, onValueChange = { periodLength = it }, label = { Text("Hành kinh") }, modifier = Modifier.weight(1f))
        }
        Button(
            onClick = {
                profile?.let {
                    viewModel.update(
                        it.copy(
                            name = name,
                            birthday = runCatching { LocalDate.parse(birthday) }.getOrNull(),
                            heightCm = height.toFloatOrNull(),
                            weightKg = weight.toFloatOrNull(),
                            healthGoal = goal,
                            averageCycleLength = cycleLength.toIntOrNull() ?: 28,
                            periodLength = periodLength.toIntOrNull() ?: 5
                        )
                    )
                    onSaved()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lưu hồ sơ")
        }
    }
}
