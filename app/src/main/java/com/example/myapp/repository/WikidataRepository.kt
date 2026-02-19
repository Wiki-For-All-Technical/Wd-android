package com.example.myapp.repository

import android.webkit.CookieManager
import com.example.myapp.data.Entity
import com.example.myapp.data.EntityResponse
import com.example.myapp.data.ParseResponse
import com.example.myapp.data.SearchResponse
import com.example.myapp.data.UserInfoResponse
import com.example.myapp.network.WikidataApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
                var request = chain.request().newBuilder()
                    .header("User-Agent", "WikidataMobileLite/1.0 (android; contact@example.com)")
                    .build()
                // Use WebView cookies so edits work after login
                val url = request.url.toString()
                if (url.startsWith("https://www.wikidata.org")) {
                    val cookie = CookieManager.getInstance().getCookie("https://www.wikidata.org")
                    if (!cookie.isNullOrBlank()) {
                        request = request.newBuilder().header("Cookie", cookie).build()
                    }
                }
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

    /**
     * Search Wikidata entities. Returns Result like wikiapp so we can handle errors without throwing.
     */
    suspend fun searchEntities(
        search: String,
        language: String = "en",
        offset: Int = 0,
        limit: Int = 50
    ): Result<SearchResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchEntities(
                search = search,
                language = language,
                offset = offset,
                limit = limit
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response from server"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("HTTP ${response.code()}: ${errorBody.take(200)}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message ?: e.toString()}"))
        }
    }

    /** Search Wikidata properties (for qualifier picker). Uses wbsearchentities with type=property. */
    suspend fun searchProperties(
        search: String,
        language: String = "en",
        offset: Int = 0,
        limit: Int = 20
    ): Result<SearchResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchEntities(
                search = search,
                language = language,
                offset = offset,
                limit = limit,
                type = "property"
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.success(body)
                else Result.failure(Exception("Empty response from server"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("HTTP ${response.code()}: ${errorBody.take(200)}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message ?: e.toString()}"))
        }
    }

    suspend fun getEntity(entityId: String): Result<Entity> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getEntity(ids = entityId)
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
            val body = response.body() ?: return@withContext Result.failure(Exception("Empty response"))
            val entity = body.entities[entityId] ?: body.entities.values.firstOrNull()
                ?: return@withContext Result.failure(Exception("Entity not found"))
            if (entity.missing != null) {
                return@withContext Result.failure(Exception("Entity does not exist"))
            }
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRandomEntity(): EntityResponse {
        val randomResponse = apiService.getRandomEntities()
        val randomId = randomResponse.query.random.firstOrNull()?.title ?: "Q42"
        val response = apiService.getEntity(ids = randomId)
        if (response.isSuccessful && response.body() != null) return response.body()!!
        throw Exception("Failed to load random entity: ${response.code()}")
    }

    suspend fun getUserInfo(): UserInfoResponse {
        return apiService.getUserInfo()
    }
    
    /**
     * Fetch multiple entities at once (useful for getting property labels)
     * @param entityIds List of entity IDs to fetch
     * @param languages Languages to fetch labels for
     * @return Result containing Map of entity ID to Entity
     */
    suspend fun getEntities(
        entityIds: List<String>,
        languages: String = "en",
        props: String = "labels"
    ): Result<Map<String, com.example.myapp.data.Entity>> {
        return try {
            if (entityIds.isEmpty()) {
                Result.success(emptyMap())
            } else {
                // Wikidata API has a limit of 50 entities per request
                val batches = entityIds.chunked(50)
                val allEntities = mutableMapOf<String, com.example.myapp.data.Entity>()
                
                for (batch in batches) {
                    val idsString = batch.joinToString("|")
                    val response = apiService.getEntity(ids = idsString, languages = languages, props = props)
                    response.body()?.entities?.let { allEntities.putAll(it) }
                }
                
                Result.success(allEntities)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get labels for property IDs
     * @param propertyIds List of property IDs (e.g., ["P31", "P279"])
     * @return Result containing Map of property ID to label string
     */
    suspend fun getPropertyLabels(
        propertyIds: List<String>,
        language: String = "en"
    ): Result<Map<String, String>> {
        return try {
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
            Result.failure(e)
        }
    }

    /** Get CSRF token required for edit API. Uses cookies from WebView login when available. */
    suspend fun getEditToken(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCsrfToken()
            val token = response.query.tokens.csrftoken
            if (token.isNullOrBlank()) Result.failure(Exception("Empty CSRF token"))
            else Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Set label for an entity. Requires logged-in session (cookies). */
    suspend fun setLabel(entityId: String, language: String, value: String): Result<Unit> = withContext(Dispatchers.IO) {
        getEditToken().fold(
            onSuccess = { token ->
                try {
                    val response = apiService.setLabel(id = entityId, language = language, value = value, token = token)
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.error != null) {
                            Result.failure(Exception(body.error.info ?: body.error.code ?: "Edit failed"))
                        } else {
                            Result.success(Unit)
                        }
                    } else {
                        Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    /** Set description for an entity. Requires logged-in session (cookies). */
    suspend fun setDescription(entityId: String, language: String, value: String): Result<Unit> = withContext(Dispatchers.IO) {
        getEditToken().fold(
            onSuccess = { token ->
                try {
                    val response = apiService.setDescription(id = entityId, language = language, value = value, token = token)
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.error != null) {
                            Result.failure(Exception(body.error.info ?: body.error.code ?: "Edit failed"))
                        } else {
                            Result.success(Unit)
                        }
                    } else {
                        Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    /** Build value for wbcreateclaim. For item/property use JSON; for string use datavalue JSON. */
    private fun buildCreateClaimValue(value: String): String {
        val trimmed = value.trim()
        return when {
            trimmed.matches(Regex("^Q\\d+$", RegexOption.IGNORE_CASE)) -> {
                val numId = trimmed.drop(1).toIntOrNull() ?: 0
                """{"entity-type":"item","numeric-id":$numId}"""
            }
            trimmed.matches(Regex("^P\\d+$", RegexOption.IGNORE_CASE)) -> {
                val numId = trimmed.drop(1).toIntOrNull() ?: 0
                """{"entity-type":"property","numeric-id":$numId}"""
            }
            else -> """{"type":"string","value":${escapeJsonString(trimmed)}}"""
        }
    }

    /** Build value for wbsetqualifier (same format as create claim). */
    private fun buildQualifierValue(value: String): String = buildCreateClaimValue(value)

    /** Build full datavalue JSON for wbsetclaim/wbsetreference. */
    private fun buildDatavalueJson(value: String): String {
        val trimmed = value.trim()
        return when {
            trimmed.matches(Regex("^Q\\d+$", RegexOption.IGNORE_CASE)) -> {
                val numId = trimmed.drop(1).toIntOrNull() ?: 0
                val inner = """{"entity-type":"item","numeric-id":$numId}"""
                """{"type":"wikibase-entityid","value":$inner}"""
            }
            trimmed.matches(Regex("^P\\d+$", RegexOption.IGNORE_CASE)) -> {
                val numId = trimmed.drop(1).toIntOrNull() ?: 0
                val inner = """{"entity-type":"property","numeric-id":$numId}"""
                """{"type":"wikibase-entityid","value":$inner}"""
            }
            else -> """{"type":"string","value":${escapeJsonString(trimmed)}}"""
        }
    }

    private fun escapeJsonString(s: String): String = "\"" + s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t") + "\""

    /** Create a new claim (statement) on an entity. Requires logged-in session. */
    suspend fun createClaim(entityId: String, propertyId: String, value: String): Result<Unit> = withContext(Dispatchers.IO) {
        getEditToken().fold(
            onSuccess = { token ->
                try {
                    val valueJson = buildCreateClaimValue(value)
                    val response = apiService.createClaim(
                        entity = entityId,
                        property = propertyId,
                        snaktype = "value",
                        value = valueJson,
                        token = token
                    )
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.error != null) {
                            Result.failure(Exception(body.error.info ?: body.error.code ?: "Edit failed"))
                        } else {
                            Result.success(Unit)
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: response.message()
                        Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    /** Update an existing claim's main value. Requires claim GUID and new value. */
    suspend fun setClaim(claimId: String, propertyId: String, newValue: String): Result<Unit> = withContext(Dispatchers.IO) {
        getEditToken().fold(
            onSuccess = { token ->
                try {
                    val datavalueJson = buildDatavalueJson(newValue)
                    val claimJson = """{"id":"$claimId","type":"claim","mainsnak":{"snaktype":"value","property":"$propertyId","datavalue":$datavalueJson}}"""
                    val response = apiService.setClaim(claim = claimJson, token = token)
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.error != null) {
                            Result.failure(Exception(body.error.info ?: body.error.code ?: "Edit failed"))
                        } else {
                            Result.success(Unit)
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: response.message()
                        Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    /** Add or set a qualifier on a claim. Requires claim GUID, qualifier property, and value. */
    suspend fun setQualifier(claimId: String, qualifierPropertyId: String, value: String): Result<Unit> = withContext(Dispatchers.IO) {
        getEditToken().fold(
            onSuccess = { token ->
                try {
                    val valueJson = buildQualifierValue(value)
                    val response = apiService.setQualifier(
                        claim = claimId,
                        property = qualifierPropertyId,
                        snaktype = "value",
                        value = valueJson,
                        token = token
                    )
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.error != null) {
                            Result.failure(Exception(body.error.info ?: body.error.code ?: "Edit failed"))
                        } else {
                            Result.success(Unit)
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: response.message()
                        Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    /** Add a reference to a statement. Requires statement GUID, reference property, and value. */
    suspend fun setReference(statementId: String, refPropertyId: String, refValue: String): Result<Unit> = withContext(Dispatchers.IO) {
        getEditToken().fold(
            onSuccess = { token ->
                try {
                    val datavalueJson = buildDatavalueJson(refValue)
                    val snak = """{"snaktype":"value","property":"$refPropertyId","datavalue":$datavalueJson}"""
                    val snaks = """{"$refPropertyId":[$snak]}"""
                    val snaksOrder = """["$refPropertyId"]"""
                    val response = apiService.setReference(
                        statement = statementId,
                        snaks = snaks,
                        snaksOrder = snaksOrder,
                        token = token
                    )
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.error != null) {
                            Result.failure(Exception(body.error.info ?: body.error.code ?: "Edit failed"))
                        } else {
                            Result.success(Unit)
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: response.message()
                        Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }
}
