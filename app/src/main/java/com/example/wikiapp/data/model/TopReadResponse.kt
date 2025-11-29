package com.example.wikiapp.data.model

import com.google.gson.annotations.SerializedName

data class TopReadResponse(
    val items: List<TopReadDay>
)

data class TopReadDay(
    @SerializedName("articles")
    val articles: List<TopArticle>
)

data class TopArticle(
    @SerializedName("article")
    val article: String,
    @SerializedName("views")
    val views: Long,
    @SerializedName("rank")
    val rank: Int
)






