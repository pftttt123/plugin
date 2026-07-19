package com.setupnotebook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.setupnotebook.data.Car
import com.setupnotebook.ui.AppViewModel
import com.setupnotebook.ui.components.CarDialog
import com.setupnotebook.ui.components.ConfirmDialog
import com.setupnotebook.ui.components.EmptyState
import com.setupnotebook.ui.components.SimChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(vm: AppViewModel) {
    val cars by vm.cars.collectAsStateWithLifecycle()
    val setupItems by vm.setupItems.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Car?>(null) }
    var deleting by remember { mutableStateOf<Car?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Garage") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = MaterialTheme.colorScheme.primary,
            ) { Icon(Icons.Default.Add, contentDescription = "Add car", tint = Color.White) }
        },
    ) { padding ->
        if (cars.isEmpty()) {
            EmptyState("No cars yet", "Add the cars you race with the + button.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(cars, key = { it.id }) { car ->
                    val count = setupItems.count { it.setup.carId == car.id }
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(car.name, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SimChip(car.simTitle)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "$count setup${if (count == 1) "" else "s"}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            IconButton(onClick = { editing = car }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { deleting = car }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        CarDialog(existing = null, onSave = { n, s -> vm.addCar(n, s) }, onDismiss = { showAdd = false })
    }
    editing?.let { car ->
        CarDialog(
            existing = car,
            onSave = { n, s -> vm.updateCar(car.copy(name = n.trim(), simTitle = s.trim())) },
            onDismiss = { editing = null },
        )
    }
    deleting?.let { car ->
        val count = vm.setupCountForCar(car.id)
        ConfirmDialog(
            title = "Delete ${car.name}?",
            message = if (count > 0) "This also deletes $count setup${if (count == 1) "" else "s"} for this car." else "This car has no setups.",
            onConfirm = { vm.deleteCar(car) },
            onDismiss = { deleting = null },
        )
    }
}
