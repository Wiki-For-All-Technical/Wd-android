package com.example.wikiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wikiapp.data.model.WikidataEntity
import com.example.wikiapp.data.repository.WikidataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EntityUiState(
    val entity: WikidataEntity? = null,
    val propertyLabels: Map<String, String> = emptyMap(),
    val entityLabels: Map<String, String> = emptyMap(), // Labels for Q entities in claims
    val isLoading: Boolean = false,
    val error: String? = null
)

class EntityViewModel(
    private val repository: WikidataRepository = WikidataRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(EntityUiState())
    val uiState: StateFlow<EntityUiState> = _uiState.asStateFlow()
    
    // Cache property labels to avoid repeated API calls
    private val propertyLabelCache = mutableMapOf<String, String>()
    private val entityLabelCache = mutableMapOf<String, String>()
    
    fun loadEntity(entityId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            repository.getEntity(entityId).fold(
                onSuccess = { entity ->
                    _uiState.value = _uiState.value.copy(
                        entity = entity,
                        isLoading = false,
                        error = null
                    )
                    
                    // Fetch property labels for claims
                    loadPropertyLabels(entity)
                    
                    // Fetch entity labels for Q values in claims
                    loadEntityLabels(entity)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load entity"
                    )
                }
            )
        }
    }
    
    private fun loadPropertyLabels(entity: WikidataEntity) {
        viewModelScope.launch {
            // Get all property IDs from claims
            val propertyIds = entity.claims?.keys?.toList() ?: return@launch
            
            // Filter out properties we already have cached
            val uncachedPropertyIds = propertyIds.filter { it !in propertyLabelCache }
            
            if (uncachedPropertyIds.isEmpty()) {
                // All properties are cached, just update UI
                _uiState.value = _uiState.value.copy(
                    propertyLabels = propertyIds.associateWith { 
                        propertyLabelCache[it] ?: it 
                    }
                )
                return@launch
            }
            
            // Fetch labels for uncached properties
            repository.getPropertyLabels(uncachedPropertyIds).fold(
                onSuccess = { newLabels ->
                    // Update cache
                    propertyLabelCache.putAll(newLabels)
                    
                    // Update UI with all property labels
                    _uiState.value = _uiState.value.copy(
                        propertyLabels = propertyIds.associateWith { 
                            propertyLabelCache[it] ?: it 
                        }
                    )
                },
                onFailure = {
                    // If fetching labels fails, just use property IDs
                    _uiState.value = _uiState.value.copy(
                        propertyLabels = propertyIds.associateWith { it }
                    )
                }
            )
        }
    }
    
    private fun loadEntityLabels(entity: WikidataEntity) {
        viewModelScope.launch {
            // Extract all Q entity IDs from claims
            val entityIds = mutableSetOf<String>()
            
            entity.claims?.values?.forEach { claimList ->
                claimList.forEach { claim ->
                    // Get entity ID from mainsnak
                    extractEntityId(claim.mainsnak.datavalue?.value)?.let { entityIds.add(it) }
                    
                    // Get entity IDs from qualifiers
                    claim.qualifiers?.values?.forEach { snakList ->
                        snakList.forEach { snak ->
                            extractEntityId(snak.datavalue?.value)?.let { entityIds.add(it) }
                        }
                    }
                }
            }
            
            if (entityIds.isEmpty()) return@launch
            
            // Filter out entities we already have cached
            val uncachedEntityIds = entityIds.filter { it !in entityLabelCache }
            
            if (uncachedEntityIds.isEmpty()) {
                // All entities are cached, just update UI
                _uiState.value = _uiState.value.copy(
                    entityLabels = entityIds.associateWith { 
                        entityLabelCache[it] ?: it 
                    }
                )
                return@launch
            }
            
            // Fetch labels for uncached entities (batch in groups of 50)
            uncachedEntityIds.chunked(50).forEach { batch ->
                repository.getEntities(batch).fold(
                    onSuccess = { entities ->
                        entities.forEach { (id, entityData) ->
                            val label = entityData.labels?.get("en")?.value 
                                ?: entityData.labels?.values?.firstOrNull()?.value 
                                ?: id
                            entityLabelCache[id] = label
                        }
                        
                        // Update UI with all entity labels
                        _uiState.value = _uiState.value.copy(
                            entityLabels = entityIds.associateWith { 
                                entityLabelCache[it] ?: it 
                            }
                        )
                    },
                    onFailure = {
                        // If fetching labels fails, just use entity IDs
                    }
                )
            }
        }
    }
    
    private fun extractEntityId(value: Any?): String? {
        return when (value) {
            is Map<*, *> -> value["id"] as? String
            else -> null
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
