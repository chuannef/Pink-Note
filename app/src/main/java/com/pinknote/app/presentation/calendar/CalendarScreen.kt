package com.pinknote.app.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.domain.model.CalendarDay
import com.pinknote.app.domain.model.CalendarDayType
import com.pinknote.app.domain.model.FertilityEstimate
import com.pinknote.app.domain.model.FertilityLevel
import com.pinknote.app.presentation.common.PinkCard
import com.pinknote.app.presentation.common.PinkPage
import com.pinknote.app.presentation.theme.BlushSurface
import com.pinknote.app.presentation.theme.CalmGray
import com.pinknote.app.presentation.theme.FertileYellow
import com.pinknote.app.presentation.theme.OvulationGreen
import com.pinknote.app.presentation.theme.PeriodRed
import com.pinknote.app.presentation.theme.RoseDeep
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    onOpenDailyLog: (String) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val monthFormatter = DateTimeFormatter.ofPattern("MM/yyyy")
    val selectedDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val selectedDay = state.days.firstOrNull { it.date == state.selectedDate }

    PinkPage {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Lịch chu kỳ", style = MaterialTheme.typography.headlineMedium)
                Text(
                    if (state.hasCycleSetup) {
                        "Chọn một ngày để xem xác suất quanh ngày đó, hoặc mở nhật ký để ghi lại triệu chứng."
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
                        Text(state.month.format(monthFormatter), style = MaterialTheme.typography.headlineSmall)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(330.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(calendarGridCells(state.days)) { cell ->
                        if (cell.day == null) {
                            Spacer(modifier = Modifier.aspectRatio(1f))
                        } else {
                            CalendarDayCell(
                                day = cell.day,
                                selected = cell.day.date == state.selectedDate,
                                today = cell.day.date == LocalDate.now(),
                                onClick = { viewModel.selectDate(cell.day.date) }
                            )
                        }
                    }
                }
            }

            if (state.hasCycleSetup) {
                PinkCard(containerColor = BlushSurface) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Ngày đang chọn", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text(state.selectedDate.format(selectedDateFormatter), style = MaterialTheme.typography.titleLarge)
                            selectedDay?.let {
                                Text(
                                    it.type.viLabel(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (selectedDay?.hasLog == true) {
                            Text("Đã ghi", style = MaterialTheme.typography.labelLarge, color = RoseDeep)
                        }
                    }
                    Button(onClick = { onOpenDailyLog(state.selectedDate.toString()) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Mở nhật ký ngày này")
                    }
                }

                FertilityWindowCard(estimates = state.selectedFertilityWindow, selectedDate = state.selectedDate)

                PinkCard {
                    Text("Chú thích", style = MaterialTheme.typography.titleMedium)
                    LegendRow("Hành kinh", PeriodRed)
                    LegendRow("Hành kinh dự kiến", PeriodRed.copy(alpha = 0.55f))
                    LegendRow("Trễ kinh", Color(0xFFB71C1C))
                    LegendRow("Rụng trứng ước tính", OvulationGreen)
                    LegendRow("Cửa sổ thụ thai", FertileYellow)
                    LegendRow("Tiền kinh nguyệt", Color(0xFFCE93D8))
                    LegendRow("Ngày bình thường", CalmGray.copy(alpha = 0.5f))
                    LegendRow("Có nhật ký", MaterialTheme.colorScheme.primary)
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
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = Color(0xFF38232B),
                style = MaterialTheme.typography.bodyMedium
            )
            if (day.hasLog) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun FertilityWindowCard(estimates: List<FertilityEstimate>, selectedDate: LocalDate) {
    if (estimates.isEmpty()) return
    val selectedEstimate = estimates.firstOrNull { it.date == selectedDate }
    PinkCard {
        Text("% thụ thai quanh ngày chọn", style = MaterialTheme.typography.titleMedium)
        Text(
            selectedEstimate?.let { "${it.probabilityPercent}% - ${it.level.viLabel()}" } ?: "--",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            estimates.forEach { estimate ->
                FertilityPercentColumn(
                    estimate = estimate,
                    selected = estimate.date == selectedDate,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FertilityPercentColumn(
    estimate: FertilityEstimate,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val barColor = when (estimate.level) {
        FertilityLevel.PEAK,
        FertilityLevel.VERY_HIGH -> RoseDeep
        FertilityLevel.HIGH -> OvulationGreen
        FertilityLevel.MEDIUM -> FertileYellow
        FertilityLevel.LOW,
        FertilityLevel.VERY_LOW -> CalmGray
    }
    val barHeight = (24 + estimate.probabilityPercent * 1.4f).dp
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) BlushSurface else Color.Transparent)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .height(barHeight)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(barColor.copy(alpha = if (selected) 0.9f else 0.55f))
        )
        Text("${estimate.probabilityPercent}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(
            estimate.date.format(DateTimeFormatter.ofPattern("dd/MM")),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
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

private data class CalendarGridCell(val day: CalendarDay?)

private fun calendarGridCells(days: List<CalendarDay>): List<CalendarGridCell> {
    if (days.isEmpty()) return emptyList()
    val leadingEmptyCells = days.first().date.dayOfWeek.value - 1
    val cells = List(leadingEmptyCells) { CalendarGridCell(null) } + days.map { CalendarGridCell(it) }
    val trailingEmptyCells = (7 - cells.size % 7) % 7
    return cells + List(trailingEmptyCells) { CalendarGridCell(null) }
}

private fun CalendarDayType.viLabel(): String {
    return when (this) {
        CalendarDayType.PERIOD -> "Hành kinh"
        CalendarDayType.PREDICTED_PERIOD -> "Hành kinh dự kiến"
        CalendarDayType.LATE_PERIOD -> "Trễ kinh"
        CalendarDayType.OVULATION -> "Rụng trứng ước tính"
        CalendarDayType.FERTILE -> "Cửa sổ thụ thai"
        CalendarDayType.PMS -> "Tiền kinh nguyệt"
        CalendarDayType.NORMAL -> "Ngày bình thường"
    }
}

private fun FertilityLevel.viLabel(): String {
    return when (this) {
        FertilityLevel.VERY_LOW -> "rất thấp"
        FertilityLevel.LOW -> "thấp"
        FertilityLevel.MEDIUM -> "trung bình"
        FertilityLevel.HIGH -> "cao"
        FertilityLevel.VERY_HIGH -> "rất cao"
        FertilityLevel.PEAK -> "cao nhất"
    }
}
