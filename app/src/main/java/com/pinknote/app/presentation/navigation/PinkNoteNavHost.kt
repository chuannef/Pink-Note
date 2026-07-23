package com.pinknote.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pinknote.app.presentation.auth.LoginScreen
import com.pinknote.app.presentation.auth.RegisterScreen
import com.pinknote.app.presentation.auth.SplashScreen
import com.pinknote.app.presentation.calendar.CalendarScreen
import com.pinknote.app.presentation.dailylog.DailyLogScreen
import com.pinknote.app.presentation.home.HomeScreen
import com.pinknote.app.presentation.prediction.PredictionScreen
import com.pinknote.app.presentation.profile.EditProfileScreen
import com.pinknote.app.presentation.profile.ProfileScreen
import com.pinknote.app.presentation.reminder.ReminderScreen
import com.pinknote.app.presentation.settings.SettingsScreen
import com.pinknote.app.presentation.statistics.StatisticsScreen

private data class BottomItem(
    val route: AppRoute,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomItems = listOf(
    BottomItem(AppRoute.Home, "Home", Icons.Default.Home),
    BottomItem(AppRoute.Calendar, "Lịch", Icons.Default.CalendarMonth),
    BottomItem(AppRoute.Statistics, "Thống kê", Icons.Default.BarChart),
    BottomItem(AppRoute.Reminder, "Nhắc", Icons.Default.Notifications),
    BottomItem(AppRoute.Profile, "Hồ sơ", Icons.Default.Person),
    BottomItem(AppRoute.Settings, "Cài đặt", Icons.Default.Settings)
)

@Composable
fun PinkNoteNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomItems.any { it.route.route == currentRoute }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route.route,
                            onClick = {
                                navController.navigate(item.route.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Splash.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(AppRoute.Splash.route) {
                SplashScreen(
                    onAuthenticated = {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.Splash.route) { inclusive = true }
                        }
                    },
                    onUnauthenticated = {
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(AppRoute.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(AppRoute.Login.route) {
                LoginScreen(
                    onAuthenticated = {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.Login.route) { inclusive = true }
                        }
                    },
                    onRegister = { navController.navigate(AppRoute.Register.route) }
                )
            }
            composable(AppRoute.Register.route) {
                RegisterScreen(
                    onAuthenticated = {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.Register.route) { inclusive = true }
                        }
                    },
                    onLogin = { navController.popBackStack() }
                )
            }
            composable(AppRoute.Home.route) {
                HomeScreen(onOpenPrediction = { navController.navigate(AppRoute.Prediction.route) })
            }
            composable(AppRoute.Calendar.route) {
                CalendarScreen(onOpenDailyLog = { navController.navigate(AppRoute.DailyLog.create(it)) })
            }
            composable(AppRoute.Prediction.route) {
                PredictionScreen()
            }
            composable(
                route = AppRoute.DailyLog.route,
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) {
                DailyLogScreen(dateText = it.arguments?.getString("date").orEmpty())
            }
            composable(AppRoute.Statistics.route) {
                StatisticsScreen()
            }
            composable(AppRoute.Reminder.route) {
                ReminderScreen()
            }
            composable(AppRoute.Profile.route) {
                ProfileScreen(onEditProfile = { navController.navigate(AppRoute.EditProfile.route) })
            }
            composable(AppRoute.Settings.route) {
                SettingsScreen()
            }
            composable(AppRoute.EditProfile.route) {
                EditProfileScreen(onSaved = { navController.popBackStack() })
            }
        }
    }
}
