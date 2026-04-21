package com.example.accessibilityoverlaybubble_xml.suggestions

enum class SuggestionCategory {
    STARTER,
    QUESTION_REPLY,
    SHORT_REPLY,
    GENERAL,
    CLARIFICATION,
    CONFIRMATION;

    companion object {
        fun fromApi(value: String?): SuggestionCategory {
            return when (value?.trim()?.uppercase()) {
                "STARTER" -> STARTER
                "QUESTION_REPLY" -> QUESTION_REPLY
                "SHORT_REPLY" -> SHORT_REPLY
                "CLARIFICATION" -> CLARIFICATION
                "CONFIRMATION" -> CONFIRMATION
                else -> GENERAL
            }
        }
    }
}