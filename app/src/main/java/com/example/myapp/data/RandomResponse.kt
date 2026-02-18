package com.example.myapp.data

data class RandomResponse(
    val query: QueryContent
)

data class QueryContent(
    val random: List<RandomItem>
)

data class RandomItem(
    val id: Int,
    val ns: Int,
    val title: String
)

