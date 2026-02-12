package com.example.wikiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthState(
    val isLoggedIn: Boolean = false,
    val username: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authState.value = _authState.value.copy(
                error = "Please enter both username and password"
            )
            return
        }
        
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        
        // Simulate login - In production, this would call Wikidata OAuth API
        // For demo purposes, accept any non-empty credentials
        _authState.value = _authState.value.copy(
            isLoggedIn = true,
            username = username,
            isLoading = false,
            error = null
        )
    }
    
    fun logout() {
        _authState.value = AuthState()
    }
    
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}


