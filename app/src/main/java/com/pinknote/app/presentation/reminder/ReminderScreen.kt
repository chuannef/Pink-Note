package com.pinknote.app.presentation.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.domain.model.Reminder
import com.pinknote.app.domain.model.ReminderType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderScreen(viewModel: ReminderViewModel = hiltViewModel()) {
    val reminders by viewModel.reminders.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Nhắc nhở", style = MaterialTheme.typography.headlineMedium)
        }
        item {
            ReminderForm(onSave = viewModel::createReminder)
        }
        item {
            Text("Lịch đã đặt", style = MaterialTheme.typography.titleLarge)
        }
        if (reminders.isEmpty()) {
            item {
                Text(
                    text = "Chưa có nhắc nhở nào. Hãy tạo lịch theo nhu cầu cá nhân của bạn.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(reminders, key = { it.id }) { reminder ->
                ReminderItem(reminder = reminder, onDelete = { viewModel.cancel(reminder.id) })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderForm(
    onSave: (ReminderType, String, String, LocalDateTime) -> Unit
) {
    val context = LocalContext.current
    val defaultTime = remember {
        val nextHour = LocalTime.now().plusHours(1)
        LocalTime.of(nextHour.hour, 0)
    }
    var selectedType by remember { mutableStateOf(ReminderType.BEFORE_PERIOD) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(defaultTime) }
    var title by remember { mutableStateOf(ReminderDefaults.forType(selectedType).title) }
    var message by remember { mutableStateOf(ReminderDefaults.forType(selectedType).message) }
    var formError by remember { mutableStateOf<String?>(null) }

    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Tạo nhắc nhở cá nhân", style = MaterialTheme.typography.titleLarge)

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReminderType.entries.forEach { type ->
                    val template = ReminderDefaults.forType(type)
                    FilterChip(
                        selected = selectedType == type,
                        onClick = {
                            selectedType = type
                            title = template.title
                            message = template.message
                            formError = null
                        },
                        label = { Text(template.label) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = {
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                selectedDate = LocalDate.of(year, month + 1, day)
                                formError = null
                            },
                            selectedDate.year,
                            selectedDate.monthValue - 1,
                            selectedDate.dayOfMonth
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(selectedDate.format(dateFormatter))
                }
                TextButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                selectedTime = LocalTime.of(hour, minute)
                                formError = null
                            },
                            selectedTime.hour,
                            selectedTime.minute,
                            true
                        ).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(selectedTime.format(timeFormatter))
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    formError = null
                },
                label = { Text("Tiêu đề") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = message,
                onValueChange = {
                    message = it
                    formError = null
                },
                label = { Text("Nội dung") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            formError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    val scheduledAt = LocalDateTime.of(selectedDate, selectedTime).withSecond(0).withNano(0)
                    when {
                        title.isBlank() -> formError = "Vui lòng nhập tiêu đề nhắc nhở."
                        message.isBlank() -> formError = "Vui lòng nhập nội dung nhắc nhở."
                        !scheduledAt.isAfter(LocalDateTime.now()) -> formError = "Vui lòng chọn thời gian trong tương lai."
                        else -> {
                            onSave(selectedType, title.trim(), message.trim(), scheduledAt)
                            formError = null
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Lưu nhắc nhở", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun ReminderItem(reminder: Reminder, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                Text(reminder.message, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = reminder.scheduledAt.format(dateTimeFormatter),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa")
            }
        }
    }
}

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy")
