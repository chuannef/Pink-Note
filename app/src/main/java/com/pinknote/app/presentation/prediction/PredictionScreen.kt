package com.pinknote.app.presentation.prediction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.presentation.home.HomeViewModel
import com.pinknote.app.utils.DateUtils.toStorageString

@Composable
fun PredictionScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val prediction = state.prediction

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Dự đoán chu kỳ", style = MaterialTheme.typography.headlineMedium)
        if (prediction == null) {
            Text("Chưa có dữ liệu chu kỳ")
        } else {
            PredictionRow("Trạng thái hôm nay", prediction.countdownText)
            PredictionRow("Bắt đầu kỳ tiếp theo", prediction.nextPeriodStart.toStorageString())
            PredictionRow("Kết thúc kỳ tiếp theo", prediction.nextPeriodEnd.toStorageString())
            PredictionRow("Ngày rụng trứng", prediction.ovulationDate.toStorageString())
            PredictionRow("Khoảng dễ mang thai", "${prediction.fertileStart.toStorageString()} - ${prediction.fertileEnd.toStorageString()}")
            PredictionRow("Ngày trong chu kỳ", "Ngày ${prediction.cycleDay}")
        }
    }
}

@Composable
private fun PredictionRow(label: String, value: String) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
