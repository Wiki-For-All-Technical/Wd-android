package com.example.wikiapp.ui.search

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wikiapp.data.model.SearchResult
import com.example.wikiapp.ui.theme.*
import com.example.wikiapp.ui.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    initialQuery: String = "",
    onEntityClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var showSuggestions by remember { mutableStateOf(true) }
    
    // Initialize with query if provided
    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank() && uiState.query.isBlank()) {
            viewModel.updateQuery(initialQuery)
            viewModel.search()
            showSuggestions = false
        }
    }
    
    Scaffold(
        topBar = {
            Column {
                WikidataSearchTopBar(
                    query = uiState.query,
                    onQueryChange = { newQuery ->
                        viewModel.updateQuery(newQuery)
                        showSuggestions = true
                    },
                    onSearch = {
                        viewModel.search()
                        showSuggestions = false
                        focusManager.clearFocus()
                    },
                    onNavigateBack = onNavigateBack,
                    onClear = { 
                        viewModel.updateQuery("") 
                        showSuggestions = true
                    }
                )
                
                // Suggestions dropdown directly below search bar
                AnimatedVisibility(
                    visible = showSuggestions && uiState.query.isNotEmpty() && 
                             (uiState.suggestions.isNotEmpty() || uiState.isSuggestionsLoading),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SuggestionsDropdown(
                        suggestions = uiState.suggestions,
                        isLoading = uiState.isSuggestionsLoading,
                        query = uiState.query,
                        onSuggestionClick = { suggestion ->
                            val entityId = suggestion.id ?: ""
                            if (entityId.isNotEmpty()) {
                                showSuggestions = false
                                viewModel.clearSuggestions()
                                onEntityClick(entityId)
                            }
                        }
                    )
                }
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Error Message
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                ErrorBanner(
                    error = uiState.error ?: "",
                    onDismiss = viewModel::clearError
                )
            }
            
            // Main Content
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.results.isNotEmpty() -> {
                    SearchResults(
                        results = uiState.results,
                        query = uiState.query,
                        onEntityClick = onEntityClick
                    )
                }
                uiState.query.isNotEmpty() && !showSuggestions && !uiState.isLoading -> {
                    EmptyState(query = uiState.query)
                }
                else -> {
                    InitialState()
                }
            }
        }
    }
}

@Composable
private fun SuggestionsDropdown(
    suggestions: List<SearchResult>,
    isLoading: Boolean,
    query: String,
    onSuggestionClick: (SearchResult) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 350.dp)
        ) {
            // Loading indicator
            if (isLoading && suggestions.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = WikidataTeal,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Searching for \"$query\"...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Base50
                    )
                }
            }
            
            // Suggestions list
            if (suggestions.isNotEmpty()) {
                LazyColumn {
                    items(suggestions) { suggestion ->
                        SuggestionItem(
                            suggestion = suggestion,
                            onClick = { onSuggestionClick(suggestion) }
                        )
                        HorizontalDivider(color = Base80, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    suggestion: SearchResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search icon
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Base50
        )
        
        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Label
            Text(
                text = suggestion.label
                    ?: suggestion.display?.label?.value
                    ?: suggestion.title
                    ?: suggestion.id
                    ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Base10,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Description
            val description = suggestion.description ?: suggestion.display?.description?.value
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Base50,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // Entity ID badge (Q number)
        val entityId = suggestion.id ?: ""
        val badgeColor = when {
            entityId.startsWith("Q") -> WikidataItemGreen
            entityId.startsWith("P") -> WikidataPropertyOrange
            entityId.startsWith("L") -> WikidataLexemePurple
            else -> WikidataTeal
        }
        
        Surface(
            color = badgeColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = entityId,
                style = MaterialTheme.typography.labelSmall,
                color = badgeColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WikidataSearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onNavigateBack: () -> Unit,
    onClear: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Base30
                )
            }
            
            // Search input
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = {
                    Text(
                        "Search Wikidata (e.g., \"he\", \"Q42\")",
                        color = Base50
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = WikidataTeal
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Base50
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WikidataTeal,
                    unfocusedBorderColor = Base70,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp)
            )
        }
    }
}

@Composable
private fun ErrorBanner(
    error: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = AccentRed.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = AccentRed
                )
                Text(
                    text = error,
                    color = AccentRed,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = AccentRed
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = WikidataTeal,
                strokeWidth = 2.dp,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Searching...",
                style = MaterialTheme.typography.bodyMedium,
                color = Base30
            )
        }
    }
}

@Composable
private fun EmptyState(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Base70
            )
            Text(
                text = "No results",
                style = MaterialTheme.typography.titleLarge,
                color = Base20
            )
            Text(
                text = "There is no item matching \"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                color = Base30
            )
        }
    }
}

@Composable
private fun InitialState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = WikidataTeal.copy(alpha = 0.5f)
            )
            Text(
                text = "Search Wikidata",
                style = MaterialTheme.typography.titleLarge,
                color = Base20
            )
            Text(
                text = "Type to see suggestions\nPress Enter to search",
                style = MaterialTheme.typography.bodyMedium,
                color = Base30,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Example searches
            Column(
                modifier = Modifier.padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Try searching:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Base50
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExampleChip("Douglas Adams")
                    ExampleChip("Q42")
                    ExampleChip("Earth")
                }
            }
        }
    }
}

@Composable
private fun ExampleChip(text: String) {
    Surface(
        color = Base90,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = AccentBlue,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SearchResults(
    results: List<SearchResult>,
    query: String,
    onEntityClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Results header
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Base90
            ) {
                Text(
                    text = "${results.size} results for \"$query\"",
                    style = MaterialTheme.typography.labelLarge,
                    color = Base30,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
        
        items(results) { result ->
            WikidataSearchResultItem(
                result = result,
                onClick = {
                    val entityId = result.id ?: result.title ?: ""
                    if (entityId.isNotEmpty()) {
                        onEntityClick(entityId)
                    }
                }
            )
            HorizontalDivider(color = Base80, thickness = 0.5.dp)
        }
    }
}

@Composable
private fun WikidataSearchResultItem(
    result: SearchResult,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Entity type indicator (Q/P/L badge)
            val entityId = result.id ?: ""
            val badgeColor = when {
                entityId.startsWith("Q") -> WikidataItemGreen
                entityId.startsWith("P") -> WikidataPropertyOrange
                entityId.startsWith("L") -> WikidataLexemePurple
                else -> WikidataTeal
            }
            
            Surface(
                color = badgeColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = entityId,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Label
                Text(
                    text = result.label
                        ?: result.display?.label?.value
                        ?: result.title
                        ?: result.id
                        ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = AccentBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Description
                val description = result.description ?: result.display?.description?.value
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Base30,
                        fontStyle = FontStyle.Italic,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Aliases
                result.aliases?.takeIf { it.isNotEmpty() }?.let { aliases ->
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Base50)) {
                                append("Also known as: ")
                            }
                            withStyle(SpanStyle(color = Base30)) {
                                append(aliases.take(3).joinToString(", "))
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Arrow indicator
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Base50
            )
        }
    }
}
