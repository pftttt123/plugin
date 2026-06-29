package com.carspotter.ui.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carspotter.ui.CatalogViewModel
import com.carspotter.ui.components.CarRow
import com.carspotter.ui.model.CarUi

@Composable
fun ProgressScreen(
    viewModel: CatalogViewModel,
    onCarClick: (CarUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val cars by viewModel.allCars.collectAsStateWithLifecycle()

    val spotted = cars.filter { it.spotted }.sortedBy { it.manufacturerName + it.model }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Your progress",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { state.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Stat(value = state.spottedCount.toString(), label = "Spotted")
                    Stat(value = state.remainingCount.toString(), label = "Still to find")
                    Stat(value = state.totalCars.toString(), label = "Total")
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (spotted.isEmpty()) {
                        "Nothing spotted yet — go find some cars!"
                    } else {
                        "Spotted cars"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        items(spotted, key = { it.id }) { car ->
            CarRow(
                car = car,
                onClick = { onCarClick(car) },
                onToggleSpotted = { viewModel.toggleSpotted(car) },
            )
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
