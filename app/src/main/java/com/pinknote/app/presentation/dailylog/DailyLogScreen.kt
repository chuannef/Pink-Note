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
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import java.time.LocalDate

@Composable
fun DailyLogScreen(
    dateText: String,
    viewModel: DailyLogViewModel = hiltViewModel()
) {
    val date = runCatching { LocalDate.parse(dateText) }.getOrElse { LocalDate.now() }
    val savedLog by viewModel.log.collectAsState()
    var pain by remember { mutableFloatStateOf(0f) }
    var mood by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var discharge by remember { mutableStateOf("") }
    var medicines by remember { mutableStateOf("") }
    var hadSex by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var selectedSymptoms by remember { mutableStateOf(setOf<String>()) }
    val symptomOptions = listOf("Đau lưng", "Mệt mỏi", "Đầy hơi", "Đau đầu", "Chuột rút")

    LaunchedEffect(date) {
        viewModel.setDate(date)
    }

    LaunchedEffect(savedLog) {
        savedLog?.let {
            pain = it.painLevel.toFloat()
            mood = it.mood
            temperature = it.bodyTemperature?.toString().orEmpty()
            weight = it.weightKg?.toString().orEmpty()
            discharge = it.discharge
            medicines = it.medicines
            hadSex = it.hadSex
            note = it.note
            selectedSymptoms = it.symptoms.toSet()
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Ghi chú ngày $date", style = MaterialTheme.typography.headlineSmall)
        Text("Mức đau: ${pain.toInt()}")
        Slider(value = pain, onValueChange = { pain = it }, valueRange = 0f..10f, steps = 9)
        OutlinedTextField(value = mood, onValueChange = { mood = it }, label = { Text("Tâm trạng") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = temperature,
                onValueChange = { temperature = it },
                label = { Text("Nhiệt độ") },
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
        Text("Triệu chứng")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            symptomOptions.take(3).forEach { symptom ->
                FilterChip(
                    selected = selectedSymptoms.contains(symptom),
                    onClick = {
                        selectedSymptoms = if (selectedSymptoms.contains(symptom)) {
                            selectedSymptoms - symptom
                        } else {
                            selectedSymptoms + symptom
                        }
                    },
                    label = { Text(symptom) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            symptomOptions.drop(3).forEach { symptom ->
                FilterChip(
                    selected = selectedSymptoms.contains(symptom),
                    onClick = {
                        selectedSymptoms = if (selectedSymptoms.contains(symptom)) selectedSymptoms - symptom else selectedSymptoms + symptom
                    },
                    label = { Text(symptom) }
                )
            }
        }
        OutlinedTextField(value = discharge, onValueChange = { discharge = it }, label = { Text("Khí hư") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = medicines, onValueChange = { medicines = it }, label = { Text("Thuốc đã uống") }, modifier = Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Quan hệ")
            Switch(checked = hadSex, onCheckedChange = { hadSex = it })
        }
        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Ghi chú") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        Button(
            onClick = {
                viewModel.saveLog(
                    painLevel = pain.toInt(),
                    mood = mood,
                    bodyTemperature = temperature.toFloatOrNull(),
                    weightKg = weight.toFloatOrNull(),
                    symptoms = selectedSymptoms.toList(),
                    discharge = discharge,
                    medicines = medicines,
                    hadSex = hadSex,
                    note = note
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lưu ghi chú")
        }
    }
}
