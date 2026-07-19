package com.setupnotebook.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.setupnotebook.data.Setup
import com.setupnotebook.data.formatLapTime
import com.setupnotebook.ui.AppViewModel
import com.setupnotebook.ui.ShareUtil
import com.setupnotebook.ui.components.ConfirmDialog
import com.setupnotebook.ui.components.EmptyState
import com.setupnotebook.ui.components.NewSetupDialog
import com.setupnotebook.ui.components.SimChip
import com.setupnotebook.ui.theme.MonoValueSmall
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupsScreen(vm: AppViewModel, onOpenSetup: (Long) -> Unit) {
    val context = LocalContext.current
    val items by vm.filteredSetups.collectAsStateWithLifecycle()
    val cars by vm.cars.collectAsStateWithLifecycle()
    val tracks by vm.tracks.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val simFilter by vm.simFilter.collectAsStateWithLifecycle()
    val carFilter by vm.carFilter.collectAsStateWithLifecycle()
    val trackFilter by vm.trackFilter.collectAsStateWithLifecycle()

    var showNew by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Setup?>(null) }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
            if (text == null) {
                Toast.makeText(context, "Could not read file", Toast.LENGTH_SHORT).show()
            } else {
                vm.importJson(text) { count ->
                    Toast.makeText(
                        context,
                        if (count == null) "Not a valid setup export" else "Imported $count setup${if (count == 1) "" else "s"}",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }
    val simOptions = remember(cars) { cars.map { it.simTitle }.distinct().sorted() }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Setups") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = { importLauncher.launch("*/*") }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Import JSON", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNew = true },
                containerColor = MaterialTheme.colorScheme.primary,
            ) { Icon(Icons.Default.Add, contentDescription = "New setup", tint = Color.White) }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.query.value = it },
                placeholder = { Text("Search setups, cars, tracks…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                simOptions.forEach { sim ->
                    FilterChip(
                        selected = simFilter == sim,
                        onClick = { vm.simFilter.value = if (simFilter == sim) null else sim },
                        label = { Text(sim) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                        ),
                    )
                }
                cars.forEach { car ->
                    FilterChip(
                        selected = carFilter == car.id,
                        onClick = { vm.carFilter.value = if (carFilter == car.id) null else car.id },
                        label = { Text(car.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    )
                }
                tracks.forEach { track ->
                    FilterChip(
                        selected = trackFilter == track.id,
                        onClick = { vm.trackFilter.value = if (trackFilter == track.id) null else track.id },
                        label = { Text(track.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    )
                }
            }
            if (items.isEmpty()) {
                EmptyState(
                    title = if (query.isBlank() && simFilter == null && carFilter == null && trackFilter == null)
                        "No setups yet" else "Nothing matches",
                    hint = "Create a setup with the + button or import JSON.",
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(items, key = { it.setup.id }) { item ->
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .clickable { onOpenSetup(item.setup.id) },
                        ) {
                            Column(Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        item.setup.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    item.car?.let { SimChip(it.simTitle) }
                                    Spacer(Modifier.width(12.dp))
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "${item.car?.name ?: "—"}  ·  ${item.track?.name ?: "—"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (item.bestLapMs != null) {
                                        Icon(
                                            Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.width(16.dp),
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            formatLapTime(item.bestLapMs),
                                            style = MonoValueSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Spacer(Modifier.width(12.dp))
                                    }
                                    Text(
                                        dateFormat.format(Date(item.setup.updatedAt)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    )
                                    Spacer(Modifier.weight(1f))
                                    IconButton(onClick = { vm.duplicateSetup(item.setup) }) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = {
                                        ShareUtil.shareJson(context, item.setup.name, vm.exportJsonFor(item))
                                    }) {
                                        Icon(Icons.Default.Share, contentDescription = "Export", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { deleting = item.setup }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNew) {
        NewSetupDialog(
            cars = cars,
            tracks = tracks,
            onCreate = { name, carId, trackId -> vm.createSetup(name, carId, trackId, onOpenSetup) },
            onDismiss = { showNew = false },
        )
    }
    deleting?.let { setup ->
        ConfirmDialog(
            title = "Delete ${setup.name}?",
            message = "The setup and its lap log will be removed.",
            onConfirm = { vm.deleteSetup(setup) },
            onDismiss = { deleting = null },
        )
    }
}
