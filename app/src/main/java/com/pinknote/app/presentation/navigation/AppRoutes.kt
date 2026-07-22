package com.pinknote.app.presentation.navigation

sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Login : AppRoute("login")
    data object Register : AppRoute("register")
    data object Home : AppRoute("home")
    data object Calendar : AppRoute("calendar")
    data object Prediction : AppRoute("prediction")
    data object DailyLog : AppRoute("daily_log/{date}") {
        fun create(date: String) = "daily_log/$date"
    }
    data object Statistics : AppRoute("statistics")
    data object Reminder : AppRoute("reminder")
    data object Profile : AppRoute("profile")
    data object Settings : AppRoute("settings")
    data object EditProfile : AppRoute("edit_profile")
}
