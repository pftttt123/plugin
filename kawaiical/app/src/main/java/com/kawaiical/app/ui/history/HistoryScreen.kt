package com.kawaiical.app.ui.history

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kawaiical.app.ui.components.RollingNumber
import com.kawaiical.app.ui.components.WeeklyLineChart
import com.kawaiical.app.ui.home.KawaiiCard
import com.kawaiical.app.ui.home.StaggerIn
import com.kawaiical.app.ui.theme.BabyBlue
import com.kawaiical.app.ui.theme.PastelPink
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Text("Your week 💗", style = MaterialTheme.typography.headlineSmall)
            }

            Spacer(Modifier.height(12.dp))

            StaggerIn(index = 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatChip("Streak", state.streak, "days", Modifier.weight(1f))
                    StatChip("Average", state.average, "kcal", Modifier.weight(1f))
                    StatChip("On target", state.best, "days", Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(16.dp))

            StaggerIn(index = 1) {
                KawaiiCard {
                    Text("Calories this week", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(18.dp))
                    if (state.loaded) {
                        WeeklyLineChart(
                            values = state.days.map { it.calories },
                            goal = state.calorieGoal,
                            labels = state.days.map { day ->
                                LocalDate.ofEpochDay(day.epochDay)
                                    .dayOfWeek
                                    .getDisplayName(JavaTextStyle.SHORT, Locale.getDefault())
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            StaggerIn(index = 2) {
                KawaiiCard {
                    Text("Day by day", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    state.days.asReversed().forEach { day ->
                        val date = LocalDate.ofEpochDay(day.epochDay)
                        val fraction =
                            (day.calories.toFloat() / state.calorieGoal.coerceAtLeast(1))
                                .coerceIn(0f, 1f)
                        val animatedFraction by animateFloatAsState(
                            targetValue = fraction,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow,
                            ),
                            label = "dayFill",
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                        ) {
                            Text(
                                date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.getDefault()),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(44.dp),
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedFraction.coerceAtLeast(0.001f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(PastelPink, BabyBlue)
                                            )
                                        ),
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "${day.calories}",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (day.calories > state.calorieGoal) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.width(48.dp),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatChip(label: String, value: Int, unit: String, modifier: Modifier = Modifier) {
    KStatCard(modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(verticalAlignment = Alignment.Bottom) {
            RollingNumber(
                value = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.width(3.dp))
            Text(
                unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
    }
}

@Composable
private fun KStatCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        content = content,
    )
}
