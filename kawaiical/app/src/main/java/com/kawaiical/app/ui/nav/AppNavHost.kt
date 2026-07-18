package com.kawaiical.app.ui.nav

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kawaiical.app.data.db.MealType
import com.kawaiical.app.ui.history.HistoryScreen
import com.kawaiical.app.ui.home.HomeScreen
import com.kawaiical.app.ui.log.LogFoodScreen
import com.kawaiical.app.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val LOG = "log/{mealType}"
    const val HISTORY = "history"
    const val SETTINGS = "settings"

    fun log(meal: MealType) = "log/${meal.name}"
}

private val NavSpring = spring<IntOffset>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = {
            slideInHorizontally(NavSpring, initialOffsetX = { it / 3 }) + fadeIn(tween(220))
        },
        exitTransition = {
            slideOutHorizontally(NavSpring, targetOffsetX = { -it / 4 }) + fadeOut(tween(180))
        },
        popEnterTransition = {
            slideInHorizontally(NavSpring, initialOffsetX = { -it / 3 }) + fadeIn(tween(220))
        },
        popExitTransition = {
            slideOutHorizontally(NavSpring, targetOffsetX = { it / 4 }) + fadeOut(tween(180))
        },
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddFood = { meal -> navController.navigate(Routes.log(meal)) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(
            route = Routes.LOG,
            arguments = listOf(navArgument("mealType") { type = NavType.StringType }),
        ) { backStackEntry ->
            val meal = backStackEntry.arguments
                ?.getString("mealType")
                ?.let { runCatching { MealType.valueOf(it) }.getOrNull() }
                ?: MealType.SNACK
            LogFoodScreen(
                initialMeal = meal,
                onDone = { navController.navigateUp() },
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(onBack = { navController.navigateUp() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.navigateUp() })
        }
    }
}
