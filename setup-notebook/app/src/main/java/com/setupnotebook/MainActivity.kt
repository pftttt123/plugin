package com.setupnotebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.setupnotebook.ui.AppViewModel
import com.setupnotebook.ui.components.SpeedLinesSplash
import com.setupnotebook.ui.screens.CompareScreen
import com.setupnotebook.ui.screens.GarageScreen
import com.setupnotebook.ui.screens.SetupEditorScreen
import com.setupnotebook.ui.screens.SetupsScreen
import com.setupnotebook.ui.screens.TracksScreen
import com.setupnotebook.ui.theme.SetupNotebookTheme
import com.setupnotebook.ui.theme.TextSecondary
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SetupNotebookTheme {
                App()
            }
        }
    }
}

object Routes {
    const val Garage = "garage"
    const val Tracks = "tracks"
    const val Setups = "setups"
    const val Compare = "compare"
    const val Editor = "editor/{setupId}"
    fun editor(id: Long) = "editor/$id"
}

private val tabOrder = listOf(Routes.Garage, Routes.Tracks, Routes.Setups, Routes.Compare)

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.Garage, "Garage", Icons.Default.DirectionsCar),
    Tab(Routes.Tracks, "Tracks", Icons.Default.Flag),
    Tab(Routes.Setups, "Setups", Icons.Default.Tune),
    Tab(Routes.Compare, "Compare", Icons.Default.CompareArrows),
)

@Composable
fun App(vm: AppViewModel = viewModel(factory = AppViewModel.Factory)) {
    var splashVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1700)
        splashVisible = false
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        MainScaffold(vm)
        AnimatedVisibility(
            visible = splashVisible,
            exit = fadeOut(tween(500)),
        ) {
            SpeedLinesSplash()
        }
    }
}

/** Direction of travel between bottom-nav tabs: -1 left, +1 right, 0 for non-tab routes. */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabDirection(): Int {
    val from = tabOrder.indexOf(initialState.destination.route)
    val to = tabOrder.indexOf(targetState.destination.route)
    return if (from == -1 || to == -1) 0 else Integer.signum(to - from)
}

private val slideSpring = spring(
    dampingRatio = 0.85f,
    stiffness = 420f,
    visibilityThreshold = IntOffset.VisibilityThreshold,
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabEnter(): EnterTransition {
    val dir = tabDirection()
    return if (dir == 0) fadeIn(tween(220))
    else slideInHorizontally(slideSpring) { dir * it / 4 } + fadeIn(tween(220))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.tabExit(): ExitTransition {
    val dir = tabDirection()
    return if (dir == 0) fadeOut(tween(180))
    else slideOutHorizontally(slideSpring) { -dir * it / 4 } + fadeOut(tween(180))
}

@Composable
private fun MainScaffold(vm: AppViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            AnimatedVisibility(
                visible = currentRoute != Routes.Editor,
                enter = slideInVertically(tween(250)) { it } + fadeIn(tween(250)),
                exit = slideOutVertically(tween(250)) { it } + fadeOut(tween(200)),
            ) {
                NavigationBar(containerColor = Color(0xFF0A0A0A)) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = Color(0xFF2A0C0C),
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Garage,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            enterTransition = { tabEnter() },
            exitTransition = { tabExit() },
            popEnterTransition = { tabEnter() },
            popExitTransition = { tabExit() },
        ) {
            composable(Routes.Garage) { GarageScreen(vm) }
            composable(Routes.Tracks) { TracksScreen(vm) }
            composable(Routes.Setups) {
                SetupsScreen(vm) { id -> navController.navigate(Routes.editor(id)) }
            }
            composable(Routes.Compare) { CompareScreen(vm) }
            composable(
                route = Routes.Editor,
                arguments = listOf(navArgument("setupId") { type = NavType.LongType }),
                enterTransition = {
                    slideInVertically(
                        spring(
                            dampingRatio = 0.8f,
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntOffset.VisibilityThreshold,
                        ),
                    ) { it / 3 } + fadeIn(tween(250))
                },
                popExitTransition = {
                    slideOutVertically(tween(260)) { it / 3 } + fadeOut(tween(220))
                },
            ) { entry ->
                val setupId = entry.arguments?.getLong("setupId") ?: return@composable
                SetupEditorScreen(vm, setupId) { navController.popBackStack() }
            }
        }
    }
}
