package com.a2ui.renderer.rules

import com.a2ui.renderer.binding.DataModelStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Resolves field dependencies and updates UI state
 */
object DependencyResolver {

    /**
     * Field state updates
     */
    data class FieldState(
        val visible: Boolean? = null,
        val enabled: Boolean? = null,
        val required: Boolean? = null,
        val value: Any? = null,
        val options: List<Map<String, Any>>? = null
    ) {
        fun hasChanges(): Boolean {
            return visible != null || enabled != null || required != null || 
                   value != null || options != null
        }
    }

    /**
     * Field state updates container
     */
    data class FieldStateUpdates(
        val updates: Map<String, FieldState>
    )

    /**
     * Dependency graph for optimization
     */
    private val dependencyGraph = mutableMapOf<String, MutableSet<String>>()
    private val _fieldStates = MutableStateFlow<Map<String, FieldState>>(emptyMap())
    val fieldStates: StateFlow<Map<String, FieldState>> = _fieldStates.asStateFlow()

    /**
     * Build dependency graph from fields
     */
    fun buildGraph(fields: List<com.a2ui.renderer.config.ComponentConfig>) {
        dependencyGraph.clear()

        fields.forEach { field ->
            field.dependencies?.let { deps ->
                val referencedFields = extractFieldReferences(deps)
                referencedFields.forEach { referencedId ->
                    dependencyGraph.getOrPut(referencedId) { mutableSetOf() }.add(field.id)
                }
            }
        }
    }

    /**
     * Resolve all field dependencies
     */
    fun resolveDependencies(
        fields: List<com.a2ui.renderer.config.ComponentConfig>,
        dataModel: DataModelStore
    ): FieldStateUpdates {
        val updates = mutableMapOf<String, FieldState>()

        fields.forEach { field ->
            (field.dependencies as? DependencyConfig)?.let { deps ->
                val state = evaluateDependencies(deps, dataModel, field.id)
                if (state.hasChanges()) {
                    updates[field.id] = state
                }
            }
        }

        _fieldStates.value = updates
        return FieldStateUpdates(updates)
    }

    /**
     * Get fields that depend on a changed field
     */
    fun getDependentFields(changedFieldId: String): Set<String> {
        return dependencyGraph[changedFieldId] ?: emptySet()
    }

    /**
     * Get all fields that need updating (transitive)
     */
    fun getAllDependents(fieldId: String): Set<String> {
        val allDependents = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.add(fieldId)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val dependents = getDependentFields(current)

            dependents.forEach { dependent ->
                if (dependent !in allDependents) {
                    allDependents.add(dependent)
                    queue.add(dependent)
                }
            }
        }

        return allDependents
    }

    /**
     * Evaluate dependencies for single field
     */
    private fun evaluateDependencies(
        deps: DependencyConfig,
        dataModel: DataModelStore,
        fieldId: String
    ): FieldState {
        return FieldState(
            visible = deps.visible?.let { rule ->
                val isVisible = ExpressionEvaluator.evaluateBoolean(rule.rule, dataModel)
                if (rule.action == "disable") true else isVisible
            },
            enabled = deps.enabled?.let { rule ->
                ExpressionEvaluator.evaluateBoolean(rule.rule, dataModel)
            },
            required = deps.required?.let { rule ->
                ExpressionEvaluator.evaluateBoolean(rule.rule, dataModel)
            },
            value = deps.value?.let { rule ->
                ExpressionEvaluator.evaluate(rule.rule, dataModel)
            },
            options = deps.options?.let { rule ->
                @Suppress("UNCHECKED_CAST")
                ExpressionEvaluator.evaluateList(rule.rule, dataModel) as? List<Map<String, Any>>
            }
        )
    }

    /**
     * Extract all field references from dependency expressions
     */
    private fun extractFieldReferences(deps: DependencyConfig): Set<String> {
        val references = mutableSetOf<String>()

        listOfNotNull(
            deps.visible?.rule,
            deps.enabled?.rule,
            deps.required?.rule,
            deps.value?.rule,
            deps.options?.rule
        ).forEach { expression ->
            Regex("\\$\\.([a-zA-Z0-9_]+)").findAll(expression).forEach { match ->
                references.add(match.groupValues[1])
            }
        }

        return references
    }

    /**
     * Clear dependency graph
     */
    fun clear() {
        dependencyGraph.clear()
        _fieldStates.value = emptyMap()
    }
}
