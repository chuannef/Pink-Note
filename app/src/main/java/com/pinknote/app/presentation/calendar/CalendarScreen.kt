package com.pinknote.app.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.domain.model.CalendarDay
import com.pinknote.app.domain.model.CalendarDayType
import com.pinknote.app.presentation.common.PinkCard
import com.pinknote.app.presentation.common.PinkPage
import com.pinknote.app.presentation.theme.CalmGray
import com.pinknote.app.presentation.theme.FertileYellow
import com.pinknote.app.presentation.theme.OvulationGreen
import com.pinknote.app.presentation.theme.PeriodRed
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    onOpenDailyLog: (String) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("MM/yyyy")

    PinkPage {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Lịch chu kỳ", style = MaterialTheme.typography.headlineMedium)
            Text(
                if (state.hasCycleSetup) {
                    "Chạm vào từng ngày để ghi chú cảm giác, triệu chứng và thuốc đã dùng."
                } else {
                    "Hãy thiết lập chu kỳ ở trang Home để Pink Note bắt đầu dự đoán lịch."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        PinkCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.month.format(formatter), style = MaterialTheme.typography.headlineSmall)
                    Text(
                        if (state.hasCycleSetup) "Theo dõi tháng này" else "Chưa có dữ liệu dự đoán",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau")
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach {
                    Text(it, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth().height(360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.days) { day ->
                    CalendarDayCell(
                        day = day,
                        selected = day.date == state.selectedDate,
                        today = day.date == LocalDate.now(),
                        onClick = {
                            viewModel.selectDate(day.date)
                            onOpenDailyLog(day.date.toString())
                        }
                    )
                }
            }
        }
        if (state.hasCycleSetup) {
            PinkCard {
                Text("Chú thích", style = MaterialTheme.typography.titleMedium)
                LegendRow("Hành kinh", PeriodRed)
                LegendRow("Hành kinh dự kiến", PeriodRed.copy(alpha = 0.55f))
                LegendRow("Trễ kinh", Color(0xFFB71C1C))
                LegendRow("Rụng trứng ước tính", OvulationGreen)
                LegendRow("Cửa sổ thụ thai", FertileYellow)
                LegendRow("Tiền kinh nguyệt", Color(0xFFCE93D8))
                LegendRow("Ngày bình thường", CalmGray.copy(alpha = 0.5f))
            }
        } else {
            PinkCard {
                Text("Chưa thiết lập chu kỳ", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Sau khi nhập ngày bắt đầu kỳ kinh, độ dài chu kỳ và số ngày hành kinh, lịch sẽ hiển thị các kỳ dự đoán tiếp theo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    selected: Boolean,
    today: Boolean,
    onClick: () -> Unit
) {
    val color = when (day.type) {
        CalendarDayType.PERIOD -> PeriodRed
        CalendarDayType.PREDICTED_PERIOD -> PeriodRed.copy(alpha = 0.55f)
        CalendarDayType.LATE_PERIOD -> Color(0xFFB71C1C)
        CalendarDayType.OVULATION -> OvulationGreen
        CalendarDayType.FERTILE -> FertileYellow
        CalendarDayType.PMS -> Color(0xFFCE93D8)
        CalendarDayType.NORMAL -> CalmGray.copy(alpha = 0.28f)
    }
    val borderColor = when {
        selected -> MaterialTheme.colorScheme.primary
        today -> MaterialTheme.colorScheme.secondary
        else -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.92f))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = Color(0xFF38232B),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun LegendRow(label: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.Circle, contentDescription = null, tint = color)
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
