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
import com.pinknote.app.presentation.localization.LocalAppStrings
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
    val strings = LocalAppStrings.current
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
                    strings.nextPeriod,
                    prediction?.nextPeriodStart?.toStorageString().orEmpty(),
                    modifier = Modifier.weight(1f),
                    supporting = strings.estimated
                )
                PinkMetric(
                    strings.ovulation,
                    prediction?.ovulationDate?.toStorageString().orEmpty(),
                    modifier = Modifier.weight(1f),
                    supporting = strings.byCycle
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PinkMetric(
                    strings.cycleDay,
                    prediction?.cycleDay?.toString().orEmpty(),
                    modifier = Modifier.weight(1f),
                    supporting = strings.current
                )
                PinkMetric(
                    strings.fertilityToday,
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
    val strings = LocalAppStrings.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(strings.hello, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val strings = LocalAppStrings.current
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
                    Text(strings.cycle.lowercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(strings.today, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Text(countdownText, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                PinkPrimaryButton(onClick = onOpenPrediction) {
                    Text(strings.viewPrediction)
                }
            }
        }
    }
}

@Composable
private fun StatisticsSummary(state: StatisticsUiState) {
    val strings = LocalAppStrings.current
    PinkCard(containerColor = CreamWhite) {
        Text(strings.quickStats, style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(
                label = strings.cycle,
                value = state.cycle?.let { "${it.cycleLength} ngày" } ?: strings.noData,
                modifier = Modifier.weight(1f),
                supporting = strings.recent
            )
            StatTile(
                label = strings.period,
                value = state.cycle?.let { "${it.periodLength} ngày" } ?: strings.noData,
                modifier = Modifier.weight(1f),
                supporting = strings.configured
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(
                label = strings.averagePain,
                value = "%.1f".format(state.averagePain),
                modifier = Modifier.weight(1f),
                supporting = "Từ nhật ký"
            )
            StatTile(
                label = strings.tracking,
                value = "${state.trackedMonths} ${strings.months}",
                modifier = Modifier.weight(1f),
                supporting = "Có dữ liệu"
            )
        }
        Text(strings.painChart, style = MaterialTheme.typography.titleSmall)
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
    val strings = LocalAppStrings.current
    val settings = state.cycleSettings
    var lastPeriod by remember(settings?.lastPeriodStart) {
        mutableStateOf(settings?.lastPeriodStart?.toStorageString().orEmpty())
    }
    var cycleLength by remember(settings?.cycleLength) {
        mutableStateOf(settings?.cycleLength?.toString().orEmpty())
    }
    var periodLength by remember(settings?.periodLength) {
        mutableStateOf(settings?.periodLength?.toString().orEmpty())
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    PinkCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column {
                Text(strings.cycleSetup, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (state.isEmpty) strings.firstCycleSetupPrompt else strings.editCycleSetupPrompt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        PinkField(
            value = lastPeriod,
            onValueChange = {
                lastPeriod = it
                errorMessage = null
            },
            label = strings.lastPeriodDateLabel
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            PinkField(
                value = cycleLength,
                onValueChange = {
                    cycleLength = it
                    errorMessage = null
                },
                label = strings.cycleLengthLabel,
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Number
            )
            PinkField(
                value = periodLength,
                onValueChange = {
                    periodLength = it
                    errorMessage = null
                },
                label = strings.periodLengthLabel,
                modifier = Modifier.weight(1f),
                keyboardType = KeyboardType.Number
            )
        }
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        PinkPrimaryButton(
            onClick = {
                val date = runCatching { LocalDate.parse(lastPeriod) }.getOrNull()
                val cycleDays = cycleLength.toIntOrNull()
                val periodDays = periodLength.toIntOrNull()
                errorMessage = when {
                    date == null -> strings.invalidDate
                    cycleDays == null || cycleDays <= 0 -> strings.invalidCycleLength
                    periodDays == null || periodDays <= 0 -> strings.invalidPeriodLength
                    periodDays >= cycleDays -> strings.periodShorterThanCycle
                    else -> null
                }
                if (date != null && cycleDays != null && periodDays != null && errorMessage == null) {
                    onSave(date, cycleDays, periodDays)
                }
            }
        ) {
            Text(strings.saveCycleSettings)
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
    val strings = LocalAppStrings.current
    PinkCard(containerColor = CreamWhite) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.LocalDrink, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(strings.todayTips, style = MaterialTheme.typography.titleMedium)
                Text(
                    strings.todayTipsBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
