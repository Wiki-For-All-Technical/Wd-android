package com.example.wikiapp.data.repository

import com.example.wikiapp.data.api.ApiClient
import com.example.wikiapp.data.model.WikidataEntity
import com.example.wikiapp.data.model.WikidataSearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class WikidataRepository {
    private val apiService = ApiClient.wikidataApiService
    
    suspend fun searchEntities(
        query: String,
        language: String = "en",
        type: String = "item",
        limit: Int = 50
    ): Result<WikidataSearchResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d("WikidataRepository", "Searching for: $query")
            val response = apiService.searchEntities(
                search = query,
                language = language,
                type = type,
                limit = limit
            )
            Log.d("WikidataRepository", "Response code: ${response.code()}, Success: ${response.isSuccessful}")
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Log.d("WikidataRepository", "Response body received, search results: ${body.search?.size ?: 0}")
                    // Check for API errors
                    if (body.error != null) {
                        val errorMsg = body.error.info ?: body.error.message ?: body.error.code ?: "Unknown error"
                        Log.e("WikidataRepository", "API Error: $errorMsg")
                        return@withContext Result.failure(Exception("API Error: $errorMsg"))
                    }
                    // Return success even if search list is empty
                    Result.success(body)
                } else {
                    Log.e("WikidataRepository", "Empty response body")
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Unknown error"
                } catch (e: Exception) {
                    "Error reading error body: ${e.message}"
                }
                Log.e("WikidataRepository", "HTTP Error ${response.code()}: $errorBody")
                Result.failure(Exception("HTTP ${response.code()}: ${errorBody.take(200)}"))
            }
        } catch (e: Exception) {
            Log.e("WikidataRepository", "Exception during search", e)
            Result.failure(Exception("Network error: ${e.message ?: e.toString()}"))
        }
    }
    
    suspend fun getEntity(
        entityId: String,
        languages: String = "en"
    ): Result<WikidataEntity> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getEntities(
                ids = entityId,
                languages = languages
            )
            if (response.isSuccessful && response.body() != null) {
                val entities = response.body()!!.entities
                val entity = entities[entityId] ?: entities.values.firstOrNull()
                if (entity != null) {
                    Result.success(entity)
                } else {
                    Result.failure(Exception("Entity not found"))
                }
            } else {
                Result.failure(Exception("Failed to get entity: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Fetch multiple entities at once (useful for getting property labels)
     * @param entityIds List of entity IDs to fetch
     * @param languages Languages to fetch labels for
     * @return Map of entity ID to WikidataEntity
     */
    suspend fun getEntities(
        entityIds: List<String>,
        languages: String = "en",
        props: String = "labels"
    ): Result<Map<String, WikidataEntity>> = withContext(Dispatchers.IO) {
        try {
            if (entityIds.isEmpty()) {
                return@withContext Result.success(emptyMap())
            }
            
            // Wikidata API has a limit of 50 entities per request
            val batches = entityIds.chunked(50)
            val allEntities = mutableMapOf<String, WikidataEntity>()
            
            for (batch in batches) {
                val idsString = batch.joinToString("|")
                val response = apiService.getEntities(
                    ids = idsString,
                    languages = languages,
                    props = props
                )
                
                if (response.isSuccessful && response.body() != null) {
                    allEntities.putAll(response.body()!!.entities)
                }
            }
            
            Result.success(allEntities)
        } catch (e: Exception) {
            Log.e("WikidataRepository", "Exception fetching entities", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get labels for property IDs
     * @param propertyIds List of property IDs (e.g., ["P31", "P279"])
     * @return Map of property ID to label string
     */
    suspend fun getPropertyLabels(
        propertyIds: List<String>,
        language: String = "en"
    ): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        try {
            val entitiesResult = getEntities(
                entityIds = propertyIds,
                languages = language,
                props = "labels"
            )
            
            entitiesResult.map { entities ->
                entities.mapValues { (_, entity) ->
                    entity.labels?.get(language)?.value 
                        ?: entity.labels?.values?.firstOrNull()?.value 
                        ?: entity.id
                }
            }
        } catch (e: Exception) {
            Log.e("WikidataRepository", "Exception fetching property labels", e)
            Result.failure(e)
        }
    }
}
