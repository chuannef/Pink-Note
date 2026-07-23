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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.presentation.common.PinkCard
import com.pinknote.app.presentation.common.PinkMetric
import com.pinknote.app.presentation.common.PinkPage
import com.pinknote.app.presentation.common.PinkPrimaryButton
import com.pinknote.app.presentation.theme.BlushSurface
import com.pinknote.app.presentation.theme.CreamWhite
import com.pinknote.app.presentation.theme.PastelPink
import com.pinknote.app.utils.DateUtils.toStorageString
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    onOpenPrediction: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val prediction = state.prediction
    val rawProgress = prediction?.let {
        val distance = ChronoUnit.DAYS.between(LocalDate.now(), it.nextPeriodStart).toFloat()
        1f - (distance / state.cycleSettings.cycleLength).coerceIn(0f, 1f)
    } ?: 0f
    val progress by animateFloatAsState(
        targetValue = rawProgress,
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
            CycleSetupCard(state = state, onSave = viewModel::saveCycle)
            TipCard()
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
