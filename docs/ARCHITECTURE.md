# PinkNote Architecture

## Bước 1: Thiết kế Architecture

PinkNote dùng MVVM + Repository Pattern + Clean Architecture.

Tại sao dùng:
- `presentation`: chỉ giữ UI Compose và ViewModel.
- `domain`: giữ model nghiệp vụ, repository interface và use case dự đoán chu kỳ.
- `data`: triển khai Firebase, Room cache, DataStore và mapper.
- `di`: Hilt module để thay implementation mà không sửa UI.
- `worker`: WorkManager cho nhắc nhở chạy nền.

Luồng hoạt động:
UI gọi ViewModel, ViewModel gọi Repository hoặc UseCase, Repository đọc/ghi Room và Firebase, sau đó phát dữ liệu qua Flow/StateFlow.

Cách mở rộng:
Khi app lớn hơn, có thể tách thành module `core`, `feature-auth`, `feature-cycle`, `feature-statistics` mà vẫn giữ interface domain như hiện tại.

## Bước 2: Tạo Project Android

Project được tạo bằng Kotlin DSL:
- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`

Cấu hình:
- Min SDK 26, Android 8.0.
- Target SDK 35, Android 15.
- Kotlin 2.0.21.
- Java 17.
- Jetpack Compose + Material 3.

## Bước 3: Dependencies

Các nhóm dependency chính:
- Hilt: Dependency Injection.
- Firebase Auth, Firestore, Storage, Messaging.
- Room: cache local.
- DataStore: cài đặt theme, ngôn ngữ, notification toggle.
- WorkManager: lịch nhắc nhở.
- Navigation Compose: Navigation Component cho Compose.
- MPAndroidChart: biểu đồ thống kê.
- Lottie Compose: splash animation.

## Bước 4: Kết nối Firebase

File `app/google-services.json` hiện là placeholder để project có cấu trúc đầy đủ.

Khi chạy thật:
1. Tạo Firebase project.
2. Thêm Android app package `com.pinknote.app`.
3. Tải `google-services.json`.
4. Ghi đè file placeholder.
5. Bật Authentication Email/Password và Google.
6. Tạo Cloud Firestore, Firebase Storage và Cloud Messaging.

## Bước 5: Thiết kế Database

Firestore collections:
- `users/{uid}`
- `cycle/{uid}`
- `daily_logs/{uid}/days/{yyyy-MM-dd}`
- `notifications/{uid}/items/{notificationId}`
- `settings/{uid}`

Room tables:
- `users`
- `cycle`
- `daily_logs`
- `notifications`

Room được dùng làm offline cache. Với hồ sơ, chu kỳ, ghi chú và nhắc nhở, app lưu local trước rồi đồng bộ Firebase best-effort.

## Bước 6: Thiết kế UI

UI dùng Material Design 3, màu hồng pastel, trắng và trạng thái ngày theo màu:
- Đỏ: ngày hành kinh.
- Xanh lá: ngày rụng trứng.
- Vàng: khoảng dễ mang thai.
- Xám: ngày bình thường.

Navigation:
- Splash, Login, Register.
- Bottom Navigation: Home, Calendar, Statistics, Reminder, Profile, Settings.
- Detail screens: Prediction, Daily Log, Edit Profile.

## Bước 7: Authentication

Luồng email:
1. Người dùng nhập email/password.
2. ViewModel gọi `AuthRepository`.
3. Firebase Auth tạo hoặc xác thực tài khoản.
4. Hồ sơ được lưu vào Firestore `users`.
5. Cache local vào Room.

Luồng Google:
1. UI lấy Google ID token.
2. Repository chuyển token thành Firebase credential.
3. Firebase Auth đăng nhập.
4. Nếu chưa có hồ sơ, tạo hồ sơ mặc định.

## Bước 8: Home

Home hiển thị:
- Xin chào và avatar chữ cái.
- Countdown.
- Progress circle.
- Kỳ kinh tiếp theo.
- Ngày rụng trứng.
- Today's Tips.
- Form thiết lập chu kỳ.

Home không tự tính trực tiếp mà gọi `PredictCycleUseCase`, giúp thuật toán dễ test và dễ thay đổi.

## Bước 9: Calendar

Calendar build danh sách ngày theo tháng từ `PredictCycleUseCase.buildCalendarDays`.

Khi bấm ngày:
1. Calendar route sang `daily_log/{date}`.
2. DailyLogViewModel đọc log theo UID + ngày.
3. Người dùng lưu ghi chú vào Room và Firebase.

## Bước 10: Prediction Algorithm

Công thức mặc định:
- Kỳ tiếp theo = ngày bắt đầu gần nhất + N chu kỳ cho đến sau hôm nay.
- Kết thúc kỳ = bắt đầu + số ngày hành kinh - 1.
- Rụng trứng = 14 ngày trước kỳ tiếp theo.
- Dễ mang thai = từ 5 ngày trước rụng trứng đến 1 ngày sau rụng trứng.

Use case trả về:
- next period start/end.
- ovulation date.
- fertile window.
- day type hôm nay.
- countdown text.
- cycle day.

## Bước 11: Notification

WorkManager tạo OneTimeWorkRequest cho từng reminder.

Các loại reminder:
- Trước kỳ kinh.
- Ngày bắt đầu kỳ.
- Ngày rụng trứng.
- Uống thuốc.
- Uống nước.
- Workout.

Firebase Cloud Messaging service đã có sẵn để mở rộng server push sau này.

## Bước 12: Statistics

Statistics dùng MPAndroidChart để vẽ mức đau theo thời gian.

Hiện có:
- Chu kỳ gần nhất.
- Độ dài hành kinh.
- Mức đau trung bình.
- Số tháng theo dõi.

Cách mở rộng:
- Thêm chart mood.
- Thêm cycle history collection.
- Tính trung bình chu kỳ từ nhiều kỳ thực tế thay vì chỉ dùng settings.

## Bước 13: Testing

Đã thêm unit test cho `PredictCycleUseCase`.

Nên bổ sung tiếp:
- Repository tests bằng fake datasource.
- ViewModel tests với coroutine test dispatcher.
- UI tests cho Login, Home, Calendar.

## Bước 14: Deployment

Checklist release:
1. Thay `google-services.json` thật.
2. Thay `default_web_client_id`.
3. Bật Firebase Auth providers.
4. Thêm Firestore rules.
5. Bật minify nếu đã có ProGuard rules đầy đủ.
6. Tạo keystore release.
7. Build `release`.
8. Test trên Android 8, Android 12, Android 15.
9. Upload lên Google Play Console.
