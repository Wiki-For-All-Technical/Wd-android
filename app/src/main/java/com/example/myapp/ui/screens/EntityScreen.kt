package com.example.myapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapp.data.AppView
import com.example.myapp.data.Claim
import com.example.myapp.data.SearchResult
import com.example.myapp.ui.components.SidebarNavigationDrawer
import com.example.myapp.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntityScreen(viewModel: MainViewModel) {
    val currentEntity by viewModel.currentEntity.collectAsState()
    val isEntityLoading by viewModel.isEntityLoading.collectAsState()
    val selectedEntityId by viewModel.selectedEntityId.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val entityError by viewModel.entityError.collectAsState()
    val editError by viewModel.editError.collectAsState()
    val propertyLabels by viewModel.propertyLabels.collectAsState()
    val itemLabels by viewModel.itemLabels.collectAsState()

    SidebarNavigationDrawer(viewModel = viewModel) {
        if (isEntityLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (entityError != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = entityError!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = {
                            viewModel.clearEntityError()
                            if (selectedEntityId != null) viewModel.loadEntity(selectedEntityId!!)
                        }
                    ) { Text("Retry") }
                }
            }
        } else if (currentEntity != null) {
            val entity = currentEntity!!
            val entityLabelEn = entity.labels?.get("en")?.value
                ?: entity.labels?.values?.firstOrNull()?.value
                ?: entity.id
            val entityDescriptionEn = entity.descriptions?.get("en")?.value
                ?: entity.descriptions?.values?.firstOrNull()?.value

            var isEditMenuExpanded by remember { mutableStateOf(false) }
            var languagesExpanded by remember { mutableStateOf(true) }
            var showAllSitelinks by remember { mutableStateOf(false) }
            var showAddStatement by remember { mutableStateOf(false) }
            var addStatementSelectedPropertyId by remember { mutableStateOf<String?>(null) }
            var addStatementSelectedPropertyLabel by remember { mutableStateOf<String?>(null) }
            var addStatementValue by remember { mutableStateOf("") }

            val statementPropertySearchQuery by viewModel.qualifierPropertySearchQuery.collectAsState()
            val statementPropertySuggestions by viewModel.qualifierPropertySuggestions.collectAsState()
            val isStatementPropertySuggestionsLoading by viewModel.isQualifierPropertySuggestionsLoading.collectAsState()
            val valueSuggestions by viewModel.valueSuggestions.collectAsState()
            val isValueSuggestionsLoading by viewModel.isValueSuggestionsLoading.collectAsState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // —— Edit error banner (save failures) ——
                AnimatedVisibility(visible = editError != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = editError ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearEditError() }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Dismiss")
                            }
                        }
                    }
                }
                // —— Header: title + description line with pencil "edit" (like reference) ——
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "$entityLabelEn (${entity.id})",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entityDescriptionEn ?: "—",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            if (isLoggedIn) {
                                TextButton(
                                    onClick = {
                                        viewModel.setEditContext(
                                            mode = MainViewModel.EditMode.EDIT_DESCRIPTION,
                                            title = "Edit description",
                                            entityLabel = entityLabelEn,
                                            currentValue = entityDescriptionEn
                                        )
                                        viewModel.navigateTo(AppView.EDIT)
                                    }
                                ) {
                                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("edit")
                                }
                            } else {
                                TextButton(onClick = {
                                    viewModel.setPendingEditEntityId(entity.id)
                                    viewModel.navigateTo(AppView.LOGIN)
                                }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Log in to edit")
                                }
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Box {
                                if (isLoggedIn) {
                                    IconButton(onClick = { isEditMenuExpanded = true }) {
                                        Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                                    }
                                    DropdownMenu(
                                        expanded = isEditMenuExpanded,
                                        onDismissRequest = { isEditMenuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Edit label") },
                                            onClick = {
                                                isEditMenuExpanded = false
                                                viewModel.setEditContext(
                                                    mode = MainViewModel.EditMode.EDIT_LABEL,
                                                    title = "Edit label",
                                                    entityLabel = entityLabelEn,
                                                    currentValue = entityLabelEn
                                                )
                                                viewModel.navigateTo(AppView.EDIT)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Edit description") },
                                            onClick = {
                                                isEditMenuExpanded = false
                                                viewModel.setEditContext(
                                                    mode = MainViewModel.EditMode.EDIT_DESCRIPTION,
                                                    title = "Edit description",
                                                    entityLabel = entityLabelEn,
                                                    currentValue = entityDescriptionEn
                                                )
                                                viewModel.navigateTo(AppView.EDIT)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Add statement") },
                                            onClick = {
                                                isEditMenuExpanded = false
                                                viewModel.setEditContext(
                                                    mode = MainViewModel.EditMode.ADD_STATEMENT,
                                                    title = "Add statement",
                                                    entityLabel = entityLabelEn
                                                )
                                                viewModel.navigateTo(AppView.EDIT)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // —— In more languages (collapsible table) ——
                val labels = entity.labels ?: emptyMap()
                val descriptions = entity.descriptions ?: emptyMap()
                val aliases = entity.aliases ?: emptyMap()
                val allLangs = (labels.keys + descriptions.keys + aliases.keys).toSortedSet()
                if (allLangs.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { languagesExpanded = !languagesExpanded },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (languagesExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = null
                                )
                                Text(
                                    "In more languages",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            AnimatedVisibility(
                                visible = languagesExpanded,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Language", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(100.dp))
                                        Text("Label", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                        Text("Description", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                        Text("Also known as", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    }
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    allLangs.forEach { lang ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(lang, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(100.dp))
                                            Text(labels[lang]?.value ?: "—", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                                            Text(descriptions[lang]?.value ?: "—", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                            Text(aliases[lang]?.joinToString(", ") { it.value } ?: "—", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // —— Statements: each property block with edit / add reference / add value ——
                entity.claims?.let { claims ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Statements",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isLoggedIn) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                IconButton(
                                    onClick = {
                                        showAddStatement = !showAddStatement
                                        if (showAddStatement) {
                                            addStatementSelectedPropertyId = null
                                            addStatementSelectedPropertyLabel = null
                                            addStatementValue = ""
                                            viewModel.clearQualifierPropertySuggestions()
                                        }
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = "Add statement", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = showAddStatement,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Add statement",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(
                                        onClick = {
                                            showAddStatement = false
                                            addStatementSelectedPropertyId = null
                                            addStatementSelectedPropertyLabel = null
                                            addStatementValue = ""
                                            viewModel.clearQualifierPropertySuggestions()
                                            viewModel.clearValueSuggestions()
                                        }
                                    ) { Text("remove") }
                                }
                                if (addStatementSelectedPropertyId == null) {
                                    OutlinedTextField(
                                        value = statementPropertySearchQuery,
                                        onValueChange = { viewModel.setQualifierPropertySearchQuery(it) },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("property") },
                                        singleLine = true
                                    )
                                    if (isStatementPropertySuggestionsLoading) {
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
                                        statementPropertySuggestions.forEach { suggestion ->
                                            QualifierPropertySuggestionItem(
                                                suggestion = suggestion,
                                                onClick = {
                                                    val id = suggestion.id ?: return@QualifierPropertySuggestionItem
                                                    addStatementSelectedPropertyId = id
                                                    addStatementSelectedPropertyLabel = suggestion.label
                                                        ?: suggestion.title ?: id
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${addStatementSelectedPropertyId}: ${addStatementSelectedPropertyLabel ?: ""}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = {
                                            addStatementSelectedPropertyId = null
                                            addStatementSelectedPropertyLabel = null
                                        }) { Text("remove") }
                                    }
                                    OutlinedTextField(
                                        value = addStatementValue,
                                        onValueChange = {
                                            addStatementValue = it
                                            viewModel.setValueSearchQuery(it)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Value") },
                                        placeholder = { Text("e.g. Q42 or value...") },
                                        singleLine = true
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
                                            QualifierPropertySuggestionItem(
                                                suggestion = suggestion,
                                                onClick = {
                                                    val id = suggestion.id ?: return@QualifierPropertySuggestionItem
                                                    addStatementValue = id
                                                    viewModel.clearValueSuggestions()
                                                }
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                showAddStatement = false
                                                addStatementSelectedPropertyId = null
                                                addStatementSelectedPropertyLabel = null
                                                addStatementValue = ""
                                                viewModel.clearQualifierPropertySuggestions()
                                                viewModel.clearValueSuggestions()
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(Icons.Rounded.Close, contentDescription = "Cancel", modifier = Modifier.size(24.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                if (addStatementSelectedPropertyId != null && addStatementValue.isNotBlank()) {
                                                    viewModel.setEditContext(
                                                        mode = MainViewModel.EditMode.ADD_STATEMENT,
                                                        title = "Add statement",
                                                        entityLabel = entityLabelEn,
                                                        propertyId = addStatementSelectedPropertyId!!,
                                                        propertyLabel = addStatementSelectedPropertyLabel ?: addStatementSelectedPropertyId!!,
                                                        currentValue = addStatementValue
                                                    )
                                                    viewModel.saveEdit(
                                                        mode = MainViewModel.EditMode.ADD_STATEMENT,
                                                        newLabel = "",
                                                        newDescription = "",
                                                        newValue = addStatementValue
                                                    )
                                                    showAddStatement = false
                                                    addStatementSelectedPropertyId = null
                                                    addStatementSelectedPropertyLabel = null
                                                    addStatementValue = ""
                                                    viewModel.clearQualifierPropertySuggestions()
                                                    viewModel.clearValueSuggestions()
                                                }
                                            },
                                            modifier = Modifier.size(40.dp),
                                            enabled = addStatementSelectedPropertyId != null && addStatementValue.isNotBlank()
                                        ) {
                                            Icon(Icons.Rounded.Check, contentDescription = "Save", modifier = Modifier.size(24.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    claims.entries.sortedBy { it.key }.forEach { (propId, list) ->
                        val propLabel = propertyLabels[propId] ?: propId
                        StatementBlock(
                            viewModel = viewModel,
                            entityId = entity.id,
                            entityLabel = entityLabelEn,
                            propertyLabel = propLabel,
                            propertyId = propId,
                            claims = list,
                            propertyLabels = propertyLabels,
                            itemLabels = itemLabels,
                            isLoggedIn = isLoggedIn,
                            onAddValue = {
                                viewModel.setEditContext(
                                    mode = MainViewModel.EditMode.ADD_STATEMENT,
                                    title = "Add value",
                                    entityLabel = entityLabelEn,
                                    propertyId = propId,
                                    propertyLabel = propLabel
                                )
                                viewModel.navigateTo(AppView.EDIT)
                            },
                            onAddReference = {
                                if (isLoggedIn) {
                                    viewModel.setEditContext(
                                        mode = MainViewModel.EditMode.EDIT_DESCRIPTION,
                                        title = "Edit description",
                                        entityLabel = entityLabelEn,
                                        currentValue = entityDescriptionEn
                                    )
                                    viewModel.navigateTo(AppView.EDIT)
                                } else {
                                    viewModel.setPendingEditEntityId(entity.id)
                                    viewModel.navigateTo(AppView.LOGIN)
                                }
                            }
                        )
                    }
                }

                // —— Sitelinks ——
                entity.sitelinks?.let { sitelinks ->
                    val entries = sitelinks.entries.sortedBy { it.key }
                    val visible = if (showAllSitelinks) entries else entries.take(12)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sitelinks (${sitelinks.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (entries.size > 12) {
                                    TextButton(onClick = { showAllSitelinks = !showAllSitelinks }) {
                                        Text(if (showAllSitelinks) "Show less" else "Show all")
                                    }
                                }
                            }
                            visible.forEach { (site, link) ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(site, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(92.dp))
                                    Text(link.title, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("No entity loaded")
                    if (selectedEntityId != null) {
                        TextButton(onClick = { viewModel.loadEntity(selectedEntityId!!) }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

/** Format a snak/claim value for display. Uses itemLabels so "Label (Q123)" shows when available. */
private fun formatSnakValue(value: Any?, itemLabels: Map<String, String>, propertyLabels: Map<String, String>): String {
    return when (value) {
        null -> ""
        is Map<*, *> -> {
            val id = (value["id"] ?: value["entity-id"])?.toString()
            val text = (value["text"] ?: value["amount"] ?: value["time"])?.toString()
            when {
                id != null -> {
                    val label = itemLabels[id] ?: propertyLabels[id] ?: text
                    if (label != null && label != id) "$label ($id)" else id
                }
                text != null -> text
                else -> value["id"]?.toString() ?: value.toString()
            }
        }
        else -> value.toString()
    }
}

private fun formatClaimValue(claim: Claim, itemLabels: Map<String, String>, propertyLabels: Map<String, String>): String {
    val v = claim.mainsnak.datavalue?.value
    if (v == null) return claim.mainsnak.snaktype
    return formatSnakValue(v, itemLabels, propertyLabels).ifBlank { v.toString() }
}

@Composable
private fun QualifierPropertySuggestionItem(
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

@Composable
private fun StatementBlock(
    viewModel: MainViewModel,
    entityId: String,
    entityLabel: String,
    propertyLabel: String,
    propertyId: String,
    claims: List<Claim>,
    propertyLabels: Map<String, String>,
    itemLabels: Map<String, String>,
    isLoggedIn: Boolean,
    onAddValue: () -> Unit,
    onAddReference: () -> Unit
) {
    var editingKey by remember { mutableStateOf<String?>(null) }
    var editingValue by remember { mutableStateOf("") }
    var addReferenceKey by remember { mutableStateOf<String?>(null) }
    var refSelectedPropertyId by remember { mutableStateOf<String?>(null) }
    var refSelectedPropertyLabel by remember { mutableStateOf<String?>(null) }
    var refValue by remember { mutableStateOf("") }
    var refExpandedKey by remember { mutableStateOf<String?>(null) }
    var addQualifierKey by remember { mutableStateOf<String?>(null) }
    var qualifierPropId by remember { mutableStateOf("") }
    var qualifierValue by remember { mutableStateOf("") }
    var qualifierSelectedPropertyId by remember { mutableStateOf<String?>(null) }
    var qualifierSelectedPropertyLabel by remember { mutableStateOf<String?>(null) }

    val qualifierPropertySearchQuery by viewModel.qualifierPropertySearchQuery.collectAsState()
    val qualifierPropertySuggestions by viewModel.qualifierPropertySuggestions.collectAsState()
    val isQualifierPropertySuggestionsLoading by viewModel.isQualifierPropertySuggestionsLoading.collectAsState()
    val valueSuggestions by viewModel.valueSuggestions.collectAsState()
    val isValueSuggestionsLoading by viewModel.isValueSuggestionsLoading.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = propertyId,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = propertyLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            claims.forEachIndexed { index, claim ->
                val valueStr = formatClaimValue(claim, itemLabels, propertyLabels)
                val claimKey = "$propertyId|$index|$valueStr"
                val isEditing = editingKey == claimKey
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = valueStr,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                        if (isLoggedIn && !isEditing) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    IconButton(
                                        onClick = {
                                            editingKey = claimKey
                                            editingValue = valueStr
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit value", modifier = Modifier.size(18.dp))
                                    }
                                }
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                IconButton(
                                    onClick = {
                                        addReferenceKey = if (addReferenceKey == claimKey) null else claimKey
                                        if (addReferenceKey == claimKey) {
                                            refSelectedPropertyId = null
                                            refSelectedPropertyLabel = null
                                            refValue = ""
                                            viewModel.clearQualifierPropertySuggestions()
                                        }
                                    },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Rounded.Link, contentDescription = "Add reference", modifier = Modifier.size(18.dp))
                                    }
                                }
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    IconButton(
                                        onClick = { onAddValue() },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = "Create new item", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                    claim.qualifiers?.entries?.forEach { (qPropId, qSnakList) ->
                        qSnakList.forEach { qSnak ->
                            val qLabel = propertyLabels[qPropId] ?: qPropId
                            val qVal = formatSnakValue(qSnak.datavalue?.value, itemLabels, propertyLabels)
                            if (qVal.isNotBlank()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 12.dp, top = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$qPropId $qLabel: $qVal",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isLoggedIn) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Surface(
                                                shape = MaterialTheme.shapes.small,
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                                color = MaterialTheme.colorScheme.surface
                                            ) {
                                                IconButton(
                                                    onClick = { /* TODO: edit qualifier value */ },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Rounded.Edit, contentDescription = "Edit qualifier", modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Surface(
                                                shape = MaterialTheme.shapes.small,
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                                color = MaterialTheme.colorScheme.surface
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        addQualifierKey = claimKey
                                                        qualifierPropId = ""
                                                        qualifierValue = ""
                                                        qualifierSelectedPropertyId = null
                                                        qualifierSelectedPropertyLabel = null
                                                        viewModel.clearQualifierPropertySuggestions()
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Rounded.Add, contentDescription = "Add qualifier", modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = addQualifierKey == claimKey,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Add qualifier",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(
                                        onClick = {
                                            addQualifierKey = null
                                            qualifierPropId = ""
                                            qualifierValue = ""
                                            qualifierSelectedPropertyId = null
                                            qualifierSelectedPropertyLabel = null
                                            viewModel.clearQualifierPropertySuggestions()
                                            viewModel.clearValueSuggestions()
                                        }
                                    ) { Text("remove") }
                                }
                                if (qualifierSelectedPropertyId == null) {
                                    OutlinedTextField(
                                        value = qualifierPropertySearchQuery,
                                        onValueChange = { viewModel.setQualifierPropertySearchQuery(it) },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("property") },
                                        singleLine = true
                                    )
                                    if (isQualifierPropertySuggestionsLoading) {
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
                                        qualifierPropertySuggestions.forEach { suggestion ->
                                            QualifierPropertySuggestionItem(
                                                suggestion = suggestion,
                                                onClick = {
                                                    val id = suggestion.id ?: return@QualifierPropertySuggestionItem
                                                    qualifierPropId = id
                                                    qualifierSelectedPropertyId = id
                                                    qualifierSelectedPropertyLabel = suggestion.label
                                                        ?: suggestion.title ?: id
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${qualifierSelectedPropertyId}: ${qualifierSelectedPropertyLabel ?: ""}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = {
                                            qualifierSelectedPropertyId = null
                                            qualifierSelectedPropertyLabel = null
                                            qualifierPropId = ""
                                        }) { Text("remove") }
                                    }
                                    OutlinedTextField(
                                        value = qualifierValue,
                                        onValueChange = {
                                            qualifierValue = it
                                            viewModel.setValueSearchQuery(it)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Value") },
                                        placeholder = { Text("e.g. 1921") },
                                        singleLine = true
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
                                            QualifierPropertySuggestionItem(
                                                suggestion = suggestion,
                                                onClick = {
                                                    val id = suggestion.id ?: return@QualifierPropertySuggestionItem
                                                    qualifierValue = id
                                                    viewModel.clearValueSuggestions()
                                                }
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                addQualifierKey = null
                                                qualifierPropId = ""
                                                qualifierValue = ""
                                                qualifierSelectedPropertyId = null
                                                qualifierSelectedPropertyLabel = null
                                                viewModel.clearQualifierPropertySuggestions()
                                                viewModel.clearValueSuggestions()
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(Icons.Rounded.Close, contentDescription = "Cancel", modifier = Modifier.size(24.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                if (qualifierPropId.isNotBlank() && qualifierValue.isNotBlank()) {
                                                    viewModel.setEditContext(
                                                        mode = MainViewModel.EditMode.ADD_STATEMENT,
                                                        title = "Add qualifier",
                                                        entityLabel = entityLabel,
                                                        propertyId = propertyId,
                                                        propertyLabel = propertyLabel,
                                                        currentValue = qualifierValue,
                                                        claimId = claim.id,
                                                        qualifierPropertyId = qualifierPropId
                                                    )
                                                    viewModel.saveEdit(
                                                        mode = MainViewModel.EditMode.ADD_STATEMENT,
                                                        newLabel = "",
                                                        newDescription = "",
                                                        newValue = qualifierValue
                                                    )
                                                    addQualifierKey = null
                                                    qualifierPropId = ""
                                                    qualifierValue = ""
                                                    qualifierSelectedPropertyId = null
                                                    qualifierSelectedPropertyLabel = null
                                                    viewModel.clearQualifierPropertySuggestions()
                                                    viewModel.clearValueSuggestions()
                                                }
                                            },
                                            modifier = Modifier.size(40.dp),
                                            enabled = qualifierPropId.isNotBlank() && qualifierValue.isNotBlank()
                                        ) {
                                            Icon(Icons.Rounded.Check, contentDescription = "Save", modifier = Modifier.size(24.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = isEditing,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Edit value:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = editingValue,
                                    onValueChange = {
                                        editingValue = it
                                        viewModel.setValueSearchQuery(it)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp, max = 120.dp),
                                    placeholder = { Text("Enter new value...") },
                                    maxLines = 4,
                                    singleLine = false
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
                                        QualifierPropertySuggestionItem(
                                            suggestion = suggestion,
                                            onClick = {
                                                val id = suggestion.id ?: return@QualifierPropertySuggestionItem
                                                editingValue = id
                                                viewModel.clearValueSuggestions()
                                            }
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            editingKey = null
                                            viewModel.clearValueSuggestions()
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Cancel", modifier = Modifier.size(24.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.setEditContext(
                                                mode = MainViewModel.EditMode.ADD_STATEMENT,
                                                title = "Edit statement",
                                                entityLabel = entityLabel,
                                                propertyId = propertyId,
                                                propertyLabel = propertyLabel,
                                                currentValue = editingValue,
                                                claimId = claim.id
                                            )
                                            viewModel.saveEdit(
                                                mode = MainViewModel.EditMode.ADD_STATEMENT,
                                                newLabel = "",
                                                newDescription = "",
                                                newValue = editingValue
                                            )
                                            editingKey = null
                                        },
                                        modifier = Modifier.size(40.dp),
                                        enabled = editingValue.isNotBlank()
                                    ) {
                                        Icon(Icons.Rounded.Check, contentDescription = "Save", modifier = Modifier.size(24.dp))
                                    }
                                }
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            editingKey = null
                                            addQualifierKey = claimKey
                                            qualifierPropId = ""
                                            qualifierValue = ""
                                            qualifierSelectedPropertyId = null
                                            qualifierSelectedPropertyLabel = null
                                            viewModel.clearQualifierPropertySuggestions()
                                        },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Text(
                                        "Add qualifier",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = addReferenceKey == claimKey,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Add reference",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(
                                        onClick = {
                                            addReferenceKey = null
                                            refSelectedPropertyId = null
                                            refSelectedPropertyLabel = null
                                            refValue = ""
                                            viewModel.clearQualifierPropertySuggestions()
                                            viewModel.clearValueSuggestions()
                                        }
                                    ) { Text("remove") }
                                }
                                if (refSelectedPropertyId == null) {
                                    OutlinedTextField(
                                        value = qualifierPropertySearchQuery,
                                        onValueChange = { viewModel.setQualifierPropertySearchQuery(it) },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("property") },
                                        singleLine = true
                                    )
                                    if (isQualifierPropertySuggestionsLoading) {
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
                                        qualifierPropertySuggestions.forEach { suggestion ->
                                            QualifierPropertySuggestionItem(
                                                suggestion = suggestion,
                                                onClick = {
                                                    val id = suggestion.id ?: return@QualifierPropertySuggestionItem
                                                    refSelectedPropertyId = id
                                                    refSelectedPropertyLabel = suggestion.label
                                                        ?: suggestion.title ?: id
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "${refSelectedPropertyId}: ${refSelectedPropertyLabel ?: ""}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = {
                                            refSelectedPropertyId = null
                                            refSelectedPropertyLabel = null
                                        }) { Text("remove") }
                                    }
                                    OutlinedTextField(
                                        value = refValue,
                                        onValueChange = {
                                            refValue = it
                                            viewModel.setValueSearchQuery(it)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Value") },
                                        placeholder = { Text("e.g. URL or value...") },
                                        singleLine = true
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
                                            QualifierPropertySuggestionItem(
                                                suggestion = suggestion,
                                                onClick = {
                                                    val id = suggestion.id ?: return@QualifierPropertySuggestionItem
                                                    refValue = id
                                                    viewModel.clearValueSuggestions()
                                                }
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                addReferenceKey = null
                                                refSelectedPropertyId = null
                                                refSelectedPropertyLabel = null
                                                refValue = ""
                                                viewModel.clearQualifierPropertySuggestions()
                                                viewModel.clearValueSuggestions()
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(Icons.Rounded.Close, contentDescription = "Cancel", modifier = Modifier.size(24.dp))
                                        }
                                        IconButton(
                                            onClick = {
                                                if (refSelectedPropertyId != null && refValue.isNotBlank()) {
                                                    viewModel.setEditContext(
                                                        mode = MainViewModel.EditMode.ADD_STATEMENT,
                                                        title = "Add reference",
                                                        entityLabel = entityLabel,
                                                        propertyId = propertyId,
                                                        propertyLabel = propertyLabel,
                                                        currentValue = refValue,
                                                        claimId = claim.id,
                                                        referencePropertyId = refSelectedPropertyId
                                                    )
                                                    viewModel.saveEdit(
                                                        mode = MainViewModel.EditMode.ADD_STATEMENT,
                                                        newLabel = "",
                                                        newDescription = "",
                                                        newValue = refValue
                                                    )
                                                    addReferenceKey = null
                                                    refSelectedPropertyId = null
                                                    refSelectedPropertyLabel = null
                                                    refValue = ""
                                                    viewModel.clearQualifierPropertySuggestions()
                                                    viewModel.clearValueSuggestions()
                                                }
                                            },
                                            modifier = Modifier.size(40.dp),
                                            enabled = refSelectedPropertyId != null && refValue.isNotBlank()
                                        ) {
                                            Icon(Icons.Rounded.Check, contentDescription = "Save", modifier = Modifier.size(24.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    val refCount = claim.references?.size ?: 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = { refExpandedKey = if (refExpandedKey == claimKey) null else claimKey }
                        ) {
                            Icon(Icons.Rounded.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$refCount reference${if (refCount != 1) "s" else ""}")
                        }
                    }
                    AnimatedVisibility(
                        visible = refExpandedKey == claimKey && !claim.references.isNullOrEmpty(),
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            claim.references!!.forEachIndexed { refIndex, ref ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        ref.snaks?.entries?.forEach { (snakPropId, snakList) ->
                                            snakList.forEach { snak ->
                                                val snakLabel = propertyLabels[snakPropId] ?: snakPropId
                                                val snakVal = formatSnakValue(snak.datavalue?.value, itemLabels, propertyLabels)
                                                if (snakVal.isNotBlank()) {
                                                    Text(
                                                        text = "$snakPropId $snakLabel: $snakVal",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
