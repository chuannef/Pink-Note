package com.pinknote.app.presentation.auth

internal fun validatePasswordResetEmail(email: String): String? {
    val normalizedEmail = email.trim()
    return when {
        normalizedEmail.isBlank() -> "Hãy nhập email đã đăng ký."
        !normalizedEmail.contains("@") -> "Email chưa đúng định dạng."
        else -> null
    }
}

internal fun passwordResetSentMessage(email: String): String {
    return "Nếu ${email.trim()} đã đăng ký PinkNote, email đặt lại mật khẩu đã được gửi. Hãy kiểm tra Hộp thư đến hoặc Spam."
}
