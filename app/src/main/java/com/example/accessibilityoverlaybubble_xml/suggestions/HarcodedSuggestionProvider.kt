package com.example.accessibilityoverlaybubble_xml.suggestions

import com.example.accessibilityoverlaybubble_xml.data.UserTone

class HardcodedSuggestionProvider : SuggestionProvider {

    private val contextAnalyzer = MessageContextAnalyser()

    override suspend fun getSuggestions(currentText: String?, tone: UserTone): List<SuggestionItem> {
        val context = contextAnalyzer.analyze(currentText)

        return when {
            context.isEmpty -> starterSuggestions(tone)
            context.containsClarificationCue -> clarificationSuggestions(tone)
            context.endsWithQuestionMark -> questionReplySuggestions(tone)
            context.isShortText -> shortReplySuggestions(tone)
            else -> generalSuggestions(tone)
        }
    }

    private fun starterSuggestions(tone: UserTone): List<SuggestionItem> {
        return when (tone) {
            UserTone.CASUAL -> listOf(
                SuggestionItem("Hey, how are you?", SuggestionCategory.STARTER, tone = tone.name),
                SuggestionItem("Sounds good — what time works for you?", SuggestionCategory.STARTER, tone = tone.name),
                SuggestionItem("What are you up to later?", SuggestionCategory.STARTER, tone = tone.name)
            )
            UserTone.POLITE -> listOf(
                SuggestionItem("Hi, I hope you’re doing well.", SuggestionCategory.STARTER, tone = tone.name),
                SuggestionItem("That sounds good — what time would suit you?", SuggestionCategory.STARTER, tone = tone.name),
                SuggestionItem("Would you mind clarifying that for me?", SuggestionCategory.STARTER, tone = tone.name)
            )
            UserTone.PROFESSIONAL -> listOf(
                SuggestionItem("Hello, I hope you are well.", SuggestionCategory.STARTER, tone = tone.name),
                SuggestionItem("That works for me — what time would be most convenient?", SuggestionCategory.STARTER, tone = tone.name),
                SuggestionItem("Could you please provide further clarification?", SuggestionCategory.STARTER, tone = tone.name)
            )
        }
    }

    private fun clarificationSuggestions(tone: UserTone): List<SuggestionItem> {
        return when (tone) {
            UserTone.CASUAL -> listOf(
                SuggestionItem("Can you explain that a bit more?", SuggestionCategory.CLARIFICATION, tone = tone.name),
                SuggestionItem("I’m not fully following — what do you mean?", SuggestionCategory.CLARIFICATION, tone = tone.name),
                SuggestionItem("Could you give me a bit more detail?", SuggestionCategory.CLARIFICATION, tone = tone.name)
            )
            UserTone.POLITE -> listOf(
                SuggestionItem("Could you explain that a little more clearly?", SuggestionCategory.CLARIFICATION, tone = tone.name),
                SuggestionItem("I’m not entirely sure I understand — could you clarify?", SuggestionCategory.CLARIFICATION, tone = tone.name),
                SuggestionItem("Would you mind giving me a little more detail?", SuggestionCategory.CLARIFICATION, tone = tone.name)
            )
            UserTone.PROFESSIONAL -> listOf(
                SuggestionItem("Could you please explain that in more detail?", SuggestionCategory.CLARIFICATION, tone = tone.name),
                SuggestionItem("I would appreciate some clarification on that point.", SuggestionCategory.CLARIFICATION, tone = tone.name),
                SuggestionItem("Would you be able to provide further detail?", SuggestionCategory.CLARIFICATION, tone = tone.name)
            )
        }
    }

    private fun questionReplySuggestions(tone: UserTone): List<SuggestionItem> {
        return when (tone) {
            UserTone.CASUAL -> listOf(
                SuggestionItem("Yeah, that should be fine.", SuggestionCategory.QUESTION_REPLY, tone = tone.name),
                SuggestionItem("I’m not totally sure, but I’ll check.", SuggestionCategory.QUESTION_REPLY, tone = tone.name),
                SuggestionItem("Let me get back to you on that.", SuggestionCategory.QUESTION_REPLY, tone = tone.name)
            )
            UserTone.POLITE -> listOf(
                SuggestionItem("Yes, that should be fine.", SuggestionCategory.QUESTION_REPLY, tone = tone.name),
                SuggestionItem("I’m not completely sure, but I’ll check for you.", SuggestionCategory.QUESTION_REPLY, tone = tone.name),
                SuggestionItem("I’ll get back to you on that shortly.", SuggestionCategory.QUESTION_REPLY, tone = tone.name)
            )
            UserTone.PROFESSIONAL -> listOf(
                SuggestionItem("Yes, that should be acceptable.", SuggestionCategory.QUESTION_REPLY, tone = tone.name),
                SuggestionItem("I’m not fully certain, but I will confirm and respond shortly.", SuggestionCategory.QUESTION_REPLY, tone = tone.name),
                SuggestionItem("I will follow up with you on that as soon as possible.", SuggestionCategory.QUESTION_REPLY, tone = tone.name)
            )
        }
    }

    private fun shortReplySuggestions(tone: UserTone): List<SuggestionItem> {
        return when (tone) {
            UserTone.CASUAL -> listOf(
                SuggestionItem("Sounds good to me.", SuggestionCategory.SHORT_REPLY, tone = tone.name),
                SuggestionItem("Okay, that works for me.", SuggestionCategory.SHORT_REPLY, tone = tone.name),
                SuggestionItem("No worries, I’ll get back to you soon.", SuggestionCategory.SHORT_REPLY, tone = tone.name)
            )
            UserTone.POLITE -> listOf(
                SuggestionItem("That sounds good to me.", SuggestionCategory.SHORT_REPLY, tone = tone.name),
                SuggestionItem("Okay, that works for me.", SuggestionCategory.SHORT_REPLY, tone = tone.name),
                SuggestionItem("No problem — I’ll get back to you shortly.", SuggestionCategory.SHORT_REPLY, tone = tone.name)
            )
            UserTone.PROFESSIONAL -> listOf(
                SuggestionItem("That sounds appropriate to me.", SuggestionCategory.SHORT_REPLY, tone = tone.name),
                SuggestionItem("Yes, that arrangement works for me.", SuggestionCategory.SHORT_REPLY, tone = tone.name),
                SuggestionItem("I will get back to you shortly.", SuggestionCategory.SHORT_REPLY, tone = tone.name)
            )
        }
    }

    private fun generalSuggestions(tone: UserTone): List<SuggestionItem> {
        return when (tone) {
            UserTone.CASUAL -> listOf(
                SuggestionItem("That makes sense — thanks for explaining.", SuggestionCategory.GENERAL, tone = tone.name),
                SuggestionItem("Got it, give me a moment to reply properly.", SuggestionCategory.GENERAL, tone = tone.name),
                SuggestionItem("Thanks — I’ll take a look and get back to you.", SuggestionCategory.GENERAL, tone = tone.name)
            )
            UserTone.POLITE -> listOf(
                SuggestionItem("That makes sense — thank you for explaining.", SuggestionCategory.GENERAL, tone = tone.name),
                SuggestionItem("I understand. Please give me a moment to respond properly.", SuggestionCategory.GENERAL, tone = tone.name),
                SuggestionItem("Thank you — I’ll take a look and reply shortly.", SuggestionCategory.GENERAL, tone = tone.name)
            )
            UserTone.PROFESSIONAL -> listOf(
                SuggestionItem("That makes sense — thank you for the clarification.", SuggestionCategory.GENERAL, tone = tone.name),
                SuggestionItem("Understood. Please allow me a moment to respond properly.", SuggestionCategory.GENERAL, tone = tone.name),
                SuggestionItem("Thank you — I will review this and respond shortly.", SuggestionCategory.GENERAL, tone = tone.name)
            )
        }
    }
}