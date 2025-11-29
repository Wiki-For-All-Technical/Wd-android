package com.example.wikiapp.data.repository

import com.example.wikiapp.data.api.ApiClient
import com.example.wikiapp.data.model.TopArticle
import com.example.wikiapp.data.model.WikidataEntity
import com.example.wikiapp.data.model.WikidataEntityResponse
import com.example.wikiapp.data.model.WikidataSearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class HomeRepository {
    private val wikidataApi = ApiClient.wikidataApiService
    private val restApi = ApiClient.wikimediaRestService

    suspend fun getTopReadArticles(): Result<List<TopArticle>> = withContext(Dispatchers.IO) {
        try {
            val today = LocalDate.now().minusDays(2) // pageviews endpoint lags ~1 day
            val year = today.format(DateTimeFormatter.ofPattern("yyyy"))
            val month = today.format(DateTimeFormatter.ofPattern("MM"))
            val day = today.format(DateTimeFormatter.ofPattern("dd"))
            val resp = restApi.getTopRead("en.wikipedia", year, month, day)
            if (resp.isSuccessful && resp.body() != null && resp.body()!!.items.isNotEmpty()) {
                Result.success(resp.body()!!.items.first().articles.take(20))
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchFeaturedEntity(): Result<WikidataSearchResponse> = withContext(Dispatchers.IO) {
        try {
            val seed = ('a'..'z').random().toString()
            val resp = wikidataApi.searchEntities(search = seed, limit = 20, language = "en")
            if (resp.isSuccessful && resp.body() != null) {
                Result.success(resp.body()!!)
            } else {
                Result.success(WikidataSearchResponse(search = emptyList()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEntityForEnWikiTitle(title: String): Result<WikidataEntity?> = withContext(Dispatchers.IO) {
        try {
            val resp = wikidataApi.getEntityByTitle(sites = "enwiki", titles = title)
            if (resp.isSuccessful && resp.body() != null) {
                val entities: Map<String, WikidataEntity> = resp.body()!!.entities
                Result.success(entities.values.firstOrNull())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}






