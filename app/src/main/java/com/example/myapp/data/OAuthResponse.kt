package com.example.myapp.data

data class OAuthTokenResponse(
    val oauth_token: String,
    val oauth_token_secret: String,
    val oauth_callback_confirmed: String? = null
)

data class OAuthAccessTokenResponse(
    val oauth_token: String,
    val oauth_token_secret: String,
    val oauth_callback_confirmed: String? = null
)

data class UserInfoResponse(
    val query: UserInfoQuery
)

data class UserInfoQuery(
    val userinfo: UserInfo
)

data class UserInfo(
    val id: Int? = null,
    val name: String? = null,
    val realname: String? = null,
    val email: String? = null
)
