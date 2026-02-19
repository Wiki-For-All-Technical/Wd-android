package com.example.myapp.network

import com.example.myapp.data.OAuthAccessTokenResponse
import com.example.myapp.data.OAuthTokenResponse
import com.example.myapp.data.UserInfoResponse
import retrofit2.http.*
import okhttp3.ResponseBody

interface OAuthApiService {
    
    @POST("w/index.php?title=Special:OAuth/initiate")
    @FormUrlEncoded
    suspend fun initiateOAuth(
        @Field("oauth_callback") oauthCallback: String = "oob",
        @Field("oauth_consumer_key") consumerKey: String,
        @Field("oauth_signature_method") signatureMethod: String = "HMAC-SHA1",
        @Field("oauth_version") version: String = "1.0"
    ): ResponseBody

    @POST("w/index.php?title=Special:OAuth/token")
    @FormUrlEncoded
    suspend fun getAccessToken(
        @Field("oauth_token") oauthToken: String,
        @Field("oauth_verifier") oauthVerifier: String,
        @Field("oauth_consumer_key") consumerKey: String,
        @Field("oauth_signature_method") signatureMethod: String = "HMAC-SHA1",
        @Field("oauth_version") version: String = "1.0"
    ): ResponseBody

    @GET("w/api.php")
    suspend fun getUserInfo(
        @Query("action") action: String = "query",
        @Query("meta") meta: String = "userinfo",
        @Query("format") format: String = "json"
    ): UserInfoResponse
}
