package com.pinknote.app.presentation.settings

internal const val DELETE_ACCOUNT_CONFIRMATION_TEXT = "XOA"

internal fun isDeleteAccountConfirmationValid(input: String): Boolean {
    return input.trim() == DELETE_ACCOUNT_CONFIRMATION_TEXT
}
