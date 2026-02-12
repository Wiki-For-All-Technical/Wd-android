package com.example.wikiapp.ui.edit

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wikiapp.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    entityId: String,
    entityLabel: String,
    propertyId: String? = null,
    propertyLabel: String? = null,
    currentValue: String? = null,
    onNavigateBack: () -> Unit,
    onSave: (String) -> Unit
) {
    var editedValue by remember { mutableStateOf(currentValue ?: "") }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Entry animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    Scaffold(
        topBar = {
            EditTopBar(
                entityId = entityId,
                onNavigateBack = onNavigateBack,
                onSave = {
                    if (editedValue.isNotBlank()) {
                        showSaveConfirmation = true
                    }
                },
                canSave = editedValue.isNotBlank() && editedValue != currentValue
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(400, easing = EaseOutCubic)
                )
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Entity Header Card
                    item {
                        EntityHeaderCard(
                            entityId = entityId,
                            entityLabel = entityLabel
                        )
                    }
                    
                    // Property being edited
                    if (propertyId != null) {
                        item {
                            PropertyCard(
                                propertyId = propertyId,
                                propertyLabel = propertyLabel
                            )
                        }
                    }
                    
                    // Current Value Display
                    if (currentValue != null) {
                        item {
                            CurrentValueCard(currentValue = currentValue)
                        }
                    }
                    
                    // Edit Input Section
                    item {
                        EditInputSection(
                            value = editedValue,
                            onValueChange = { editedValue = it },
                            onDone = {
                                focusManager.clearFocus()
                                if (editedValue.isNotBlank() && editedValue != currentValue) {
                                    showSaveConfirmation = true
                                }
                            }
                        )
                    }
                    
                    // Edit Guidelines
                    item {
                        EditGuidelinesCard()
                    }
                    
                    // Quick Actions
                    item {
                        QuickActionsSection()
                    }
                }
            }
            
            // Save Confirmation Dialog
            if (showSaveConfirmation) {
                SaveConfirmationDialog(
                    entityLabel = entityLabel,
                    propertyLabel = propertyLabel ?: "value",
                    newValue = editedValue,
                    onConfirm = {
                        showSaveConfirmation = false
                        isSaving = true
                        // Simulate save
                    },
                    onDismiss = { showSaveConfirmation = false }
                )
            }
            
            // Saving Overlay
            AnimatedVisibility(
                visible = isSaving,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SavingOverlay(
                    onComplete = {
                        isSaving = false
                        showSuccess = true
                    }
                )
            }
            
            // Success Overlay
            AnimatedVisibility(
                visible = showSuccess,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                SuccessOverlay(
                    onDismiss = {
                        showSuccess = false
                        onSave(editedValue)
                        onNavigateBack()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTopBar(
    entityId: String,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp
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
                
                Column {
                    Text(
                        text = "Edit Statement",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Base10
                    )
                    Text(
                        text = entityId,
                        style = MaterialTheme.typography.labelSmall,
                        color = WikidataItemGreen
                    )
                }
            }
            
            // Save button
            Button(
                onClick = onSave,
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WikidataTeal,
                    disabledContainerColor = Base70
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save")
            }
        }
    }
}

@Composable
private fun EntityHeaderCard(
    entityId: String,
    entityLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = WikidataItemGreen.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WikidataItemGreen.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Entity icon
            Surface(
                color = WikidataItemGreen.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        tint = WikidataItemGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = entityLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Base10,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Surface(
                    color = WikidataItemGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = entityId,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = WikidataItemGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PropertyCard(
    propertyId: String,
    propertyLabel: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = WikidataPropertyOrange.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, WikidataPropertyOrange.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Property icon
            Surface(
                color = WikidataPropertyOrange.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Label,
                        contentDescription = null,
                        tint = WikidataPropertyOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Property",
                    style = MaterialTheme.typography.labelSmall,
                    color = Base50
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = propertyLabel ?: propertyId,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Base10
                    )
                    Surface(
                        color = WikidataPropertyOrange.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = propertyId,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = WikidataPropertyOrange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentValueCard(currentValue: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Base90
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.History,
                    contentDescription = null,
                    tint = Base50,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Current Value",
                    style = MaterialTheme.typography.labelMedium,
                    color = Base50
                )
            }
            
            Text(
                text = currentValue,
                style = MaterialTheme.typography.bodyLarge,
                color = Base20
            )
        }
    }
}

@Composable
private fun EditInputSection(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, WikidataTeal.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = WikidataTeal,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "New Value",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = WikidataTeal
                )
            }
            
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter new value...", color = Base50) },
                minLines = 3,
                maxLines = 6,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WikidataTeal,
                    unfocusedBorderColor = Base70,
                    focusedContainerColor = WikidataTeal.copy(alpha = 0.02f),
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            )
            
            // Character count
            Text(
                text = "${value.length} characters",
                style = MaterialTheme.typography.labelSmall,
                color = Base50,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun EditGuidelinesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AccentBlue.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Editing Guidelines",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentBlue
                )
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GuidelineItem(
                    icon = Icons.Default.Check,
                    text = "Use reliable sources for all edits"
                )
                GuidelineItem(
                    icon = Icons.Default.Check,
                    text = "Keep values factual and verifiable"
                )
                GuidelineItem(
                    icon = Icons.Default.Check,
                    text = "Add references when possible"
                )
            }
        }
    }
}

@Composable
private fun GuidelineItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = WikidataItemGreen,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Base30
        )
    }
}

@Composable
private fun QuickActionsSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.labelMedium,
            color = Base50
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickActionChip(
                icon = Icons.Default.Add,
                label = "Add Reference",
                modifier = Modifier.weight(1f)
            )
            QuickActionChip(
                icon = Icons.Default.Language,
                label = "Add Language",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { },
        color = Base90,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = AccentBlue
            )
        }
    }
}

@Composable
private fun SaveConfirmationDialog(
    entityLabel: String,
    propertyLabel: String,
    newValue: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                color = WikidataTeal.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        tint = WikidataTeal,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "Confirm Edit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "You are about to update $propertyLabel for:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Base30
                )
                
                Surface(
                    color = WikidataItemGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = entityLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Base10,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                
                Text(
                    text = "New value:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Base50
                )
                
                Surface(
                    color = Base90,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = newValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Base10,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = WikidataTeal)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Base30)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun SavingOverlay(onComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500) // Simulate save operation
        onComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = WikidataTeal,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Saving changes...",
                style = MaterialTheme.typography.bodyLarge,
                color = Base30
            )
        }
    }
}

@Composable
private fun SuccessOverlay(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        onDismiss()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated check mark
            Surface(
                color = WikidataItemGreen.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = WikidataItemGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            
            Text(
                text = "Changes Saved!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = WikidataItemGreen
            )
            
            Text(
                text = "Your edit has been submitted successfully",
                style = MaterialTheme.typography.bodyMedium,
                color = Base50,
                textAlign = TextAlign.Center
            )
        }
    }
}

