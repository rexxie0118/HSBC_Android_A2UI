package com.a2ui.renderer.config

import com.a2ui.renderer.rules.ValidationConfig
import com.a2ui.renderer.rules.DependencyConfig

data class UIConfig(
    val journeys: Map<String, JourneyConfig> = emptyMap(),
    val pages: Map<String, PageConfig> = emptyMap(),
    val allComponents: Map<String, ComponentConfig> = emptyMap(),
    val currentJourneyId: String? = null
) {
    fun getJourney(journeyId: String): JourneyConfig? = journeys[journeyId]
    
    fun getCurrentJourney(): JourneyConfig? = currentJourneyId?.let { journeys[it] }
    
    fun getPage(journeyId: String, pageId: String): PageConfig? {
        return pages[pageId]
    }
    
    fun getSection(pageId: String, sectionId: String): SectionConfig? {
        val page = pages[pageId] ?: return null
        return page.sections.find { it.id == sectionId }
    }
    
    fun getComponent(componentId: String): ComponentConfig? {
        return allComponents[componentId]
    }
}

data class JourneyConfig(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val pageIds: List<String> = emptyList(),
    val defaultPageId: String? = null,
    val navigation: NavigationConfig,
    val analytics: AnalyticsConfig
)

data class PageConfig(
    val id: String,
    val name: String,
    val title: String,
    val journeyId: String,
    val theme: String,
    val statusBar: StatusBarConfig,
    val navigationBar: NavigationBarConfig,
    val sections: List<SectionConfig> = emptyList(),
    val scrollable: Boolean,
    val pullToRefresh: Boolean,
    val refreshOnResume: Boolean,
    val analytics: PageAnalyticsConfig
)

data class SectionConfig(
    val id: String,
    val name: String,
    val pageId: String,
    val order: Int,
    val visible: Boolean,
    val theme: SectionThemeConfig,
    val components: List<ComponentConfig> = emptyList()
)

data class ComponentConfig(
    val id: String,
    val type: String,
    val sectionId: String,
    val properties: ComponentProperties? = null,
    val theme: SectionThemeConfig? = null,
    val action: ActionConfig? = null,
    val children: List<String> = emptyList(),
    val validation: ValidationConfig? = null,
    val dependencies: DependencyConfig? = null,
    val visibility: VisibilityConfig? = null
)

data class ListDataBinding(
    val dataFile: String,
    val arrayKey: String,
    val itemLayout: String,
    val textField: String,
    val subtitleField: String?,
    val valueField: String,
    val valuePrefix: String? = null
)

data class NavigationConfig(
    val allowBack: Boolean,
    val allowForward: Boolean,
    val preserveState: Boolean
)

data class AnalyticsConfig(
    val enabled: Boolean,
    val trackPageViews: Boolean,
    val trackUserActions: Boolean
)

data class PageAnalyticsConfig(
    val pageName: String,
    val trackViews: Boolean
)

data class StatusBarConfig(
    val visible: Boolean,
    val style: String,
    val backgroundColor: String
)

data class NavigationBarConfig(
    val visible: Boolean,
    val style: String,
    val backgroundColor: String
)

data class SectionThemeConfig(
    val backgroundColor: String = "#FFFFFF",
    val padding: PaddingConfig = PaddingConfig(),
    val borderRadius: String = "medium",
    val border: BorderConfig? = null,
    val margin: MarginConfig? = null,
    val shadow: String? = null
)

data class PaddingConfig(
    val all: String? = null,
    val top: String? = null,
    val bottom: String? = null,
    val start: String? = null,
    val end: String? = null
)

data class MarginConfig(
    val all: String? = null,
    val top: String? = null,
    val bottom: String? = null,
    val start: String? = null,
    val end: String? = null
)

data class BorderConfig(
    val width: Double,
    val color: String,
    val position: String? = null
)

data class ComponentProperties(
    val text: TextValue? = null,
    val icon: String? = null,
    val usageHint: String? = null,
    val distribution: String? = null,
    val alignment: String? = null,
    val children: List<String>? = null,
    val inlineChildren: List<InlineComponent>? = null,
    val padding: PaddingConfig? = null,
    val color: String? = null,
    val fontWeight: String? = null,
    val fontStyle: String? = null,
    val textAlign: String? = null,
    val size: Int? = null,
    val tintColor: String? = null,
    val fit: String? = null,
    val height: Int? = null,
    val width: Int? = null,
    val weight: Double? = null,
    val label: TextValue? = null,
    val textFieldType: String? = null,
    val child: String? = null,
    val primary: Boolean? = null,
    val backgroundColor: String? = null,
    val shape: String? = null,
    val border: BorderConfig? = null,
    val tabItems: List<TabItem>? = null,
    val thickness: Double? = null,
    val indentStart: Int? = null,
    val indentEnd: Int? = null,
    val dataBinding: ListDataBinding? = null,
    val childrenTemplate: ChildrenTemplate? = null,
    val elevation: Int? = null,
    // Dropdown properties
    val options: List<Map<String, Any>>? = null,
    val placeholder: TextValue? = null,
    val displayMode: String? = null
)

data class ChildrenTemplate(
    val dataBinding: String,
    val componentId: String,
    val itemVar: String? = null
)

data class InlineComponent(
    val type: String,
    val properties: ComponentProperties? = null,
    val action: ActionConfig? = null
)

data class TextValue(
    val literalString: String
)

data class TabItem(
    val title: String,
    val child: String
)

data class ActionConfig(
    val event: String,
    val context: Map<String, Any>? = null
)

data class VisibilityConfig(
    val rule: String,
    val transition: String = "fade"
)
