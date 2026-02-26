package com.a2ui.renderer.form.state

/**
 * NEW: Data class for representing form choice option (used for dropdowns, radio buttons, etc.)
 */
data class ChoiceOption(
    val value: Any,
    val label: String,
    val selected: Boolean = false
)