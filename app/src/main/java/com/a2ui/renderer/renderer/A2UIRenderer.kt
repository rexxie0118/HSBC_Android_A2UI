package com.a2ui.renderer.renderer

import android.content.Context
import com.a2ui.renderer.config.ConfigManager
import com.a2ui.renderer.config.PageConfig
import com.a2ui.renderer.config.SectionConfig
import com.a2ui.renderer.config.ComponentConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class A2UIRenderer(private val context: Context) {
    
    private val _uiState = MutableStateFlow<UIState>(UIState.Idle)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()
    
    private var currentPage: PageConfig? = null
    
    fun loadPage(pageId: String) {
        val journey = ConfigManager.getCurrentJourney() ?: run {
            _uiState.value = UIState.Error("No journey loaded")
            return
        }
        
        val page = ConfigManager.getPage(journey.id, pageId) ?: run {
            _uiState.value = UIState.Error("Page not found: $pageId")
            return
        }
        
        currentPage = page
        _uiState.value = UIState.Ready(page)
    }
    
    fun loadPage(journeyId: String, pageId: String) {
        val journey = ConfigManager.getJourney(journeyId) ?: run {
            _uiState.value = UIState.Error("Journey not found: $journeyId")
            return
        }
        
        val page = ConfigManager.getPage(journeyId, pageId) ?: run {
            _uiState.value = UIState.Error("Page not found: $pageId")
            return
        }
        
        currentPage = page
        _uiState.value = UIState.Ready(page)
    }
    
    fun getCurrentPage(): PageConfig? = currentPage
    
    fun getSection(sectionId: String): SectionConfig? {
        return currentPage?.sections?.find { it.id == sectionId }
    }
    
    fun getComponent(sectionId: String, componentId: String): ComponentConfig? {
        return getSection(sectionId)?.components?.find { it.id == componentId }
    }
    
    fun navigateToPage(pageId: String) {
        loadPage(pageId)
    }
    
    fun navigateToDefaultPage() {
        val journey = ConfigManager.getCurrentJourney()
        journey?.defaultPageId?.let { loadPage(it) }
    }
}

sealed class UIState {
    object Idle : UIState()
    data class Ready(val page: PageConfig) : UIState()
    data class Error(val message: String) : UIState()
}
