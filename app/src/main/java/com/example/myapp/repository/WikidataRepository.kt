package com.example.myapp.repository

import com.example.myapp.data.SearchResponse
import com.example.myapp.data.EntityResponse
import com.example.myapp.data.ParseResponse
import com.example.myapp.network.WikidataApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class WikidataRepository {

    private val apiService: WikidataApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "WikidataMobileLite/1.0 (android; contact@example.com)")
                    .build()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl("https://www.wikidata.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create()
    }

    suspend fun getMainPage(): ParseResponse {
        return apiService.getMainPage()
    }

    suspend fun searchEntities(
        search: String,
        language: String = "en",
        offset: Int = 0
    ): SearchResponse {
        return apiService.searchEntities(
            search = search,
            language = language,
            offset = offset
        )
    }

    suspend fun getEntity(entityId: String): EntityResponse {
        return apiService.getEntity(ids = entityId)
    }

    suspend fun getRandomEntity(): EntityResponse {
        val randomResponse = apiService.getRandomEntities()
        val randomId = randomResponse.query.random.firstOrNull()?.title ?: "Q42" // Default to Q42 if fails
        return apiService.getEntity(ids = randomId)
    }
}
