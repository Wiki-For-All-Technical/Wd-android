package com.example.myapp.data

import com.google.gson.annotations.SerializedName

data class Entity(
    val id: String,
    val type: String? = null,
    val labels: Map<String, Label>? = null,
    val descriptions: Map<String, Description>? = null,
    val aliases: Map<String, List<Alias>>? = null,
    val claims: Map<String, List<Claim>>? = null,
    val sitelinks: Map<String, Sitelink>? = null,
    val missing: String? = null  // present when entity not found
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
    val references: List<Reference>? = null
)

data class Snak(
    val snaktype: String,
    val property: String,
    val datavalue: DataValue? = null,
    val datatype: String? = null
)

data class DataValue(
    val value: Any? = null,
    val type: String? = null
)

data class Reference(
    val hash: String? = null,
    val snaks: Map<String, List<Snak>>? = null
)

data class Sitelink(
    val site: String,
    val title: String,
    val badges: List<String>? = null
)

