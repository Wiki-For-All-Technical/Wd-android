package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.myapp.data.AppView
import com.example.myapp.viewmodel.MainViewModel

@Composable
fun AppScreen(viewModel: MainViewModel) {
    val currentView by viewModel.currentView.collectAsState()
    MainAppWithSidebar(viewModel = viewModel)
}

@Composable
fun MainAppWithSidebar(viewModel: MainViewModel) {
    val currentView by viewModel.currentView.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when (currentView) {
            AppView.HOME -> HomeScreen(viewModel)
            AppView.SEARCH -> SearchScreen(viewModel)
            AppView.ENTITY -> EntityScreen(viewModel)
            AppView.PROFILE -> ProfileScreen(viewModel)
            AppView.LOGIN, AppView.OAUTH_CONSENT -> LoginScreen(viewModel)
            AppView.RECENT_CHANGES -> RecentChangesScreen(viewModel)
            AppView.EDIT -> EditScreen(viewModel)
            AppView.NEARBY -> NearbyScreen(viewModel)
            AppView.SETTINGS -> SettingsScreen(viewModel)
            AppView.CONTRIBUTIONS -> ContributionsScreen(viewModel)
            AppView.THEME -> ThemeScreen(viewModel)
        }
    }
}
