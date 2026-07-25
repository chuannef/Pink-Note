package com.pinknote.app.presentation.home

import java.time.LocalDate
import kotlin.math.absoluteValue

data class DailyTip(
    val category: TipCategory,
    val viTitle: String,
    val viBody: String,
    val enTitle: String,
    val enBody: String
)

enum class TipCategory {
    DID_YOU_KNOW,
    TODAY_TIP,
    SELF_CARE
}

fun dailyTipForUser(uid: String, date: LocalDate = LocalDate.now()): DailyTip {
    val stableKey = "${uid.ifBlank { "guest" }}-${date}"
    val index = stableKey.hashCode().absoluteValue % dailyTips.size
    return dailyTips[index]
}

private val dailyTips = listOf(
    DailyTip(
        category = TipCategory.DID_YOU_KNOW,
        viTitle = "Bạn có biết?",
        viBody = "Không phải tất cả phụ nữ đều có chu kỳ 28 ngày.\n\nChu kỳ bình thường ở người trưởng thành thường dao động từ 21-35 ngày.",
        enTitle = "Did you know?",
        enBody = "Not everyone has a 28-day cycle.\n\nA normal adult cycle often ranges from 21 to 35 days."
    ),
    DailyTip(
        category = TipCategory.DID_YOU_KNOW,
        viTitle = "Bạn có biết?",
        viBody = "Ngày rụng trứng không phải lúc nào cũng là ngày thứ 14.\n\nNgày này thay đổi theo từng người và từng chu kỳ.",
        enTitle = "Did you know?",
        enBody = "Ovulation is not always on day 14.\n\nIt can change from person to person and from cycle to cycle."
    ),
    DailyTip(
        category = TipCategory.DID_YOU_KNOW,
        viTitle = "Bạn có biết?",
        viBody = "Tinh trùng có thể sống trong cơ thể từ 3-5 ngày.\n\nĐó là lý do khả năng mang thai bắt đầu trước khi rụng trứng.",
        enTitle = "Did you know?",
        enBody = "Sperm can live in the body for 3-5 days.\n\nThat is why the fertile window can begin before ovulation."
    ),
    DailyTip(
        category = TipCategory.DID_YOU_KNOW,
        viTitle = "Bạn có biết?",
        viBody = "Trứng sau khi rụng chỉ có khả năng thụ tinh khoảng 12-24 giờ.",
        enTitle = "Did you know?",
        enBody = "After ovulation, an egg can usually be fertilized for about 12-24 hours."
    ),
    DailyTip(
        category = TipCategory.DID_YOU_KNOW,
        viTitle = "Bạn có biết?",
        viBody = "Stress kéo dài có thể làm kỳ kinh đến muộn hoặc thay đổi độ dài chu kỳ.",
        enTitle = "Did you know?",
        enBody = "Long-lasting stress can delay your period or change your cycle length."
    ),
    DailyTip(
        category = TipCategory.DID_YOU_KNOW,
        viTitle = "Bạn có biết?",
        viBody = "Việc thức khuya liên tục có thể ảnh hưởng đến hormone điều hòa chu kỳ kinh nguyệt.",
        enTitle = "Did you know?",
        enBody = "Staying up late repeatedly can affect hormones that help regulate the menstrual cycle."
    ),
    DailyTip(
        category = TipCategory.DID_YOU_KNOW,
        viTitle = "Bạn có biết?",
        viBody = "Không phải tháng nào cũng chắc chắn có rụng trứng.",
        enTitle = "Did you know?",
        enBody = "Ovulation does not necessarily happen every single month."
    ),
    DailyTip(
        category = TipCategory.DID_YOU_KNOW,
        viTitle = "Bạn có biết?",
        viBody = "Ứng dụng chỉ dự đoán ngày rụng trứng dựa trên dữ liệu lịch sử.\n\nMuốn xác định chính xác cần thêm dữ liệu như nhiệt độ cơ thể hoặc que thử LH.",
        enTitle = "Did you know?",
        enBody = "The app estimates ovulation from historical data.\n\nMore precise tracking may need extra data such as body temperature or LH tests."
    ),
    DailyTip(
        category = TipCategory.TODAY_TIP,
        viTitle = "Gợi ý hôm nay",
        viBody = "Nếu hôm nay bạn đang trong kỳ kinh, hãy thử đi bộ nhẹ khoảng 15-20 phút.\n\nNhiều người cảm thấy dễ chịu hơn sau khi vận động nhẹ.",
        enTitle = "Today's tip",
        enBody = "If you are on your period today, try a gentle 15-20 minute walk.\n\nMany people feel better after light movement."
    ),
    DailyTip(
        category = TipCategory.TODAY_TIP,
        viTitle = "Gợi ý hôm nay",
        viBody = "Nếu bạn thường đau bụng kinh, hãy ghi lại mức độ đau mỗi chu kỳ.\n\nThông tin này có thể giúp bác sĩ đánh giá nếu cần khám sau này.",
        enTitle = "Today's tip",
        enBody = "If you often have cramps, record your pain level each cycle.\n\nThis can help a doctor assess your symptoms if you need care later."
    ),
    DailyTip(
        category = TipCategory.TODAY_TIP,
        viTitle = "Gợi ý hôm nay",
        viBody = "Đừng chỉ ghi ngày bắt đầu kỳ kinh.\n\nNếu có thể, hãy ghi đau bụng, lượng máu, tâm trạng và triệu chứng. Ứng dụng sẽ dự đoán chính xác hơn.",
        enTitle = "Today's tip",
        enBody = "Do not only record the first period day.\n\nIf possible, note cramps, flow, mood, and symptoms. The app can make better predictions."
    ),
    DailyTip(
        category = TipCategory.TODAY_TIP,
        viTitle = "Gợi ý hôm nay",
        viBody = "Nếu tháng này bạn đi công tác, đổi múi giờ hoặc mất ngủ nhiều ngày, chu kỳ có thể thay đổi.",
        enTitle = "Today's tip",
        enBody = "If you travel, change time zones, or lose sleep for several days this month, your cycle may shift."
    ),
    DailyTip(
        category = TipCategory.TODAY_TIP,
        viTitle = "Gợi ý hôm nay",
        viBody = "Một chu kỳ đến sớm hoặc muộn vài ngày thường chưa phải điều bất thường.",
        enTitle = "Today's tip",
        enBody = "A period that arrives a few days early or late is often not unusual."
    ),
    DailyTip(
        category = TipCategory.SELF_CARE,
        viTitle = "Chăm sóc bản thân",
        viBody = "Đến gần kỳ kinh, nhiều người cảm thấy dễ cáu gắt hơn.\n\nĐó có thể là ảnh hưởng của hormone, không phải do bạn khó tính.",
        enTitle = "Self-care",
        enBody = "Near your period, it is common to feel more irritable.\n\nHormones may be involved. It does not mean you are difficult."
    ),
    DailyTip(
        category = TipCategory.SELF_CARE,
        viTitle = "Chăm sóc bản thân",
        viBody = "Nếu hôm nay bạn thấy mệt, hãy ưu tiên nghỉ ngơi thay vì cố làm mọi việc.",
        enTitle = "Self-care",
        enBody = "If you feel tired today, prioritize rest instead of trying to do everything."
    )
)
