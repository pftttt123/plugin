package com.kawaiical.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kawaiical.app.data.db.DiaryEntry
import com.kawaiical.app.data.db.MealType
import com.kawaiical.app.data.prefs.Units
import com.kawaiical.app.ui.components.CalorieRing
import com.kawaiical.app.ui.components.ConfettiOverlay
import com.kawaiical.app.ui.components.MacroBreakdown
import com.kawaiical.app.ui.components.Mascot
import com.kawaiical.app.ui.components.MascotMood
import com.kawaiical.app.ui.components.RollingNumber
import com.kawaiical.app.ui.components.WaterGlass
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val StreakMilestones = setOf(3, 7, 14, 21, 30, 50, 100)

@Composable
fun HomeScreen(
    onAddFood: (MealType) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // --- Mascot mood: cheer briefly after a log, sleepy at night ---
    var cheering by remember { mutableStateOf(false) }
    LaunchedEffect(state.entries.size) {
        val lastLog = state.entries.maxOfOrNull { it.loggedAt } ?: return@LaunchedEffect
        if (System.currentTimeMillis() - lastLog < 5_000) {
            cheering = true
            delay(3_500)
            cheering = false
        }
    }
    val hour = LocalTime.now().hour
    val mood = when {
        cheering -> MascotMood.CHEER
        hour >= 22 || hour < 6 -> MascotMood.SLEEPY
        state.goalReached -> MascotMood.HAPPY
        state.entries.isEmpty() -> MascotMood.NEUTRAL
        else -> MascotMood.HAPPY
    }

    // --- Confetti: on goal completion and on streak milestones ---
    var burst by remember { mutableIntStateOf(0) }
    var prevGoalReached by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(state.goalReached, state.loaded) {
        if (!state.loaded) return@LaunchedEffect
        if (prevGoalReached == false && state.goalReached) burst++
        prevGoalReached = state.goalReached
    }
    var prevStreak by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(state.streak, state.loaded) {
        if (!state.loaded) return@LaunchedEffect
        val prev = prevStreak
        if (prev != null && state.streak > prev && state.streak in StreakMilestones) burst++
        prevStreak = state.streak
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddFood(suggestedMeal(hour)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(50),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Log food", style = MaterialTheme.typography.labelLarge)
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                        bottom = padding.calculateBottomPadding() + 96.dp,
                    ),
            ) {
                StaggerIn(index = 0) {
                    Header(
                        streak = state.streak,
                        onOpenHistory = onOpenHistory,
                        onOpenSettings = onOpenSettings,
                    )
                }

                Spacer(Modifier.height(16.dp))

                StaggerIn(index = 1) {
                    MascotRow(mood = mood, state = state)
                }

                Spacer(Modifier.height(20.dp))

                StaggerIn(index = 2) {
                    CalorieCard(state = state)
                }

                Spacer(Modifier.height(16.dp))

                StaggerIn(index = 3) {
                    KawaiiCard {
                        Text("Macros", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(14.dp))
                        MacroBreakdown(
                            protein = state.protein,
                            carbs = state.carbs,
                            fat = state.fat,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                StaggerIn(index = 4) {
                    WaterCard(
                        glasses = state.waterGlasses,
                        goal = state.prefs.waterGoal,
                        units = state.prefs.units,
                        onAdd = { viewModel.addWater(1) },
                        onRemove = { viewModel.addWater(-1) },
                    )
                }

                Spacer(Modifier.height(16.dp))

                MealType.entries.forEachIndexed { i, meal ->
                    StaggerIn(index = 5 + i) {
                        MealSection(
                            meal = meal,
                            entries = state.byMeal[meal].orEmpty(),
                            onAdd = { onAddFood(meal) },
                            onDelete = viewModel::deleteEntry,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                }
            }

            ConfettiOverlay(burstKey = burst, modifier = Modifier.fillMaxSize())
        }
    }
}

/** Fade + slide-up entrance with a per-section stagger, used on first load. */
@Composable
fun StaggerIn(index: Int, content: @Composable () -> Unit) {
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
    val delay = index * 70
    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn(tween(420, delayMillis = delay, easing = FastOutSlowInEasing)) +
            slideInVertically(
                tween(460, delayMillis = delay, easing = FastOutSlowInEasing),
                initialOffsetY = { it / 3 },
            ),
    ) {
        content()
    }
}

@Composable
private fun Header(streak: Int, onOpenHistory: () -> Unit, onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                greeting(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                LocalDate.now().format(
                    DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (streak > 0) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    "🔥 $streak",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        FilledIconButton(
            onClick = onOpenHistory,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        ) {
            Icon(Icons.Rounded.BarChart, contentDescription = "History")
        }
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = onOpenSettings,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
            Icon(Icons.Rounded.Settings, contentDescription = "Settings")
        }
    }
}

@Composable
private fun MascotRow(mood: MascotMood, state: HomeUiState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Mascot(mood = mood, modifier = Modifier.size(120.dp))
        Spacer(Modifier.width(12.dp))
        Card(
            shape = RoundedCornerShape(
                topStart = 6.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp,
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = speechBubble(mood, state),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun CalorieCard(state: HomeUiState) {
    KawaiiCard {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            CalorieRing(
                progress = if (state.prefs.calorieGoal > 0) {
                    state.totalCalories.toFloat() / state.prefs.calorieGoal
                } else 0f,
                overGoal = state.totalCalories > state.prefs.calorieGoal,
                modifier = Modifier.size(250.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RollingNumber(
                        value = state.totalCalories,
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "of ${state.prefs.calorieGoal} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (state.totalCalories > state.prefs.calorieGoal) {
                            "+${state.totalCalories - state.prefs.calorieGoal} over 💛"
                        } else {
                            "${state.remaining} left ✨"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun WaterCard(
    glasses: Int,
    goal: Int,
    units: Units,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
) {
    KawaiiCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WaterGlass(
                glasses = glasses,
                goal = goal,
                modifier = Modifier.size(width = 64.dp, height = 80.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Water", style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = Alignment.Bottom) {
                    RollingNumber(
                        value = glasses,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        " / $goal glasses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    if (units == Units.METRIC) "250 ml each" else "8 fl oz each",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledIconButton(
                onClick = onRemove,
                enabled = glasses > 0,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Icon(Icons.Rounded.Remove, contentDescription = "Remove glass")
            }
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = onAdd,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add glass")
            }
        }
    }
}

@Composable
private fun MealSection(
    meal: MealType,
    entries: List<DiaryEntry>,
    onAdd: () -> Unit,
    onDelete: (DiaryEntry) -> Unit,
) {
    KawaiiCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${meal.emoji} ${meal.label}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
            if (entries.isNotEmpty()) {
                Text(
                    "${entries.sumOf { it.calories }} kcal",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(10.dp))
            }
            FilledIconButton(
                onClick = onAdd,
                modifier = Modifier.size(34.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add to ${meal.label}")
            }
        }
        if (entries.isEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Nothing logged yet~",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Spacer(Modifier.height(8.dp))
            entries.forEach { entry ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(entry.foodName, style = MaterialTheme.typography.bodyLarge)
                        if (entry.servings != 1f) {
                            Text(
                                "×${trimServings(entry.servings)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        "${entry.calories} kcal",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    IconButton(onClick = { onDelete(entry) }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Delete ${entry.foodName}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Shared soft, rounded card with a gentle drop shadow. */
@Composable
fun KawaiiCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(Modifier.padding(20.dp), content = content)
    }
}

private fun greeting(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 6 -> "Up late, cutie? 🌙"
        hour < 12 -> "Good morning! 🌸"
        hour < 18 -> "Good afternoon! 💖"
        hour < 22 -> "Good evening! ✨"
        else -> "Sleepy time soon 💤"
    }
}

private fun suggestedMeal(hour: Int): MealType = when (hour) {
    in 5..10 -> MealType.BREAKFAST
    in 11..14 -> MealType.LUNCH
    in 17..21 -> MealType.DINNER
    else -> MealType.SNACK
}

private fun speechBubble(mood: MascotMood, state: HomeUiState): String = when {
    mood == MascotMood.CHEER -> "Yayy, logged it! Nom nom~ 🎀"
    mood == MascotMood.SLEEPY -> "So sleepy… proud of you today 💤"
    state.totalCalories > state.prefs.calorieGoal ->
        "A lil extra today, and that's okay! Balance, bestie 💕"
    state.goalReached -> "Goal complete!! You're amazing!! 💖🎉"
    state.entries.isEmpty() -> "Log your first bite of the day — I believe in you! 🌸"
    else -> "You're doing great! ${state.remaining} kcal to go~ ✨"
}

private fun trimServings(value: Float): String =
    if (value == value.toLong().toFloat()) value.toLong().toString() else "%.1f".format(value)
