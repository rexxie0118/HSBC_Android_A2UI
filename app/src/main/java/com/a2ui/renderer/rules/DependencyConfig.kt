package com.a2ui.renderer.rules

/**
 * Dependency configuration for component fields
 */
data class DependencyConfig(
    val visible: VisibilityRule? = null,
    val enabled: EnabledRule? = null,
    val required: RequiredRule? = null,
    val value: ValueRule? = null,
    val options: OptionsRule? = null
)

/**
 * Visibility rule
 */
data class VisibilityRule(val rule: String, val action: String = "hide")

/**
 * Enabled rule
 */
data class EnabledRule(val rule: String)

/**
 * Required rule
 */
data class RequiredRule(val rule: String)

/**
 * Value rule
 */
data class ValueRule(val rule: String, val transform: String? = null)

/**
 * Options rule
 */
data class OptionsRule(val rule: String, val source: String? = null)
