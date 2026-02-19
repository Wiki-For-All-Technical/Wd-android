package com.example.myapp.network

import com.example.myapp.data.EntityResponse
import com.example.myapp.data.ParseResponse
import com.example.myapp.data.RandomResponse
import com.example.myapp.data.SearchResponse
import com.example.myapp.data.EditResponse
import com.example.myapp.data.TokenResponse
import com.example.myapp.data.UserInfoResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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
        @Query("limit") limit: Int = 50,
        @Query("type") type: String = "item"
    ): Response<SearchResponse>

    @GET("w/api.php")
    suspend fun getEntity(
        @Query("action") action: String = "wbgetentities",
        @Query("ids") ids: String,
        @Query("format") format: String = "json",
        @Query("languages") languages: String = "en",
        @Query("props") props: String = "labels|descriptions|claims|aliases|sitelinks"
    ): Response<EntityResponse>

    @GET("w/api.php")
    suspend fun getRandomEntities(
        @Query("action") action: String = "query",
        @Query("list") list: String = "random",
        @Query("rnnamespace") rnnamespace: Int = 0,
        @Query("rnlimit") rnlimit: Int = 1,
        @Query("format") format: String = "json"
    ): RandomResponse

    @GET("w/api.php")
    suspend fun getUserInfo(
        @Query("action") action: String = "query",
        @Query("meta") meta: String = "userinfo",
        @Query("format") format: String = "json"
    ): UserInfoResponse

    @GET("w/api.php")
    suspend fun getCsrfToken(
        @Query("action") action: String = "query",
        @Query("meta") meta: String = "tokens",
        @Query("type") type: String = "csrf",
        @Query("format") format: String = "json"
    ): TokenResponse

    @POST("w/api.php")
    @FormUrlEncoded
    suspend fun setLabel(
        @Field("action") action: String = "wbsetlabel",
        @Field("id") id: String,
        @Field("language") language: String,
        @Field("value") value: String,
        @Field("token") token: String,
        @Field("format") format: String = "json"
    ): Response<EditResponse>

    @POST("w/api.php")
    @FormUrlEncoded
    suspend fun setDescription(
        @Field("action") action: String = "wbsetdescription",
        @Field("id") id: String,
        @Field("language") language: String,
        @Field("value") value: String,
        @Field("token") token: String,
        @Field("format") format: String = "json"
    ): Response<EditResponse>

    @POST("w/api.php")
    @FormUrlEncoded
    suspend fun createClaim(
        @Field("action") action: String = "wbcreateclaim",
        @Field("entity") entity: String,
        @Field("property") property: String,
        @Field("snaktype") snaktype: String = "value",
        @Field("value") value: String,
        @Field("token") token: String,
        @Field("format") format: String = "json"
    ): Response<EditResponse>

    @POST("w/api.php")
    @FormUrlEncoded
    suspend fun setClaim(
        @Field("action") action: String = "wbsetclaim",
        @Field("claim") claim: String,
        @Field("token") token: String,
        @Field("format") format: String = "json"
    ): Response<EditResponse>

    @POST("w/api.php")
    @FormUrlEncoded
    suspend fun setQualifier(
        @Field("action") action: String = "wbsetqualifier",
        @Field("claim") claim: String,
        @Field("property") property: String,
        @Field("snaktype") snaktype: String = "value",
        @Field("value") value: String,
        @Field("token") token: String,
        @Field("format") format: String = "json"
    ): Response<EditResponse>

    @POST("w/api.php")
    @FormUrlEncoded
    suspend fun setReference(
        @Field("action") action: String = "wbsetreference",
        @Field("statement") statement: String,
        @Field("snaks") snaks: String,
        @Field("snaks-order") snaksOrder: String,
        @Field("token") token: String,
        @Field("format") format: String = "json"
    ): Response<EditResponse>
}
