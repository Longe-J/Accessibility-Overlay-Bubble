package com.example.accessibilityoverlaybubble_xml.suggestions

import com.example.accessibilityoverlaybubble_xml.data.UserTone

interface SuggestionProvider {
    suspend fun getSuggestions(currentText: String?, tone: UserTone): List<SuggestionItem>
}