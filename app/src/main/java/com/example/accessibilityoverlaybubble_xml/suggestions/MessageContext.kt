package com.example.accessibilityoverlaybubble_xml.suggestions

data class MessageContext(
    val originalText: String,
    val isEmpty: Boolean,
    val endsWithQuestionMark: Boolean,
    val isShortText: Boolean,
    val containsClarificationCue: Boolean
)
