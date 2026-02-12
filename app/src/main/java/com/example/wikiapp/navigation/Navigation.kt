package com.example.wikiapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wikiapp.ui.edit.EditScreen
import com.example.wikiapp.ui.entity.EntityDetailScreen
import com.example.wikiapp.ui.home.HomeScreen
import com.example.wikiapp.ui.login.LoginScreen
import com.example.wikiapp.ui.search.SearchScreen
import com.example.wikiapp.ui.viewmodel.AuthViewModel
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    object Home : Screen("home")
    
    object Search : Screen("search?query={query}") {
        const val baseRoute = "search"
        fun createRouteWithQuery(query: String) = "search?query=$query"
    }
    
    object EntityDetail : Screen("entity/{entityId}") {
        fun createRoute(entityId: String) = "entity/$entityId"
    }
    
    object Login : Screen("login?returnTo={returnTo}") {
        const val baseRoute = "login"
        fun createRoute(returnTo: String = "") = "login?returnTo=$returnTo"
    }
    
    object Edit : Screen("edit/{entityId}?label={label}&propertyId={propertyId}&propertyLabel={propertyLabel}&currentValue={currentValue}") {
        const val baseRoute = "edit"
        fun createRoute(
            entityId: String,
            entityLabel: String,
            propertyId: String? = null,
            propertyLabel: String? = null,
            currentValue: String? = null
        ): String {
            val encodedLabel = URLEncoder.encode(entityLabel, "UTF-8")
            val encodedPropertyLabel = propertyLabel?.let { URLEncoder.encode(it, "UTF-8") } ?: ""
            val encodedCurrentValue = currentValue?.let { URLEncoder.encode(it, "UTF-8") } ?: ""
            return "edit/$entityId?label=$encodedLabel&propertyId=${propertyId ?: ""}&propertyLabel=$encodedPropertyLabel&currentValue=$encodedCurrentValue"
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    // Shared AuthViewModel across all screens
    val authViewModel: AuthViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                onNavigationItemClick = { route ->
                    when (route) {
                        "search" -> navController.navigate(Screen.Search.baseRoute)
                        "home" -> { /* Already on home */ }
                        else -> { /* Handle other routes */ }
                    }
                }
            )
        }
        
        composable(
            route = Screen.Search.route,
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val initialQuery = backStackEntry.arguments?.getString("query") ?: ""
            SearchScreen(
                initialQuery = initialQuery,
                onEntityClick = { entityId ->
                    navController.navigate(Screen.EntityDetail.createRoute(entityId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.EntityDetail.route,
            arguments = listOf(
                navArgument("entityId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val entityId = backStackEntry.arguments?.getString("entityId") ?: ""
            EntityDetailScreen(
                entityId = entityId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEntityClick = { id ->
                    navController.navigate(Screen.EntityDetail.createRoute(id))
                },
                onEditClick = {
                    // Navigate to login if not logged in
                    navController.navigate(Screen.Login.createRoute(entityId))
                },
                onNavigateToEdit = { eId, eLabel, propId, propLabel, currentVal ->
                    navController.navigate(
                        Screen.Edit.createRoute(eId, eLabel, propId, propLabel, currentVal)
                    )
                },
                authViewModel = authViewModel
            )
        }
        
        composable(
            route = Screen.Login.route,
            arguments = listOf(
                navArgument("returnTo") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val returnTo = backStackEntry.arguments?.getString("returnTo") ?: ""
            LoginScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLoginSuccess = {
                    // Go back to the entity page after login
                    navController.popBackStack()
                },
                authViewModel = authViewModel
            )
        }
        
        composable(
            route = Screen.Edit.route,
            arguments = listOf(
                navArgument("entityId") { type = NavType.StringType },
                navArgument("label") { 
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("propertyId") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("propertyLabel") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                },
                navArgument("currentValue") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val entityId = backStackEntry.arguments?.getString("entityId") ?: ""
            val label = backStackEntry.arguments?.getString("label")?.let { 
                try { URLDecoder.decode(it, "UTF-8") } catch (e: Exception) { it }
            } ?: entityId
            val propertyId = backStackEntry.arguments?.getString("propertyId")?.takeIf { it.isNotBlank() }
            val propertyLabel = backStackEntry.arguments?.getString("propertyLabel")?.let { 
                try { URLDecoder.decode(it, "UTF-8") } catch (e: Exception) { it }
            }?.takeIf { it.isNotBlank() }
            val currentValue = backStackEntry.arguments?.getString("currentValue")?.let { 
                try { URLDecoder.decode(it, "UTF-8") } catch (e: Exception) { it }
            }?.takeIf { it.isNotBlank() }
            
            EditScreen(
                entityId = entityId,
                entityLabel = label,
                propertyId = propertyId,
                propertyLabel = propertyLabel,
                currentValue = currentValue,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSave = { newValue ->
                    // In a real app, this would save to the Wikidata API
                }
            )
        }
    }
}
