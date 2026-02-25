package com.a2ui.renderer.renderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a2ui.renderer.binding.BindingResolver
import com.a2ui.renderer.binding.DataModelStore
import com.a2ui.renderer.config.ChildrenTemplate
import com.a2ui.renderer.config.ComponentConfig

/**
 * Renders dynamic lists from templates
 * Iterates over data arrays and renders component for each item
 */
object ListTemplateRenderer {

    /**
     * Render a dynamic list from template
     * @param template Template configuration
     * @param dataModel Data model containing the list
     * @param components Available component definitions
     * @param horizontal If true, renders as horizontal row
     */
    @Composable
    fun RenderList(
        template: ChildrenTemplate,
        dataModel: DataModelStore,
        components: Map<String, ComponentConfig>,
        modifier: Modifier = Modifier,
        horizontal: Boolean = false,
        onAction: (String, Map<String, Any>?) -> Unit = { _, _ -> },
        onNavigate: (String) -> Unit = {}
    ) {
        // Get the list data from binding path
        val dataList = getListData(template.dataBinding, dataModel)
        
        if (dataList.isEmpty()) {
            return
        }
        
        if (horizontal) {
            LazyRow(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(dataList) { index, item ->
                    RenderListItem(
                        item = item,
                        index = index,
                        template = template,
                        dataModel = dataModel,
                        components = components,
                        onAction = onAction,
                        onNavigate = onNavigate
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(dataList) { index, item ->
                    RenderListItem(
                        item = item,
                        index = index,
                        template = template,
                        dataModel = dataModel,
                        components = components,
                        onAction = onAction,
                        onNavigate = onNavigate
                    )
                }
            }
        }
    }
    
    /**
     * Extract list data from data model using binding path
     */
    @Suppress("UNCHECKED_CAST")
    private fun getListData(
        dataBinding: String,
        dataModel: DataModelStore
    ): List<Map<String, Any>> {
        val value = BindingResolver.resolve(dataBinding, dataModel)
        
        return when (value) {
            is List<*> -> {
                value.mapNotNull { item ->
                    when (item) {
                        is Map<*, *> -> item as Map<String, Any>
                        else -> mapOf("value" to item.toString())
                    }
                }
            }
            else -> emptyList()
        }
    }
    
    /**
     * Render a single list item with item-scoped data model
     */
    @Composable
    private fun RenderListItem(
        item: Map<String, Any>,
        index: Int,
        template: ChildrenTemplate,
        dataModel: DataModelStore,
        components: Map<String, ComponentConfig>,
        onAction: (String, Map<String, Any>?) -> Unit,
        onNavigate: (String) -> Unit
    ) {
        // Get the component template
        val componentConfig = components[template.componentId]
            ?: ComponentConfig(
                id = template.componentId,
                type = "Text",
                sectionId = ""
            )
        
        // Render the component
        renderComponent(
            component = componentConfig,
            onAction = onAction,
            onNavigate = onNavigate
        )
    }
    
    /**
     * Render static list with explicit children
     */
    @Composable
    fun RenderStaticList(
        children: List<String>,
        components: Map<String, ComponentConfig>,
        dataModel: DataModelStore,
        modifier: Modifier = Modifier,
        horizontal: Boolean = false,
        onAction: (String, Map<String, Any>?) -> Unit = { _, _ -> },
        onNavigate: (String) -> Unit = {}
    ) {
        val childComponents = children.mapNotNull { components[it] }
        
        if (horizontal) {
            Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                childComponents.forEach { component ->
                    renderComponent(
                        component = component,
                        onAction = onAction,
                        onNavigate = onNavigate
                    )
                }
            }
        } else {
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                childComponents.forEach { component ->
                    renderComponent(
                        component = component,
                        onAction = onAction,
                        onNavigate = onNavigate
                    )
                }
            }
        }
    }
}
