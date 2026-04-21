package com.example.accessibilityoverlaybubble_xml.network

import android.util.Log
import com.example.accessibilityoverlaybubble_xml.network.dto.MobileSuggestionRequest
import com.example.accessibilityoverlaybubble_xml.suggestions.SuggestionCategory
import com.example.accessibilityoverlaybubble_xml.suggestions.SuggestionItem

class SuggestionRepository(
    private val api: SuggestionApiService
) {

    companion object {
        private const val TAG = "SuggestionRepository"
    }

    suspend fun fetchSuggestions(
        artefactId: String,
        context: String,
        tone: String,
        maxSuggestions: Int = 3
    ): List<SuggestionItem> {
        return try {
            Log.d(
                TAG,
                "Sending request: artefactId=$artefactId, context=$context, tone=$tone, maxSuggestions=$maxSuggestions"
            )

            val response = api.getMobileSuggestions(
                MobileSuggestionRequest(
                    artefactId = artefactId,
                    context = context,
                    tone = tone,
                    maxSuggestions = maxSuggestions
                )
            )

            Log.d(
                TAG,
                "Response: success=${response.success}, itemCount=${response.items?.size}, message=${response.message}"
            )

            response.items?.forEachIndexed { index, dto ->
                Log.d(
                    TAG,
                    "Item[$index]: text=${dto.text}, category=${dto.category}, tone=${dto.tone}, confidence=${dto.confidence}"
                )
            }

            if (!response.success || response.items == null) {
                Log.d(TAG, "fetchSuggestions: backend returned no usable items")
                emptyList()
            } else {
                response.items.map { dto ->
                    SuggestionItem(
                        text = dto.text,
                        category = SuggestionCategory.fromApi(dto.category),
                        tone = dto.tone,
                        confidence = dto.confidence
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchSuggestions failed", e)
            emptyList()
        }
    }
}