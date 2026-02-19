package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.R
import com.example.myapp.data.SettingsDataStore
import com.example.myapp.ui.components.SidebarNavigationDrawer
import com.example.myapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val dataStore = remember { SettingsDataStore(context) }
    val scope = rememberCoroutineScope()

    val blackThemeEnabled by dataStore.blackThemeFlow.collectAsState(initial = false)
    val fontSize by dataStore.fontSizeFlow.collectAsState(initial = 16f)
    val fontStyle by dataStore.fontStyleFlow.collectAsState(initial = "sans")
    val themeMode by dataStore.themeModeFlow.collectAsState(initial = "system")
    val colorScheme by dataStore.colorSchemeFlow.collectAsState(initial = "blue")
    val textToSpeechEnabled by dataStore.textToSpeechFlow.collectAsState(initial = true)
    val offlineModeEnabled by dataStore.offlineModeFlow.collectAsState(initial = false)
    val showIconsEnabled by dataStore.showIconsFlow.collectAsState(initial = true)

    SidebarNavigationDrawer(viewModel = viewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---------- THEME ----------
            SettingsCard {
                Text(stringResource(R.string.theme))
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("system", "light", "dark").forEach { mode ->
                        FilterChip(
                            selected = themeMode == mode,
                            onClick = {
                                scope.launch { dataStore.saveThemeMode(mode) }
                            },
                            label = { Text(mode.replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                SettingsSwitchRow(
                    icon = Icons.Rounded.DarkMode,
                    title = stringResource(R.string.black_theme),
                    subtitle = "Use pure black dark theme",
                    checked = blackThemeEnabled,
                    onCheckedChange = {
                        scope.launch { dataStore.saveBlackTheme(it) }
                    }
                )
            }

            // ---------- COLOR SCHEME ----------
            SettingsCard {
                Text(stringResource(R.string.color_scheme))
                Spacer(Modifier.height(8.dp))

                val colors = listOf(
                    "blue" to stringResource(R.string.blue),
                    "green" to stringResource(R.string.green),
                    "purple" to stringResource(R.string.purple),
                    "pink" to stringResource(R.string.pink),
                    "red" to stringResource(R.string.red),
                    "orange" to stringResource(R.string.orange),
                    "brown" to stringResource(R.string.brown),
                    "black" to stringResource(R.string.black),
                    "cyan" to stringResource(R.string.cyan)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(colors) { (scheme, label) ->
                        FilterChip(
                            selected = colorScheme == scheme,
                            onClick = {
                                scope.launch {
                                    dataStore.saveColorScheme(scheme)
                                }
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // ---------- FONT ----------
            SettingsCard {
                Text(stringResource(R.string.font_style))
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "sans" to stringResource(R.string.sans),
                        "serif" to stringResource(R.string.serif),
                        "mono" to stringResource(R.string.mono),
                        "cursive" to stringResource(R.string.cursive)
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = fontStyle == key,
                            onClick = {
                                scope.launch { dataStore.saveFontStyle(key) }
                            },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("${stringResource(R.string.font_size)}: ${fontSize.toInt()}")
                Slider(
                    value = fontSize,
                    onValueChange = {
                        scope.launch { dataStore.saveFontSize(it) }
                    },
                    valueRange = 12f..24f
                )
                Spacer(Modifier.height(16.dp))
                Text("Preview", fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text("The quick brown fox jumps over the lazy dog")
            }

            // ---------- APP FEATURES ----------
            SettingsCard {
                Text(stringResource(R.string.app_features))
                Spacer(Modifier.height(8.dp))

                SettingsSwitchRow(
                    icon = Icons.Rounded.VolumeUp,
                    title = "Text To Speech",
                    subtitle = "Enable read aloud feature",
                    checked = textToSpeechEnabled,
                    onCheckedChange = {
                        scope.launch { dataStore.saveTextToSpeech(it) }
                    }
                )

                SettingsSwitchRow(
                    icon = Icons.Rounded.CloudOff,
                    title = "Offline Mode",
                    subtitle = "Use cached data when offline",
                    checked = offlineModeEnabled,
                    onCheckedChange = {
                        scope.launch { dataStore.saveOfflineMode(it) }
                    }
                )

                SettingsSwitchRow(
                    icon = Icons.Rounded.Visibility,
                    title = "Show Icons",
                    subtitle = "Display icons in lists",
                    checked = showIconsEnabled,
                    onCheckedChange = {
                        scope.launch { dataStore.saveShowIcons(it) }
                    }
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        scope.launch { dataStore.resetAllToDefaults() }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.reset_settings))
                }
            }
        }
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
