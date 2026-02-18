package com.example.myapp.data

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    val success: Int? = null,
    val search: List<SearchResult>? = null,
    val searchinfo: SearchStats? = null,
    @SerializedName("search-continue") val searchContinue: Int? = null
)

data class SearchResult(
    val id: String? = null,
    val title: String? = null,
    val concepturi: String? = null,
    val url: String? = null,
    val label: String? = null,
    val description: String? = null,
    val match: Match? = null,
    val aliases: List<String>? = null,
    @SerializedName("display") val display: SearchResultDisplay? = null
)

data class SearchResultDisplay(
    @SerializedName("label") val label: DisplayValue? = null,
    @SerializedName("description") val description: DisplayValue? = null
)

data class DisplayValue(
    val value: String? = null,
    val language: String? = null
)

data class Match(
    val type: String? = null,
    val language: String? = null,
    val text: String? = null
)

data class SearchStats(
    val totalhits: Int? = null
)

