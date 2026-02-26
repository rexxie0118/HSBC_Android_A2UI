package com.a2ui.renderer.form.engine

import com.a2ui.renderer.form.state.FormState
import com.a2ui.renderer.form.dependency.DependencyGraph
import com.a2ui.renderer.form.evaluation.ExpressionEvaluator
import kotlinx.coroutines.flow.StateFlow

class DerivedStateManager(
    formState: StateFlow<FormState>,
    dependencyGraph: DependencyGraph,
    expressionEvaluator: ExpressionEvaluator
) {
    // placeholder implementation to satisfy compiler
}