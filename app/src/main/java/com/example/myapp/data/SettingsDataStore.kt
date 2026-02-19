package com.example.myapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val BLACK_THEME = booleanPreferencesKey("black_theme")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val FONT_STYLE = stringPreferencesKey("font_style")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val COLOR_SCHEME = stringPreferencesKey("color_scheme")
        val LANGUAGE = stringPreferencesKey("app_language")
        val TEXT_TO_SPEECH = booleanPreferencesKey("text_to_speech")
        val OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        val SHOW_ICONS = booleanPreferencesKey("show_icons")
    }

    suspend fun saveBlackTheme(enabled: Boolean) {
        context.dataStore.edit { it[BLACK_THEME] = enabled }
    }

    suspend fun saveFontSize(size: Float) {
        context.dataStore.edit { it[FONT_SIZE] = size }
    }

    suspend fun saveFontStyle(style: String) {
        context.dataStore.edit { it[FONT_STYLE] = style }
    }

    suspend fun saveThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun saveColorScheme(scheme: String) {
        context.dataStore.edit { it[COLOR_SCHEME] = scheme }
    }

    suspend fun saveLanguage(language: String) {
        context.dataStore.edit { it[LANGUAGE] = language }
    }

    suspend fun saveTextToSpeech(enabled: Boolean) {
        context.dataStore.edit { it[TEXT_TO_SPEECH] = enabled }
    }

    suspend fun saveOfflineMode(enabled: Boolean) {
        context.dataStore.edit { it[OFFLINE_MODE] = enabled }
    }

    suspend fun saveShowIcons(enabled: Boolean) {
        context.dataStore.edit { it[SHOW_ICONS] = enabled }
    }

    suspend fun resetAllToDefaults() {
        context.dataStore.edit { prefs ->
            prefs.remove(BLACK_THEME)
            prefs.remove(FONT_SIZE)
            prefs.remove(FONT_STYLE)
            prefs.remove(THEME_MODE)
            prefs.remove(COLOR_SCHEME)
            prefs.remove(LANGUAGE)
            prefs.remove(TEXT_TO_SPEECH)
            prefs.remove(OFFLINE_MODE)
            prefs.remove(SHOW_ICONS)
        }
    }

    val blackThemeFlow: Flow<Boolean> =
        context.dataStore.data.map { it[BLACK_THEME] ?: false }

    val fontSizeFlow: Flow<Float> =
        context.dataStore.data.map { it[FONT_SIZE] ?: 16f }

    val fontStyleFlow: Flow<String> =
        context.dataStore.data.map { it[FONT_STYLE] ?: "sans" }

    val themeModeFlow: Flow<String> =
        context.dataStore.data.map { it[THEME_MODE] ?: "system" }

    val colorSchemeFlow: Flow<String> =
        context.dataStore.data.map { it[COLOR_SCHEME] ?: "blue" }

    val languageFlow: Flow<String> =
        context.dataStore.data.map { it[LANGUAGE] ?: "en" }

    val textToSpeechFlow: Flow<Boolean> =
        context.dataStore.data.map { it[TEXT_TO_SPEECH] ?: true }

    val offlineModeFlow: Flow<Boolean> =
        context.dataStore.data.map { it[OFFLINE_MODE] ?: false }

    val showIconsFlow: Flow<Boolean> =
        context.dataStore.data.map { it[SHOW_ICONS] ?: true }
}
