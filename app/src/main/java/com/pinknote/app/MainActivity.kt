package com.pinknote.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.pinknote.app.domain.model.ThemeMode
import com.pinknote.app.presentation.localization.LocalAppStrings
import com.pinknote.app.presentation.localization.appStrings
import com.pinknote.app.presentation.navigation.PinkNoteNavHost
import com.pinknote.app.presentation.settings.SettingsViewModel
import com.pinknote.app.presentation.theme.PinkNoteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.settings.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            CompositionLocalProvider(LocalAppStrings provides appStrings(settings.language)) {
                PinkNoteTheme(darkTheme = darkTheme) {
                    PinkNoteNavHost()
                }
            }
        }
    }
}
