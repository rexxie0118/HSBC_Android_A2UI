package com.a2ui.skill.hierarchical

import com.a2ui.renderer.config.*
import com.a2ui.renderer.form.engine.FormEngine
import com.a2ui.renderer.form.state.FormState

/**
 * A2UI Hierarchical Renderer
 * Renders hierarchical configurations for both static pages and journey flows
 */
class A2UIHierarchicalRenderer(
    private val formEngine: FormEngine, 
    private val configManager: A2UIConfigManager
) {

    /**
     * Function to render a single page based on UI configuration
     */
    fun renderPage(
        pageId: String
    ) {
        val pageConfig = configManager.getPageConfig(pageId)
        val formState = formEngine.formState.value 

        println("Rendering page: $pageId")
        
        // Page title if defined
        pageConfig?.title?.let { title ->
            println("Title: $title")
        }

        // Render all sections in page in the specified order
        val orderedSections = pageConfig?.sections?.sortedBy { it.order } ?: emptyList()

        orderedSections.forEach { section ->
            renderSection(section, formState)
        }
    }

    /**
     * Function to render a journey 
     */
    fun renderJourney(
        journeyId: String
    ) {
        val journeyConfig = configManager.getJourneyConfig(journeyId)

        if (journeyConfig != null) {
            println("Rendering journey: $journeyId")
            
            // Iterate through journey pages
            journeyConfig.pageIds.forEach { pageId ->
                renderPage(pageId)
            }
        }
    }

    /**
     * Private function to render a single section with its components in order
     */
    private fun renderSection(
        sectionConfig: SectionConfig,
        formState: FormState
    ) {
        println("Rendering section: ${sectionConfig.id}")
        
        // Skip if section should not be visible
        if (getVisibility(formState, sectionConfig.id)) { // Use a helper function
            
            // Apply section theming conceptually
            println("Applying section theme: ${sectionConfig.theme}")

            sectionConfig.components.forEach { componentConfig ->
                // Only show component if it passes visibility check by Form Engine
                if (getVisibility(formState, componentConfig.id)) { // Use helper function
                    // Instead of calling ComponentRenderer directly, we'll simulate the component rendering
                    simulateRenderComponent(
                        componentConfig = componentConfig,
                        formState = formState,
                        onValueChange = { newValue ->
                            // Update Form Engine with the new value
                            kotlinx.coroutines.runBlocking {
                                formEngine.updateValue(componentConfig.id, newValue)
                            }
                        },
                        onAction = { actionEvent ->
                            // Handle actions through Form Engine
                            kotlinx.coroutines.runBlocking {
                                formEngine.dispatchAction(actionEvent, componentConfig.id)
                            }
                        }
                    )
                }
            }
        }
    }

    /**
     * Simulate component rendering
     */
    private fun simulateRenderComponent(
        componentConfig: ComponentConfig,
        formState: FormState,
        onValueChange: (Any?) -> Unit,
        onAction: (com.a2ui.renderer.form.engine.FormAction) -> Unit
    ) {
        println("Simulating render of component: ${componentConfig.id} (${componentConfig.type})")
        
        // In a real application, ComponentRenderer would connect to UI toolkit
        // components like TextField, Button, etc.
    }

    /**
     * Helper function to get visibility state
     */
    private fun getVisibility(formState: FormState, elementId: String): Boolean {
        return formState.visibility[elementId] ?: true
    }

    /**
     * Render navigation controls conceptually 
     */
    fun renderNavigationControl(
        journeyConfig: JourneyConfig,
        currentPageId: String
    ) {
        val currentIndex = journeyConfig.pageIds.indexOf(currentPageId)
        
        println("Current page index: $currentIndex of ${journeyConfig.pageIds.size}")
        
        // Conceptually showing navigation possibilities
        if (journeyConfig.navigation.allowBack && currentIndex > 0) {
            println("Can navigate back")
        }
        
        if (journeyConfig.navigation.allowForward && currentIndex < journeyConfig.pageIds.size - 1) {
            // Validate before moving forward
            val hasErrors = formEngine.hasErrors()
            if (!hasErrors) {
                println("Can navigate forward")
            } else {
                println("Cannot navigate: Form validation errors present")
            }
        }
    }
}

/**
 * Configuration manager
 */
class A2UIConfigManager(
    private val uiConfig: UIConfig
) {
    fun getPageConfig(pageId: String): PageConfig? {
        return uiConfig.pages[pageId]
    }

    fun getJourneyConfig(journeyId: String): JourneyConfig? {
        return uiConfig.journeys[journeyId]
    }

    fun getComponentConfig(componentId: String): ComponentConfig? {
        return uiConfig.allComponents[componentId]
    }

    fun getPageIdsForJourney(journeyId: String): List<String> {
        return uiConfig.journeys[journeyId]?.pageIds ?: emptyList()
    }
}