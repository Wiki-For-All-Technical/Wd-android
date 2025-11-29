package com.example.wikiapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wikiapp.data.model.TopArticle
import com.example.wikiapp.data.model.WikidataEntity
import com.example.wikiapp.data.model.WikidataSearchResponse
import com.example.wikiapp.data.repository.HomeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class HomeSection(
    val type: SectionType,
    val data: Any?
)

enum class SectionType { Featured, TodayOnWikidata, TopRead }

data class HomeUiState(
    val sections: List<HomeSection> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(
    private val repository: HomeRepository = HomeRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)

            val featuredDeferred = async { repository.searchFeaturedEntity() }
            val topReadDeferred = async { repository.getTopReadArticles() }

            val sections = mutableListOf<HomeSection>()
            var error: String? = null

            featuredDeferred.await().fold(
                onSuccess = { res: WikidataSearchResponse ->
                    sections.add(HomeSection(SectionType.Featured, res))
                },
                onFailure = { e -> error = e.message ?: "Failed loading featured" }
            )

            topReadDeferred.await().fold(
                onSuccess = { articles: List<TopArticle> ->
                    sections.add(HomeSection(SectionType.TopRead, articles))
                },
                onFailure = { e -> error = e.message ?: "Failed loading top read" }
            )

            // Randomize order but keep Featured near top
            val featured = sections.firstOrNull { it.type == SectionType.Featured }
            val others = sections.filter { it.type != SectionType.Featured }.shuffled()
            val finalList = buildList {
                featured?.let { add(it) }
                addAll(others)
            }

            _uiState.value = HomeUiState(
                sections = finalList,
                isLoading = false,
                error = error
            )
        }
    }
}






