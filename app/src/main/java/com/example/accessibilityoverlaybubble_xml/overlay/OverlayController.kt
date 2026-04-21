package com.example.accessibilityoverlaybubble_xml.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.accessibilityoverlaybubble_xml.R
import com.example.accessibilityoverlaybubble_xml.data.PreferencesManager
import com.example.accessibilityoverlaybubble_xml.data.UserTone
import com.example.accessibilityoverlaybubble_xml.suggestions.ApiSuggestionProvider
import com.example.accessibilityoverlaybubble_xml.suggestions.FallbackSuggestionProvider
import com.example.accessibilityoverlaybubble_xml.suggestions.HardcodedSuggestionProvider
import com.example.accessibilityoverlaybubble_xml.suggestions.SuggestionItem
import com.example.accessibilityoverlaybubble_xml.suggestions.SuggestionProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OverlayController(
    private val context: Context,
    private val onSuggestionSelected: (String) -> Unit
) {

    companion object {
        private const val TAG = "OverlayController"
        private const val REFRESH_DEBOUNCE_MS = 350L
    }

    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private val preferencesManager = PreferencesManager(context)

    private val suggestionProvider: SuggestionProvider = FallbackSuggestionProvider(
        primaryProvider = ApiSuggestionProvider(context),
        fallbackProvider = HardcodedSuggestionProvider()
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var suggestionJob: Job? = null

    private var overlayView: View? = null
    private var isShowing = false
    private var isExpanded = false
    private var lastKnownText: String? = null
    private var lastRequestedText: String? = null
    private var lastRequestedTone: UserTone? = null

    private var txtBubble: TextView? = null
    private var txtPanelSubtitle: TextView? = null
    private var panelContainer: LinearLayout? = null
    private var btnMinimise: Button? = null
    private var btnSuggestion1: Button? = null
    private var btnSuggestion2: Button? = null
    private var btnSuggestion3: Button? = null

    private var currentSuggestions: List<SuggestionItem> = emptyList()

    fun showOverlay(currentText: String?) {
        updateCurrentText(currentText, refreshNow = false)
        Log.d(TAG, "showOverlay called with currentText=$currentText")

        if (!isShowing) {
            if (overlayView == null) {
                createOverlayView()
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                x = 32
                y = 300
            }

            try {
                windowManager.addView(overlayView, params)
                isShowing = true
                setExpanded(false)
                Log.d(TAG, "Overlay shown")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show overlay", e)
                return
            }
        }

        refreshSuggestions()
    }

    /**
     * Call this whenever the focused field text changes.
     * This keeps the overlay's context fresh even if the bubble is collapsed.
     */
    fun updateCurrentText(currentText: String?, refreshNow: Boolean = false) {
        val trimmedText = currentText?.trim()
        val previousText = lastKnownText?.trim()

        if (trimmedText == previousText) {
            return
        }

        lastKnownText = trimmedText
        Log.d(TAG, "Updated lastKnownText=$lastKnownText")

        if (isShowing && refreshNow) {
            refreshSuggestions(forceImmediate = true)
        }
    }

    fun hideOverlay() {
        if (!isShowing || overlayView == null) return

        suggestionJob?.cancel()

        try {
            windowManager.removeView(overlayView)
            isShowing = false
            isExpanded = false
            Log.d(TAG, "Overlay hidden")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to hide overlay", e)
        }
    }

    fun removeOverlayCompletely() {
        suggestionJob?.cancel()
        scope.cancel()

        if (overlayView != null) {
            try {
                if (isShowing) {
                    windowManager.removeView(overlayView)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove overlay completely", e)
            } finally {
                overlayView = null
                txtBubble = null
                txtPanelSubtitle = null
                panelContainer = null
                btnMinimise = null
                btnSuggestion1 = null
                btnSuggestion2 = null
                btnSuggestion3 = null
                currentSuggestions = emptyList()
                lastKnownText = null
                lastRequestedText = null
                lastRequestedTone = null
                isShowing = false
                isExpanded = false
            }
        }
    }

    private fun createOverlayView() {
        overlayView = LayoutInflater.from(context).inflate(R.layout.overlay_assistant, null)

        txtBubble = overlayView?.findViewById(R.id.txtBubble)
        txtPanelSubtitle = overlayView?.findViewById(R.id.txtPanelSubtitle)
        panelContainer = overlayView?.findViewById(R.id.panelContainer)
        btnMinimise = overlayView?.findViewById(R.id.btnMinimise)
        btnSuggestion1 = overlayView?.findViewById(R.id.btnSuggestion1)
        btnSuggestion2 = overlayView?.findViewById(R.id.btnSuggestion2)
        btnSuggestion3 = overlayView?.findViewById(R.id.btnSuggestion3)

        txtBubble?.setOnClickListener {
            setExpanded(true)
            Log.d(TAG, "Bubble expanded, refreshing suggestions with lastKnownText=$lastKnownText")
            refreshSuggestions(forceImmediate = true)
        }

        btnMinimise?.setOnClickListener {
            setExpanded(false)
        }

        btnSuggestion1?.setOnClickListener { handleSuggestionTap(0) }
        btnSuggestion2?.setOnClickListener { handleSuggestionTap(1) }
        btnSuggestion3?.setOnClickListener { handleSuggestionTap(2) }
    }

    private fun refreshSuggestions(forceImmediate: Boolean = false) {
        suggestionJob?.cancel()
        suggestionJob = scope.launch {
            if (!forceImmediate) {
                delay(REFRESH_DEBOUNCE_MS)
            }
            updateSuggestions(lastKnownText)
        }
    }

    private suspend fun updateSuggestions(currentText: String?) {
        val tone = preferencesManager.getUserTone()
        val trimmedText = currentText?.trim().orEmpty()

        if (trimmedText.isBlank()) {
            currentSuggestions = emptyList()
            btnSuggestion1?.text = context.getString(R.string.no_suggestion_available)
            btnSuggestion2?.text = context.getString(R.string.no_suggestion_available)
            btnSuggestion3?.text = context.getString(R.string.no_suggestion_available)
            txtPanelSubtitle?.text = context.getString(R.string.no_suggestions_available)
            Log.d(TAG, "No current text available for suggestions")
            return
        }

        val shouldSkip =
            trimmedText == (lastRequestedText ?: "") &&
                    tone == lastRequestedTone &&
                    currentSuggestions.isNotEmpty()

        if (shouldSkip) {
            Log.d(TAG, "Skipping fetch because context/tone unchanged")
            return
        }

        txtPanelSubtitle?.text = context.getString(R.string.loading_suggestions)

        Log.d(TAG, "Fetching fresh suggestions for text=$trimmedText tone=$tone")

        currentSuggestions = suggestionProvider.getSuggestions(trimmedText, tone)

        lastRequestedText = trimmedText
        lastRequestedTone = tone

        if (currentSuggestions.size >= 3) {
            btnSuggestion1?.text = currentSuggestions[0].text
            btnSuggestion2?.text = currentSuggestions[1].text
            btnSuggestion3?.text = currentSuggestions[2].text

            val category = currentSuggestions.firstOrNull()?.category?.name?.lowercase() ?: "general"
            val sourceTone = currentSuggestions.firstOrNull()?.tone?.lowercase() ?: tone.name.lowercase()

            txtPanelSubtitle?.text = context.getString(
                R.string.showing_suggestions_format,
                category,
                sourceTone
            )

            Log.d(TAG, "Overlay button 1 text=${btnSuggestion1?.text}")
            Log.d(TAG, "Overlay button 2 text=${btnSuggestion2?.text}")
            Log.d(TAG, "Overlay button 3 text=${btnSuggestion3?.text}")
        } else {
            currentSuggestions = emptyList()
            btnSuggestion1?.text = context.getString(R.string.no_suggestion_available)
            btnSuggestion2?.text = context.getString(R.string.no_suggestion_available)
            btnSuggestion3?.text = context.getString(R.string.no_suggestion_available)
            txtPanelSubtitle?.text = context.getString(R.string.no_suggestions_available)
        }
    }

    private fun handleSuggestionTap(index: Int) {
        if (index !in currentSuggestions.indices) return

        val selectedSuggestion = currentSuggestions[index]

        Log.d(
            TAG,
            "Suggestion tapped: text=${selectedSuggestion.text}, category=${selectedSuggestion.category}, confidence=${selectedSuggestion.confidence}"
        )

        onSuggestionSelected(selectedSuggestion.text)
        setExpanded(false)
    }

    private fun setExpanded(expanded: Boolean) {
        isExpanded = expanded

        if (expanded) {
            txtBubble?.visibility = View.GONE
            panelContainer?.visibility = View.VISIBLE
        } else {
            txtBubble?.visibility = View.VISIBLE
            panelContainer?.visibility = View.GONE
        }
    }
}