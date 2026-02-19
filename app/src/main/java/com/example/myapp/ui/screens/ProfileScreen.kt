package com.example.myapp.ui.screens

import android.webkit.CookieManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapp.data.AppView
import com.example.myapp.data.SearchResult
import com.example.myapp.ui.components.SidebarNavigationDrawer
import com.example.myapp.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val username by viewModel.username.collectAsState()

    SidebarNavigationDrawer(viewModel = viewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoggedIn) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = username.ifEmpty { "User" },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Logged in",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Divider()
                        Text(
                            text = "My Contributions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Coming soon",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Watchlist",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Coming soon",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                CookieManager.getInstance().removeAllCookies(null)
                                CookieManager.getInstance().flush()
                                viewModel.setLoggedIn(false)
                                viewModel.setUsername("")
                                viewModel.navigateTo(AppView.HOME)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Rounded.Logout, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Log out")
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Rounded.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "You are not logged in.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(
                            onClick = { viewModel.navigateTo(AppView.LOGIN) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Log in")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentChangesScreen(viewModel: MainViewModel) {
    SidebarNavigationDrawer(viewModel = viewModel) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Recent Changes - Coming Soon")
        }
    }
}

@Composable
private fun ValueSuggestionItem(
    suggestion: SearchResult,
    onClick: () -> Unit
) {
    val label = suggestion.label ?: suggestion.title ?: suggestion.id ?: ""
    val description = suggestion.description ?: ""
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(viewModel: MainViewModel) {
    val selectedEntityId by viewModel.selectedEntityId.collectAsState()
    val editMode by viewModel.editMode.collectAsState()
    val editTitle by viewModel.editTitle.collectAsState()
    val editEntityLabel by viewModel.editEntityLabel.collectAsState()
    val editPropertyId by viewModel.editPropertyId.collectAsState()
    val editPropertyLabel by viewModel.editPropertyLabel.collectAsState()
    val editCurrentValue by viewModel.editCurrentValue.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val editError by viewModel.editError.collectAsState()
    val valueSuggestions by viewModel.valueSuggestions.collectAsState()
    val isValueSuggestionsLoading by viewModel.isValueSuggestionsLoading.collectAsState()
    var newValue by remember { mutableStateOf(editCurrentValue ?: "") }
    var newLabel by remember { mutableStateOf(editEntityLabel) }
    var newDescription by remember { mutableStateOf(editCurrentValue ?: "") }
    LaunchedEffect(editCurrentValue, editEntityLabel, editMode) {
        newValue = editCurrentValue ?: ""
        newLabel = editEntityLabel
        if (editMode == MainViewModel.EditMode.EDIT_DESCRIPTION || editMode == MainViewModel.EditMode.CREATE_ITEM) {
            newDescription = editCurrentValue ?: ""
        }
    }

    SidebarNavigationDrawer(viewModel = viewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (editError != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(
                        text = editError!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            Text(
                text = editTitle ?: when (editMode) {
                    MainViewModel.EditMode.CREATE_ITEM -> "Create new item"
                    MainViewModel.EditMode.EDIT_LABEL -> "Edit label"
                    MainViewModel.EditMode.EDIT_DESCRIPTION -> "Edit description"
                    MainViewModel.EditMode.ADD_STATEMENT -> "Add statement"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (editMode != MainViewModel.EditMode.CREATE_ITEM) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Entity", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = editEntityLabel.ifEmpty { selectedEntityId ?: "—" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val entityId = selectedEntityId
                        if (!entityId.isNullOrEmpty()) {
                            Text(
                                text = entityId,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            if (editMode == MainViewModel.EditMode.CREATE_ITEM) {
                OutlinedTextField(
                    value = newLabel,
                    onValueChange = { newLabel = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Label (e.g. English)") },
                    placeholder = { Text("Item label") }
                )
                OutlinedTextField(
                    value = newDescription,
                    onValueChange = { newDescription = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description (optional)") },
                    placeholder = { Text("Short description") },
                    minLines = 2
                )
            } else if (editMode == MainViewModel.EditMode.EDIT_LABEL) {
                OutlinedTextField(
                    value = newLabel,
                    onValueChange = { newLabel = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Label") },
                    placeholder = { Text("New label") }
                )
            } else if (editMode == MainViewModel.EditMode.EDIT_DESCRIPTION) {
                OutlinedTextField(
                    value = newDescription,
                    onValueChange = { newDescription = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                    placeholder = { Text("New description") },
                    minLines = 3
                )
            } else {
                if (editPropertyId != null || editPropertyLabel != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Property", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = editPropertyLabel ?: editPropertyId ?: "",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                if (editCurrentValue != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Current value", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = editCurrentValue!!, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                OutlinedTextField(
                    value = newValue,
                    onValueChange = {
                        newValue = it
                        viewModel.setValueSearchQuery(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("New value") },
                    placeholder = { Text("Enter new value...") },
                    minLines = 2
                )
                if (isValueSuggestionsLoading) {
                    Text(
                        "Searching…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    valueSuggestions.forEach { suggestion ->
                        ValueSuggestionItem(
                            suggestion = suggestion,
                            onClick = {
                                val id = suggestion.id ?: return@ValueSuggestionItem
                                newValue = id
                                viewModel.clearValueSuggestions()
                            }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.clearEditContext()
                        viewModel.clearValueSuggestions()
                        if (selectedEntityId != null) viewModel.navigateTo(AppView.ENTITY) else viewModel.navigateTo(AppView.SEARCH)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        viewModel.saveEdit(
                            mode = editMode,
                            newLabel = newLabel,
                            newDescription = newDescription,
                            newValue = newValue
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving && when (editMode) {
                        MainViewModel.EditMode.CREATE_ITEM -> newLabel.isNotBlank()
                        MainViewModel.EditMode.EDIT_LABEL -> newLabel.isNotBlank()
                        MainViewModel.EditMode.EDIT_DESCRIPTION -> true
                        MainViewModel.EditMode.ADD_STATEMENT -> newValue.isNotBlank()
                    }
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(viewModel: MainViewModel) {
    SidebarNavigationDrawer(viewModel = viewModel) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Nearby - Coming Soon")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionsScreen(viewModel: MainViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    SidebarNavigationDrawer(viewModel = viewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Your Contributions",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (isLoggedIn) {
                        Text(
                            text = "View and manage your contributions to Wikidata",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "Please log in to view your contributions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(viewModel: MainViewModel) {
    SidebarNavigationDrawer(viewModel = viewModel) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Rounded.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Theme Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customize the appearance of the app",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Light")
                        }
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Dark")
                        }
                    }
                }
            }
        }
    }
}
