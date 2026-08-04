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
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.pinknote.app.domain.model.AppLanguage
import com.pinknote.app.domain.model.AppMode
import com.pinknote.app.domain.model.DailyLog
import com.pinknote.app.domain.model.PregnancySettings
import com.pinknote.app.domain.model.PregnancySummary
import com.pinknote.app.domain.model.PregnancyTrimester
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
            ModeSwitcher(
                selectedMode = state.appMode,
                language = strings.languageCode,
                onModeSelected = viewModel::saveAppMode
            )
            if (state.appMode == AppMode.PREGNANCY) {
                PregnancyHomeContent(
                    summary = state.pregnancySummary,
                    settings = state.pregnancySettings,
                    language = strings.languageCode,
                    onSave = viewModel::savePregnancy
                )
                Spacer(Modifier.height(8.dp))
                return@Column
            }
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
                TipCard(uid = state.user?.uid.orEmpty())
                CycleEducationCard(strings.languageCode)
                StatisticsSummary(statisticsState)
                if (state.isEmpty) {
                    CycleSetupCard(state = state, onSave = viewModel::saveCycle)
                }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ModeSwitcher(
    selectedMode: AppMode,
    language: AppLanguage,
    onModeSelected: (AppMode) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        AppMode.entries.forEach { mode ->
            FilterChip(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                label = { Text(appModeLabel(mode, language)) }
            )
        }
    }
}

@Composable
private fun PregnancyHomeContent(
    summary: PregnancySummary?,
    settings: PregnancySettings?,
    language: AppLanguage,
    onSave: (LocalDate?, LocalDate?) -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = summary?.progress ?: 0f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 90f),
        label = "pregnancy-progress"
    )

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
                    Text(
                        summary?.let { "${it.gestationalWeek}w" } ?: "--",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(pregnancyWeekLabel(language), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(pregnancyModeTitle(language), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    summary?.let { pregnancyStatusText(it, language) } ?: pregnancySetupPrompt(language),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        PinkMetric(
            label = dueDateLabel(language),
            value = summary?.dueDate?.toStorageString().orEmpty(),
            modifier = Modifier.weight(1f),
            supporting = estimateLabel(language)
        )
        PinkMetric(
            label = trimesterLabel(language),
            value = summary?.trimester?.let { trimesterText(it, language) }.orEmpty(),
            modifier = Modifier.weight(1f),
            supporting = pregnancyCareLabel(language)
        )
    }
    PregnancyEducationCard(language)
    PregnancyWarningCard(language)
    PregnancySetupCard(settings = settings, language = language, onSave = onSave)
}

@Composable
private fun PregnancyEducationCard(language: AppLanguage) {
    val items = when (language) {
        AppLanguage.VI -> listOf(
            "Thai kỳ thường được theo dõi theo từng tuần và chia thành 3 tam cá nguyệt.",
            "Ghi lại triệu chứng, cân nặng, lịch khám và câu hỏi muốn hỏi bác sĩ.",
            "Nội dung trong app chỉ hỗ trợ theo dõi, không thay thế tư vấn y tế."
        )
        AppLanguage.EN -> listOf(
            "Pregnancy is usually tracked by week and grouped into 3 trimesters.",
            "Record symptoms, weight, appointments, and questions for your clinician.",
            "App content supports tracking only and does not replace medical advice."
        )
    }

    PinkCard(containerColor = CreamWhite) {
        Text(educationTitle(language), style = MaterialTheme.typography.titleMedium)
        items.forEach { item ->
            Text(item, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CycleEducationCard(language: AppLanguage) {
    val items = when (language) {
        AppLanguage.VI -> listOf(
            "Kinh nguyệt là một phần tự nhiên của tuổi dậy thì và sức khỏe sinh sản.",
            "Dậy thì có thể làm cơ thể, cảm xúc và sự tự tin thay đổi theo thời gian.",
            "Giữ vệ sinh cá nhân, thay băng/tampon/cốc nguyệt san đúng cách và rửa tay sạch.",
            "Hãy nói với mẹ, người tin tưởng hoặc bác sĩ nếu đau dữ dội, ra máu quá nhiều, chóng mặt hoặc lo lắng.",
            "Cơ thể của bạn thuộc về bạn. Ranh giới cá nhân, an toàn và đồng thuận luôn quan trọng."
        )
        AppLanguage.EN -> listOf(
            "Periods are a natural part of puberty and reproductive health.",
            "Puberty can change your body, emotions, and body confidence over time.",
            "Keep personal hygiene, change period products safely, and wash your hands.",
            "Talk to a parent, trusted adult, or clinician if pain is severe, bleeding is very heavy, you feel dizzy, or you feel worried.",
            "Your body belongs to you. Personal boundaries, safety, and consent always matter."
        )
    }

    PinkCard(containerColor = CreamWhite) {
        Text(cycleEducationTitle(language), style = MaterialTheme.typography.titleMedium)
        items.forEach { item ->
            Text(item, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PregnancyWarningCard(language: AppLanguage) {
    PinkCard(containerColor = CreamWhite) {
        Text(warningTitle(language), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(
            warningBody(language),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PregnancySetupCard(
    settings: PregnancySettings?,
    language: AppLanguage,
    onSave: (LocalDate?, LocalDate?) -> Unit
) {
    var lmp by remember(settings?.lastMenstrualPeriod) {
        val text = settings?.lastMenstrualPeriod?.toStorageString().orEmpty()
        mutableStateOf(TextFieldValue(text = text, selection = TextRange(text.length)))
    }
    var dueDate by remember(settings?.dueDate) {
        val text = settings?.dueDate?.toStorageString().orEmpty()
        mutableStateOf(TextFieldValue(text = text, selection = TextRange(text.length)))
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    PinkCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column {
                Text(pregnancySetupTitle(language), style = MaterialTheme.typography.titleMedium)
                Text(
                    pregnancySetupHint(language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        PinkFieldValue(
            value = lmp,
            onValueChange = {
                val formatted = formatCycleDateInput(rawInput = it.text, previousValue = lmp.text)
                lmp = TextFieldValue(text = formatted, selection = TextRange(formatted.length))
                errorMessage = null
            },
            label = lmpLabel(language),
            keyboardType = KeyboardType.Number
        )
        PinkFieldValue(
            value = dueDate,
            onValueChange = {
                val formatted = formatCycleDateInput(rawInput = it.text, previousValue = dueDate.text)
                dueDate = TextFieldValue(text = formatted, selection = TextRange(formatted.length))
                errorMessage = null
            },
            label = dueDateInputLabel(language),
            keyboardType = KeyboardType.Number
        )
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        PinkPrimaryButton(
            onClick = {
                val parsedLmp = lmp.text.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                val parsedDueDate = dueDate.text.takeIf { it.isNotBlank() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                errorMessage = when {
                    lmp.text.isBlank() && dueDate.text.isBlank() -> pregnancyDateRequired(language)
                    lmp.text.isNotBlank() && parsedLmp == null -> pregnancyInvalidDate(language)
                    dueDate.text.isNotBlank() && parsedDueDate == null -> pregnancyInvalidDate(language)
                    else -> null
                }
                if (errorMessage == null) {
                    onSave(parsedLmp, parsedDueDate)
                }
            }
        ) {
            Text(savePregnancyLabel(language))
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
        val text = settings?.lastPeriodStart?.toStorageString().orEmpty()
        mutableStateOf(TextFieldValue(text = text, selection = TextRange(text.length)))
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
        PinkFieldValue(
            value = lastPeriod,
            onValueChange = {
                val formatted = formatCycleDateInput(rawInput = it.text, previousValue = lastPeriod.text)
                lastPeriod = TextFieldValue(text = formatted, selection = TextRange(formatted.length))
                errorMessage = null
            },
            label = strings.lastPeriodDateLabel,
            keyboardType = KeyboardType.Number
        )
        Text(
            strings.optionalCycleSetupHint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                val date = runCatching { LocalDate.parse(lastPeriod.text) }.getOrNull()
                val cycleSetup = CycleSetupDefaults.resolve(
                    cycleLengthInput = cycleLength,
                    periodLengthInput = periodLength
                )
                errorMessage = when {
                    date == null -> strings.invalidDate
                    cycleSetup.error == CycleSetupInputError.INVALID_CYCLE_LENGTH -> strings.invalidCycleLength
                    cycleSetup.error == CycleSetupInputError.INVALID_PERIOD_LENGTH -> strings.invalidPeriodLength
                    cycleSetup.error == CycleSetupInputError.PERIOD_SHORTER_THAN_CYCLE -> strings.periodShorterThanCycle
                    else -> null
                }
                if (date != null && cycleSetup.error == null) {
                    onSave(date, cycleSetup.cycleLength, cycleSetup.periodLength)
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
private fun PinkFieldValue(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
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
private fun TipCard(uid: String) {
    val strings = LocalAppStrings.current
    val tip = remember(uid) { dailyTipForUser(uid) }
    val isEnglish = strings.languageCode == AppLanguage.EN
    val icon = when (tip.category) {
        TipCategory.DID_YOU_KNOW -> Icons.Default.TipsAndUpdates
        TipCategory.TODAY_TIP -> Icons.Default.LocalDrink
        TipCategory.SELF_CARE -> Icons.Default.Spa
    }
    val title = if (isEnglish) tip.enTitle else tip.viTitle
    val body = if (isEnglish) tip.enBody else tip.viBody

    PinkCard(containerColor = CreamWhite) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun appModeLabel(mode: AppMode, language: AppLanguage): String {
    return when (mode) {
        AppMode.CYCLE_TRACKING -> when (language) {
            AppLanguage.VI -> "Theo dõi chu kỳ"
            AppLanguage.EN -> "Cycle tracking"
        }
        AppMode.PREGNANCY -> when (language) {
            AppLanguage.VI -> "Thai kỳ"
            AppLanguage.EN -> "Pregnancy"
        }
    }
}

private fun pregnancyModeTitle(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Hành trình thai kỳ"
    AppLanguage.EN -> "Pregnancy journey"
}

private fun pregnancyWeekLabel(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "tuần thai"
    AppLanguage.EN -> "weeks"
}

private fun pregnancySetupPrompt(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Thiết lập ngày đầu kỳ kinh cuối hoặc ngày dự sinh để Pink Note theo dõi thai kỳ."
    AppLanguage.EN -> "Set your last menstrual period or due date so Pink Note can track your pregnancy."
}

private fun pregnancyStatusText(summary: PregnancySummary, language: AppLanguage): String = when (language) {
    AppLanguage.VI -> when {
        summary.daysUntilDue > 0 -> "Còn ${summary.daysUntilDue} ngày đến ngày dự sinh ước tính."
        summary.daysUntilDue == 0L -> "Hôm nay là ngày dự sinh ước tính."
        else -> "Đã qua ngày dự sinh ước tính ${-summary.daysUntilDue} ngày."
    }
    AppLanguage.EN -> when {
        summary.daysUntilDue > 0 -> "${summary.daysUntilDue} days until the estimated due date."
        summary.daysUntilDue == 0L -> "Today is the estimated due date."
        else -> "${-summary.daysUntilDue} days past the estimated due date."
    }
}

private fun dueDateLabel(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Ngày dự sinh"
    AppLanguage.EN -> "Due date"
}

private fun estimateLabel(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Ước tính"
    AppLanguage.EN -> "Estimated"
}

private fun trimesterLabel(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Tam cá nguyệt"
    AppLanguage.EN -> "Trimester"
}

private fun pregnancyCareLabel(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Theo tuần"
    AppLanguage.EN -> "Weekly care"
}

private fun trimesterText(trimester: PregnancyTrimester, language: AppLanguage): String {
    return when (trimester) {
        PregnancyTrimester.FIRST -> when (language) {
            AppLanguage.VI -> "Thứ 1"
            AppLanguage.EN -> "First"
        }
        PregnancyTrimester.SECOND -> when (language) {
            AppLanguage.VI -> "Thứ 2"
            AppLanguage.EN -> "Second"
        }
        PregnancyTrimester.THIRD -> when (language) {
            AppLanguage.VI -> "Thứ 3"
            AppLanguage.EN -> "Third"
        }
    }
}

private fun educationTitle(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Kiến thức thai kỳ"
    AppLanguage.EN -> "Pregnancy education"
}

private fun cycleEducationTitle(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Giáo dục sức khỏe"
    AppLanguage.EN -> "Health education"
}

private fun warningTitle(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Khi cần liên hệ bác sĩ"
    AppLanguage.EN -> "When to contact a clinician"
}

private fun warningBody(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Nếu có đau đầu dữ dội, nhìn mờ, đau ngực, khó thở, ra máu, đau bụng dữ dội, rỉ dịch hoặc thai máy giảm, hãy liên hệ bác sĩ hoặc cơ sở y tế ngay."
    AppLanguage.EN -> "If you have a severe headache, vision changes, chest pain, trouble breathing, bleeding, severe belly pain, fluid leakage, or reduced fetal movement, contact a clinician or emergency care."
}

private fun pregnancySetupTitle(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Thiết lập thai kỳ"
    AppLanguage.EN -> "Pregnancy setup"
}

private fun pregnancySetupHint(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Nhập một trong hai ngày. Nếu bác sĩ đã cho ngày dự sinh, hãy ưu tiên ngày dự sinh."
    AppLanguage.EN -> "Enter either date. If a clinician gave you a due date, prefer that date."
}

private fun lmpLabel(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Ngày đầu kỳ kinh cuối yyyy-MM-dd"
    AppLanguage.EN -> "Last menstrual period yyyy-MM-dd"
}

private fun dueDateInputLabel(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Ngày dự sinh yyyy-MM-dd"
    AppLanguage.EN -> "Due date yyyy-MM-dd"
}

private fun pregnancyDateRequired(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Hãy nhập ngày đầu kỳ kinh cuối hoặc ngày dự sinh."
    AppLanguage.EN -> "Enter a last menstrual period or due date."
}

private fun pregnancyInvalidDate(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Hãy nhập ngày theo định dạng yyyy-MM-dd."
    AppLanguage.EN -> "Enter a date in yyyy-MM-dd format."
}

private fun savePregnancyLabel(language: AppLanguage): String = when (language) {
    AppLanguage.VI -> "Lưu thai kỳ"
    AppLanguage.EN -> "Save pregnancy"
}
