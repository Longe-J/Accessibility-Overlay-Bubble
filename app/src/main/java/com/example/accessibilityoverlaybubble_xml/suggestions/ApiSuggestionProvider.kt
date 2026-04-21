package com.example.accessibilityoverlaybubble_xml.suggestions

import android.content.Context
import android.util.Log
import com.example.accessibilityoverlaybubble_xml.data.PreferencesManager
import com.example.accessibilityoverlaybubble_xml.data.UserTone
import com.example.accessibilityoverlaybubble_xml.network.RetrofitProvider
import com.example.accessibilityoverlaybubble_xml.network.SuggestionRepository

class ApiSuggestionProvider(context: Context) : SuggestionProvider {

    companion object {
        private const val TAG = "ApiSuggestionProvider"
    }

    private val preferencesManager = PreferencesManager(context)
    private val repository = SuggestionRepository(RetrofitProvider.suggestionApiService)

    override suspend fun getSuggestions(
        currentText: String?,
        tone: UserTone
    ): List<SuggestionItem> {
        val artefactId = preferencesManager.getArtefactId()
        val contextText = currentText?.trim().orEmpty()
        val toneValue = tone.name.lowercase()

        Log.d(TAG, "artefactId=$artefactId")
        Log.d(TAG, "contextText=$contextText")
        Log.d(TAG, "tone=$toneValue")

        if (artefactId.isBlank()) {
            Log.d(TAG, "No artefactId configured")
            return emptyList()
        }

        // Still skip truly blank context, but now only truly blank.
        if (contextText.isBlank()) {
            Log.d(TAG, "Blank context; skipping API and allowing fallback")
            return emptyList()
        }

        val items = repository.fetchSuggestions(
            artefactId = artefactId,
            context = contextText,
            tone = toneValue,
            maxSuggestions = 3
        )

        Log.d(TAG, "API returned ${items.size} suggestions")
        return items
    }
}