package com.example.accessibilityoverlaybubble_xml.network

import com.example.accessibilityoverlaybubble_xml.network.dto.MobileSuggestionRequest
import com.example.accessibilityoverlaybubble_xml.network.dto.MobileSuggestionResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface SuggestionApiService {

    @POST("api/mobile-suggestions")
    suspend fun getMobileSuggestions(
        @Body request: MobileSuggestionRequest
    ): MobileSuggestionResponse
}
