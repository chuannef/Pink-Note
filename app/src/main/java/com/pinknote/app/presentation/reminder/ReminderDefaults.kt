package com.pinknote.app.presentation.reminder

import com.pinknote.app.domain.model.ReminderType

data class ReminderTemplate(
    val label: String,
    val title: String,
    val message: String
)

object ReminderDefaults {
    fun forType(type: ReminderType): ReminderTemplate {
        return when (type) {
            ReminderType.BEFORE_PERIOD -> ReminderTemplate(
                label = "Trước kỳ kinh",
                title = "Sắp đến kỳ kinh",
                message = "Còn khoảng vài ngày nữa, hãy chuẩn bị và nghỉ ngơi nhiều hơn."
            )
            ReminderType.PERIOD_START -> ReminderTemplate(
                label = "Bắt đầu kỳ",
                title = "Ngày bắt đầu kỳ kinh",
                message = "Hãy ghi lại tình trạng hôm nay để Pink Note theo dõi chính xác hơn."
            )
            ReminderType.OVULATION -> ReminderTemplate(
                label = "Rụng trứng",
                title = "Ngày rụng trứng",
                message = "Hôm nay là ngày cần chú ý theo dõi sức khỏe sinh sản."
            )
            ReminderType.MEDICINE -> ReminderTemplate(
                label = "Uống thuốc",
                title = "Uống thuốc",
                message = "Đừng quên uống thuốc theo lịch cá nhân của bạn."
            )
            ReminderType.WATER -> ReminderTemplate(
                label = "Uống nước",
                title = "Uống nước",
                message = "Đã đến giờ uống nước."
            )
            ReminderType.WORKOUT -> ReminderTemplate(
                label = "Workout",
                title = "Workout",
                message = "Dành một chút thời gian vận động nhẹ theo kế hoạch hôm nay."
            )
        }
    }
}
