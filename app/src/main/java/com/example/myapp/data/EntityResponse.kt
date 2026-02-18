package com.example.myapp.data

data class EntityResponse(
    val entities: Map<String, Entity>,
    val success: Int? = null
)

