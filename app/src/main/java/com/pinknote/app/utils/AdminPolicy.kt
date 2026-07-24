package com.pinknote.app.utils

object AdminPolicy {
    const val ROLE_ADMIN = "admin"
    const val ROLE_USER = "user"

    fun normalizeRole(role: String?): String {
        return if (role.equals(ROLE_ADMIN, ignoreCase = true)) ROLE_ADMIN else ROLE_USER
    }

    fun isAdmin(role: String): Boolean = role.equals(ROLE_ADMIN, ignoreCase = true)
}
