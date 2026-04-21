package com.example.accessibilityoverlaybubble_xml.suggestions

import android.util.Log
import com.example.accessibilityoverlaybubble_xml.data.UserTone

class FallbackSuggestionProvider(
    private val primaryProvider: SuggestionProvider,
    private val fallbackProvider: SuggestionProvider
) : SuggestionProvider {

    companion object {
        private const val TAG = "FallbackSuggestionProv"
    }

    override suspend fun getSuggestions(
        currentText: String?,
        tone: UserTone
    ): List<SuggestionItem> {
        return try {
            Log.d(TAG, "Trying primary provider. currentText=$currentText, tone=${tone.name}")

            val primaryItems = primaryProvider.getSuggestions(currentText, tone)

            if (primaryItems.size >= 3) {
                Log.d(TAG, "Using primary provider suggestions")
                primaryItems
            } else {
                Log.d(
                    TAG,
                    "Primary provider returned insufficient suggestions (${primaryItems.size}); using fallback"
                )
                fallbackProvider.getSuggestions(currentText, tone)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Primary provider failed; using fallback", e)
            fallbackProvider.getSuggestions(currentText, tone)
        }
    }
}