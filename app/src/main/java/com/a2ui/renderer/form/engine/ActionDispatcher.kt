package com.a2ui.renderer.form.engine

import com.a2ui.renderer.form.validation.ValidationEngine
import com.a2ui.renderer.form.state.ValidationError
import com.a2ui.renderer.form.state.FormState

class ActionDispatcher(
    formEngineStateProvider: suspend () -> FormState,
    validationEngine: ValidationEngine
) {
    suspend fun dispatchAction(action: FormAction, targetElementId: String = ""): ActionResult {
        return ActionResult.Success
    }
    
    suspend fun canNavigateTo(destinationViewId: String, validationResults: Map<String, List<ValidationError>>): Boolean {
        return true
    }
}