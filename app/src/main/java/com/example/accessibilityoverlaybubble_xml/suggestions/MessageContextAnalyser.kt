package com.example.accessibilityoverlaybubble_xml.suggestions

class MessageContextAnalyser {

    fun analyze(currentText: String?): MessageContext {
        val cleanedText = currentText?.trim().orEmpty()
        val lowerText = cleanedText.lowercase()

        val clarificationPhrases = listOf(
            "what do you mean",
            "not sure",
            "clarify",
            "confused",
            "don't understand"
        )

        val containsClarificationCue = clarificationPhrases.any { phrase ->
            lowerText.contains(phrase)
        }

        return MessageContext(
            originalText = cleanedText,
            isEmpty = cleanedText.isBlank(),
            endsWithQuestionMark = cleanedText.endsWith("?"),
            isShortText = cleanedText.length in 1..20,
            containsClarificationCue = containsClarificationCue
        )
    }
}
