package com.a2ui.renderer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a2ui.renderer.config.ConfigManager
import com.a2ui.renderer.ui.theme.A2UIRendererTheme
import com.a2ui.renderer.ui.NavigationHost
import com.a2ui.renderer.ui.components.ThemeToggleButton

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ConfigManager (loads themes, settings, etc.)
        ConfigManager.init(this)
        
        setContent {
            A2UIRendererTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Top bar with theme toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            ThemeToggleButton()
                        }
                        
                        // Main content
                        NavigationHost()
                    }
                }
            }
        }
    }
}
