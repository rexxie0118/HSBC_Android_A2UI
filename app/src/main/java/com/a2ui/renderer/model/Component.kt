package com.a2ui.renderer.model

data class Surface(
    val surfaceId: String,
    val rootComponentId: String,
    val components: MutableMap<String, Component> = mutableMapOf(),
    val dataModel: MutableMap<String, Any?> = mutableMapOf()
)

data class Component(
    val id: String,
    val type: String,
    val properties: Properties? = null,
    val action: UserAction? = null
)

data class Properties(
    val text: BoundValue? = null,
    val child: String? = null,
    val children: Children? = null,
    val items: List<TabItem>? = null,
    val options: List<ChoiceOption>? = null,
    val imageUrl: BoundValue? = null,
    val icon: String? = null,
    val primary: Boolean? = null,
    val checked: BoundValue? = null,
    val value: BoundValue? = null,
    val minValue: Double? = null,
    val maxValue: Double? = null,
    val label: BoundValue? = null,
    val textFieldType: String? = null,
    val validationRegexp: String? = null,
    val enableDate: Boolean? = null,
    val enableTime: Boolean? = null,
    val maxAllowedSelections: Int? = null,
    val selections: BoundValue? = null,
    val fit: String? = null,
    val usageHint: String? = null,
    val distribution: String? = null,
    val alignment: String? = null,
    val weight: Double? = null,
    val template: ListTemplate? = null,
    val entryPointChild: String? = null,
    val contentChild: String? = null,
    val tabItems: List<TabItem>? = null,
    val color: String? = null,
    val backgroundColor: String? = null,
    val padding: Padding? = null,
    val margin: Margin? = null,
    val fontStyle: String? = null,
    val textAlign: String? = null,
    val fontWeight: String? = null,
    val height: Double? = null,
    val width: Double? = null,
    val shape: String? = null
)

data class Children(
    val explicitList: List<String>? = null,
    val template: ListTemplate? = null
)

data class ListTemplate(
    val dataBinding: String,
    val componentId: String
)

data class TabItem(
    val title: String,
    val child: String
)

data class ChoiceOption(
    val label: BoundValue,
    val value: BoundValue
)

data class Padding(
    val top: Int? = null,
    val bottom: Int? = null,
    val start: Int? = null,
    val end: Int? = null,
    val all: Int? = null
)

data class Margin(
    val top: Int? = null,
    val bottom: Int? = null,
    val start: Int? = null,
    val end: Int? = null,
    val all: Int? = null
)
