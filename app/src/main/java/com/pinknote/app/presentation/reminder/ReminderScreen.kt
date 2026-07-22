package com.pinknote.app.presentation.reminder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.domain.model.Reminder
import com.pinknote.app.domain.model.ReminderType
import java.time.LocalDateTime

@Composable
fun ReminderScreen(viewModel: ReminderViewModel = hiltViewModel()) {
    val reminders by viewModel.reminders.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Nhắc nhở", style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    viewModel.createReminder(
                        ReminderType.BEFORE_PERIOD,
                        "Sắp đến kỳ kinh",
                        "Còn khoảng 3 ngày nữa, hãy chuẩn bị và nghỉ ngơi nhiều hơn.",
                        LocalDateTime.now().plusDays(3)
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Trước kỳ")
            }
            Button(
                onClick = {
                    viewModel.createReminder(
                        ReminderType.OVULATION,
                        "Ngày rụng trứng",
                        "Hôm nay là ngày cần chú ý theo dõi sức khỏe sinh sản.",
                        LocalDateTime.now().plusDays(1)
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Rụng trứng")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    viewModel.createReminder(
                        ReminderType.WATER,
                        "Uống nước",
                        "Đã đến giờ uống nước.",
                        LocalDateTime.now().plusHours(2)
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Nước")
            }
            Button(
                onClick = {
                    viewModel.createReminder(
                        ReminderType.MEDICINE,
                        "Uống thuốc",
                        "Đừng quên thuốc đã đặt lịch.",
                        LocalDateTime.now().plusHours(1)
                    )
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Thuốc")
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(reminders, key = { it.id }) { reminder ->
                ReminderItem(reminder = reminder, onDelete = { viewModel.cancel(reminder.id) })
            }
        }
    }
}

@Composable
private fun ReminderItem(reminder: Reminder, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                Text(reminder.message, style = MaterialTheme.typography.bodyMedium)
                Text(reminder.scheduledAt.toString(), style = MaterialTheme.typography.labelMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa")
            }
        }
    }
}
