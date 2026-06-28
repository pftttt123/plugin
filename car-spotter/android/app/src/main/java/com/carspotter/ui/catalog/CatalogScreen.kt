package com.carspotter.ui.catalog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.stickyHeader
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carspotter.ui.CatalogViewModel
import com.carspotter.ui.components.CarRow
import com.carspotter.ui.model.CarUi
import com.carspotter.ui.model.SpottedFilter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    onCarClick: (CarUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::setQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            placeholder = { Text("Search manufacturer or model") },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.filter == SpottedFilter.ALL,
                onClick = { viewModel.setFilter(SpottedFilter.ALL) },
                label = { Text("All") },
            )
            FilterChip(
                selected = state.filter == SpottedFilter.NOT_SPOTTED,
                onClick = { viewModel.setFilter(SpottedFilter.NOT_SPOTTED) },
                label = { Text("Need to find") },
            )
            FilterChip(
                selected = state.filter == SpottedFilter.SPOTTED,
                onClick = { viewModel.setFilter(SpottedFilter.SPOTTED) },
                label = { Text("Spotted") },
            )
        }

        Text(
            text = "${state.spottedCount} of ${state.totalCars} spotted",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )

        when {
            state.isLoading -> CenteredBox { CircularProgressIndicator() }

            state.error != null -> CenteredBox {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error!!, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::refresh) { Text("Retry") }
                }
            }

            state.groups.isEmpty() -> CenteredBox {
                Text("No cars match your search.")
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                state.groups.forEach { group ->
                    stickyHeader(key = "h-${group.id}") {
                        Surface(
                            color = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }
                    items(group.cars, key = { it.id }) { car ->
                        CarRow(
                            car = car,
                            onClick = { onCarClick(car) },
                            onToggleSpotted = { viewModel.toggleSpotted(car) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}
