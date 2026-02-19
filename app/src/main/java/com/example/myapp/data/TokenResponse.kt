package com.example.myapp.data

data class TokenResponse(
    val query: TokenQuery
)

data class TokenQuery(
    val tokens: TokenResult
)

data class TokenResult(
    val csrftoken: String
)

/** Response from Wikidata edit API (wbsetlabel, wbsetdescription, etc.) */
data class EditResponse(
    val success: Int? = null,
    val error: EditError? = null
)

data class EditError(
    val code: String? = null,
    val info: String? = null
)
