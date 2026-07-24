package com.pinknote.app.presentation.dailylog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.presentation.common.PinkCard
import com.pinknote.app.presentation.common.PinkPage
import com.pinknote.app.presentation.common.PinkPrimaryButton
import com.pinknote.app.presentation.theme.CreamWhite
import java.time.LocalDate

@Composable
fun DailyLogScreen(
    dateText: String,
    onSaved: () -> Unit,
    viewModel: DailyLogViewModel = hiltViewModel()
) {
    val date = runCatching { LocalDate.parse(dateText) }.getOrElse { LocalDate.now() }
    val savedLog by viewModel.log.collectAsState()
    var pain by remember { mutableFloatStateOf(0f) }
    var mood by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var isPeriodDay by remember { mutableStateOf<Boolean?>(null) }
    var discharge by remember { mutableStateOf("") }
    var medicines by remember { mutableStateOf("") }
    var hadSex by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var selectedSymptoms by remember { mutableStateOf(setOf<String>()) }
    val symptomOptions = listOf("Đau lưng", "Mệt mỏi", "Đầy hơi", "Đau đầu", "Chuột rút")

    LaunchedEffect(date) {
        viewModel.setDate(date)
        pain = 0f
        mood = ""
        temperature = ""
        weight = ""
        isPeriodDay = null
        discharge = ""
        medicines = ""
        hadSex = false
        note = ""
        selectedSymptoms = emptySet()
    }

    LaunchedEffect(Unit) {
        viewModel.saveEvents.collect {
            onSaved()
        }
    }

    LaunchedEffect(savedLog) {
        savedLog?.takeIf { it.date == date }?.let {
            pain = it.painLevel.toFloat()
            mood = it.mood
            temperature = it.bodyTemperature?.toString().orEmpty()
            weight = it.weightKg?.toString().orEmpty()
            isPeriodDay = it.isPeriodDay
            discharge = it.discharge
            medicines = it.medicines
            hadSex = it.hadSex
            note = it.note
            selectedSymptoms = it.symptoms.toSet()
        }
    }

    PinkPage {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Nhật ký ngày $date", style = MaterialTheme.typography.headlineMedium)
                Text("Ghi lại những tín hiệu nhỏ để PinkNote hiểu chu kỳ của bạn hơn.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            PinkCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Mức đau hôm nay: ${pain.toInt()}/10", style = MaterialTheme.typography.titleMedium)
                }
                Slider(value = pain, onValueChange = { pain = it }, valueRange = 0f..10f, steps = 9)
                PinkField(value = mood, onValueChange = { mood = it }, label = "Tâm trạng")
            }
            PeriodConfirmationCard(
                value = isPeriodDay,
                onValueChange = { isPeriodDay = it }
            )
            PinkCard {
                Text("Chỉ số cơ thể", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    PinkField(
                        value = temperature,
                        onValueChange = { temperature = it },
                        label = "Nhiệt độ",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    PinkField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = "Cân nặng",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            PinkCard {
                Text("Triệu chứng", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    symptomOptions.take(3).forEach { symptom ->
                        SymptomChip(symptom, selectedSymptoms.contains(symptom)) {
                            selectedSymptoms = selectedSymptoms.toggle(symptom)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    symptomOptions.drop(3).forEach { symptom ->
                        SymptomChip(symptom, selectedSymptoms.contains(symptom)) {
                            selectedSymptoms = selectedSymptoms.toggle(symptom)
                        }
                    }
                }
                PinkField(value = discharge, onValueChange = { discharge = it }, label = "Khí hư")
                PinkField(value = medicines, onValueChange = { medicines = it }, label = "Thuốc đã uống")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Quan hệ", style = MaterialTheme.typography.bodyLarge)
                    }
                    Switch(checked = hadSex, onCheckedChange = { hadSex = it })
                }
            }
            PinkCard {
                PinkField(value = note, onValueChange = { note = it }, label = "Ghi chú", minLines = 4)
                PinkPrimaryButton(
                    onClick = {
                        viewModel.saveLog(
                            painLevel = pain.toInt(),
                            mood = mood,
                            bodyTemperature = temperature.toFloatOrNull(),
                            weightKg = weight.toFloatOrNull(),
                            isPeriodDay = isPeriodDay,
                            symptoms = selectedSymptoms.toList(),
                            discharge = discharge,
                            medicines = medicines,
                            hadSex = hadSex,
                            note = note
                        )
                    }
                ) {
                    Text("Lưu ghi chú")
                }
            }
        }
    }
}

@Composable
private fun PeriodConfirmationCard(
    value: Boolean?,
    onValueChange: (Boolean?) -> Unit
) {
    PinkCard {
        Text("Xác nhận hành kinh", style = MaterialTheme.typography.titleMedium)
        Text(
            "Chọn trạng thái thực tế để lịch ưu tiên dữ liệu bạn xác nhận thay vì chỉ dùng dự đoán.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = value == true,
                onClick = { onValueChange(if (value == true) null else true) },
                label = { Text("Có hành kinh") }
            )
            FilterChip(
                selected = value == false,
                onClick = { onValueChange(if (value == false) null else false) },
                label = { Text("Không hành kinh") }
            )
        }
    }
}

@Composable
private fun SymptomChip(symptom: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(symptom) }
    )
}

@Composable
private fun PinkField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
            focusedContainerColor = CreamWhite,
            unfocusedContainerColor = CreamWhite
        ),
        modifier = modifier.fillMaxWidth()
    )
}

private fun Set<String>.toggle(value: String): Set<String> {
    return if (contains(value)) this - value else this + value
}
