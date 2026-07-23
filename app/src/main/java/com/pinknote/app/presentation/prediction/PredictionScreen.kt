package com.pinknote.app.presentation.prediction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.presentation.common.PinkCard
import com.pinknote.app.presentation.common.PinkPage
import com.pinknote.app.presentation.home.HomeViewModel
import com.pinknote.app.presentation.theme.BlushSurface
import com.pinknote.app.utils.DateUtils.toStorageString

@Composable
fun PredictionScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val prediction = state.prediction

    PinkPage {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Dự đoán chu kỳ", style = MaterialTheme.typography.headlineMedium)
            Text("Các mốc dự đoán dựa trên thiết lập chu kỳ hiện tại của bạn.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (prediction == null) {
            PinkCard {
                Text("Chưa có dữ liệu chu kỳ", style = MaterialTheme.typography.titleMedium)
                Text("Quay lại Home để nhập ngày bắt đầu kỳ kinh gần nhất.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            PinkCard(containerColor = BlushSurface) {
                Text("Trạng thái hôm nay", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(prediction.countdownText, style = MaterialTheme.typography.titleLarge)
            }
            PredictionRow(Icons.Default.CalendarMonth, "Bắt đầu kỳ tiếp theo", prediction.nextPeriodStart.toStorageString())
            PredictionRow(Icons.Default.WaterDrop, "Kết thúc kỳ tiếp theo", prediction.nextPeriodEnd.toStorageString())
            PredictionRow(Icons.Default.Eco, "Ngày rụng trứng", prediction.ovulationDate.toStorageString())
            PredictionRow(
                Icons.Default.Favorite,
                "Khoảng dễ mang thai",
                "${prediction.fertileStart.toStorageString()} - ${prediction.fertileEnd.toStorageString()}"
            )
            PredictionRow(Icons.Default.CalendarMonth, "Ngày trong chu kỳ", "Ngày ${prediction.cycleDay}")
        }
    }
}

@Composable
private fun PredictionRow(icon: ImageVector, label: String, value: String) {
    PinkCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column {
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
