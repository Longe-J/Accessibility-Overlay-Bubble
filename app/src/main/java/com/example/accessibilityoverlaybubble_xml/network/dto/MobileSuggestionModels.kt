package com.example.accessibilityoverlaybubble_xml.network.dto

data class MobileSuggestionRequest(
    val artefactId: String,
    val context: String,
    val tone: String,
    val maxSuggestions: Int = 3
)

data class SuggestionItemDto(
    val text: String,
    val category: String,
    val tone: String,
    val confidence: Double?
)

data class MobileSuggestionResponse(
    val success: Boolean,
    val artefactId: String?,
    val tone: String?,
    val maxSuggestions: Int?,
    val items: List<SuggestionItemDto>?,
    val message: String?
)
