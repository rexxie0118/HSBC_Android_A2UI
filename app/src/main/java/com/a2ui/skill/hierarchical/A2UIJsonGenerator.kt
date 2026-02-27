package com.a2ui.skill.hierarchical

import com.a2ui.renderer.config.*
import com.a2ui.renderer.rules.ValidationConfig
import com.a2ui.renderer.rules.DependencyConfig

/**
 * A2UI JSON Structure Generator and Parser
 * Manages creation, validation, and serialization of hierarchical UI configurations
 */
class A2UIJsonGenerator {
    
    /**
     * Generate UIConfig from structured parameters for a static page
     */
    fun generateStaticPageConfig(
        pageId: String,
        title: String,
        components: List<ComponentConfig>,
        theme: String = "default"
    ): UIConfig {
        val pageConfig = PageConfig(
            id = pageId,
            name = pageId, // Using ID as name by default
            title = title,
            journeyId = "",
            theme = theme,
            statusBar = StatusBarConfig(true, "default", "#FFFFFF"),
            navigationBar = NavigationBarConfig(true, "default", "#FFFFFF"),
            sections = listOf(
                SectionConfig(
                    id = "${pageId}_main_section",
                    name = "Main Section",
                    pageId = pageId,
                    order = 1,
                    visible = true,
                    theme = SectionThemeConfig(),
                    components = components
                )
            ),
            scrollable = true,
            pullToRefresh = false,
            refreshOnResume = false,
            analytics = PageAnalyticsConfig(title, true)
        )
        
        // Generate all components map
        val allComponents = mutableMapOf<String, ComponentConfig>()
        components.forEach { component ->
            allComponents[component.id] = component
        }
        
        return UIConfig(
            journeys = emptyMap(), // No journeys for static page
            pages = mapOf(pageId to pageConfig),
            allComponents = allComponents,
            currentJourneyId = null
        )
    }
    
    /**
     * Generate UIConfig for a multi-page journey
     */
    fun generateJourneyConfig(
        journeyId: String,
        journeyName: String,
        journeyPages: List<PageConfig>, 
        defaultPageId: String? = null
    ): UIConfig {
        // Create page map
        val pages = mutableMapOf<String, PageConfig>()
        journeyPages.forEach { page ->
            pages[page.id] = page
        }
        
        // Collect all components from all pages
        val allComponents = mutableMapOf<String, ComponentConfig>()
        journeyPages.forEach { page ->
            page.sections.forEach { section ->
                section.components.forEach { component ->
                    allComponents[component.id] = component
                }
            }
        }
        
        val journeyConfig = JourneyConfig(
            id = journeyId,
            name = journeyName,
            version = "1.0.0",
            description = "$journeyName journey",
            pageIds = journeyPages.map { it.id },
            defaultPageId = defaultPageId ?: journeyPages.firstOrNull()?.id,
            navigation = NavigationConfig(
                allowBack = true,
                allowForward = false, // Only move forward when validation passes
                preserveState = true
            ),
            analytics = AnalyticsConfig(
                enabled = true,
                trackPageViews = true,
                trackUserActions = true
            )
        )
        
        return UIConfig(
            journeys = mapOf(journeyId to journeyConfig),
            pages = pages,
            allComponents = allComponents,
            currentJourneyId = journeyId
        )
    }
    
    /**
     * Add a component to existing UIConfig
     */
    fun addComponentToConfig(config: UIConfig, component: ComponentConfig): UIConfig {
        val newComponentMap = config.allComponents.toMutableMap()
        newComponentMap[component.id] = component
        
        return config.copy(allComponents = newComponentMap)
    }
    
    /**
     * Add a page to existing UIConfig
     */
    fun addPageToConfig(config: UIConfig, page: PageConfig): UIConfig {
        val newPageMap = config.pages.toMutableMap()
        newPageMap[page.id] = page
        
        // Also add the page's components to allComponents if not already there
        val updatedComponents = config.allComponents.toMutableMap()
        page.sections.forEach { section ->
            section.components.forEach { component -> 
                updatedComponents[component.id] = component
            }
        }
        
        return config.copy(pages = newPageMap, allComponents = updatedComponents)
    }
}

/**
 * Builder pattern for more complex configurations
 */
class ComponentConfigBuilder {
    private lateinit var id: String
    private lateinit var type: String
    private lateinit var sectionId: String
    private var properties: ComponentProperties? = null
    private var action: ActionConfig? = null
    private var validation: ValidationConfig? = null
    private var dependencies: DependencyConfig? = null
    private var children: List<String> = emptyList()
    private var visibility: VisibilityConfig? = null
    
    fun id(id: String) = apply { this.id = id }
    fun type(type: String) = apply { this.type = type }
    fun sectionId(sectionId: String) = apply { this.sectionId = sectionId }
    fun properties(properties: ComponentProperties?) = apply { this.properties = properties }
    fun action(action: ActionConfig?) = apply { this.action = action }
    fun validation(validation: ValidationConfig?) = apply { this.validation = validation }
    fun dependencies(dependencies: DependencyConfig?) = apply { this.dependencies = dependencies }
    fun children(children: List<String>) = apply { this.children = children }
    fun visibility(visibility: VisibilityConfig?) = apply { this.visibility = visibility }
    
    fun build(): ComponentConfig {
        return ComponentConfig(
            id = id,
            type = type,
            sectionId = sectionId,
            properties = properties,
            action = action,
            validation = validation,
            dependencies = dependencies,
            children = children,
            visibility = visibility
        )
    }
    
    companion object {
        fun textField(
            id: String,
            sectionId: String,
            label: String = "",
            hint: String = "",
            required: Boolean = false
        ): ComponentConfig {
            val builder = ComponentConfigBuilder()
            
            return builder.id(id)
                .type("FORM_INPUT")
                .sectionId(sectionId)
                .properties(ComponentProperties(
                    label = if (label.isNotBlank()) TextValue(literalString = label) else null,
                    usageHint = if (hint.isNotBlank()) hint else null
                ))
                .validation(if (required) ValidationConfig(required = true) else null)
                .build()
        }
        
        fun button(
            id: String,
            sectionId: String,
            text: String,
            primary: Boolean = true
        ): ComponentConfig {
            val builder = ComponentConfigBuilder()
            
            return builder.id(id)
                .type("BUTTON")
                .sectionId(sectionId)
                .properties(ComponentProperties(
                    text = TextValue(literalString = text),
                    primary = primary,
                    backgroundColor = if (primary) "#D32F2F" else "#E0E0E0" // HSBC red vs light gray
                ))
                .build()
        }
        
        fun dropdown(
            id: String,
            sectionId: String,
            label: String = "",
            placeholder: String = "Select...",
            options: List<Map<String, String>> = emptyList()
        ): ComponentConfig {
            val builder = ComponentConfigBuilder()
            
            return builder.id(id)
                .type("DROPDOWN")
                .sectionId(sectionId)
                .properties(ComponentProperties(
                    label = if (label.isNotBlank()) TextValue(literalString = label) else null,
                    placeholder = if (placeholder.isNotBlank()) TextValue(literalString = placeholder) else null,
                    options = options
                ))
                .build()
        }
    }
}

/**
 * Builder for page configurations
 */
class PageConfigBuilder {
    private lateinit var id: String
    private var name: String = ""
    private var title: String = ""
    private lateinit var journeyId: String 
    private var theme: String = ""
    private var sections: List<SectionConfig> = emptyList()
    private var scrollable: Boolean = true
    private var pullToRefresh: Boolean = false
    private var refreshOnResume: Boolean = false
    
    fun id(id: String) = apply { this.id = id }
    fun name(name: String) = apply { this.name = name }
    fun title(title: String) = apply { this.title = title }
    fun journeyId(journeyId: String) = apply { this.journeyId = journeyId }
    fun theme(theme: String) = apply { this.theme = theme }
    fun sections(sections: List<SectionConfig>) = apply { this.sections = sections }
    fun scrollable(scrollable: Boolean) = apply { this.scrollable = scrollable }
    fun pullToRefresh(pullToRefresh: Boolean) = apply { this.pullToRefresh = pullToRefresh }
    fun refreshOnResume(refreshOnResume: Boolean) = apply { this.refreshOnResume = refreshOnResume }
    
    fun build(): PageConfig {
        return PageConfig(
            id = id,
            name = if (name.isNotBlank()) name else id,
            title = if (title.isNotBlank()) title else id,
            journeyId = journeyId,
            theme = if (theme.isNotBlank()) theme else "default",
            statusBar = StatusBarConfig(visible = true, style = "default", backgroundColor = "#ffffff"),
            navigationBar = NavigationBarConfig(visible = true, style = "default", backgroundColor = "#ffffff"), 
            sections = sections,
            scrollable = scrollable,
            pullToRefresh = pullToRefresh,
            refreshOnResume = refreshOnResume,
            analytics = PageAnalyticsConfig(if (title.isNotBlank()) title else id, true)
        )
    }
}