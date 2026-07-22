package com.pinknote.app.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import com.pinknote.app.presentation.theme.CalmGray
import com.pinknote.app.presentation.theme.FertileYellow
import com.pinknote.app.presentation.theme.OvulationGreen
import com.pinknote.app.presentation.theme.PeriodRed
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    onOpenDailyLog: (String) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("MM/yyyy")

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = viewModel::previousMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước")
            }
            Text(state.month.format(formatter), style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = viewModel::nextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau")
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach {
                Text(it, fontWeight = FontWeight.Bold)
            }
        }

        LazyVerticalGrid(columns = GridCells.Fixed(7), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.days) { day ->
                CalendarDayCell(day = day, onClick = {
                    viewModel.selectDate(day.date)
                    onOpenDailyLog(day.date.toString())
                })
            }
        }

        Text("Đỏ: hành kinh, xanh: rụng trứng, vàng: dễ mang thai, xám: bình thường", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CalendarDayCell(day: CalendarDay, onClick: () -> Unit) {
    val color = when (day.type) {
        CalendarDayType.PERIOD -> PeriodRed
        CalendarDayType.OVULATION -> OvulationGreen
        CalendarDayType.FERTILE -> FertileYellow
        CalendarDayType.NORMAL -> CalmGray.copy(alpha = 0.35f)
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (day.hasLog) "${day.date.dayOfMonth}*" else day.date.dayOfMonth.toString(),
            color = Color.Black,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
