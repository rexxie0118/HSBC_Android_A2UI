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
import com.a2ui.renderer.renderer.renderPage

sealed class Screen(val route: String) {
    object Homepage : Screen("homepage")
    object Wealth : Screen("wealth")
    // Dynamic pages: {journeyId}/{pageId}
}

@Composable
fun NavigationHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // Initialize ConfigManager
    LaunchedEffect(Unit) {
        ConfigManager.init(context)
    }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Homepage.route
    ) {
        // Homepage
        composable(Screen.Homepage.route) {
            val page = ConfigManager.getPage("banking_journey", "homepage")
            if (page != null) {
                GenericPage(
                    page = page,
                    navController = navController,
                    journeyId = "banking_journey"
                )
            }
        }
        
        // Wealth Page
        composable(Screen.Wealth.route) {
            val page = ConfigManager.getPage("banking_journey", "wealth_page")
            GenericPage(
                page = page,
                navController = navController,
                journeyId = "banking_journey"
            )
        }
        
        // Dynamic route for ANY journey/page
        composable("{journeyId}/{pageId}") { backStackEntry ->
            val journeyId = backStackEntry.arguments?.getString("journeyId") ?: "banking_journey"
            val pageId = backStackEntry.arguments?.getString("pageId") ?: return@composable
            
            android.util.Log.i("NavigationHost", "Getting page: journeyId=$journeyId, pageId=$pageId")
            android.util.Log.i("NavigationHost", "=== ROUTE HIT === journeyId=$journeyId pageId=$pageId")
            val page = ConfigManager.getPage(journeyId, pageId)
            android.util.Log.i("NavigationHost", "Page result: ${if (page != null) "FOUND ${page.id}" else "NULL - pages map has: " + com.a2ui.renderer.config.ConfigManager.getAllPageIds()}")
            if (page == null) {
                android.util.Log.e("NavigationHost", "PAGE NOT FOUND! journeyId=$journeyId pageId=$pageId")
            }
            GenericPage(
                page = page,
                navController = navController,
                journeyId = journeyId
            )
        }
    }
}

/**
 * Generic page renderer - feature agnostic
 * Navigation comes from JSON config, not hardcoded
 */
@Composable
fun GenericPage(
    page: PageConfig?,
    navController: NavHostController,
    journeyId: String
) {
    if (page != null) {
        renderPage(
            page = page,
            onAction = { event, data ->
                android.util.Log.i("GenericPage", "=== ACTION RECEIVED === event=$event")
                when {
                    event.startsWith("navigate:") -> {
                        val parts = event.substringAfter("navigate:").split(":")
                        val (navJourneyId, pageId) = if (parts.size == 2) {
                            parts[0] to parts[1]
                        } else {
                            journeyId to parts[0]
                        }
                        navController.navigate("$navJourneyId/$pageId")
                    }
                    event == "navigate_back" -> navController.popBackStack()
                    event == "navigate_home" -> navController.navigate(Screen.Homepage.route) {
                        popUpTo(0) { inclusive = true }
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
