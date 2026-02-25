package com.a2ui.renderer.state

import androidx.compose.runtime.*

object UIStateHolder {
    var accountListExpanded by mutableStateOf(true)
    
    fun toggleAccountList() {
        accountListExpanded = !accountListExpanded
    }
}
