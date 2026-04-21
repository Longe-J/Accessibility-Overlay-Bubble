package com.example.accessibilityoverlaybubble_xml.textactions

import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

class TextInsertionHelper {

    companion object {
        private const val TAG = "TextInsertionHelper"
    }

    fun replaceText(node: AccessibilityNodeInfo?, newText: String): Boolean {
        if (node == null) {
            Log.d(TAG, "replaceText failed: node is null")
            return false
        }

        if (!node.isEditable) {
            Log.d(TAG, "replaceText failed: node is not editable")
            return false
        }

        val arguments = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                newText
            )
        }

        val success = node.performAction(
            AccessibilityNodeInfo.ACTION_SET_TEXT,
            arguments
        )

        Log.d(TAG, "replaceText result: success=$success, newText=$newText")
        return success
    }
}
