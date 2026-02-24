package com.a2ui.renderer.config

import android.content.Context
import org.json.JSONObject
import org.json.JSONArray
import java.io.InputStream
import com.a2ui.renderer.data.DataProvider
import com.a2ui.renderer.data.PreferencesManager

data class GlobalSettings(
    val appId: String,
    val appName: String,
    val versionCode: Int,
    val versionName: String,
    val theme: ThemeSettings,
    val language: LanguageSettings,
    val font: FontSettings,
    val spacing: SpacingSettings,
    val borderRadius: BorderRadiusSettings,
    val elevation: ElevationSettings,
    val animation: AnimationSettings,
    val accessibility: AccessibilitySettings
)

data class ThemeSettings(
    val defaultTheme: String,
    val darkModeEnabled: Boolean,
    val currentMode: String
)

data class LanguageSettings(
    val default: String,
    val supported: List<String>,
    val current: String
)

data class FontSettings(
    val default: String,
    val sizes: Map<String, Int>,
    val weights: Map<String, String>
)

data class SpacingSettings(
    val unit: Int,
    val xs: Int,
    val sm: Int,
    val md: Int,
    val lg: Int,
    val xl: Int,
    val xxl: Int,
    val xxxl: Int
)

data class BorderRadiusSettings(
    val none: Int,
    val small: Int,
    val medium: Int,
    val large: Int,
    val xl: Int,
    val xxl: Int,
    val full: Int
)

data class ElevationSettings(
    val none: Int,
    val low: Int,
    val medium: Int,
    val high: Int,
    val xl: Int
)

data class AnimationSettings(
    val duration: Map<String, Int>,
    val easing: Map<String, String>
)

data class AccessibilitySettings(
    val minimumTouchTarget: Int,
    val contrastRatio: Double
)

data class Theme(
    val id: String,
    val mode: String,
    val colors: Map<String, String>,
    val shadows: Map<String, ShadowConfig>,
    val typography: TypographyConfig
)

data class ShadowConfig(
    val offsetX: Int,
    val offsetY: Int,
    val blur: Int,
    val color: String,
    val alpha: Double
)

data class TypographyConfig(
    val fontFamily: String,
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val h5: TextStyle,
    val h6: TextStyle,
    val body1: TextStyle,
    val body2: TextStyle,
    val caption: TextStyle,
    val overline: TextStyle,
    val button: TextStyle
)

data class TextStyle(
    val size: Int,
    val weight: String,
    val lineHeight: Int,
    val letterSpacing: Double
)

data class IconMapping(
    val version: String,
    val icons: Map<String, IconConfig>,
    val categories: Map<String, List<String>>
)

data class IconConfig(
    val resource: String,
    val description: String,
    val category: String
)

object ConfigManager {
    private var globalSettings: GlobalSettings? = null
    private var themes: Map<String, Theme> = emptyMap()
    private var iconMapping: IconMapping? = null
    private var uiConfig: UIConfig = UIConfig()
    private var preferencesManager: PreferencesManager? = null
    
    private lateinit var context: Context
    private var currentTheme: Theme? = null
    private val _themeFlow = MutableStateFlow<Theme?>(null)
    val themeFlow: StateFlow<Theme?> = _themeFlow.asStateFlow()
    
    fun init(context: Context) {
        this.context = context.applicationContext
        preferencesManager = PreferencesManager(context)
        loadAllConfigs()
    }
    
    private fun loadAllConfigs() {
        try {
            loadGlobalSettings()
            loadThemes()
            loadIconMapping()
            loadUIConfig()
            
            // Load saved theme preference, or use default
            val savedThemeId = preferencesManager?.getSelectedTheme() 
                ?: globalSettings?.theme?.defaultTheme 
                ?: "banking_light"
            currentTheme = themes[savedThemeId]
            _themeFlow.value = currentTheme
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun loadUIConfig() {
        val journeys = mutableMapOf<String, JourneyConfig>()
        val pages = mutableMapOf<String, PageConfig>()
        val allComponents = mutableMapOf<String, ComponentConfig>()
        
        val journeyJson = loadJsonFromRaw("banking_journey")
        val journey = parseJourneyConfig(journeyJson)
        
        val pageIds = journey.pageIds
        for (pageId in pageIds) {
            val page = loadPageConfig(pageId, journey.id)
            pages[page.id] = page
        }
        
        // Load ALL components from all section files into allComponents map
        val sectionFiles = listOf("top_nav_section", "quick_actions_section", 
                                  "products_section", "cashflow_section",
                                  "content_sections", "investment_section", 
                                  "rewards_section", "wellness_section",
                                  "explore_section", "footer_section", 
                                  "bottom_bar_section", "wealth_header_section",
                                  "wealth_total_section", "wealth_tabs_section",
                                  "account_list_section")
        
        for (file in sectionFiles) {
            val lines = loadJsonlFromRaw(file)
            for (line in lines) {
                val json = JSONObject(line)
                val type = json.optString("type", "UNKNOWN")
                if (type != "section" && type != "page" && type != "journey" && json.has("id") && json.has("properties")) {
                    val component = parseComponentConfig(json, "")
                    allComponents[component.id] = component
                }
            }
        }
        
        journeys[journey.id] = journey
        uiConfig = UIConfig(journeys = journeys, pages = pages, allComponents = allComponents, currentJourneyId = journey.id)
    }
    
    private fun parseJourneyConfig(json: JSONObject): JourneyConfig {
        val pageIds = json.getJSONArray("pages").toList()
        return JourneyConfig(
            id = json.getString("id"),
            name = json.getString("name"),
            version = json.getString("version"),
            description = json.getString("description"),
            pageIds = pageIds,
            defaultPageId = json.getString("defaultPage"),
            navigation = NavigationConfig(
                allowBack = json.getJSONObject("navigation").getBoolean("allowBack"),
                allowForward = json.getJSONObject("navigation").getBoolean("allowForward"),
                preserveState = json.getJSONObject("navigation").getBoolean("preserveState")
            ),
            analytics = AnalyticsConfig(
                enabled = json.getJSONObject("analytics").getBoolean("enabled"),
                trackPageViews = json.getJSONObject("analytics").getBoolean("trackPageViews"),
                trackUserActions = json.getJSONObject("analytics").getBoolean("trackUserActions")
            )
        )
    }
    
    private fun loadPageConfig(pageId: String, journeyId: String): PageConfig {
        val lines = loadJsonlFromRaw(pageId)
        val pageJson = lines.firstOrNull()?.let { JSONObject(it) } ?: JSONObject()
        
        val sectionIds = pageJson.getJSONArray("sections").toList()
        val sections = sectionIds.map { sectionId ->
            loadSectionConfig(sectionId, pageId)
        }
        
        return parsePageConfig(pageJson, journeyId, sections)
    }
    
    private fun parsePageConfig(json: JSONObject, journeyId: String, sections: List<SectionConfig>): PageConfig {
        return PageConfig(
            id = json.getString("id"),
            name = json.getString("name"),
            title = json.getString("title"),
            journeyId = journeyId,
            theme = json.getString("theme"),
            statusBar = StatusBarConfig(
                visible = json.getJSONObject("statusBar").getBoolean("visible"),
                style = json.getJSONObject("statusBar").getString("style"),
                backgroundColor = json.getJSONObject("statusBar").getString("backgroundColor")
            ),
            navigationBar = NavigationBarConfig(
                visible = json.getJSONObject("navigationBar").getBoolean("visible"),
                style = json.getJSONObject("navigationBar").getString("style"),
                backgroundColor = json.getJSONObject("navigationBar").getString("backgroundColor")
            ),
            sections = sections,
            scrollable = json.getBoolean("scrollable"),
            pullToRefresh = json.getBoolean("pullToRefresh"),
            refreshOnResume = json.getBoolean("refreshOnResume"),
            analytics = PageAnalyticsConfig(
                pageName = json.getJSONObject("analytics").getString("pageName"),
                trackViews = json.getJSONObject("analytics").getBoolean("trackViews")
            )
        )
    }
    
    private fun loadSectionConfig(sectionId: String, pageId: String): SectionConfig {
        val sectionFiles = listOf("top_nav_section", "quick_actions_section", 
                                  "products_section", "cashflow_section",
                                  "content_sections", "investment_section", 
                                  "rewards_section", "wellness_section",
                                  "explore_section", "footer_section", 
                                  "bottom_bar_section", "wealth_header_section",
                                  "wealth_total_section", "wealth_tabs_section",
                                  "account_list_section")
        
        for (file in sectionFiles) {
            val lines = loadJsonlFromRaw(file)
            for (line in lines) {
                val json = JSONObject(line)
                if (json.getString("type") == "section" && json.getString("id") == sectionId) {
                    val componentIds = json.getJSONArray("components").toList()
                    val allComponents = loadAllComponentsFromFile(file, sectionId)
                    val components = allComponents.filter { it.id in componentIds }
                    return parseSectionConfig(json, pageId, components)
                }
            }
        }
        
        return SectionConfig(
            id = sectionId,
            name = sectionId,
            pageId = pageId,
            order = 0,
            visible = true,
            theme = SectionThemeConfig(),
            components = emptyList()
        )
    }
    
    private fun loadAllComponentsFromFile(sectionFile: String, sectionId: String): List<ComponentConfig> {
        val components = mutableListOf<ComponentConfig>()
        val lines = loadJsonlFromRaw(sectionFile)
        
        for (line in lines) {
            try {
                val json = JSONObject(line)
                if (json.has("properties") && json.has("id")) {
                    components.add(parseComponentConfig(json, sectionId))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return components
    }
    
    private fun parseSectionConfig(json: JSONObject, pageId: String, components: List<ComponentConfig>): SectionConfig {
        val themeJson = json.getJSONObject("theme")
        return SectionConfig(
            id = json.getString("id"),
            name = json.getString("name"),
            pageId = pageId,
            order = json.getInt("order"),
            visible = json.getBoolean("visible"),
            theme = SectionThemeConfig(
                backgroundColor = themeJson.getString("backgroundColor"),
                padding = parsePaddingConfig(themeJson.optJSONObject("padding")),
                borderRadius = themeJson.getString("borderRadius"),
                border = themeJson.optJSONObject("border")?.let { parseBorderConfig(it) },
                margin = themeJson.optJSONObject("margin")?.let { parseMarginConfig(it) },
                shadow = themeJson.optString("shadow", null)
            ),
            components = components
        )
    }
    
    private fun loadComponentsForSection(sectionId: String, sectionFile: String): List<ComponentConfig> {
        val components = mutableListOf<ComponentConfig>()
        val lines = loadJsonlFromRaw(sectionFile)
        
        for (line in lines) {
            val json = JSONObject(line)
            if (json.getString("type") == "component") {
                components.add(parseComponentConfig(json, sectionId))
            }
        }
        
        return components
    }
    
    private fun parseComponentConfig(json: JSONObject, sectionId: String): ComponentConfig {
        return ComponentConfig(
            id = json.getString("id"),
            type = json.getString("type"),
            sectionId = sectionId,
            properties = json.optJSONObject("properties")?.let { parseComponentProperties(it) },
            theme = json.optJSONObject("theme")?.let { parseSectionThemeConfig(it) },
            action = json.optJSONObject("action")?.let { parseActionConfig(it) },
            children = json.optJSONArray("children")?.toList() ?: emptyList()
        )
    }
    
    private fun parseComponentProperties(json: JSONObject): ComponentProperties {
        return ComponentProperties(
            text = json.optJSONObject("text")?.let { TextValue(it.getString("literalString")) },
            icon = json.optString("icon", null),
            usageHint = json.optString("usageHint", null),
            distribution = json.optString("distribution", null),
            alignment = json.optString("alignment", null),
            children = json.optJSONArray("children")?.toList() ?: json.optJSONObject("children")?.getJSONArray("explicitList")?.toList(),
            inlineChildren = parseInlineChildren(json),
            padding = json.optJSONObject("padding")?.let { parsePaddingConfig(it) },
            color = json.optString("color", null),
            fontWeight = json.optString("fontWeight", null),
            fontStyle = json.optString("fontStyle", null),
            textAlign = json.optString("textAlign", null),
            size = if (json.has("size")) json.getInt("size") else null,
            tintColor = json.optString("tintColor", null),
            fit = json.optString("fit", null),
            height = if (json.has("height")) json.getInt("height") else null,
            width = if (json.has("width")) json.getInt("width") else null,
            weight = if (json.has("weight")) json.getDouble("weight") else null,
            label = json.optJSONObject("label")?.let { TextValue(it.getString("literalString")) },
            textFieldType = json.optString("textFieldType", null),
            child = json.optString("child", null),
            primary = if (json.has("primary")) json.getBoolean("primary") else null,
            backgroundColor = json.optString("backgroundColor", null),
            shape = json.optString("shape", null),
            border = json.optJSONObject("border")?.let { parseBorderConfig(it) },
            tabItems = json.optJSONArray("tabItems")?.let { parseTabItems(it) },
            thickness = if (json.has("thickness")) json.getDouble("thickness") else null,
            indentStart = if (json.has("indentStart")) json.getInt("indentStart") else null,
            indentEnd = if (json.has("indentEnd")) json.getInt("indentEnd") else null,
            dataBinding = json.optJSONObject("dataBinding")?.let { parseDataBinding(it) }
        )
    }
    
    private fun parseDataBinding(json: JSONObject): ListDataBinding {
        return ListDataBinding(
            dataFile = json.optString("dataFile", ""),
            arrayKey = json.optString("arrayKey", "items"),
            itemLayout = json.optString("itemLayout", "default_list_item"),
            textField = json.optString("textField", "name"),
            subtitleField = json.optString("subtitleField", null),
            valueField = json.optString("valueField", "value"),
            valuePrefix = json.optString("valuePrefix", null)
        )
    }
    
    private fun parseInlineChildren(json: JSONObject): List<InlineComponent>? {
        val childrenJson = json.optJSONObject("children")
        if (childrenJson == null || !childrenJson.has("inlineList")) return null
        
        val inlineList = childrenJson.optJSONArray("inlineList") ?: return null
        val inlineChildren = mutableListOf<InlineComponent>()
        
        for (i in 0 until inlineList.length()) {
            val childJson = inlineList.getJSONObject(i)
            inlineChildren.add(
                InlineComponent(
                    type = childJson.optString("type", "Text"),
                    properties = childJson.optJSONObject("properties")?.let { parseComponentProperties(it) },
                    action = childJson.optJSONObject("action")?.let { parseActionConfig(it) }
                )
            )
        }
        
        return inlineChildren
    }
    
    private fun parseTabItems(json: JSONArray): List<TabItem> {
        val items = mutableListOf<TabItem>()
        for (i in 0 until json.length()) {
            val item = json.getJSONObject(i)
            items.add(
                TabItem(
                    title = item.getString("title"),
                    child = item.getString("child")
                )
            )
        }
        return items
    }
    
    private fun parseSectionThemeConfig(json: JSONObject): SectionThemeConfig {
        return SectionThemeConfig(
            backgroundColor = json.optString("backgroundColor", "#FFFFFF"),
            padding = parsePaddingConfig(json.optJSONObject("padding")),
            borderRadius = json.optString("borderRadius", "medium"),
            border = json.optJSONObject("border")?.let { parseBorderConfig(it) },
            margin = json.optJSONObject("margin")?.let { parseMarginConfig(it) },
            shadow = json.optString("shadow", null)
        )
    }
    
    private fun parseActionConfig(json: JSONObject): ActionConfig {
        return ActionConfig(
            event = json.getString("event"),
            context = json.optJSONObject("context")?.toMapAny()
        )
    }
    
    private fun parsePaddingConfig(json: JSONObject?): PaddingConfig {
        if (json == null) return PaddingConfig()
        return PaddingConfig(
            all = json.optString("all", null),
            top = json.optString("top", null),
            bottom = json.optString("bottom", null),
            start = json.optString("start", null),
            end = json.optString("end", null)
        )
    }
    
    private fun parseMarginConfig(json: JSONObject?): MarginConfig {
        if (json == null) return MarginConfig()
        return MarginConfig(
            all = json.optString("all", null),
            top = json.optString("top", null),
            bottom = json.optString("bottom", null),
            start = json.optString("start", null),
            end = json.optString("end", null)
        )
    }
    
    private fun parseBorderConfig(json: JSONObject): BorderConfig {
        return BorderConfig(
            width = json.getDouble("width"),
            color = json.getString("color"),
            position = json.optString("position", null)
        )
    }
    
    private fun loadGlobalSettings() {
        val json = loadJsonFromRaw("global_settings")
        globalSettings = parseGlobalSettings(json)
    }
    
    private fun parseGlobalSettings(json: JSONObject): GlobalSettings {
        return GlobalSettings(
            appId = json.getJSONObject("app").getString("appId"),
            appName = json.getJSONObject("app").getString("appName"),
            versionCode = json.getJSONObject("app").getInt("versionCode"),
            versionName = json.getJSONObject("app").getString("versionName"),
            theme = parseThemeSettings(json.getJSONObject("theme")),
            language = parseLanguageSettings(json.getJSONObject("language")),
            font = parseFontSettings(json.getJSONObject("font")),
            spacing = parseSpacingSettings(json.getJSONObject("spacing")),
            borderRadius = parseBorderRadiusSettings(json.getJSONObject("borderRadius")),
            elevation = parseElevationSettings(json.getJSONObject("elevation")),
            animation = parseAnimationSettings(json.getJSONObject("animation")),
            accessibility = parseAccessibilitySettings(json.getJSONObject("accessibility"))
        )
    }
    
    private fun parseThemeSettings(json: JSONObject): ThemeSettings {
        return ThemeSettings(
            defaultTheme = json.getString("defaultTheme"),
            darkModeEnabled = json.getBoolean("darkModeEnabled"),
            currentMode = json.getString("currentMode")
        )
    }
    
    private fun parseLanguageSettings(json: JSONObject): LanguageSettings {
        return LanguageSettings(
            default = json.getString("default"),
            supported = json.getJSONArray("supported").toList(),
            current = json.getString("current")
        )
    }
    
    private fun parseFontSettings(json: JSONObject): FontSettings {
        val sizesJson = json.getJSONObject("sizes")
        val sizes = mutableMapOf<String, Int>()
        sizesJson.keys().forEach { key ->
            sizes[key] = sizesJson.getInt(key)
        }
        return FontSettings(
            default = json.getString("default"),
            sizes = sizes,
            weights = json.getJSONObject("weights").toMap()
        )
    }
    
    private fun parseSpacingSettings(json: JSONObject): SpacingSettings {
        return SpacingSettings(
            unit = json.getInt("unit"),
            xs = json.getInt("xs"),
            sm = json.getInt("sm"),
            md = json.getInt("md"),
            lg = json.getInt("lg"),
            xl = json.getInt("xl"),
            xxl = json.getInt("xxl"),
            xxxl = json.getInt("xxxl")
        )
    }
    
    private fun parseBorderRadiusSettings(json: JSONObject): BorderRadiusSettings {
        return BorderRadiusSettings(
            none = json.getInt("none"),
            small = json.getInt("small"),
            medium = json.getInt("medium"),
            large = json.getInt("large"),
            xl = json.getInt("xl"),
            xxl = json.getInt("xxl"),
            full = json.getInt("full")
        )
    }
    
    private fun parseElevationSettings(json: JSONObject): ElevationSettings {
        return ElevationSettings(
            none = json.getInt("none"),
            low = json.getInt("low"),
            medium = json.getInt("medium"),
            high = json.getInt("high"),
            xl = json.getInt("xl")
        )
    }
    
    private fun parseAnimationSettings(json: JSONObject): AnimationSettings {
        val durationJson = json.getJSONObject("duration")
        val duration = mutableMapOf<String, Int>()
        durationJson.keys().forEach { key ->
            duration[key] = durationJson.getInt(key)
        }
        return AnimationSettings(
            duration = duration,
            easing = json.getJSONObject("easing").toMap()
        )
    }
    
    private fun parseAccessibilitySettings(json: JSONObject): AccessibilitySettings {
        return AccessibilitySettings(
            minimumTouchTarget = json.getInt("minimumTouchTarget"),
            contrastRatio = json.getDouble("contrastRatio")
        )
    }
    
    private fun loadThemes() {
        val themesMap = mutableMapOf<String, Theme>()
        val lines = loadJsonlFromRaw("themes")
        for (line in lines) {
            val json = JSONObject(line)
            val theme = parseTheme(json)
            themesMap[theme.id] = theme
        }
        themes = themesMap
    }
    
    private fun parseTheme(json: JSONObject): Theme {
        return Theme(
            id = json.getString("id"),
            mode = json.getString("mode"),
            colors = json.getJSONObject("colors").toMap(),
            shadows = parseShadows(json.getJSONObject("shadows")),
            typography = parseTypography(json.getJSONObject("typography"))
        )
    }
    
    private fun parseShadows(json: JSONObject): Map<String, ShadowConfig> {
        val shadows = mutableMapOf<String, ShadowConfig>()
        for (key in json.keys()) {
            val shadowJson = json.getJSONObject(key)
            shadows[key] = ShadowConfig(
                offsetX = shadowJson.getInt("offsetX"),
                offsetY = shadowJson.getInt("offsetY"),
                blur = shadowJson.getInt("blur"),
                color = shadowJson.getString("color"),
                alpha = shadowJson.getDouble("alpha")
            )
        }
        return shadows
    }
    
    private fun parseTypography(json: JSONObject): TypographyConfig {
        return TypographyConfig(
            fontFamily = json.getString("fontFamily"),
            h1 = parseTextStyle(json.getJSONObject("h1")),
            h2 = parseTextStyle(json.getJSONObject("h2")),
            h3 = parseTextStyle(json.getJSONObject("h3")),
            h4 = parseTextStyle(json.getJSONObject("h4")),
            h5 = parseTextStyle(json.getJSONObject("h5")),
            h6 = parseTextStyle(json.getJSONObject("h6")),
            body1 = parseTextStyle(json.getJSONObject("body1")),
            body2 = parseTextStyle(json.getJSONObject("body2")),
            caption = parseTextStyle(json.getJSONObject("caption")),
            overline = parseTextStyle(json.getJSONObject("overline")),
            button = parseTextStyle(json.getJSONObject("button"))
        )
    }
    
    private fun parseTextStyle(json: JSONObject): TextStyle {
        return TextStyle(
            size = json.getInt("size"),
            weight = json.getString("weight"),
            lineHeight = json.getInt("lineHeight"),
            letterSpacing = json.getDouble("letterSpacing")
        )
    }
    
    private fun loadIconMapping() {
        val json = loadJsonFromRaw("icons")
        iconMapping = parseIconMapping(json)
    }
    
    private fun parseIconMapping(json: JSONObject): IconMapping {
        val icons = mutableMapOf<String, IconConfig>()
        val iconsJson = json.getJSONObject("icons")
        for (key in iconsJson.keys()) {
            val iconJson = iconsJson.getJSONObject(key)
            icons[key] = IconConfig(
                resource = iconJson.getString("resource"),
                description = iconJson.getString("description"),
                category = iconJson.getString("category")
            )
        }
        
        val categories = mutableMapOf<String, List<String>>()
        val categoriesJson = json.getJSONObject("categories")
        for (key in categoriesJson.keys()) {
            categories[key] = categoriesJson.getJSONArray(key).toList()
        }
        
        return IconMapping(
            version = json.getString("version"),
            icons = icons,
            categories = categories
        )
    }
    
    fun getUIConfig(): UIConfig = uiConfig
    
    fun getJourney(journeyId: String): JourneyConfig? = uiConfig.getJourney(journeyId)
    
    fun getCurrentJourney(): JourneyConfig? = uiConfig.getCurrentJourney()
    
    fun getPage(journeyId: String, pageId: String): PageConfig? = uiConfig.getPage(journeyId, pageId)
    
    fun getSection(pageId: String, sectionId: String): SectionConfig? = uiConfig.getSection(pageId, sectionId)
    
    fun getComponent(componentId: String): ComponentConfig? = uiConfig.getComponent(componentId)
    
    fun getGlobalSettings(): GlobalSettings? = globalSettings
    fun getTheme(themeId: String): Theme? = themes[themeId]
    fun getCurrentTheme(): Theme? = currentTheme
    fun getIconMapping(): IconMapping? = iconMapping
    
    fun setTheme(themeId: String) {
        val newTheme = themes[themeId]
        if (newTheme != null && newTheme !== currentTheme) {
            currentTheme = newTheme
            _themeFlow.value = newTheme
            
            // Persist theme preference
            preferencesManager?.setSelectedTheme(themeId)
            
            globalSettings?.theme?.copy(currentMode = newTheme.mode)?.let {
                globalSettings = globalSettings?.copy(theme = it)
            }
        }
    }
    
    fun resolveColor(colorRef: String): String {
        if (colorRef == "transparent") {
            return "#00000000"
        }
        if (!colorRef.startsWith("\${") || !colorRef.endsWith("}")) {
            return colorRef
        }
        val key = colorRef.drop(2).dropLast(1)
        val colorKey = if (key.startsWith("colors.")) {
            key.removePrefix("colors.")
        } else {
            key
        }
        return currentTheme?.colors?.get(colorKey) ?: colorRef
    }
    
    fun resolveSpacing(spacingRef: String): Int {
        if (!spacingRef.startsWith("\${") || !spacingRef.endsWith("}")) {
            return spacingRef.toIntOrNull() ?: 0
        }
        val key = spacingRef.drop(2).dropLast(1)
        return globalSettings?.spacing?.let {
            when (key) {
                "unit" -> it.unit
                "xs" -> it.xs
                "sm" -> it.sm
                "md" -> it.md
                "lg" -> it.lg
                "xl" -> it.xl
                "xxl" -> it.xxl
                "xxxl" -> it.xxxl
                else -> it.md
            }
        } ?: 12
    }
    
    fun resolveBorderRadius(radiusRef: String): Int {
        if (!radiusRef.startsWith("\${") || !radiusRef.endsWith("}")) {
            return radiusRef.toIntOrNull() ?: 16
        }
        val key = radiusRef.drop(2).dropLast(1)
        return globalSettings?.borderRadius?.let {
            when (key) {
                "none" -> it.none
                "small" -> it.small
                "medium" -> it.medium
                "large" -> it.large
                "xl" -> it.xl
                "xxl" -> it.xxl
                "full" -> it.full
                else -> it.medium
            }
        } ?: 16
    }
    
    private fun loadJsonFromRaw(resourceName: String): JSONObject {
        val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (resourceId == 0) {
            println("Resource not found: $resourceName")
            return JSONObject()
        }
        val inputStream: InputStream = context.resources.openRawResource(resourceId)
        val jsonStr = inputStream.bufferedReader().use { it.readText() }
        println("Loaded JSON $resourceName: ${jsonStr.length} chars")
        return JSONObject(jsonStr)
    }
    
    private fun loadJsonlFromRaw(resourceName: String): List<String> {
        val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
        if (resourceId == 0) {
            println("Resource not found: $resourceName")
            return emptyList()
        }
        val inputStream: InputStream = context.resources.openRawResource(resourceId)
        val lines = inputStream.bufferedReader().readLines().filter { it.isNotBlank() }
        println("Loaded $resourceName: ${lines.size} lines")
        return lines
    }
    
    private fun JSONArray.toList(): List<String> {
        return (0 until length()).map { getString(it) }
    }
    
    private fun JSONObject.toMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        keys().forEach { key ->
            map[key] = getString(key)
        }
        return map
    }
    
    private fun JSONObject.toMapAny(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        keys().forEach { key ->
            map[key] = get(key)
        }
        return map
    }
}
