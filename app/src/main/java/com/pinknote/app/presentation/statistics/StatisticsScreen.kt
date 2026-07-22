package com.pinknote.app.presentation.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.presentation.theme.RoseDeep
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Thống kê", style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Chu kỳ gần nhất", "${state.cycle?.cycleLength ?: 0} ngày", Modifier.weight(1f))
            StatCard("Hành kinh", "${state.cycle?.periodLength ?: 0} ngày", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Mức đau TB", "%.1f".format(state.averagePain), Modifier.weight(1f))
            StatCard("Số tháng", state.trackedMonths.toString(), Modifier.weight(1f))
        }
        Text("Biểu đồ mức đau", style = MaterialTheme.typography.titleMedium)
        PainChart(entries = state.logs.sortedBy { it.date }.mapIndexed { index, log ->
            Entry(index.toFloat(), log.painLevel.toFloat())
        })
        if (state.logs.isEmpty()) {
            Text("Chưa có dữ liệu ghi chú để thống kê.")
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun PainChart(entries: List<Entry>) {
    val color = RoseDeep.toArgb()
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        factory = { context ->
            LineChart(context).apply {
                description = Description().apply { text = "Pain level" }
                axisRight.isEnabled = false
                legend.isEnabled = false
            }
        },
        update = { chart ->
            val dataSet = LineDataSet(entries, "Pain").apply {
                setColor(color)
                setCircleColor(color)
                lineWidth = 2f
                circleRadius = 4f
                valueTextSize = 10f
            }
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}
