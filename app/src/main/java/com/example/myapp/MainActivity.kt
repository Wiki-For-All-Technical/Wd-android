package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import com.example.myapp.data.SettingsDataStore
import com.example.myapp.ui.screens.AppScreen
import com.example.myapp.ui.theme.WikidataMobileLiteTheme
import com.example.myapp.util.LocaleHelper
import com.example.myapp.viewmodel.MainViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved language before UI loads (from Wikiappsettings)
        val dataStore = SettingsDataStore(this)
        lifecycleScope.launch {
            dataStore.languageFlow.collect { language ->
                val currentLang = resources.configuration.locales.get(0).language
                if (currentLang != language) {
                    LocaleHelper.applyLanguage(this@MainActivity, language)
                }
            }
        }

        setContent {
            val context = LocalContext.current
            val settingsDataStore = remember { SettingsDataStore(context) }

            val themeMode by settingsDataStore.themeModeFlow.collectAsState(initial = "system")
            val colorSchemeName by settingsDataStore.colorSchemeFlow.collectAsState(initial = "blue")
            val useBlackTheme by settingsDataStore.blackThemeFlow.collectAsState(initial = false)
            val fontStyle by settingsDataStore.fontStyleFlow.collectAsState(initial = "sans")
            val fontScale by settingsDataStore.fontSizeFlow.collectAsState(initial = 16f)

            val fontFamily = when (fontStyle) {
                "serif" -> FontFamily.Serif
                "mono" -> FontFamily.Monospace
                "cursive" -> FontFamily.Cursive
                else -> FontFamily.SansSerif
            }

            WikidataMobileLiteTheme(
                themeMode = themeMode,
                colorSchemeName = colorSchemeName,
                useBlackTheme = useBlackTheme,
                fontFamily = fontFamily,
                fontScale = fontScale
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppScreen(viewModel = viewModel)
                }
            }
        }
    }
}
