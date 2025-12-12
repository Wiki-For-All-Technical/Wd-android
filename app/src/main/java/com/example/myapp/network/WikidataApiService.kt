package com.example.myapp.network

import com.example.myapp.data.SearchResponse
import com.example.myapp.data.EntityResponse
import com.example.myapp.data.RandomResponse
import retrofit2.http.GET
import retrofit2.http.Query

import com.example.myapp.data.ParseResponse

interface WikidataApiService {

    @GET("w/api.php")
    suspend fun getMainPage(
        @Query("action") action: String = "parse",
        @Query("page") page: String = "Wikidata:Main_Page",
        @Query("format") format: String = "json",
        @Query("prop") prop: String = "text",
        @Query("mobileformat") mobileformat: Boolean = true
    ): ParseResponse

    @GET("w/api.php")
    suspend fun searchEntities(
        @Query("action") action: String = "wbsearchentities",
        @Query("search") search: String,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json",
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20,
        // Added type=item to filter for regular items only, removing properties/lexemes/etc from general search
        @Query("type") type: String = "item"
    ): SearchResponse

    @GET("w/api.php")
    suspend fun getEntity(
        @Query("action") action: String = "wbgetentities",
        @Query("ids") ids: String,
        @Query("format") format: String = "json",
        @Query("languages") languages: String = "en"
    ): EntityResponse

    @GET("w/api.php")
    suspend fun getRandomEntities(
        @Query("action") action: String = "query",
        @Query("list") list: String = "random",
        @Query("rnnamespace") rnnamespace: Int = 0,
        @Query("rnlimit") rnlimit: Int = 1,
        @Query("format") format: String = "json"
    ): RandomResponse
}
