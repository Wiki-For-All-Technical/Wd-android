package com.example.wikiapp.data.api

import com.example.wikiapp.data.model.WikidataEntityResponse
import com.example.wikiapp.data.model.WikidataSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WikidataApiService {
    
    /**
     * Search for entities in Wikidata
     * @param search The search query
     * @param language Language code (e.g., "en")
     * @param type Search type (item/property). Default item
     * @param limit Maximum number of results (default: 50)
     * @param continueParam Continue parameter for pagination
     */
    @GET("w/api.php")
    suspend fun searchEntities(
        @Query("action") action: String = "wbsearchentities",
        @Query("search") search: String,
        @Query("language") language: String = "en",
        @Query("type") type: String = "item",
        @Query("limit") limit: Int = 50,
        @Query("continue") continueParam: Int? = null,
        @Query("format") format: String = "json"
    ): Response<WikidataSearchResponse>
    
    /**
     * Recent changes for items
     */
    @GET("w/api.php")
    suspend fun getRecentChanges(
        @Query("action") action: String = "query",
        @Query("list") list: String = "recentchanges",
        @Query("rcnamespace") rcnamespace: Int = 0,
        @Query("rclimit") rclimit: Int = 20,
        @Query("rcprop") rcprop: String = "title|ids|timestamp",
        @Query("format") format: String = "json"
    ): Response<Map<String, Any>>
    
    /**
     * Get entity details by ID
     * @param ids Comma-separated list of entity IDs (e.g., "Q42")
     * @param languages Comma-separated list of language codes
     * @param props Properties to include (e.g., "labels|descriptions|claims|sitelinks")
     */
    @GET("w/api.php")
    suspend fun getEntities(
        @Query("action") action: String = "wbgetentities",
        @Query("ids") ids: String,
        @Query("languages") languages: String = "en",
        @Query("props") props: String = "labels|descriptions|aliases|claims|sitelinks",
        @Query("format") format: String = "json"
    ): Response<WikidataEntityResponse>
    
    /**
     * Get entity by title (for Wikipedia articles)
     */
    @GET("w/api.php")
    suspend fun getEntityByTitle(
        @Query("action") action: String = "wbgetentities",
        @Query("sites") sites: String,
        @Query("titles") titles: String,
        @Query("languages") languages: String = "en",
        @Query("props") props: String = "labels|descriptions|aliases|claims|sitelinks",
        @Query("format") format: String = "json"
    ): Response<WikidataEntityResponse>
}

