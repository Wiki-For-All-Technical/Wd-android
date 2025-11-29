package com.example.wikiapp.data.model

import com.google.gson.annotations.SerializedName

data class WikidataEntityResponse(
    val entities: Map<String, WikidataEntity>
)

data class WikidataEntity(
    val id: String,
    val type: String? = null,
    val labels: Map<String, Label>? = null,
    val descriptions: Map<String, Description>? = null,
    val aliases: Map<String, List<Alias>>? = null,
    val claims: Map<String, List<Claim>>? = null,
    val sitelinks: Map<String, SiteLink>? = null,
    val lastrevid: Long? = null,
    val modified: String? = null
)

data class Label(
    val language: String,
    val value: String
)

data class Description(
    val language: String,
    val value: String
)

data class Alias(
    val language: String,
    val value: String
)

data class Claim(
    val id: String? = null,
    val mainsnak: Snak,
    val type: String? = null,
    val rank: String? = null,
    val qualifiers: Map<String, List<Snak>>? = null,
    val qualifiersOrder: List<String>? = null,
    val references: List<Reference>? = null
)

data class Snak(
    val snaktype: String,
    val property: String,
    val datatype: String? = null,
    val datavalue: DataValue? = null
)

data class DataValue(
    val type: String,
    val value: Any? = null
)

data class Reference(
    val hash: String? = null,
    val snaks: Map<String, List<Snak>>? = null,
    val snaksOrder: List<String>? = null
)

data class SiteLink(
    val site: String,
    val title: String,
    val badges: List<String>? = null,
    val url: String? = null
)

// Helper data classes for different value types
data class EntityIdValue(
    @SerializedName("entity-type")
    val entityType: String,
    @SerializedName("numeric-id")
    val numericId: Long,
    val id: String
)

data class StringValue(
    val value: String
)

data class TimeValue(
    val time: String,
    val timezone: Int = 0,
    val before: Int = 0,
    val after: Int = 0,
    val precision: Int,
    @SerializedName("calendarmodel")
    val calendarModel: String
)

data class QuantityValue(
    val amount: String,
    val unit: String,
    @SerializedName("upperBound")
    val upperBound: String? = null,
    @SerializedName("lowerBound")
    val lowerBound: String? = null
)

data class GlobeCoordinateValue(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val precision: Double? = null,
    val globe: String? = null
)





