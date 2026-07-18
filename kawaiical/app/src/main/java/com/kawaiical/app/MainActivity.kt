package com.kawaiical.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kawaiical.app.data.prefs.ThemeMode
import com.kawaiical.app.data.prefs.UserPreferences
import com.kawaiical.app.ui.nav.AppNavHost
import com.kawaiical.app.ui.theme.KawaiiCalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefsRepository = (application as KawaiiCalApplication)
            .container.preferencesRepository

        setContent {
            val prefs by prefsRepository.preferences
                .collectAsStateWithLifecycle(initialValue = UserPreferences())
            val darkTheme = when (prefs.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            KawaiiCalTheme(darkTheme = darkTheme) {
                AppNavHost()
            }
        }
    }
}
