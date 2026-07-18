package com.kawaiical.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kawaiical.app.data.prefs.ThemeMode
import com.kawaiical.app.data.prefs.Units
import com.kawaiical.app.ui.components.Mascot
import com.kawaiical.app.ui.components.MascotMood
import com.kawaiical.app.ui.components.PillSelector
import com.kawaiical.app.ui.components.RollingNumber
import com.kawaiical.app.ui.home.KawaiiCard
import com.kawaiical.app.ui.home.StaggerIn
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory),
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()

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
                Text("Settings 🎀", style = MaterialTheme.typography.headlineSmall)
            }

            Spacer(Modifier.height(12.dp))

            StaggerIn(index = 0) {
                KawaiiCard {
                    Text("Daily calorie goal", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))

                    // Local slider state so dragging feels instant; persisted on release
                    var sliderValue by remember { mutableFloatStateOf(prefs.calorieGoal.toFloat()) }
                    LaunchedEffect(prefs.calorieGoal) {
                        sliderValue = prefs.calorieGoal.toFloat()
                    }

                    Row(verticalAlignment = Alignment.Bottom) {
                        RollingNumber(
                            value = (sliderValue / 50f).roundToInt() * 50,
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            " kcal",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = {
                            viewModel.setCalorieGoal((sliderValue / 50f).roundToInt() * 50)
                        },
                        valueRange = 1200f..4000f,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            StaggerIn(index = 1) {
                KawaiiCard {
                    Text("Water goal", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(10.dp))
                    PillSelector(
                        options = listOf(6, 8, 10, 12),
                        selected = prefs.waterGoal,
                        onSelect = viewModel::setWaterGoal,
                        label = { "$it" },
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "glasses per day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            StaggerIn(index = 2) {
                KawaiiCard {
                    Text("Appearance", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(10.dp))
                    PillSelector(
                        options = ThemeMode.entries.toList(),
                        selected = prefs.themeMode,
                        onSelect = viewModel::setThemeMode,
                        label = {
                            when (it) {
                                ThemeMode.SYSTEM -> "Auto"
                                ThemeMode.LIGHT -> "☀️ Light"
                                ThemeMode.DARK -> "🌙 Dark"
                            }
                        },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            StaggerIn(index = 3) {
                KawaiiCard {
                    Text("Units", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(10.dp))
                    PillSelector(
                        options = Units.entries.toList(),
                        selected = prefs.units,
                        onSelect = viewModel::setUnits,
                        label = { if (it == Units.METRIC) "Metric (g, ml)" else "Imperial (oz)" },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            StaggerIn(index = 4) {
                KawaiiCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Mascot(mood = MascotMood.HAPPY, modifier = Modifier.size(90.dp))
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Mochi says hi!", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Be kind to yourself — calories are just data, " +
                                    "and you're doing amazing 💕",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
