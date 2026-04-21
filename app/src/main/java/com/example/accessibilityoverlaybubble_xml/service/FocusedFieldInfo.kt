package com.example.accessibilityoverlaybubble_xml.service

data class FocusedFieldInfo(
    val packageName: String?,
    val className: String?,
    val text: String?,
    val hintText: String?,
    val viewId: String?,
    val isEditable: Boolean,
    val timestamp: Long
)