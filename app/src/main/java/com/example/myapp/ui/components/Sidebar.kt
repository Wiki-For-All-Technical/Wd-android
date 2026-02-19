package com.example.myapp.ui.components

import android.webkit.CookieManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.data.AppView
import com.example.myapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidebarNavigationDrawer(
    viewModel: MainViewModel,
    content: @Composable () -> Unit
) {
    val isSidebarOpen by viewModel.isSidebarOpen.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val username by viewModel.username.collectAsState()
    val currentView by viewModel.currentView.collectAsState()
    val drawerState = rememberDrawerState(
        if (isSidebarOpen) DrawerValue.Open else DrawerValue.Closed
    )
    val scope = rememberCoroutineScope()

    androidx.compose.runtime.LaunchedEffect(isSidebarOpen) {
        if (isSidebarOpen) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    androidx.compose.runtime.LaunchedEffect(drawerState.currentValue) {
        val isOpen = drawerState.currentValue == DrawerValue.Open
        if (isOpen != isSidebarOpen) {
            viewModel.setSidebarOpen(isOpen)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            NavigationDrawerContent(
                viewModel = viewModel,
                isLoggedIn = isLoggedIn,
                username = username,
                currentView = currentView,
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                    viewModel.setSidebarOpen(false)
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Wikidata") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                            viewModel.setSidebarOpen(true)
                        }) {
                            Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        if (isLoggedIn) {
                            IconButton(onClick = {
                                viewModel.navigateTo(AppView.PROFILE)
                            }) {
                                Icon(Icons.Rounded.Person, contentDescription = "Profile")
                            }
                        } else {
                            IconButton(onClick = {
                                viewModel.navigateTo(AppView.LOGIN)
                            }) {
                                Icon(Icons.Rounded.Login, contentDescription = "Login")
                            }
                        }
                        IconButton(onClick = {
                            viewModel.navigateTo(AppView.SEARCH)
                        }) {
                            Icon(Icons.Rounded.Search, contentDescription = "Search")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                content()
            }
        }
    }
}

@Composable
fun NavigationDrawerContent(
    viewModel: MainViewModel,
    isLoggedIn: Boolean,
    username: String,
    currentView: AppView,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        if (isLoggedIn && username.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text(
                        text = "Logged in",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Divider()

        Spacer(modifier = Modifier.height(16.dp))

        NavigationDrawerItem(
            label = { Text("Home") },
            icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
            selected = currentView == AppView.HOME,
            onClick = {
                viewModel.navigateTo(AppView.HOME)
                onCloseDrawer()
            },
            modifier = Modifier.fillMaxWidth()
        )

        NavigationDrawerItem(
            label = { Text("Search") },
            icon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            selected = currentView == AppView.SEARCH,
            onClick = {
                viewModel.navigateTo(AppView.SEARCH)
                onCloseDrawer()
            },
            modifier = Modifier.fillMaxWidth()
        )

        NavigationDrawerItem(
            label = { Text("Settings") },
            icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
            selected = currentView == AppView.SETTINGS,
            onClick = {
                viewModel.navigateTo(AppView.SETTINGS)
                onCloseDrawer()
            },
            modifier = Modifier.fillMaxWidth()
        )

        NavigationDrawerItem(
            label = { Text("Contributions") },
            icon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
            selected = currentView == AppView.CONTRIBUTIONS,
            onClick = {
                viewModel.navigateTo(AppView.CONTRIBUTIONS)
                onCloseDrawer()
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Divider()

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoggedIn) {
            OutlinedButton(
                onClick = {
                    CookieManager.getInstance().removeAllCookies(null)
                    CookieManager.getInstance().flush()
                    viewModel.setLoggedIn(false)
                    viewModel.setUsername("")
                    viewModel.navigateTo(AppView.HOME)
                    onCloseDrawer()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Rounded.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
        } else {
            Button(
                onClick = {
                    viewModel.navigateTo(AppView.LOGIN)
                    onCloseDrawer()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Login, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Login")
            }
        }
    }
}
