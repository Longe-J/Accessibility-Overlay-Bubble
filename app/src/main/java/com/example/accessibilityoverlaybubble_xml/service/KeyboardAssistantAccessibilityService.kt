package com.example.accessibilityoverlaybubble_xml.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.accessibilityoverlaybubble_xml.overlay.OverlayController
import com.example.accessibilityoverlaybubble_xml.textactions.TextInsertionHelper

class KeyboardAssistantAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "KeyboardAssistantSvc"
        private const val MAX_CONTEXT_LINES = 8
        private const val MAX_CONTEXT_CHARS = 500
    }

    private var currentFocusedField: FocusedFieldInfo? = null
    private var currentFocusedNode: AccessibilityNodeInfo? = null

    private lateinit var overlayController: OverlayController
    private val textInsertionHelper = TextInsertionHelper()

    override fun onServiceConnected() {
        super.onServiceConnected()

        overlayController = OverlayController(
            context = this,
            onSuggestionSelected = { selectedText ->
                insertSuggestionIntoFocusedField(selectedText)
            }
        )

        Log.d(TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_FOCUSED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                inspectCurrentFocus(event)
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        overlayController.removeOverlayCompletely()
        currentFocusedNode = null
        super.onDestroy()
    }

    private fun inspectCurrentFocus(event: AccessibilityEvent) {
        val sourceNode = event.source
        val rootNode = rootInActiveWindow

        val candidateNode = when {
            sourceNode != null && sourceNode.isEditable -> sourceNode
            else -> findFocusedEditableNode(rootNode)
        }

        if (candidateNode == null) {
            if (currentFocusedField != null) {
                Log.d(TAG, "No editable field currently focused")
                currentFocusedField = null
                currentFocusedNode = null
                overlayController.hideOverlay()
            }
            return
        }

        currentFocusedNode = candidateNode

        val draftText = extractDraftText(candidateNode)
        val conversationContext = buildConversationContext(rootNode, candidateNode, draftText)

        val focusedFieldInfo = FocusedFieldInfo(
            packageName = candidateNode.packageName?.toString(),
            className = candidateNode.className?.toString(),
            text = conversationContext,
            hintText = candidateNode.hintText?.toString(),
            viewId = candidateNode.viewIdResourceName,
            isEditable = candidateNode.isEditable,
            timestamp = System.currentTimeMillis()
        )

        val fieldChanged = focusedFieldInfo != currentFocusedField
        currentFocusedField = focusedFieldInfo

        if (fieldChanged) {
            Log.d(
                TAG,
                "Focused editable field updated: " +
                        "package=${focusedFieldInfo.packageName}, " +
                        "class=${focusedFieldInfo.className}, " +
                        "text=${focusedFieldInfo.text}, " +
                        "hint=${focusedFieldInfo.hintText}, " +
                        "viewId=${focusedFieldInfo.viewId}"
            )
        }

        val shouldRefreshImmediately =
            event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED

        overlayController.updateCurrentText(
            focusedFieldInfo.text,
            refreshNow = shouldRefreshImmediately
        )

        overlayController.showOverlay(focusedFieldInfo.text)
    }

    private fun extractDraftText(node: AccessibilityNodeInfo): String {
        val rawText = node.text?.toString()?.trim().orEmpty()
        val rawHint = node.hintText?.toString()?.trim().orEmpty()

        Log.d(TAG, "raw node.text=$rawText")
        Log.d(TAG, "raw node.hintText=$rawHint")

        val obviousPlaceholders = setOf(
            "type a message",
            "write a message",
            "message",
            "rcs message"
        )

        val finalDraft = when {
            rawText.isNotBlank() && rawText.lowercase() !in obviousPlaceholders -> rawText
            else -> ""
        }

        Log.d(TAG, "finalDraft=$finalDraft")
        return finalDraft
    }

    private fun buildConversationContext(
        rootNode: AccessibilityNodeInfo?,
        inputNode: AccessibilityNodeInfo,
        draftText: String
    ): String {
        if (rootNode == null) {
            return draftText
        }

        val collectedTexts = mutableListOf<String>()
        collectVisibleText(rootNode, inputNode, collectedTexts)

        val cleanedLines = collectedTexts
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { isJunkText(it) }
            .distinct()

        val recentLines = cleanedLines.takeLast(MAX_CONTEXT_LINES)
        val conversationPart = recentLines.joinToString("\n")

        val combined = buildString {
            if (conversationPart.isNotBlank()) {
                append("Recent conversation:\n")
                append(conversationPart)
            }

            if (draftText.isNotBlank()) {
                if (isNotEmpty()) append("\n\n")
                append("Current draft:\n")
                append(draftText)
            }
        }.take(MAX_CONTEXT_CHARS)

        Log.d(TAG, "conversationPart=$conversationPart")
        Log.d(TAG, "combinedContext=$combined")

        return combined
    }

    private fun collectVisibleText(
        node: AccessibilityNodeInfo?,
        inputNode: AccessibilityNodeInfo,
        output: MutableList<String>
    ) {
        if (node == null) return

        if (node != inputNode) {
            val textValue = node.text?.toString()?.trim().orEmpty()
            if (textValue.isNotBlank()) {
                output.add(textValue)
            }

            val contentDesc = node.contentDescription?.toString()?.trim().orEmpty()
            if (contentDesc.isNotBlank()) {
                output.add(contentDesc)
            }
        }

        for (i in 0 until node.childCount) {
            collectVisibleText(node.getChild(i), inputNode, output)
        }
    }

    private fun isJunkText(value: String): Boolean {
        val lower = value.lowercase()

        if (lower in setOf("message", "rcs message", "type a message", "write a message")) {
            return true
        }

        if (lower.contains("image omitted")) return true
        if (lower.contains("video omitted")) return true
        if (lower.contains("voice call")) return true
        if (lower.contains("missed call")) return true
        if (lower.contains("tap to")) return true
        if (lower.contains("search")) return true
        if (lower.contains("attach")) return true
        if (lower.contains("send")) return true
        if (lower.contains("emoji")) return true

        return false
    }

    private fun insertSuggestionIntoFocusedField(selectedText: String) {
        val success = textInsertionHelper.replaceText(currentFocusedNode, selectedText)

        Log.d(
            TAG,
            "insertSuggestionIntoFocusedField: success=$success, selectedText=$selectedText"
        )
    }

    private fun findFocusedEditableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null

        if (node.isFocused && node.isEditable) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val result = findFocusedEditableNode(child)
            if (result != null) {
                return result
            }
        }

        return null
    }
}