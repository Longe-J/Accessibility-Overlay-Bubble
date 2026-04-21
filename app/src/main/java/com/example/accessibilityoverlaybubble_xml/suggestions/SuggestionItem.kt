package com.example.accessibilityoverlaybubble_xml.suggestions

data class SuggestionItem(
    val text: String,
    val category: SuggestionCategory,
    val tone: String? = null,
    val confidence: Double? = null
)