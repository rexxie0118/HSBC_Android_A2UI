package com.a2ui.renderer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.a2ui.renderer.config.ConfigManager
import com.a2ui.renderer.config.PageConfig
import com.a2ui.renderer.renderer.PageRenderer
import com.a2ui.renderer.renderer.A2UIRenderer

sealed class Screen(val route: String) {
    object Homepage : Screen("homepage")
    object Wealth : Screen("wealth")
}

@Composable
fun NavigationHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var isInitialized by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        ConfigManager.init(context)
        isInitialized = true
    }
    
    if (!isInitialized) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Homepage.route
    ) {
        composable(Screen.Homepage.route) {
            val page = ConfigManager.getPage("banking_journey", "homepage")
            if (page != null) {
                ConfigDrivenPage(
                    page = page,
                    onAction = { event, data ->
                        when (event) {
                            "navigate_wealth" -> {
                                navController.navigate(Screen.Wealth.route)
                            }
                        }
                    },
                    onNavigate = { route ->
                        navController.navigate(route)
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Page not found")
                }
            }
        }
        
        composable(Screen.Wealth.route) {
            val page = ConfigManager.getPage("banking_journey", "wealth_page")
            if (page != null) {
                ConfigDrivenPage(
                    page = page,
                    onAction = { event, data ->
                        when (event) {
                            "navigate_home" -> {
                                navController.navigate(Screen.Homepage.route) {
                                    popUpTo(Screen.Wealth.route) { inclusive = true }
                                }
                            }
                        }
                    },
                    onNavigate = { route ->
                        navController.navigate(route)
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Wealth Page - Coming Soon")
                }
            }
        }
    }
}

@Composable
fun ConfigDrivenPage(
    page: PageConfig,
    onAction: (String, Map<String, Any>?) -> Unit,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (page.statusBar.visible) {
            StatusBar(
                style = page.statusBar.style,
                backgroundColor = ConfigManager.resolveColor(page.statusBar.backgroundColor)
            )
        }
        
        PageRenderer(
            page = page,
            onAction = onAction,
            onNavigate = onNavigate,
            modifier = Modifier.weight(1f)
        )
        
        if (page.navigationBar.visible) {
            NavigationBarView(
                style = page.navigationBar.style,
                backgroundColor = ConfigManager.resolveColor(page.navigationBar.backgroundColor)
            )
        }
    }
}

@Composable
fun StatusBar(
    style: String,
    backgroundColor: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Color(android.graphics.Color.parseColor(backgroundColor)))
    )
}

@Composable
fun NavigationBarView(
    style: String,
    backgroundColor: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(android.graphics.Color.parseColor(backgroundColor)))
    )
}
