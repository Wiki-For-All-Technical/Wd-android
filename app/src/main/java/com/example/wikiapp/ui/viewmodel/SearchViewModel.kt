package com.example.wikiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wikiapp.data.model.SearchResult
import com.example.wikiapp.data.repository.WikidataRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val suggestions: List<SearchResult> = emptyList(),
    val isLoading: Boolean = false,
    val isSuggestionsLoading: Boolean = false,
    val error: String? = null
)

class SearchViewModel(
    private val repository: WikidataRepository = WikidataRepository()
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var suggestJob: Job? = null
    
    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        
        // Cancel previous suggestion request
        suggestJob?.cancel()
        
        // Clear suggestions if query is empty
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                suggestions = emptyList(),
                isSuggestionsLoading = false
            )
            return
        }
        
        // Start fetching suggestions after just 1 character
        if (query.length >= 1) {
            _uiState.value = _uiState.value.copy(isSuggestionsLoading = true)
            
            suggestJob = viewModelScope.launch {
                // Short debounce for fast typing
                delay(150)
                
                repository.searchEntities(query, limit = 10).fold(
                    onSuccess = { response ->
                        _uiState.value = _uiState.value.copy(
                            suggestions = response.search ?: emptyList(),
                            isSuggestionsLoading = false
                        )
                    },
                    onFailure = {
                        _uiState.value = _uiState.value.copy(
                            isSuggestionsLoading = false
                        )
                    }
                )
            }
        }
    }
    
    fun search() {
        val query = _uiState.value.query.trim()
        if (query.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                results = emptyList(),
                error = null
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                suggestions = emptyList() // Clear suggestions when searching
            )
            
            repository.searchEntities(query, limit = 50).fold(
                onSuccess = { response ->
                    _uiState.value = _uiState.value.copy(
                        results = response.search ?: emptyList(),
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Search failed. Please check your internet connection."
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSuggestions() {
        _uiState.value = _uiState.value.copy(suggestions = emptyList())
    }
}
