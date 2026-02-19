package com.example.myapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.*
import com.example.myapp.repository.WikidataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class MainViewModel : ViewModel() {

    private val repository = WikidataRepository()

    enum class EditMode {
        CREATE_ITEM,
        EDIT_LABEL,
        EDIT_DESCRIPTION,
        ADD_STATEMENT
    }

    private val _currentEntity = MutableStateFlow<Entity?>(null)
    val currentEntity: StateFlow<Entity?> = _currentEntity.asStateFlow()

    private val _isEntityLoading = MutableStateFlow(false)
    val isEntityLoading: StateFlow<Boolean> = _isEntityLoading.asStateFlow()

    private val _entityError = MutableStateFlow<String?>(null)
    val entityError: StateFlow<String?> = _entityError.asStateFlow()

    private val _propertyLabels = MutableStateFlow<Map<String, String>>(emptyMap())
    val propertyLabels: StateFlow<Map<String, String>> = _propertyLabels.asStateFlow()

    private val _itemLabels = MutableStateFlow<Map<String, String>>(emptyMap())
    val itemLabels: StateFlow<Map<String, String>> = _itemLabels.asStateFlow()

    // App State
    private val _currentView = MutableStateFlow(AppView.HOME)
    val currentView: StateFlow<AppView> = _currentView.asStateFlow()

    private val _selectedEntityId = MutableStateFlow<String?>(null)
    val selectedEntityId: StateFlow<String?> = _selectedEntityId.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _isSidebarOpen = MutableStateFlow(false)
    val isSidebarOpen: StateFlow<Boolean> = _isSidebarOpen.asStateFlow()

    // Login State
    private val _isLoginLoading = MutableStateFlow(false)
    val isLoginLoading: StateFlow<Boolean> = _isLoginLoading.asStateFlow()

    // Search State
    private val _searchTerm = MutableStateFlow("")
    val searchTerm: StateFlow<String> = _searchTerm.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _searchSuggestions = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchSuggestions: StateFlow<List<SearchResult>> = _searchSuggestions.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _isSuggestionsLoading = MutableStateFlow(false)
    val isSuggestionsLoading: StateFlow<Boolean> = _isSuggestionsLoading.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    private val _searchStats = MutableStateFlow<SearchStats?>(null)
    val searchStats: StateFlow<SearchStats?> = _searchStats.asStateFlow()

    private val _searchOffset = MutableStateFlow(0)
    val searchOffset: StateFlow<Int> = _searchOffset.asStateFlow()

    // Qualifier property search (add qualifier flow)
    private val _qualifierPropertySearchQuery = MutableStateFlow("")
    val qualifierPropertySearchQuery: StateFlow<String> = _qualifierPropertySearchQuery.asStateFlow()
    private val _qualifierPropertySuggestions = MutableStateFlow<List<SearchResult>>(emptyList())
    val qualifierPropertySuggestions: StateFlow<List<SearchResult>> = _qualifierPropertySuggestions.asStateFlow()
    private val _isQualifierPropertySuggestionsLoading = MutableStateFlow(false)
    val isQualifierPropertySuggestionsLoading: StateFlow<Boolean> = _isQualifierPropertySuggestionsLoading.asStateFlow()
    private var qualifierPropertySearchJob: Job? = null

    // Value search (for statement/qualifier/reference value fields - entity suggestions)
    private val _valueSearchQuery = MutableStateFlow("")
    val valueSearchQuery: StateFlow<String> = _valueSearchQuery.asStateFlow()
    private val _valueSuggestions = MutableStateFlow<List<SearchResult>>(emptyList())
    val valueSuggestions: StateFlow<List<SearchResult>> = _valueSuggestions.asStateFlow()
    private val _isValueSuggestionsLoading = MutableStateFlow(false)
    val isValueSuggestionsLoading: StateFlow<Boolean> = _isValueSuggestionsLoading.asStateFlow()
    private var valueSearchJob: Job? = null

    private var suggestionJob: Job? = null
    
    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    // OAuth State
    private val _oauthUrl = MutableStateFlow("")
    val oauthUrl: StateFlow<String> = _oauthUrl.asStateFlow()

    // Home Page Data
    private val _homePageData = MutableStateFlow<HomePageData?>(null)
    val homePageData: StateFlow<HomePageData?> = _homePageData.asStateFlow()

    private val _isHomeLoading = MutableStateFlow(false)
    val isHomeLoading: StateFlow<Boolean> = _isHomeLoading.asStateFlow()

    private val _wikidataStats = MutableStateFlow<WikidataStats?>(null)
    val wikidataStats: StateFlow<WikidataStats?> = _wikidataStats.asStateFlow()

    private var searchJob: Job? = null

    // Edit / Login-while-editing: when user taps Edit but is not logged in
    private val _pendingEditEntityId = MutableStateFlow<String?>(null)
    val pendingEditEntityId: StateFlow<String?> = _pendingEditEntityId.asStateFlow()
    private val _editEntityLabel = MutableStateFlow("")
    val editEntityLabel: StateFlow<String> = _editEntityLabel.asStateFlow()
    private val _editMode = MutableStateFlow(EditMode.ADD_STATEMENT)
    val editMode: StateFlow<EditMode> = _editMode.asStateFlow()
    private val _editTitle = MutableStateFlow<String?>(null)
    val editTitle: StateFlow<String?> = _editTitle.asStateFlow()
    private val _editPropertyId = MutableStateFlow<String?>(null)
    val editPropertyId: StateFlow<String?> = _editPropertyId.asStateFlow()
    private val _editPropertyLabel = MutableStateFlow<String?>(null)
    val editPropertyLabel: StateFlow<String?> = _editPropertyLabel.asStateFlow()
    private val _editCurrentValue = MutableStateFlow<String?>(null)
    val editCurrentValue: StateFlow<String?> = _editCurrentValue.asStateFlow()
    private val _editClaimId = MutableStateFlow<String?>(null)
    val editClaimId: StateFlow<String?> = _editClaimId.asStateFlow()
    private val _editQualifierPropertyId = MutableStateFlow<String?>(null)
    val editQualifierPropertyId: StateFlow<String?> = _editQualifierPropertyId.asStateFlow()
    private val _editReferencePropertyId = MutableStateFlow<String?>(null)
    val editReferencePropertyId: StateFlow<String?> = _editReferencePropertyId.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    private val _editError = MutableStateFlow<String?>(null)
    val editError: StateFlow<String?> = _editError.asStateFlow()

    init {
        loadHomePage()
        loadWikidataStats()
    }

    // Navigation methods
    fun navigateTo(view: AppView) {
        _currentView.value = view
    }

    fun setSelectedEntityId(entityId: String) {
        _selectedEntityId.value = entityId
    }

    fun setSearchTerm(term: String) {
        _searchTerm.value = term
        
        // Cancel previous suggestion request
        suggestionJob?.cancel()
        
        // Clear suggestions if query is empty
        if (term.isBlank()) {
            _searchSuggestions.value = emptyList()
            _isSuggestionsLoading.value = false
            return
        }
        
        // Start fetching suggestions after 1 character
        if (term.length >= 1) {
            _isSuggestionsLoading.value = true
            
            suggestionJob = viewModelScope.launch {
                delay(150)
                repository.searchEntities(search = term, language = "en", offset = 0, limit = 10).fold(
                    onSuccess = { response ->
                        _searchSuggestions.value = response.search ?: emptyList()
                        _isSuggestionsLoading.value = false
                    },
                    onFailure = {
                        Log.e("MainViewModel", "Error fetching suggestions", it)
                        _searchSuggestions.value = emptyList()
                        _isSuggestionsLoading.value = false
                    }
                )
            }
        }
    }
    
    fun clearSuggestions() {
        _searchSuggestions.value = emptyList()
    }

    fun setQualifierPropertySearchQuery(term: String) {
        _qualifierPropertySearchQuery.value = term
        qualifierPropertySearchJob?.cancel()
        if (term.isBlank()) {
            _qualifierPropertySuggestions.value = emptyList()
            _isQualifierPropertySuggestionsLoading.value = false
            return
        }
        _isQualifierPropertySuggestionsLoading.value = true
        qualifierPropertySearchJob = viewModelScope.launch {
            delay(200)
            repository.searchProperties(search = term, language = "en", offset = 0, limit = 15).fold(
                onSuccess = { response ->
                    _qualifierPropertySuggestions.value = response.search ?: emptyList()
                    _isQualifierPropertySuggestionsLoading.value = false
                },
                onFailure = {
                    Log.e("MainViewModel", "Qualifier property search failed", it)
                    _qualifierPropertySuggestions.value = emptyList()
                    _isQualifierPropertySuggestionsLoading.value = false
                }
            )
        }
    }

    fun clearQualifierPropertySuggestions() {
        qualifierPropertySearchJob?.cancel()
        _qualifierPropertySearchQuery.value = ""
        _qualifierPropertySuggestions.value = emptyList()
        _isQualifierPropertySuggestionsLoading.value = false
    }

    fun setValueSearchQuery(term: String) {
        _valueSearchQuery.value = term
        valueSearchJob?.cancel()
        if (term.isBlank()) {
            _valueSuggestions.value = emptyList()
            _isValueSuggestionsLoading.value = false
            return
        }
        _isValueSuggestionsLoading.value = true
        valueSearchJob = viewModelScope.launch {
            delay(200)
            repository.searchEntities(search = term, language = "en", offset = 0, limit = 15).fold(
                onSuccess = { response ->
                    _valueSuggestions.value = response.search ?: emptyList()
                    _isValueSuggestionsLoading.value = false
                },
                onFailure = {
                    Log.e("MainViewModel", "Value search failed", it)
                    _valueSuggestions.value = emptyList()
                    _isValueSuggestionsLoading.value = false
                }
            )
        }
    }

    fun clearValueSuggestions() {
        valueSearchJob?.cancel()
        _valueSearchQuery.value = ""
        _valueSuggestions.value = emptyList()
        _isValueSuggestionsLoading.value = false
    }
    
    fun clearSearchError() {
        _searchError.value = null
    }

    /** Run full search with current query (e.g. when user presses Enter / Search). */
    fun performSearch() {
        val term = _searchTerm.value.trim()
        if (term.isEmpty()) {
            _searchResults.value = emptyList()
            _searchStats.value = null
            return
        }
        searchEntities(term, 0)
    }

    fun setSidebarOpen(open: Boolean) {
        _isSidebarOpen.value = open
    }

    fun setLoggedIn(loggedIn: Boolean) {
        _isLoggedIn.value = loggedIn
    }

    fun setUsername(name: String) {
        _username.value = name
    }

    /** Call when user taps Edit on entity but is not logged in → go to Login; after login we return to entity. */
    fun setPendingEditEntityId(entityId: String?) {
        _pendingEditEntityId.value = entityId
    }

    /** Set context for Edit screen (mode + optional entity/property/value/claim). */
    fun setEditContext(
        mode: EditMode,
        title: String? = null,
        entityLabel: String = "",
        propertyId: String? = null,
        propertyLabel: String? = null,
        currentValue: String? = null,
        claimId: String? = null,
        qualifierPropertyId: String? = null,
        referencePropertyId: String? = null
    ) {
        _editMode.value = mode
        _editTitle.value = title
        _editEntityLabel.value = entityLabel
        _editPropertyId.value = propertyId
        _editPropertyLabel.value = propertyLabel
        _editCurrentValue.value = currentValue
        _editClaimId.value = claimId
        _editQualifierPropertyId.value = qualifierPropertyId
        _editReferencePropertyId.value = referencePropertyId
    }

    fun clearEditContext() {
        _editMode.value = EditMode.ADD_STATEMENT
        _editTitle.value = null
        _editEntityLabel.value = ""
        _editPropertyId.value = null
        _editPropertyLabel.value = null
        _editCurrentValue.value = null
        _editClaimId.value = null
        _editQualifierPropertyId.value = null
        _editReferencePropertyId.value = null
        _editError.value = null
        clearValueSuggestions()
    }

    fun clearEditError() {
        _editError.value = null
    }

    /** Persist edit to Wikidata (label/description). Call from EditScreen Save. */
    fun saveEdit(
        mode: EditMode,
        newLabel: String,
        newDescription: String,
        newValue: String
    ) {
        val entityId = _selectedEntityId.value
        viewModelScope.launch {
            _isSaving.value = true
            _editError.value = null
            when (mode) {
                EditMode.EDIT_LABEL -> {
                    if (!entityId.isNullOrEmpty()) {
                        repository.setLabel(entityId, "en", newLabel.trim()).fold(
                            onSuccess = {
                                loadEntity(entityId)
                                clearEditContext()
                                navigateTo(AppView.ENTITY)
                            },
                            onFailure = { e ->
                                _editError.value = e.message ?: "Failed to save label"
                            }
                        )
                    } else {
                        _editError.value = "No entity selected"
                    }
                }
                EditMode.EDIT_DESCRIPTION -> {
                    if (!entityId.isNullOrEmpty()) {
                        repository.setDescription(entityId, "en", newDescription.trim()).fold(
                            onSuccess = {
                                loadEntity(entityId)
                                clearEditContext()
                                navigateTo(AppView.ENTITY)
                            },
                            onFailure = { e ->
                                _editError.value = e.message ?: "Failed to save description"
                            }
                        )
                    } else {
                        _editError.value = "No entity selected"
                    }
                }
                EditMode.ADD_STATEMENT, EditMode.CREATE_ITEM -> {
                    if (entityId.isNullOrEmpty()) {
                        _editError.value = "No entity selected"
                    } else {
                        val claimId = _editClaimId.value
                        val qualifierPropId = _editQualifierPropertyId.value
                        val refPropId = _editReferencePropertyId.value
                        val propId = _editPropertyId.value
                        when {
                            claimId != null && qualifierPropId != null -> {
                                repository.setQualifier(claimId, qualifierPropId, newValue.trim()).fold(
                                    onSuccess = {
                                        loadEntity(entityId)
                                        clearEditContext()
                                        navigateTo(AppView.ENTITY)
                                    },
                                    onFailure = { e ->
                                        _editError.value = e.message ?: "Failed to add qualifier"
                                    }
                                )
                            }
                            claimId != null && refPropId != null -> {
                                repository.setReference(claimId, refPropId, newValue.trim()).fold(
                                    onSuccess = {
                                        loadEntity(entityId)
                                        clearEditContext()
                                        navigateTo(AppView.ENTITY)
                                    },
                                    onFailure = { e ->
                                        _editError.value = e.message ?: "Failed to add reference"
                                    }
                                )
                            }
                            claimId != null && propId != null -> {
                                repository.setClaim(claimId, propId, newValue.trim()).fold(
                                    onSuccess = {
                                        loadEntity(entityId)
                                        clearEditContext()
                                        navigateTo(AppView.ENTITY)
                                    },
                                    onFailure = { e ->
                                        _editError.value = e.message ?: "Failed to edit statement"
                                    }
                                )
                            }
                            propId != null -> {
                                repository.createClaim(entityId, propId, newValue.trim()).fold(
                                    onSuccess = {
                                        loadEntity(entityId)
                                        clearEditContext()
                                        navigateTo(AppView.ENTITY)
                                    },
                                    onFailure = { e ->
                                        _editError.value = e.message ?: "Failed to add statement"
                                    }
                                )
                            }
                            else -> {
                                _editError.value = "Missing property or claim"
                            }
                        }
                    }
                }
            }
            _isSaving.value = false
        }
    }

    /** Call when login has just succeeded (e.g. from WebView). Navigates to ENTITY if user came from Edit, else PROFILE. */
    fun onLoginSuccessNavigate() {
        val pendingId = _pendingEditEntityId.value
        if (pendingId != null) {
            _pendingEditEntityId.value = null
            loadEntity(pendingId)
            navigateTo(AppView.ENTITY)
        } else {
            navigateTo(AppView.PROFILE)
        }
    }

    /**
     * Check if user is logged in via Wikidata cookies (from WebView).
     * Calls the Wikidata API with WebView cookies; if logged in, sets username and navigates.
     * Call this after the WebView loads a page (e.g. after login redirect).
     */
    fun checkLoginFromCookies() {
        viewModelScope.launch {
            try {
                delay(400) // Brief delay so cookies from redirect are synced to CookieManager
                val userInfo = repository.getUserInfo()
                val ui = userInfo.query.userinfo
                val id = ui.id ?: 0
                val name = ui.name?.trim()
                if (id != 0 && !name.isNullOrBlank()) {
                    _username.value = name
                    _isLoggedIn.value = true
                    Log.d("MainViewModel", "Logged in via Wikidata: $name")
                    onLoginSuccessNavigate()
                }
            } catch (e: Exception) {
                Log.w("MainViewModel", "Login check failed (user may not be logged in)", e)
            }
        }
    }

    // Login: navigates to login screen (Wikimedia login page in WebView)
    fun initiateOAuthLogin() {
        viewModelScope.launch {
            _isLoginLoading.value = true
            try {
                navigateTo(AppView.LOGIN)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Login navigation failed", e)
            } finally {
                _isLoginLoading.value = false
            }
        }
    }

    fun completeOAuthLogin(oauthVerifier: String) {
        viewModelScope.launch {
            _isLoginLoading.value = true
            try {
                // In a real implementation, you would:
                // 1. Exchange oauth_verifier for access token
                // 2. Use access token to get user info
                // For now, we'll simulate getting user info
                delay(1000)
                
                // Try to get user info from Wikidata API
                try {
                    val userInfo = repository.getUserInfo()
                    userInfo.query.userinfo.name?.let {
                        _username.value = it
                        _isLoggedIn.value = true
                        Log.d("MainViewModel", "Logged in as: $it")
                    }
                } catch (e: Exception) {
                    // If API call fails, simulate success for demo
                    Log.w("MainViewModel", "Could not fetch user info, using verifier", e)
                    _username.value = "User" // Fallback
                    _isLoggedIn.value = true
                }
                val pendingId = _pendingEditEntityId.value
                if (pendingId != null) {
                    _pendingEditEntityId.value = null
                    loadEntity(pendingId)
                    navigateTo(AppView.ENTITY)
                    // After entity loads, user can tap Edit again (we're now logged in)
                } else {
                    navigateTo(AppView.PROFILE)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "OAuth completion failed", e)
            } finally {
                _isLoginLoading.value = false
            }
        }
    }

    // Legacy login method (kept for compatibility)
    fun login(username: String, password: String) {
        // Redirect to OAuth flow instead
        initiateOAuthLogin()
    }

    fun authorizeOAuth() {
        // This is called from OAuth consent screen
        // The actual authorization happens in the WebView
        // This method can be used for manual authorization if needed
        viewModelScope.launch {
            _isLoginLoading.value = true
            delay(500)
            // If user manually authorizes, mark as logged in
            if (_username.value.isNotEmpty()) {
                _isLoggedIn.value = true
                navigateTo(AppView.PROFILE)
            }
            _isLoginLoading.value = false
        }
    }

    // Search functionality
    fun searchEntities(term: String, offset: Int = 0) {
        if (term.trim().isEmpty()) {
            _searchResults.value = emptyList()
            _searchStats.value = null
            return
        }

        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            if (offset == 0) {
                _searchOffset.value = 0
                _searchError.value = null
                _searchSuggestions.value = emptyList()
            }
            val pageSize = 100
            repository.searchEntities(search = term, language = "en", offset = offset, limit = pageSize).fold(
                onSuccess = { response ->
                    val newItems = response.search ?: emptyList()
                    if (offset == 0) {
                        _searchResults.value = newItems
                    } else {
                        _searchResults.value = _searchResults.value + newItems
                    }
                    _searchOffset.value = offset + newItems.size
                    _searchStats.value = response.searchinfo
                    _searchError.value = null
                    _isSearching.value = false
                },
                onFailure = { exception ->
                    Log.e("MainViewModel", "Search failed", exception)
                    _searchError.value = exception.message ?: "Search failed. Please check your internet connection."
                    if (offset == 0) _searchResults.value = emptyList()
                    _isSearching.value = false
                }
            )
        }
    }

    /** Load more search results (append to current list). */
    fun loadMoreSearchResults() {
        val term = _searchTerm.value.trim()
        if (term.isEmpty()) return
        searchEntities(term, _searchOffset.value)
    }

    fun loadEntity(entityId: String) {
        viewModelScope.launch {
            _isEntityLoading.value = true
            _selectedEntityId.value = entityId
            _currentEntity.value = null
            _entityError.value = null
            _propertyLabels.value = emptyMap()
            repository.getEntity(entityId).fold(
                onSuccess = { entity ->
                    _currentEntity.value = entity
                    _entityError.value = null

                    // Pre-fetch property labels (P numbers) and item labels (Q numbers) for names
                    val allPIds = mutableSetOf<String>()
                    val allQIds = mutableSetOf<String>()
                    fun addId(id: String?) {
                        if (id == null) return
                        when {
                            id.startsWith("P") -> allPIds.add(id)
                            id.startsWith("Q") -> allQIds.add(id)
                        }
                    }
                    entity.claims?.keys?.let { allPIds.addAll(it) }
                    entity.claims?.values?.forEach { claimList ->
                        claimList.forEach { claim ->
                            val v = claim.mainsnak.datavalue?.value
                            if (v is Map<*, *>) addId((v["id"] ?: v["entity-id"])?.toString())
                            claim.qualifiers?.keys?.let { allPIds.addAll(it) }
                            claim.qualifiers?.values?.forEach { snakList ->
                                snakList.forEach { snak ->
                                    val qv = snak.datavalue?.value
                                    if (qv is Map<*, *>) addId((qv["id"] ?: qv["entity-id"])?.toString())
                                }
                            }
                            claim.references?.forEach { ref ->
                                ref.snaks?.entries?.forEach { (pId, snakList) ->
                                    allPIds.add(pId)
                                    snakList.forEach { snak ->
                                        val sv = snak.datavalue?.value
                                        if (sv is Map<*, *>) addId((sv["id"] ?: sv["entity-id"])?.toString())
                                    }
                                }
                            }
                        }
                    }
                    val allIds = (allPIds + allQIds).distinct().take(100)
                    if (allIds.isNotEmpty()) {
                        viewModelScope.launch {
                            repository.getEntities(allIds, "en", "labels").fold(
                                onSuccess = { entities ->
                                    val propLabels = entities.filterKeys { it.startsWith("P") }.mapValues { (key, e) ->
                                        e.labels?.get("en")?.value ?: key
                                    }
                                    val itemLabelsMap = entities.filterKeys { it.startsWith("Q") }.mapValues { (key, e) ->
                                        e.labels?.get("en")?.value ?: key
                                    }
                                    _propertyLabels.value = propLabels
                                    _itemLabels.value = itemLabelsMap
                                },
                                onFailure = { Log.w("MainViewModel", "Labels fetch failed", it) }
                            )
                        }
                    }
                },
                onFailure = { e ->
                    Log.e("MainViewModel", "loadEntity failed", e)
                    _currentEntity.value = null
                    _entityError.value = e.message ?: "Failed to load entity"
                }
            )
            _isEntityLoading.value = false
        }
    }

    fun clearEntityError() {
        _entityError.value = null
    }

    fun loadRandomEntity() {
        viewModelScope.launch {
            _isEntityLoading.value = true
            try {
                val response = repository.getRandomEntity()
                val entity = response.entities.values.firstOrNull()
                if (entity != null) {
                    _selectedEntityId.value = entity.id
                    _currentEntity.value = entity
                    _currentView.value = AppView.ENTITY
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isEntityLoading.value = false
            }
        }
    }

    fun triggerRefresh() {
        _refreshTrigger.value += 1
    }

    fun handleNextPage() {
        loadMoreSearchResults()
    }

    fun handlePrevPage() {
        val current = _searchResults.value.size
        val prevOffset = maxOf(0, _searchOffset.value - 100)
        if (prevOffset == 0) {
            searchEntities(_searchTerm.value, 0)
        } else {
            searchEntities(_searchTerm.value, prevOffset)
        }
    }

    fun loadWikidataStats() {
        viewModelScope.launch {
            try {
                // For now, use hardcoded stats. In production, fetch from API
                _wikidataStats.value = com.example.myapp.data.WikidataStats(
                    totalItems = "108M+",
                    activeUsers = "5K+",
                    editsToday = "10K+"
                )
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error loading stats", e)
                // Keep default stats on error
            }
        }
    }

    fun loadHomePage() {
        viewModelScope.launch {
            _isHomeLoading.value = true
            try {
                val response = repository.getMainPage()
                val html = response.parse.text.html
                val parsedData = withContext(Dispatchers.IO) {
                    parseHomePageHtml(html)
                }
                _homePageData.value = parsedData
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isHomeLoading.value = false
            }
        }
    }

    private fun parseHomePageHtml(html: String): HomePageData {
        val doc = Jsoup.parse(html)
        
        val welcomeText = doc.select("div.welcome-title").text().ifEmpty { "Welcome to Wikidata" }
        val welcomeDesc = doc.select("div.welcome-subtitle").text().ifEmpty { 
            "the free knowledge base with data items that anyone can edit." 
        }

        val getInvolvedList = mutableListOf<String>()
        val getInvolvedHeader = doc.getElementsContainingOwnText("Get involved").firstOrNull()
        val getInvolvedContainer = getInvolvedHeader?.parent()?.parent()
        getInvolvedContainer?.select("li")?.forEach { 
            getInvolvedList.add(it.text())
        }

        val learnDataList = mutableListOf<String>()
        val learnDataHeader = doc.getElementsContainingOwnText("Learn about data").firstOrNull()
        val learnDataContainer = learnDataHeader?.parent()?.parent()
        learnDataContainer?.select("li")?.forEach {
            learnDataList.add(it.text())
        }

        val highlightsList = mutableListOf<String>()
        val highlightsHeader = doc.getElementsContainingOwnText("Current highlights").firstOrNull()
        val highlightsContainer = highlightsHeader?.parent()?.parent()
        highlightsContainer?.select("li")?.forEach {
            highlightsList.add(it.text())
        }
        
        val discoverText = StringBuilder()
        val discoverHeader = doc.getElementsContainingOwnText("Discover").firstOrNull()
        val discoverContainer = discoverHeader?.parent()?.parent()
        if (discoverContainer != null) {
            discoverText.append(discoverContainer.text().replace("Discover", "").trim())
        }

        val contactLinks = mutableListOf<String>()
        val contactHeader = doc.getElementsContainingOwnText("Contact").firstOrNull()
        val contactContainer = contactHeader?.parent()?.parent()
        contactContainer?.select("li")?.forEach {
            contactLinks.add(it.text())
        }
        
        return HomePageData(
            welcomeText = welcomeText,
            welcomeDescription = welcomeDesc,
            getInvolved = if (getInvolvedList.isNotEmpty()) getInvolvedList else listOf(
                "What is Wikidata? Read the Wikidata introduction.",
                "Explore Wikidata by looking at a featured showcase item.",
                "Get started with Wikidata's SPARQL query service."
            ),
            learnAboutData = if (learnDataList.isNotEmpty()) learnDataList else listOf(
                "Item: Earth (Q2)",
                "Property: highest point (P610)",
                "custom value: Mount Everest (Q513)"
            ),
            currentHighlights = if (highlightsList.isNotEmpty()) highlightsList else listOf(
                "Scott Buckley (Q61759294)",
                "Fátima Bosch (Q136265985)"
            ),
            discoverText = if (discoverText.isNotEmpty()) discoverText.toString() else "Innovative applications and contributions from the Wikidata community",
            contactLinks = if (contactLinks.isNotEmpty()) contactLinks else listOf(
                "Wikidata mailing list",
                "Wikidata technical mailing list"
            )
        )
    }
}

