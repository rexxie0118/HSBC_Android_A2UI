package com.a2ui.renderer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Check
import com.a2ui.renderer.config.ComponentConfig

/**
 * Dropdown component with bottom sheet support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun renderDropdown(
    component: ComponentConfig,
    onAction: (String, Map<String, Any>?) -> Unit,
    onNavigate: (String) -> Unit
) {
    val props = component.properties ?: return
    val options = props.options ?: emptyList()
    val placeholder = props.placeholder?.literalString ?: "Select"
    val displayMode = props.displayMode ?: "inline"
    val label = props.label?.literalString
    
    var expanded by remember { mutableStateOf(false) }
    var selectedValue by remember { mutableStateOf<String?>(null) }
    var selectedLabel by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Label
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        // Dropdown button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    if (displayMode == "bottomSheet") {
                        expanded = true
                    }
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedLabel ?: placeholder,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selectedValue != null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Close dropdown" else "Open dropdown",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Bottom divider
        Divider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
        
        // Bottom sheet for options
        if (expanded && displayMode == "bottomSheet") {
            ModalBottomSheet(
                onDismissRequest = { expanded = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = "Select an option",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                
                LazyColumn {
                    items(options) { option ->
                        val optionLabel = option["label"] as? String ?: ""
                        val optionValue = option["value"] as? String ?: ""
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedValue = optionValue
                                    selectedLabel = optionLabel
                                    expanded = false
                                    
                                    // Trigger action if defined
                                    component.action?.let { action ->
                                        onAction(action.event, action.context)
                                    }
                                }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = optionLabel,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            if (selectedValue == optionValue) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
