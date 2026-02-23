package com.a2ui.renderer.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a2ui.renderer.config.ConfigManager
import com.a2ui.renderer.config.SectionConfig
import com.a2ui.renderer.config.ComponentConfig
import com.a2ui.renderer.config.PageConfig
import com.a2ui.renderer.icon.IconManager

@Composable
fun PageRenderer(
    page: PageConfig,
    onAction: (String, Map<String, Any>?) -> Unit,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF5F5F5))
    ) {
        page.sections.filter { it.visible }.forEach { section ->
            renderSection(section, onAction, onNavigate)
        }
    }
}

@Composable
fun renderSection(
    section: SectionConfig,
    onAction: (String, Map<String, Any>?) -> Unit,
    onNavigate: (String) -> Unit
) {
    val theme = section.theme
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(theme.backgroundColor)))
    } catch (e: Exception) {
        Color(0xFFEEEEEE)
    }
    
    val paddingValues = resolvePadding(theme.padding)
    val marginValues = resolveMargin(theme.margin)
    
    Column(
        modifier = Modifier
            .then(
                if (marginValues != null) Modifier.padding(marginValues) else Modifier
            )
            .fillMaxWidth()
            .background(backgroundColor)
            .then(
                if (paddingValues != null) Modifier.padding(paddingValues) else Modifier.padding(16.dp)
            )
    ) {
        section.components.forEach { component ->
            renderComponent(component, onAction, onNavigate)
        }
    }
}

@Composable
fun renderComponent(
    component: ComponentConfig,
    onAction: (String, Map<String, Any>?) -> Unit,
    onNavigate: (String) -> Unit
) {
    when (component.type) {
        "Column" -> renderColumn(component, onAction, onNavigate)
        "Row" -> renderRow(component, onAction, onNavigate)
        "Text" -> renderText(component, onAction)
        "Icon" -> renderIcon(component, onAction)
        "Card" -> renderCard(component, onAction, onNavigate)
        "Tabs" -> renderTabs(component, onAction, onNavigate)
        "TextField" -> renderTextField(component)
        "Divider" -> renderDivider(component)
        "Image" -> renderImage(component)
        "Button" -> renderButton(component, onAction)
        "Spacer" -> renderSpacer(component)
        "Box" -> renderBox(component, onAction, onNavigate)
        "BottomNavigation" -> renderBottomNavigation(component, onAction, onNavigate)
        "StatusBar" -> renderStatusBar(component)
        "Section" -> renderNestedSection(component, onAction, onNavigate)
        "ListDivider" -> renderListDivider(component)
    }
}

@Composable
fun renderInlineComponent(
    inline: com.a2ui.renderer.config.InlineComponent,
    onAction: (String, Map<String, Any>?) -> Unit,
    onNavigate: (String) -> Unit
) {
    when (inline.type) {
        "Text" -> {
            val props = inline.properties
            val text = props?.text?.literalString ?: ""
            val usageHint = props?.usageHint
            val colorStr = props?.color
            val textColor = if (colorStr != null) {
                try { Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(colorStr))) } catch (e: Exception) { Color.Black }
            } else Color.Black
            
            Text(
                text = text,
                fontSize = when (usageHint) {
                    "h1" -> 36.sp; "h2" -> 28.sp; "h3" -> 24.sp; "h4" -> 22.sp
                    "h5" -> 18.sp; "h6" -> 16.sp; "caption" -> 12.sp; "body2" -> 14.sp
                    else -> 16.sp
                },
                color = textColor,
                modifier = if (inline.action != null) Modifier.clickable { onAction(inline.action.event, inline.action.context) } else Modifier
            )
        }
        "Icon" -> {
            val props = inline.properties
            val iconName = props?.icon
            val painter = IconManager.getIcon(iconName)
            val size = props?.size?.dp ?: 24.dp
            val tintColor = props?.tintColor?.let { 
                try { Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(it))) } catch (e: Exception) { null }
            }
            
            if (painter != null) {
                androidx.compose.foundation.Image(
                    painter = painter,
                    contentDescription = iconName,
                    modifier = Modifier.size(size),
                    colorFilter = if (tintColor != null) androidx.compose.ui.graphics.ColorFilter.tint(tintColor) else null
                )
            }
        }
        "Box" -> {
            val props = inline.properties
            val shape = props?.shape
            val backgroundColor = props?.backgroundColor?.let { 
                try { Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(it))) } catch (e: Exception) { Color.Transparent }
            } ?: Color.Transparent
            val width = props?.width?.toFloat()?.dp ?: 24.dp
            val height = props?.height?.toFloat()?.dp ?: 24.dp
            
            val cornerRadius = when (shape) {
                "circle" -> 1000.dp
                "rounded" -> 8.dp
                "square" -> 0.dp
                else -> 0.dp
            }
            
            Box(
                modifier = Modifier
                    .width(width)
                    .height(height)
                    .background(backgroundColor, RoundedCornerShape(cornerRadius))
            )
        }
        "Spacer" -> Spacer(modifier = Modifier.width(8.dp))
        "Divider" -> {
            val props = inline.properties
            val color = props?.color?.let { 
                try { Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(it))) } catch (e: Exception) { Color(0xFFE0E0E0) }
            } ?: Color(0xFFE0E0E0)
            val thickness = props?.thickness?.toFloat()?.dp ?: 1.dp
            androidx.compose.material3.Divider(color = color, thickness = thickness)
        }
    }
}

@Composable
fun renderColumn(
    component: ComponentConfig,
    onAction: (String, Map<String, Any>?) -> Unit,
    onNavigate: (String) -> Unit
) {
    val props = component.properties ?: return
    
    val paddingValues = resolvePadding(props.padding)
    val marginValues = resolveMargin(null)
    val backgroundColor = props.backgroundColor?.let {
        try { Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(it))) } catch (e: Exception) { null }
    }
    
    val distribution = when (props.distribution) {
        "spaceBetween" -> Arrangement.SpaceBetween
        "spaceEvenly" -> Arrangement.SpaceEvenly
        "spaceAround" -> Arrangement.SpaceAround
        "center" -> Arrangement.Center
        "bottom" -> Arrangement.Bottom
        else -> Arrangement.Top
    }
    
    val alignment = when (props.alignment) {
        "centerX" -> Alignment.CenterHorizontally
        "end" -> Alignment.End
        "center" -> Alignment.CenterHorizontally
        else -> Alignment.Start
    }
    
    val children = props.children ?: component.children
    val inlineChildren = props.inlineChildren
    
    Column(
        modifier = Modifier
            .then(
                if (marginValues != null) Modifier.padding(marginValues) else Modifier
            )
            .wrapContentWidth()
            .then(
                when {
                    backgroundColor != null -> Modifier.background(backgroundColor)
                    else -> Modifier
                }
            )
            .then(
                if (paddingValues != null) Modifier.padding(paddingValues) else Modifier.padding(12.dp)
            ),
        verticalArrangement = distribution,
        horizontalAlignment = alignment
    ) {
        children?.forEach { childId ->
            val childComponent = ConfigManager.getComponent(childId)
            if (childComponent != null) {
                renderComponent(childComponent, onAction, onNavigate)
            }
        }
        inlineChildren?.forEach { inline ->
            renderInlineComponent(inline, onAction, onNavigate)
        }
    }
}

@Composable
fun renderRow(
    component: ComponentConfig,
    onAction: (String, Map<String, Any>?) -> Unit,
    onNavigate: (String) -> Unit
) {
    val props = component.properties ?: return
    
    val paddingValues = resolvePadding(props.padding)
    val marginValues = resolveMargin(null)
    val backgroundColor = props.backgroundColor?.let {
        try { Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(it))) } catch (e: Exception) { null }
    }
    
    val distribution = when (props.distribution) {
        "spaceBetween" -> Arrangement.SpaceBetween
        "spaceEvenly" -> Arrangement.SpaceEvenly
        "spaceAround" -> Arrangement.SpaceAround
        "center" -> Arrangement.Center
        "end" -> Arrangement.End
        else -> Arrangement.Start
    }
    
    val alignment = when (props.alignment) {
        "centerY" -> Alignment.CenterVertically
        "top" -> Alignment.Top
        "bottom" -> Alignment.Bottom
        else -> Alignment.CenterVertically
    }
    
    val children = props.children ?: component.children
    val inlineChildren = props.inlineChildren
    
    println("DEBUG renderRow: component=${component.id}, children=$children")
    
    Row(
        modifier = Modifier
            .then(
                if (marginValues != null) Modifier.padding(marginValues) else Modifier
            )
            .fillMaxWidth()
            .then(
                when {
                    backgroundColor != null -> Modifier.background(backgroundColor)
                    else -> Modifier
                }
            )
            .then(
                if (paddingValues != null) Modifier.padding(paddingValues) else Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ),
        horizontalArrangement = distribution,
        verticalAlignment = alignment
    ) {
        children?.forEach { childId ->
            println("DEBUG renderRow: looking for child=$childId")
            val childComponent = ConfigManager.getComponent(childId)
            if (childComponent != null) {
                println("DEBUG renderRow: rendering child=$childId, type=${childComponent.type}")
                renderComponent(childComponent, onAction, onNavigate)
            } else {
                println("DEBUG renderRow: child=$childId NOT FOUND")
            }
        }
        inlineChildren?.forEach { inline ->
            renderInlineComponent(inline, onAction, onNavigate)
        }
    }
}

@Composable
fun renderText(
    component: ComponentConfig,
    onAction: (String, Map<String, Any>?) -> Unit
) {
    val props = component.properties ?: return
    val text = props.text?.literalString ?: ""
    val usageHint = props.usageHint
    val colorStr = props.color
    val bgColorStr = props.backgroundColor
    
    val textColor = if (colorStr != null) {
        try {
            Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(colorStr)))
        } catch (e: Exception) {
            Color.Black
        }
    } else {
        Color.Black
    }
    
    val backgroundColor = if (bgColorStr != null) {
        try {
            Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(bgColorStr)))
        } catch (e: Exception) {
            Color.Transparent
        }
    } else {
        Color.Transparent
    }
    
    val paddingValues = resolvePadding(props.padding)
    val marginValues = resolveMargin(null)
    val fontStyle = props.fontStyle
    val textAlign = props.textAlign
    
    Text(
        text = text,
        fontSize = when (usageHint) {
            "h1" -> 36.sp
            "h2" -> 28.sp
            "h3" -> 24.sp
            "h4" -> 22.sp
            "h5" -> 18.sp
            "h6" -> 16.sp
            "caption" -> 12.sp
            "overline" -> 11.sp
            "button" -> 14.sp
            "subtitle1" -> 16.sp
            "subtitle2" -> 14.sp
            "body1" -> 16.sp
            "body2" -> 14.sp
            "body" -> 16.sp
            else -> 16.sp
        },
        fontWeight = props.fontWeight?.let {
            when (it.lowercase()) {
                "bold" -> FontWeight.Bold
                "medium" -> FontWeight.Medium
                "light" -> FontWeight.Light
                "thin" -> FontWeight.Thin
                "black" -> FontWeight.Black
                "extraBold" -> FontWeight.ExtraBold
                "semiBold" -> FontWeight.SemiBold
                else -> FontWeight.Normal
            }
        } ?: when (usageHint) {
            "h1", "h2", "h3", "h4", "h5", "h6" -> FontWeight.Bold
            "button", "overline" -> FontWeight.Medium
            else -> FontWeight.Normal
        },
        fontStyle = when (fontStyle) {
            "italic" -> FontStyle.Italic
            else -> FontStyle.Normal
        },
        textAlign = when (textAlign) {
            "center" -> TextAlign.Center
            "end" -> TextAlign.End
            "right" -> TextAlign.End
            "justify" -> TextAlign.Justify
            else -> TextAlign.Start
        },
        color = textColor,
        modifier = Modifier
            .then(
                if (marginValues != null) Modifier.padding(marginValues) else Modifier
            )
            .then(
                if (backgroundColor != Color.Transparent) {
                    Modifier
                        .background(backgroundColor, RoundedCornerShape(12.dp))
                        .then(
                            if (paddingValues != null) {
                                Modifier.padding(paddingValues)
                            } else Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                } else if (paddingValues != null) {
                    Modifier.padding(paddingValues)
                } else Modifier
            )
            .then(
                if (component.action != null) {
                    Modifier.clickable {
                        onAction(component.action.event, component.action.context)
                    }
                } else Modifier
            )
    )
}

@Composable
fun renderIcon(
    component: ComponentConfig,
    onAction: (String, Map<String, Any>?) -> Unit
) {
    val props = component.properties ?: return
    val iconName = props.icon
    val usageHint = props.usageHint
    
    val tintColor = props.tintColor?.let { 
        try {
            val resolved = ConfigManager.resolveColor(it)
            Color(android.graphics.Color.parseColor(resolved))
        } catch (e: Exception) {
            null
        }
    }
    
    val size = when (usageHint) {
        "action" -> 56.dp
        "product" -> 44.dp
        "decorative" -> 28.dp
        "navigation" -> 28.dp
        "logo" -> 36.dp
        else -> 32.dp
    }
    
    val painter = IconManager.getIcon(iconName)
    
    if (painter == null) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "?", fontSize = 12.sp, color = Color.Gray)
        }
        return
    }
    
    val showCircle = usageHint == "action" || usageHint == "product"
    
    androidx.compose.foundation.Image(
        painter = painter,
        contentDescription = iconName,
        modifier = Modifier
            .then(
                if (showCircle) {
                    Modifier
                        .size(64.dp)
                        .background(Color.White, CircleShape)
                        .padding(8.dp)
                } else {
                    Modifier.size(size)
                }
            )
            .then(
                if (component.action != null) {
                    Modifier.clickable {
                        onAction(component.action.event, component.action.context)
                    }
                } else {
                    Modifier
                }
            ),
        colorFilter = if (tintColor != null) androidx.compose.ui.graphics.ColorFilter.tint(tintColor) else null
    )
}

@Composable
fun renderCard(
    component: ComponentConfig,
    onAction: (String, Map<String, Any>?) -> Unit,
    onNavigate: (String) -> Unit
) {
    val props = component.properties ?: return
    val childId = props.child
    
    val paddingValues = resolvePadding(props.padding)
    val marginValues = resolveMargin(null)
    val backgroundColor = props.backgroundColor?.let {
        try { Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(it))) } catch (e: Exception) { Color.White }
    } ?: Color.White
    val elevation = props.weight?.toFloat()?.dp ?: 2.dp
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (marginValues != null) Modifier.padding(marginValues) else Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ),
        elevation = CardDefaults.cardElevation(elevation),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (paddingValues != null) Modifier.padding(paddingValues) else Modifier.padding(16.dp)
                )
        ) {
            childId?.let { 
                val childComponent = ConfigManager.getComponent(childId)
                if (childComponent != null) {
                    renderComponent(childComponent, onAction, onNavigate)
                }
            }
        }
    }
}

@Composable
fun renderTabs(
    component: ComponentConfig,
    onAction: (String, Map<String, Any>?) -> Unit,
    onNavigate: (String) -> Unit
) {
    val props = component.properties ?: return
    val tabItems = props.tabItems ?: return
    
    var selectedTab by remember { mutableStateOf(0) }
    
    Column {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color.Black,
            divider = {}
        ) {
            tabItems.forEachIndexed { index, tab ->
                Tab(
                    selected = index == selectedTab,
                    onClick = { 
                        selectedTab = index
                        component.action?.let { action ->
                            onAction(action.event, mapOf("tab" to index, "title" to tab.title))
                        }
                    },
                    text = { 
                        Text(
                            tab.title,
                            color = if (index == selectedTab) Color(0xFFD32F2F) else Color.Gray,
                            fontWeight = if (index == selectedTab) FontWeight.Medium else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
        
        if (tabItems.isNotEmpty()) {
            val childComponent = ConfigManager.getComponent(tabItems[selectedTab].child)
            if (childComponent != null) {
                renderComponent(childComponent, onAction, onNavigate)
            }
        }
    }
}

@Composable
fun renderTextField(
    component: ComponentConfig
) {
    val props = component.properties ?: return
    val label = props.label?.literalString ?: ""
    
    OutlinedTextField(
        value = "",
        onValueChange = { },
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun renderDivider(
    component: ComponentConfig
) {
    val props = component.properties ?: return
    val color = props.color?.let {
        try { Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(it))) } catch (e: Exception) { null }
    } ?: Color(0xFFE0E0E0)
    val thickness = props.thickness?.toFloat()?.dp ?: 1.dp
    
    androidx.compose.material3.Divider(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        color = color,
        thickness = thickness
    )
}

@Composable
fun renderImage(
    component: ComponentConfig
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color(0xFFF5F5F5))
    ) {
        Text(
            text = "Image",
            modifier = Modifier.align(Alignment.Center),
            color = Color.Gray
        )
    }
}

@Composable
fun renderButton(
    component: ComponentConfig,
    onAction: (String, Map<String, Any>?) -> Unit
) {
    val props = component.properties ?: return
    val text = props.text?.literalString ?: ""
    val isPrimary = props.primary ?: true
    
    val backgroundColor = props.backgroundColor?.let {
        try { Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(it))) } catch (e: Exception) { null }
    } ?: if (isPrimary) Color(0xFFD32F2F) else Color.Transparent
    
    val contentColor = if (isPrimary) Color.White else Color(0xFFD32F2F)
    
    Button(
        onClick = {
            component.action?.let { action ->
                onAction(action.event, action.context)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}

@Composable
fun renderSpacer(
    component: ComponentConfig
) {
    val props = component.properties ?: return
    val height = props.weight?.toFloat()?.dp ?: 8.dp
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun renderBox(
    component: ComponentConfig,
    onAction: (String, Map<String, Any>?) -> Unit,
    onNavigate: (String) -> Unit
) {
    val props = component.properties ?: return
    val childId = props.child
    val backgroundColor = props.backgroundColor?.let {
        try { Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(it))) } catch (e: Exception) { null }
    }
    val paddingValues = resolvePadding(props.padding)
    val marginValues = resolveMargin(null)
    val shape = props.shape
    val width = props.width?.toFloat()?.dp
    val height = props.height?.toFloat()?.dp
    
    val cornerRadius = when (shape) {
        "circle" -> 1000.dp
        "rounded" -> 8.dp
        "square" -> 0.dp
        else -> 0.dp
    }
    
    val border = props.border
    val borderColor = border?.color?.let { 
        try { Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(it))) } catch (e: Exception) { null }
    }
    val borderWidth = border?.width?.toFloat()?.dp ?: 0.dp
    
    Box(
        modifier = Modifier
            .then(
                if (width != null) Modifier.width(width) else Modifier
            )
            .then(
                if (height != null) Modifier.height(height) else Modifier
            )
            .then(
                if (marginValues != null) Modifier.padding(marginValues) else Modifier
            )
            .then(
                when {
                    backgroundColor != null -> Modifier.background(backgroundColor, RoundedCornerShape(cornerRadius))
                    else -> Modifier
                }
            )
            .then(
                if (borderWidth > 0.dp && borderColor != null) {
                    Modifier.border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
                } else Modifier
            )
            .then(
                if (paddingValues != null) Modifier.padding(paddingValues) else Modifier
            )
    ) {
        childId?.let { 
            val childComponent = ConfigManager.getComponent(childId)
            if (childComponent != null) {
                renderComponent(childComponent, onAction, onNavigate)
            }
        }
    }
}

@Composable
fun renderBottomNavigation(
    component: ComponentConfig,
    onAction: (String, Map<String, Any>?) -> Unit,
    onNavigate: (String) -> Unit
) {
    val props = component.properties ?: return
    val tabItems = props.tabItems ?: return
    
    var selectedTab by remember { mutableStateOf(0) }
    
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        tabItems.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = index == selectedTab,
                onClick = {
                    selectedTab = index
                    component.action?.let { action ->
                        onAction(action.event, mapOf("tab" to index, "title" to tab.title))
                    }
                },
                icon = {
                    Icon(
                        painter = IconManager.getIcon(tab.title.lowercase()) ?: painterResource(android.R.drawable.ic_menu_help),
                        contentDescription = tab.title
                    )
                },
                label = { Text(tab.title, fontSize = 12.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFD32F2F),
                    selectedTextColor = Color(0xFFD32F2F),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun renderStatusBar(
    component: ComponentConfig
) {
    val props = component.properties ?: return
    val backgroundColor = props.backgroundColor?.let {
        try { Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(it))) } catch (e: Exception) { null }
    } ?: Color.Transparent
    
    val height = props.weight?.toFloat()?.dp ?: 24.dp
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(backgroundColor)
    )
}

@Composable
fun renderNestedSection(
    component: ComponentConfig,
    onAction: (String, Map<String, Any>?) -> Unit,
    onNavigate: (String) -> Unit
) {
    val props = component.properties ?: return
    val childId = props.child
    val children = props.children ?: component.children
    val inlineChildren = props.inlineChildren
    val paddingValues = resolvePadding(props.padding)
    val marginValues = resolveMargin(null)
    val shape = props.shape
    
    val cornerRadius = when (shape) {
        "none" -> 0.dp
        "circle" -> 1000.dp
        "small" -> 8.dp
        "medium" -> 12.dp
        "large" -> 24.dp
        else -> 16.dp
    }
    
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .then(
                if (marginValues != null) Modifier.padding(marginValues) else Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
            .fillMaxWidth()
            .wrapContentHeight()
            .background(Color.White, RoundedCornerShape(cornerRadius))
            .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(cornerRadius))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .then(
                    if (paddingValues != null) Modifier.padding(paddingValues) else Modifier.padding(16.dp)
                )
        ) {
            // Render child component
            childId?.let { 
                val childComponent = ConfigManager.getComponent(childId)
                if (childComponent != null) {
                    renderComponent(childComponent, onAction, onNavigate)
                }
            }
            // Render referenced children
            children?.forEach { childId ->
                val childComponent = ConfigManager.getComponent(childId)
                if (childComponent != null) {
                    renderComponent(childComponent, onAction, onNavigate)
                }
            }
            // Render inline children
            inlineChildren?.forEach { inline ->
                renderInlineComponent(inline, onAction, onNavigate)
            }
        }
    }
}

@Composable
fun renderListDivider(
    component: ComponentConfig
) {
    val props = component.properties ?: return
    val color = props.color?.let {
        try { Color(android.graphics.Color.parseColor(ConfigManager.resolveColor(it))) } catch (e: Exception) { null }
    } ?: Color(0xFFE8E8E8)
    val thickness = props.thickness?.toFloat()?.dp ?: 0.5.dp
    val startIndent = props.indentStart ?: props.padding?.let { ConfigManager.resolveSpacing(it.all ?: "0") } ?: 56
    val endIndent = props.indentEnd ?: props.padding?.let { ConfigManager.resolveSpacing(it.end ?: it.all ?: "0") } ?: 0
    
    Divider(
        modifier = Modifier.padding(start = startIndent.dp, end = endIndent.dp),
        color = color,
        thickness = thickness
    )
}

private fun resolvePadding(paddingConfig: com.a2ui.renderer.config.PaddingConfig?): PaddingValues? {
    if (paddingConfig == null) return null
    return PaddingValues(
        start = ConfigManager.resolveSpacing(paddingConfig.start ?: paddingConfig.all ?: "0").dp,
        top = ConfigManager.resolveSpacing(paddingConfig.top ?: paddingConfig.all ?: "0").dp,
        end = ConfigManager.resolveSpacing(paddingConfig.end ?: paddingConfig.all ?: "0").dp,
        bottom = ConfigManager.resolveSpacing(paddingConfig.bottom ?: paddingConfig.all ?: "0").dp
    )
}

private fun resolveMargin(marginConfig: com.a2ui.renderer.config.MarginConfig?): PaddingValues? {
    if (marginConfig == null) return null
    return PaddingValues(
        start = ConfigManager.resolveSpacing(marginConfig.start ?: marginConfig.all ?: "0").dp,
        top = ConfigManager.resolveSpacing(marginConfig.top ?: marginConfig.all ?: "0").dp,
        end = ConfigManager.resolveSpacing(marginConfig.end ?: marginConfig.all ?: "0").dp,
        bottom = ConfigManager.resolveSpacing(marginConfig.bottom ?: marginConfig.all ?: "0").dp
    )
}
