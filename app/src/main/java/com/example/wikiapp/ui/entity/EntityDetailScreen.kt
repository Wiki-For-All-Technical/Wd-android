package com.example.wikiapp.ui.entity

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wikiapp.data.model.*
import com.example.wikiapp.ui.theme.*
import com.example.wikiapp.ui.viewmodel.AuthViewModel
import com.example.wikiapp.ui.viewmodel.EntityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntityDetailScreen(
    entityId: String,
    onNavigateBack: () -> Unit,
    onEntityClick: (String) -> Unit,
    onEditClick: () -> Unit = {},
    onNavigateToEdit: (entityId: String, entityLabel: String, propertyId: String?, propertyLabel: String?, currentValue: String?) -> Unit = { _, _, _, _, _ -> },
    authViewModel: AuthViewModel = viewModel(),
    viewModel: EntityViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    
    LaunchedEffect(entityId) {
        viewModel.loadEntity(entityId)
    }
    
    Scaffold(
        topBar = {
            WikidataEntityTopBar(
                entityId = entityId,
                isLoggedIn = authState.isLoggedIn,
                username = authState.username,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(modifier = Modifier.padding(paddingValues))
            }
            uiState.error != null -> {
                ErrorState(
                    error = uiState.error!!,
                    onRetry = { viewModel.loadEntity(entityId) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            uiState.entity != null -> {
                val entity = uiState.entity!!
                val entityLabel = entity.labels?.get("en")?.value 
                    ?: entity.labels?.values?.firstOrNull()?.value 
                    ?: entityId
                    
                EntityContent(
                    entity = entity,
                    propertyLabels = uiState.propertyLabels,
                    entityLabels = uiState.entityLabels,
                    onEntityClick = onEntityClick,
                    isLoggedIn = authState.isLoggedIn,
                    onEditClick = { propertyId, propertyLabel, currentValue ->
                        if (authState.isLoggedIn) {
                            // Navigate to Edit screen
                            onNavigateToEdit(entityId, entityLabel, propertyId, propertyLabel, currentValue)
                        } else {
                            // Navigate to Login
                            onEditClick()
                        }
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WikidataEntityTopBar(
    entityId: String,
    isLoggedIn: Boolean,
    username: String,
    onNavigateBack: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Base30
                    )
                }
                
                // Wikidata branding
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WikidataBarcodeIcon(size = 24)
                Text(
                        text = "WIKIDATA",
                    style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Base10,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            // Show logged in user info if logged in
            if (isLoggedIn) {
                Surface(
                    color = WikidataTeal.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = WikidataTeal
                        )
                        Text(
                            text = username,
                            style = MaterialTheme.typography.labelMedium,
                            color = WikidataTeal,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Entity ID badge when not logged in
                Text(
                    text = entityId,
                    style = MaterialTheme.typography.labelMedium,
                    color = WikidataItemGreen,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun WikidataBarcodeIcon(size: Int = 40) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(1.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barData = listOf(
            Pair(0.6f, AccentRed),
            Pair(0.8f, WikidataItemGreen),
            Pair(0.5f, WikidataTeal),
            Pair(1f, AccentRed),
            Pair(0.7f, WikidataItemGreen),
            Pair(0.55f, WikidataTeal),
            Pair(0.85f, AccentRed),
            Pair(0.5f, WikidataItemGreen),
            Pair(0.7f, WikidataTeal)
        )
        
        barData.forEach { (heightFraction, color) ->
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height((size * heightFraction).dp)
                    .clip(RoundedCornerShape(0.5.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
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
                text = "Loading entity...",
                style = MaterialTheme.typography.bodyMedium,
                color = Base30
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = AccentRed
            )
            Text(
                text = "Failed to load entity",
                style = MaterialTheme.typography.titleLarge,
                color = Base10
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = Base30
            )
            OutlinedButton(
                onClick = onRetry,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = WikidataTeal
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, WikidataTeal)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EntityContent(
    entity: WikidataEntity,
    propertyLabels: Map<String, String>,
    entityLabels: Map<String, String>,
    onEntityClick: (String) -> Unit,
    isLoggedIn: Boolean = false,
    onEditClick: (propertyId: String?, propertyLabel: String?, currentValue: String?) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        // Entity Header - Wikidata style
        item {
            EntityHeader(
                entity = entity, 
                isLoggedIn = isLoggedIn, 
                onEditClick = { onEditClick(null, null, null) }
            )
        }
        
        // Tabs bar (Item | Discussion - Read | View history | Tools)
        item {
            EntityTabsBar()
        }
        
        // Description
        entity.descriptions?.get("en")?.value?.let { description ->
            item {
                DescriptionSection(description = description)
            }
        }
        
        // Aliases Section (Also known as)
        entity.aliases?.get("en")?.takeIf { it.isNotEmpty() }?.let { aliases ->
            item {
                AliasesSection(aliases = aliases)
            }
        }
        
        // Statements Section
        entity.claims?.takeIf { it.isNotEmpty() }?.let { claims ->
            item {
                StatementsHeader(count = claims.values.sumOf { it.size })
            }
            
            claims.entries.forEach { (propertyId, claimList) ->
                val propLabel = propertyLabels[propertyId]
                item(key = propertyId) {
                    StatementGroup(
                        propertyId = propertyId,
                        propertyLabel = propLabel,
                        claims = claimList,
                        entityLabels = entityLabels,
                        onEntityClick = onEntityClick,
                        onEditClick = { currentValue ->
                            onEditClick(propertyId, propLabel, currentValue)
                        },
                        isLoggedIn = isLoggedIn
                    )
                }
            }
        }
        
        // Sitelinks Section (Wikipedia)
        entity.sitelinks?.takeIf { it.isNotEmpty() }?.let { sitelinks ->
            item {
                SitelinksSection(sitelinks = sitelinks)
            }
        }
        
        // Footer spacing
            item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
private fun EntityHeader(
    entity: WikidataEntity,
    isLoggedIn: Boolean = false,
    onEditClick: () -> Unit = {}
) {
    val entityColor = when {
        entity.id.startsWith("Q") -> WikidataItemGreen
        entity.id.startsWith("P") -> WikidataPropertyOrange
        entity.id.startsWith("L") -> WikidataLexemePurple
        else -> WikidataTeal
    }
    
    val entityType = when {
        entity.id.startsWith("Q") -> "Item"
        entity.id.startsWith("P") -> "Property"
        entity.id.startsWith("L") -> "Lexeme"
        else -> "Entity"
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Entity ID badge with type and edit button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Q/P/L number badge
                Surface(
                    color = entityColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = entity.id,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = entityColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                
                // Entity type label
                Text(
                    text = "• $entityType",
                    style = MaterialTheme.typography.labelLarge,
                    color = Base50
                )
            }
        }
        
        // Main label/title
            Text(
                text = entity.labels?.get("en")?.value 
                    ?: entity.labels?.values?.firstOrNull()?.value 
                    ?: entity.id,
                style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Normal,
            color = Base10
        )
    }
}

@Composable
private fun EntityTabsBar() {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left tabs
                TabItem(text = "Item", isSelected = true)
                TabItem(text = "Discussion", isSelected = false)
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Right tabs
                TabItem(text = "Read", isSelected = true)
                TabItem(text = "View history", isSelected = false)
                TabItem(text = "Tools", isSelected = false, hasDropdown = true)
            }
            
            HorizontalDivider(color = Base70, thickness = 1.dp)
        }
    }
}

@Composable
private fun TabItem(
    text: String,
    isSelected: Boolean,
    hasDropdown: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .clickable { }
                .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) AccentBlue else Base30
            )
            if (hasDropdown) {
                    Icon(
                    Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Base30
                )
            }
        }
        
        if (isSelected && text == "Item") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(AccentBlue)
            )
        }
    }
}

@Composable
private fun DescriptionSection(description: String) {
    Text(
        text = description,
        style = MaterialTheme.typography.bodyLarge,
        color = Base30,
        fontStyle = FontStyle.Italic,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun AliasesSection(aliases: List<Alias>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Also known as",
            style = MaterialTheme.typography.labelLarge,
            color = Base30,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            aliases.forEach { alias ->
                Surface(
                    color = Base90,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = alias.value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Base20,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatementsHeader(count: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Base90
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
        Text(
                text = "Statements",
                style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
                color = Base10
        )
            
        Surface(
                color = WikidataTeal.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = WikidataTeal,
                    fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
            }
        }
    }
}

@Composable
private fun StatementGroup(
    propertyId: String,
    propertyLabel: String?,
    claims: List<Claim>,
    entityLabels: Map<String, String>,
    onEntityClick: (String) -> Unit,
    onEditClick: (currentValue: String?) -> Unit = {},
    isLoggedIn: Boolean = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
            .background(Color.White)
        ) {
        // Property row with orange left border for properties (Wikidata style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                .drawLeftBorder(WikidataPropertyOrange)
                .background(Base90.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            // Property ID badge (P number)
            Surface(
                color = WikidataPropertyOrange.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.clickable { onEntityClick(propertyId) }
                ) {
                    Text(
                        text = propertyId,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    color = WikidataPropertyOrange,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
                
            // Property label
                Text(
                    text = propertyLabel ?: propertyId,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AccentBlue,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onEntityClick(propertyId) }
            )
            
            // Edit icon - triggers login if not logged in
            IconButton(
                onClick = { onEditClick(null) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(18.dp),
                    tint = Base50
                )
            }
        }
        
        // Values with edit icons
            claims.forEach { claim ->
                StatementValue(
                    claim = claim,
                entityLabels = entityLabels,
                onEntityClick = onEntityClick,
                onEditClick = onEditClick,
                isLoggedIn = isLoggedIn
            )
        }
        
        HorizontalDivider(color = Base80, thickness = 0.5.dp)
    }
}

// Helper function to draw left border
private fun Modifier.drawLeftBorder(color: Color) = this.then(
    Modifier.drawWithContent {
        drawContent()
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset.Zero,
            size = androidx.compose.ui.geometry.Size(3.dp.toPx(), size.height)
        )
    }
)

@Composable
private fun StatementValue(
    claim: Claim,
    entityLabels: Map<String, String>,
    onEntityClick: (String) -> Unit,
    onEditClick: (currentValue: String?) -> Unit = {},
    isLoggedIn: Boolean = false
) {
    val dataValue = claim.mainsnak.datavalue
    
    // Extract the current value as a string for editing
    val currentValueString = remember(dataValue) {
        extractDisplayValue(dataValue, entityLabels)
    }
    
    // State for inline editing
    var isEditing by remember { mutableStateOf(false) }
    var editedValue by remember { mutableStateOf(currentValueString) }
    val focusManager = LocalFocusManager.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // Main value display row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Rank indicator
        val rankColor = when (claim.rank) {
            "preferred" -> PreferredRankColor
            "deprecated" -> DeprecatedRankColor
            else -> NormalRankColor
        }
        
            Icon(
                when (claim.rank) {
                    "preferred" -> Icons.Default.KeyboardDoubleArrowUp
                    "deprecated" -> Icons.Default.KeyboardDoubleArrowDown
                    else -> Icons.Default.Remove
                },
                contentDescription = "Rank: ${claim.rank}",
                modifier = Modifier.size(16.dp),
                tint = rankColor
        )
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Main value
            when (claim.mainsnak.datatype) {
                "wikibase-item", "wikibase-property" -> {
                    EntityValueDisplay(
                        dataValue = dataValue,
                            entityLabels = entityLabels,
                            onClick = { entityId -> onEntityClick(entityId) }
                        )
                    }
                    "time" -> TimeValueDisplay(dataValue = dataValue)
                    "quantity" -> QuantityValueDisplay(dataValue = dataValue)
                    "globe-coordinate" -> CoordinateValueDisplay(dataValue = dataValue)
                    "url" -> UrlValueDisplay(dataValue = dataValue)
                    "external-id" -> ExternalIdValueDisplay(dataValue = dataValue)
                    else -> GenericValueDisplay(dataValue = dataValue)
                }
                
                // Qualifiers
                claim.qualifiers?.takeIf { it.isNotEmpty() }?.let { qualifiers ->
                    QualifiersDisplay(
                        qualifiers = qualifiers,
                        entityLabels = entityLabels,
                        onEntityClick = onEntityClick
                    )
                }
                
                // References count
                claim.references?.takeIf { it.isNotEmpty() }?.let { refs ->
                    Text(
                        text = "References (${refs.size} total)",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentBlue,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { }
                    )
                }
            }
            
            // Action icons: References badge, Edit, Add/Remove
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // References badge
                claim.references?.takeIf { it.isNotEmpty() }?.let { refs ->
                    Surface(
                        color = AccentBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${refs.size} ref${if (refs.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentBlue,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                // Edit icon
                if (isLoggedIn) {
                    IconButton(
                        onClick = { 
                            isEditing = true
                            editedValue = currentValueString
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Edit this value",
                            modifier = Modifier.size(18.dp),
                            tint = Base50
                        )
                    }
                } else {
                    IconButton(
                        onClick = { onEditClick(currentValueString) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "Edit this value",
                            modifier = Modifier.size(18.dp),
                            tint = Base50
                        )
                    }
                }
                
                // Add/Remove icon
                if (isLoggedIn) {
                    IconButton(
                        onClick = { /* Add new value */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add value",
                            modifier = Modifier.size(18.dp),
                            tint = Base50
                        )
                    }
                }
            }
        }
        
        // Inline Edit Section (expands when editing)
        AnimatedVisibility(
            visible = isEditing && isLoggedIn,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
        ) {
            InlineEditSection(
                currentValue = currentValueString,
                editedValue = editedValue,
                onValueChange = { editedValue = it },
                onSave = {
                    // Save logic here
                    isEditing = false
                    focusManager.clearFocus()
                },
                onCancel = {
                    isEditing = false
                    editedValue = currentValueString
                    focusManager.clearFocus()
                }
            )
        }
    }
}

@Composable
private fun InlineEditSection(
    currentValue: String,
    editedValue: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = Base90.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // "Edit Value:" label
            Text(
                text = "Edit Value:",
                style = MaterialTheme.typography.labelMedium,
                color = Base30,
                fontWeight = FontWeight.Medium
            )
            
            // Text input field
            OutlinedTextField(
                value = editedValue,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter value...", color = Base50) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WikidataTeal,
                    unfocusedBorderColor = Base70,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp)
            )
            
            // Save and Cancel buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Save button
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WikidataTeal
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Cancel button
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Base30
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Base70),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun extractDisplayValue(dataValue: DataValue?, entityLabels: Map<String, String>): String {
    if (dataValue == null) return ""
    return when (val value = dataValue.value) {
        is String -> value
        is Map<*, *> -> {
            val entityId = value["id"] as? String
            if (entityId != null) {
                entityLabels[entityId] ?: entityId
            } else {
                val time = value["time"] as? String
                if (time != null) {
                    time.removePrefix("+").take(10)
                } else {
                    val amount = value["amount"] as? String
                    amount?.removePrefix("+") ?: value["value"] as? String ?: ""
                }
            }
        }
        else -> value?.toString() ?: ""
    }
}

@Composable
private fun EntityValueDisplay(
    dataValue: DataValue?,
    entityLabels: Map<String, String>,
    onClick: (String) -> Unit
) {
    if (dataValue == null) {
        Text(
            text = "no value",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = Base50
        )
        return
    }
    
    val value = dataValue.value as? Map<*, *>
    val entityId = value?.get("id") as? String ?: return
    val entityLabel = entityLabels[entityId]
    
    // Determine entity type color based on Q/P/L prefix
    val entityColor = when {
            entityId.startsWith("Q") -> WikidataItemGreen
            entityId.startsWith("P") -> WikidataPropertyOrange
        entityId.startsWith("L") -> WikidataLexemePurple
            else -> WikidataTeal
        }
        
    // Clickable card showing Label + Q number
    Surface(
            modifier = Modifier
            .clickable { onClick(entityId) },
        color = entityColor.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, entityColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main content: Label with Q number in format "Label (Q1234)"
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Entity Label with Q number: "Label (Q1234)"
                Text(
                    text = if (entityLabel != null) "$entityLabel ($entityId)" else entityId,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = AccentBlue,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Entity type indicator
                val entityType = when {
                    entityId.startsWith("Q") -> "Item"
                    entityId.startsWith("P") -> "Property"
                    entityId.startsWith("L") -> "Lexeme"
                    else -> "Entity"
                }
                Text(
                    text = "• $entityType",
                    style = MaterialTheme.typography.labelSmall,
                    color = Base50
                )
            }
            
            // Arrow icon to indicate navigation
        Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View $entityId",
                modifier = Modifier.size(20.dp),
                tint = entityColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun TimeValueDisplay(dataValue: DataValue?) {
    val value = dataValue?.value as? Map<*, *>
    val time = value?.get("time") as? String ?: "Unknown"
    val precision = (value?.get("precision") as? Number)?.toInt() ?: 11
    
    val displayTime = when (precision) {
        9 -> time.substring(1, 5) // Year only
        10 -> time.substring(1, 8) // Year-month
        11 -> time.substring(1, 11) // Full date
        else -> time.removePrefix("+").take(10)
    }
    
    // Format date nicely: "11 March 1952"
    val formattedDate = try {
        if (displayTime.length >= 10) {
            val year = displayTime.substring(0, 4)
            val month = displayTime.substring(5, 7).toInt()
            val day = displayTime.substring(8, 10).toInt()
            val monthNames = listOf("", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December")
            "$day ${monthNames.getOrElse(month) { month.toString() }} $year"
        } else {
            displayTime
        }
    } catch (e: Exception) {
        displayTime
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            Icons.Default.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Base50
        )
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodyMedium,
            color = Base10
        )
    }
}

@Composable
private fun QuantityValueDisplay(dataValue: DataValue?) {
    val value = dataValue?.value as? Map<*, *>
    val amount = value?.get("amount") as? String ?: "Unknown"
    val unit = value?.get("unit") as? String
    
    val displayAmount = amount.removePrefix("+")
    val unitId = unit?.substringAfterLast("/")
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = displayAmount,
            style = MaterialTheme.typography.bodyMedium,
            color = Base10
        )
        unitId?.takeIf { it != "1" }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = AccentBlue
            )
        }
    }
}

@Composable
private fun CoordinateValueDisplay(dataValue: DataValue?) {
    val value = dataValue?.value as? Map<*, *>
    val lat = (value?.get("latitude") as? Number)?.toDouble() ?: 0.0
    val lon = (value?.get("longitude") as? Number)?.toDouble() ?: 0.0
    
        Text(
        text = "%.4f°, %.4f°".format(lat, lon),
        style = MaterialTheme.typography.bodyMedium,
        color = Base10
        )
}

@Composable
private fun UrlValueDisplay(dataValue: DataValue?) {
    val value = dataValue?.value as? String ?: dataValue?.value?.toString() ?: "Unknown"
    
    Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium,
        color = AccentBlue,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun ExternalIdValueDisplay(dataValue: DataValue?) {
    val value = dataValue?.value as? String ?: dataValue?.value?.toString() ?: "Unknown"
    
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        fontFamily = FontFamily.Monospace,
        color = Base10
        )
}

@Composable
private fun GenericValueDisplay(dataValue: DataValue?) {
    val displayText = when (val value = dataValue?.value) {
        is String -> value
        is Map<*, *> -> value["value"] as? String ?: value.toString()
        else -> value?.toString() ?: "no value"
    }
    
    Text(
        text = displayText,
        style = MaterialTheme.typography.bodyMedium,
        color = Base10
    )
}

@Composable
private fun QualifiersDisplay(
    qualifiers: Map<String, List<Snak>>,
    entityLabels: Map<String, String>,
    onEntityClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = 16.dp, top = 8.dp)
            .fillMaxWidth()
            .background(Base90.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        qualifiers.entries.take(5).forEach { (propId, snaks) ->
            snaks.forEach { snak ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Property badge
                    Surface(
                        color = WikidataPropertyOrange.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(3.dp)
                ) {
                    Text(
                        text = propId,
                        style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = WikidataPropertyOrange,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                    }
                    
                    // Value with label resolution
                    val displayValue = formatSnakValueWithLabel(snak, entityLabels)
                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.bodySmall,
                        color = Base30
                    )
                }
            }
        }
    }
}

private fun formatSnakValue(snak: Snak): String {
    val dataValue = snak.datavalue ?: return "unknown"
    return when (val value = dataValue.value) {
        is String -> value
        is Map<*, *> -> {
            value["id"] as? String
                ?: value["time"] as? String
                ?: value["amount"] as? String
                ?: value["value"] as? String
                ?: value.toString()
        }
        else -> value?.toString() ?: "unknown"
    }
}

private fun formatSnakValueWithLabel(snak: Snak, entityLabels: Map<String, String>): String {
    val dataValue = snak.datavalue ?: return "unknown"
    return when (val value = dataValue.value) {
        is String -> value
        is Map<*, *> -> {
            val entityId = value["id"] as? String
            if (entityId != null) {
                // Return label if available, otherwise the ID
                entityLabels[entityId] ?: entityId
            } else {
                value["time"] as? String
                    ?: value["amount"] as? String
                    ?: value["value"] as? String
                    ?: value.toString()
            }
        }
        else -> value?.toString() ?: "unknown"
    }
}

@Composable
private fun SitelinksSection(sitelinks: Map<String, SiteLink>) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Base90
        ) {
            Row(
            modifier = Modifier
                .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Wikipedia Articles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Base10
                )
                
                Surface(
                    color = AccentBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${sitelinks.size} languages",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentBlue,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
        
        // Sitelinks list (display only, no browser opening)
            val sortedSitelinks = sitelinks.entries
                .filter { it.key.endsWith("wiki") && !it.key.contains("quote") }
                .sortedBy { 
                    when (it.key) {
                        "enwiki" -> 0
                        "dewiki" -> 1
                        "frwiki" -> 2
                        "eswiki" -> 3
                        "jawiki" -> 4
                        else -> 10
                    }
                }
                .take(10)
            
            sortedSitelinks.forEach { (site, sitelink) ->
                val languageCode = site.removeSuffix("wiki")
                val languageName = getLanguageName(languageCode)
                
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Language badge
                    Surface(
                        color = AccentBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = languageCode.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = sitelink.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Base10,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = languageName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Base50
                        )
                    }
                }
            }
            
            HorizontalDivider(color = Base80, thickness = 0.5.dp)
        }
        
        // Show remaining count
            val remaining = sitelinks.size - sortedSitelinks.size
            if (remaining > 0) {
                Text(
                text = "+$remaining more languages available",
                    style = MaterialTheme.typography.labelMedium,
                color = Base50,
                modifier = Modifier.padding(16.dp)
                )
        }
    }
}

private fun getLanguageName(code: String): String {
    return when (code) {
        "en" -> "English"
        "de" -> "German"
        "fr" -> "French"
        "es" -> "Spanish"
        "it" -> "Italian"
        "pt" -> "Portuguese"
        "ru" -> "Russian"
        "ja" -> "Japanese"
        "zh" -> "Chinese"
        "ar" -> "Arabic"
        "ko" -> "Korean"
        "nl" -> "Dutch"
        "pl" -> "Polish"
        "sv" -> "Swedish"
        "vi" -> "Vietnamese"
        "uk" -> "Ukrainian"
        "he" -> "Hebrew"
        "fa" -> "Persian"
        "id" -> "Indonesian"
        "tr" -> "Turkish"
        "hi" -> "Hindi"
        "bn" -> "Bengali"
        "ta" -> "Tamil"
        "te" -> "Telugu"
        else -> code.uppercase()
    }
}

