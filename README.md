# PinkNote

PinkNote là ứng dụng Android hỗ trợ theo dõi chu kỳ kinh nguyệt và chăm sóc sức khỏe cá nhân. Ứng dụng tập trung vào trải nghiệm nhẹ nhàng, riêng tư và dễ sử dụng, giúp người dùng ghi nhận dữ liệu hằng ngày, theo dõi lịch chu kỳ và nhận các gợi ý phù hợp theo từng giai đoạn.

## Mục Tiêu Dự Án

PinkNote được xây dựng để giúp người dùng:

- Theo dõi ngày bắt đầu và kết thúc kỳ kinh.
- Dự đoán kỳ kinh tiếp theo, ngày rụng trứng và khoảng dễ mang thai.
- Ghi lại các chỉ số sức khỏe hằng ngày như mức đau, tâm trạng, nhiệt độ, cân nặng, triệu chứng và ghi chú cá nhân.
- Nhìn lại dữ liệu chu kỳ qua thống kê và biểu đồ.
- Nhận nhắc nhở cho kỳ kinh, ngày rụng trứng, uống thuốc, uống nước và hoạt động cá nhân.
- Quản lý hồ sơ sức khỏe và cài đặt ứng dụng.

## Chức Năng Chính

- Đăng ký, đăng nhập bằng Email hoặc Google.
- Hồ sơ người dùng với thông tin cá nhân và mục tiêu sức khỏe.
- Thiết lập chu kỳ linh hoạt, có thể chỉnh sửa bất cứ lúc nào.
- Trang Home hiển thị countdown, tiến trình chu kỳ, gợi ý hôm nay và thống kê nhanh.
- Calendar View hiển thị các ngày hành kinh, rụng trứng, dễ mang thai và ngày bình thường bằng màu sắc riêng.
- Daily Log cho từng ngày.
- Prediction Screen cho thông tin dự đoán chi tiết.
- Reminder Screen cho các nhắc nhở sức khỏe.
- Setting Screen với chế độ sáng/tối, ngôn ngữ, thông báo, đổi mật khẩu, đăng xuất và xóa tài khoản.
- Admin Console để quản lý người dùng và phân quyền `user/admin`.

## Công Nghệ Sử Dụng

- Kotlin
- Jetpack Compose
- Material Design 3
- MVVM
- Clean Architecture
- Repository Pattern
- Hilt Dependency Injection
- Kotlin Coroutines
- Flow và StateFlow
- Navigation Component
- Room
- DataStore
- WorkManager
- Firebase Authentication
- Cloud Firestore
- Firebase Storage
- Firebase Cloud Messaging
- MPAndroidChart
- Lottie

## Kiến Trúc

PinkNote được tổ chức theo hướng Clean Architecture, chia rõ trách nhiệm giữa các lớp:

- `domain`: chứa model, repository contract và use case nghiệp vụ.
- `data`: triển khai repository, Firebase remote source, Room local cache và mapper.
- `presentation`: chứa UI, ViewModel, navigation và theme.
- `worker`: xử lý các tác vụ nền như nhắc nhở.
- `utils`: chứa hằng số, xử lý ngày tháng và policy dùng chung.

Kiến trúc này giúp ứng dụng dễ mở rộng, dễ kiểm thử và hạn chế phụ thuộc trực tiếp giữa UI với Firebase hoặc database local.

## Dữ Liệu Và Đồng Bộ

Dữ liệu chính được lưu trên Cloud Firestore và được cache local bằng Room khi phù hợp. Ứng dụng dùng Repository Pattern để đồng bộ dữ liệu giữa Firebase, cache local và UI.

Các nhóm dữ liệu chính:

- Người dùng
- Thiết lập chu kỳ
- Nhật ký hằng ngày
- Nhắc nhở
- Cài đặt ứng dụng

## Phân Quyền

PinkNote hỗ trợ hai loại vai trò:

- `user`: người dùng thông thường.
- `admin`: người có quyền truy cập Admin Console.

Quyền admin được xác định bằng field `role` trong dữ liệu người dùng. Ứng dụng không tự cấp quyền admin theo tên hoặc email.

## Trải Nghiệm Giao Diện

Giao diện sử dụng Material Design 3 với tông màu hồng pastel, nền sáng nhẹ và các thành phần trực quan phù hợp với ứng dụng chăm sóc sức khỏe. Bottom Navigation giúp người dùng di chuyển nhanh giữa Home, Calendar, Reminder, Profile và Setting.

Ứng dụng ưu tiên:

- Bố cục rõ ràng.
- Trạng thái loading và empty state.
- Hiển thị dữ liệu dễ đọc.
- Tương tác đơn giản.
- Màu sắc dịu, không gây mỏi mắt.

## Trạng Thái Dự Án

PinkNote hiện đã có nền tảng ứng dụng hoàn chỉnh gồm authentication, dự đoán chu kỳ, lịch, nhật ký, thống kê, nhắc nhở, cài đặt và admin console. Dự án vẫn có thể tiếp tục mở rộng thêm kiểm thử tự động, hoàn thiện trải nghiệm biểu đồ, cải thiện thông báo nâng cao và tối ưu giao diện cho nhiều kích thước màn hình.
