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
import retrofit2.HttpException
import java.io.IOException
import org.jsoup.Jsoup

class MainViewModel : ViewModel() {

    private val repository = WikidataRepository()

    private val _currentEntity = MutableStateFlow<Entity?>(null)
    val currentEntity: StateFlow<Entity?> = _currentEntity.asStateFlow()

    private val _isEntityLoading = MutableStateFlow(false)
    val isEntityLoading: StateFlow<Boolean> = _isEntityLoading.asStateFlow()

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

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchStats = MutableStateFlow<SearchStats?>(null)
    val searchStats: StateFlow<SearchStats?> = _searchStats.asStateFlow()

    private val _searchOffset = MutableStateFlow(0)
    val searchOffset: StateFlow<Int> = _searchOffset.asStateFlow()
    
    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    // Home Page Data
    private val _homePageData = MutableStateFlow<HomePageData?>(null)
    val homePageData: StateFlow<HomePageData?> = _homePageData.asStateFlow()

    private val _isHomeLoading = MutableStateFlow(false)
    val isHomeLoading: StateFlow<Boolean> = _isHomeLoading.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadHomePage()
        observeSearchTerm()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchTerm() {
        _searchTerm
            .debounce(500L)
            .distinctUntilChanged()
            .onEach { term ->
                if (term.isNotBlank()) {
                    searchEntities(term)
                } else {
                    _searchResults.value = emptyList()
                    _searchStats.value = null
                }
            }
            .launchIn(viewModelScope)
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

    // Login Logic
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoginLoading.value = true
            delay(1500) // Simulate network delay for realistic feel
            if (username.isNotEmpty() && password.isNotEmpty()) {
                _username.value = username
                navigateTo(AppView.OAUTH_CONSENT)
            } else {
                Log.e("MainViewModel", "Login failed: Invalid credentials")
            }
            _isLoginLoading.value = false
        }
    }

    fun authorizeOAuth() {
        viewModelScope.launch {
            _isLoginLoading.value = true
            delay(1500) // Simulate network delay
            _isLoggedIn.value = true
            _isLoginLoading.value = false
            navigateTo(AppView.PROFILE)
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
            _searchOffset.value = offset

            try {
                Log.d("MainViewModel", "Searching for: $term, offset: $offset")
                val data = repository.searchEntities(term, "en", offset)
                Log.d("MainViewModel", "Search success: ${data.success}, hits: ${data.search.size}")
                
                if (data.success == 1) {
                    _searchResults.value = data.search
                    _searchStats.value = data.searchinfo
                } else {
                    _searchResults.value = emptyList()
                }
            } catch (e: IOException) {
                Log.e("MainViewModel", "Network error during search", e)
                e.printStackTrace()
                _searchResults.value = emptyList()
            } catch (e: HttpException) {
                Log.e("MainViewModel", "API error during search: ${e.code()}", e)
                e.printStackTrace()
                _searchResults.value = emptyList()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Unknown error during search", e)
                e.printStackTrace()
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun loadEntity(entityId: String) {
        viewModelScope.launch {
            _isEntityLoading.value = true
            _selectedEntityId.value = entityId
            _currentEntity.value = null
            try {
                val response = repository.getEntity(entityId)
                val entity = response.entities[entityId]
                _currentEntity.value = entity
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isEntityLoading.value = false
            }
        }
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
        val nextOffset = _searchOffset.value + 20
        searchEntities(_searchTerm.value, nextOffset)
    }

    fun handlePrevPage() {
        val prevOffset = maxOf(0, _searchOffset.value - 20)
        searchEntities(_searchTerm.value, prevOffset)
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

