package com.example.myapp.data

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    val success: Int,
    val search: List<SearchResult>,
    val searchinfo: SearchStats? = null,
    @SerializedName("search-continue") val searchContinue: Int? = null
)

data class SearchResult(
    val id: String,
    val title: String,
    val concepturi: String? = null,
    val url: String? = null,
    val label: String? = null,
    val description: String? = null,
    val match: Match? = null
)

data class Match(
    val type: String? = null,
    val language: String? = null,
    val text: String? = null
)

data class SearchStats(
    val totalhits: Int? = null
)

