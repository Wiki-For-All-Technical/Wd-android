package com.example.wikiapp.data.model

import com.google.gson.annotations.SerializedName

data class WikidataSearchResponse(
    @SerializedName("search")
    val search: List<SearchResult>? = null,
    @SerializedName("search-continue")
    val searchContinue: Int? = null,
    @SerializedName("search-info")
    val searchInfo: SearchInfo? = null,
    @SerializedName("error")
    val error: ApiError? = null,
    @SerializedName("warnings")
    val warnings: Map<String, Any>? = null
)

data class ApiError(
    val code: String? = null,
    val info: String? = null,
    @SerializedName("*")
    val message: String? = null
)

data class SearchResult(
    val id: String? = null,
    val title: String? = null,
    val pageid: Int? = null,
    val repository: String? = null,
    val url: String? = null,
    val concepturi: String? = null,
    val label: String? = null,
    val description: String? = null,
    val match: Match? = null,
    val aliases: List<String>? = null,
    @SerializedName("display")
    val display: Display? = null
)

data class Display(
    @SerializedName("label")
    val label: DisplayValue? = null,
    @SerializedName("description")
    val description: DisplayValue? = null
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

data class SearchInfo(
    val query: String? = null,
    val totalhits: Int? = null
)

