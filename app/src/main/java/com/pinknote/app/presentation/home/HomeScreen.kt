package com.pinknote.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.utils.DateUtils.toStorageString
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    onOpenPrediction: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val prediction = state.prediction
    val progress = prediction?.let {
        val distance = ChronoUnit.DAYS.between(LocalDate.now(), it.nextPeriodStart).toFloat()
        1f - (distance / state.cycleSettings.cycleLength).coerceIn(0f, 1f)
    } ?: 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Text((state.user?.name?.firstOrNull() ?: 'C').toString(), style = MaterialTheme.typography.titleLarge)
            }
            Column {
                Text("Xin chào", style = MaterialTheme.typography.bodyMedium)
                Text(state.user?.name?.ifBlank { "PinkNote" } ?: "PinkNote", style = MaterialTheme.typography.headlineSmall)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(132.dp))
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.titleLarge)
                }
                Text(prediction?.countdownText ?: "Thiết lập chu kỳ để bắt đầu", style = MaterialTheme.typography.titleMedium)
                Button(onClick = onOpenPrediction) {
                    Icon(Icons.Default.Favorite, contentDescription = null)
                    Text("Xem dự đoán", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            InfoCard(
                title = "Kỳ tiếp theo",
                value = prediction?.nextPeriodStart?.toStorageString().orEmpty(),
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                title = "Rụng trứng",
                value = prediction?.ovulationDate?.toStorageString().orEmpty(),
                modifier = Modifier.weight(1f)
            )
        }

        CycleSetupCard(state = state, onSave = viewModel::saveCycle)

        Card(shape = RoundedCornerShape(8.dp)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text("Today's Tips", style = MaterialTheme.typography.titleMedium)
                    Text("Uống đủ nước, ngủ đúng giờ và ghi lại triệu chứng để dự đoán chính xác hơn.")
                }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value.ifBlank { "--" }, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun CycleSetupCard(state: HomeUiState, onSave: (LocalDate, Int, Int) -> Unit) {
    var lastPeriod by remember(state.cycleSettings.lastPeriodStart) { mutableStateOf(state.cycleSettings.lastPeriodStart.toStorageString()) }
    var cycleLength by remember(state.cycleSettings.cycleLength) { mutableStateOf(state.cycleSettings.cycleLength.toString()) }
    var periodLength by remember(state.cycleSettings.periodLength) { mutableStateOf(state.cycleSettings.periodLength.toString()) }

    Card(shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Thiết lập chu kỳ", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = lastPeriod,
                onValueChange = { lastPeriod = it },
                label = { Text("Ngày bắt đầu gần nhất yyyy-MM-dd") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = cycleLength,
                    onValueChange = { cycleLength = it },
                    label = { Text("Chu kỳ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = periodLength,
                    onValueChange = { periodLength = it },
                    label = { Text("Hành kinh") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Button(
                onClick = {
                    val date = runCatching { LocalDate.parse(lastPeriod) }.getOrElse { LocalDate.now() }
                    onSave(date, cycleLength.toIntOrNull() ?: 28, periodLength.toIntOrNull() ?: 5)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Lưu thiết lập")
            }
        }
    }
    Spacer(Modifier.height(8.dp))
}
