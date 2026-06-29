package com.carspotter.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.carspotter.ui.CatalogViewModel
import com.carspotter.ui.catalog.CatalogScreen
import com.carspotter.ui.detail.CarDetailScreen
import com.carspotter.ui.progress.ProgressScreen

private sealed class TopLevel(val route: String, val label: String, val icon: ImageVector) {
    data object Catalog : TopLevel("catalog", "Catalog", Icons.AutoMirrored.Filled.List)
    data object Progress : TopLevel("progress", "Progress", Icons.Filled.CheckCircle)
}

private val topLevelScreens = listOf(TopLevel.Catalog, TopLevel.Progress)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarSpotterNavGraph(viewModel: CatalogViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTopLevel = topLevelScreens.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            if (isTopLevel) {
                TopAppBar(title = { Text("Car Spotter") })
            }
        },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    topLevelScreens.forEach { screen ->
                        val selected = backStackEntry?.destination?.hierarchy
                            ?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TopLevel.Catalog.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(TopLevel.Catalog.route) {
                CatalogScreen(
                    viewModel = viewModel,
                    onCarClick = { navController.navigate("detail/${it.id}") },
                )
            }
            composable(TopLevel.Progress.route) {
                ProgressScreen(
                    viewModel = viewModel,
                    onCarClick = { navController.navigate("detail/${it.id}") },
                )
            }
            composable(
                route = "detail/{carId}",
                arguments = listOf(navArgument("carId") { type = NavType.IntType }),
            ) { entry ->
                val carId = entry.arguments?.getInt("carId") ?: -1
                CarDetailScreen(
                    viewModel = viewModel,
                    carId = carId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
