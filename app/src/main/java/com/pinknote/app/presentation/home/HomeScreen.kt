package com.pinknote.app.presentation.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.pinknote.app.domain.model.DailyLog
import com.pinknote.app.presentation.common.PinkCard
import com.pinknote.app.presentation.common.PinkMetric
import com.pinknote.app.presentation.common.PinkPage
import com.pinknote.app.presentation.common.PinkPrimaryButton
import com.pinknote.app.presentation.theme.BlushSurface
import com.pinknote.app.presentation.theme.CreamWhite
import com.pinknote.app.presentation.theme.PastelPink
import com.pinknote.app.presentation.theme.RoseDeep
import com.pinknote.app.presentation.statistics.StatisticsUiState
import com.pinknote.app.presentation.statistics.StatisticsViewModel
import com.pinknote.app.utils.DateUtils.toStorageString
import java.time.LocalDate

@Composable
fun HomeScreen(
    onOpenPrediction: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    statisticsViewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val statisticsState by statisticsViewModel.uiState.collectAsState()
    val prediction = state.prediction
    val progress by animateFloatAsState(
        targetValue = prediction?.cycleProgress ?: 0f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 90f),
        label = "cycle-progress"
    )

    PinkPage {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header(name = state.user?.name?.ifBlank { "PinkNote" } ?: "PinkNote")
            CycleHero(
                progress = progress,
                countdownText = prediction?.countdownText ?: "Thiết lập chu kỳ để PinkNote dự đoán chính xác hơn.",
                onOpenPrediction = onOpenPrediction
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PinkMetric(
                    "Kỳ tiếp theo",
                    prediction?.nextPeriodStart?.toStorageString().orEmpty(),
                    modifier = Modifier.weight(1f),
                    supporting = "Dự kiến"
                )
                PinkMetric(
                    "Rụng trứng",
                    prediction?.ovulationDate?.toStorageString().orEmpty(),
                    modifier = Modifier.weight(1f),
                    supporting = "Theo chu kỳ"
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PinkMetric(
                    "Ngày chu kỳ",
                    prediction?.cycleDay?.toString().orEmpty(),
                    modifier = Modifier.weight(1f),
                    supporting = "Hiện tại"
                )
                PinkMetric(
                    "Thụ thai hôm nay",
                    prediction?.let { "${it.fertilityTodayPercent}%" }.orEmpty(),
                    modifier = Modifier.weight(1f),
                    supporting = prediction?.fertilityTodayLevel?.name?.replace('_', ' ').orEmpty()
                )
            }
            TipCard()
            StatisticsSummary(statisticsState)
            CycleSetupCard(state = state, onSave = viewModel::saveCycle)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Header(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Xin chào", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(name, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(PastelPink.copy(alpha = 0.28f)),
            contentAlignment = Alignment.Center
        ) {
            Text(name.firstOrNull()?.uppercase() ?: "P", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun CycleHero(progress: Float, countdownText: String, onOpenPrediction: () -> Unit) {
    PinkCard(containerColor = BlushSurface) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(132.dp)) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(132.dp),
                    strokeWidth = 10.dp,
                    trackColor = CreamWhite,
                    color = MaterialTheme.colorScheme.primary
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text("chu kỳ", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Hôm nay", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Text(countdownText, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                PinkPrimaryButton(onClick = onOpenPrediction) {
                    Text("Xem dự đoán")
                }
            }
        }
    }
}

@Composable
private fun StatisticsSummary(state: StatisticsUiState) {
    PinkCard(containerColor = CreamWhite) {
        Text("Thống kê nhanh", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(
                label = "Chu kỳ",
                value = "${state.cycle?.cycleLength ?: 0} ngày",
                modifier = Modifier.weight(1f),
                supporting = "Gần nhất"
            )
            StatTile(
                label = "Hành kinh",
                value = "${state.cycle?.periodLength ?: 0} ngày",
                modifier = Modifier.weight(1f),
                supporting = "Thiết lập"
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(
                label = "Mức đau TB",
                value = "%.1f".format(state.averagePain),
                modifier = Modifier.weight(1f),
                supporting = "Từ nhật ký"
            )
            StatTile(
                label = "Theo dõi",
                value = "${state.trackedMonths} tháng",
                modifier = Modifier.weight(1f),
                supporting = "Có dữ liệu"
            )
        }
        Text("Biểu đồ mức đau", style = MaterialTheme.typography.titleSmall)
        PainTrendChart(logs = state.logs)
    }
}

@Composable
private fun PainTrendChart(logs: List<DailyLog>) {
    val chartLogs = remember(logs) { logs.sortedBy { it.date }.takeLast(12) }
    val entries = remember(chartLogs) {
        chartLogs.mapIndexed { index, log -> Entry(index.toFloat(), log.painLevel.toFloat()) }
    }
    val labels = remember(chartLogs) {
        chartLogs.map { "${it.date.dayOfMonth}/${it.date.monthValue}" }
    }

    if (entries.isEmpty()) {
        Text(
            "Chưa có nhật ký để vẽ biểu đồ. Hãy ghi lại mức đau mỗi ngày để PinkNote tổng hợp xu hướng cho bạn.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val primaryColor = RoseDeep.toArgb()
    val surfaceColor = CreamWhite.toArgb()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(false)
                setScaleEnabled(false)
                setDrawGridBackground(false)
                setNoDataText("")
                axisRight.isEnabled = false
                axisLeft.axisMinimum = 0f
                axisLeft.axisMaximum = 10f
                axisLeft.granularity = 1f
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.setDrawGridLines(false)
                extraBottomOffset = 8f
                minOffset = 12f
            }
        },
        update = { chart ->
            val dataSet = LineDataSet(entries, "Mức đau").apply {
                color = primaryColor
                valueTextColor = textColor
                lineWidth = 2.8f
                circleRadius = 4.5f
                setCircleColor(primaryColor)
                setDrawCircleHole(true)
                circleHoleColor = surfaceColor
                setDrawFilled(true)
                fillColor = primaryColor
                fillAlpha = 36
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawValues(entries.size <= 7)
            }

            chart.axisLeft.textColor = textColor
            chart.axisLeft.gridColor = gridColor
            chart.xAxis.textColor = textColor
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.xAxis.labelCount = minOf(labels.size, 5).coerceAtLeast(1)
            chart.data = LineData(dataSet).apply {
                setValueTextSize(10f)
            }
            chart.invalidate()
        }
    )
}

@Composable
private fun StatTile(label: String, value: String, supporting: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(BlushSurface.copy(alpha = 0.72f), RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(supporting, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CycleSetupCard(state: HomeUiState, onSave: (LocalDate, Int, Int) -> Unit) {
    var lastPeriod by remember(state.cycleSettings.lastPeriodStart) { mutableStateOf(state.cycleSettings.lastPeriodStart.toStorageString()) }
    var cycleLength by remember(state.cycleSettings.cycleLength) { mutableStateOf(state.cycleSettings.cycleLength.toString()) }
    var periodLength by remember(state.cycleSettings.periodLength) { mutableStateOf(state.cycleSettings.periodLength.toString()) }

    PinkCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column {
                Text("Thiết lập chu kỳ", style = MaterialTheme.typography.titleMedium)
                Text("Bạn có thể chỉnh lại bất cứ lúc nào.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        PinkField(
            value = lastPeriod,
            onValueChange = { lastPeriod = it },
            label = "Ngày bắt đầu gần nhất yyyy-MM-dd"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            PinkField(
                value = cycleLength,
                onValueChange = { cycleLength = it },
                label = "Chu kỳ",
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Number
            )
            PinkField(
                value = periodLength,
                onValueChange = { periodLength = it },
                label = "Hành kinh",
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Number
            )
        }
        PinkPrimaryButton(
            onClick = {
                val date = runCatching { LocalDate.parse(lastPeriod) }.getOrElse { LocalDate.now() }
                onSave(date, cycleLength.toIntOrNull() ?: 28, periodLength.toIntOrNull() ?: 5)
            }
        ) {
            Text("Lưu thiết lập")
        }
    }
}

@Composable
private fun PinkField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
            focusedContainerColor = CreamWhite,
            unfocusedContainerColor = CreamWhite
        )
    )
}

@Composable
private fun TipCard() {
    PinkCard(containerColor = CreamWhite) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.LocalDrink, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Gợi ý hôm nay", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Uống đủ nước, ngủ đúng giờ và ghi lại triệu chứng. Dữ liệu nhỏ mỗi ngày giúp dự đoán dễ tin hơn.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
