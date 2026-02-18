package com.example.myapp.data

data class ParseResponse(
    val parse: ParseContent
)

data class ParseContent(
    val title: String? = null,
    val text: TextContent
)

data class TextContent(
    val html: String,
    val text: String? = null
)

