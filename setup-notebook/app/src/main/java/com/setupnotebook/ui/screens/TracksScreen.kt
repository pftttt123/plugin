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
import com.setupnotebook.data.Track
import com.setupnotebook.ui.AppViewModel
import com.setupnotebook.ui.components.ConfirmDialog
import com.setupnotebook.ui.components.EmptyState
import com.setupnotebook.ui.components.TrackDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracksScreen(vm: AppViewModel) {
    val tracks by vm.tracks.collectAsStateWithLifecycle()
    val setupItems by vm.setupItems.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Track?>(null) }
    var deleting by remember { mutableStateOf<Track?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Tracks") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAdd = true },
                containerColor = MaterialTheme.colorScheme.primary,
            ) { Icon(Icons.Default.Add, contentDescription = "Add track", tint = Color.White) }
        },
    ) { padding ->
        if (tracks.isEmpty()) {
            EmptyState("No tracks yet", "Add the circuits you drive with the + button.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(tracks, key = { it.id }) { track ->
                    val count = setupItems.count { it.setup.trackId == track.id }
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
                                Text(track.name, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    "$count setup${if (count == 1) "" else "s"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { editing = track }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { deleting = track }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        TrackDialog(existing = null, onSave = { vm.addTrack(it) }, onDismiss = { showAdd = false })
    }
    editing?.let { track ->
        TrackDialog(
            existing = track,
            onSave = { vm.updateTrack(track.copy(name = it.trim())) },
            onDismiss = { editing = null },
        )
    }
    deleting?.let { track ->
        val count = vm.setupCountForTrack(track.id)
        ConfirmDialog(
            title = "Delete ${track.name}?",
            message = if (count > 0) "This also deletes $count setup${if (count == 1) "" else "s"} at this track." else "This track has no setups.",
            onConfirm = { vm.deleteTrack(track) },
            onDismiss = { deleting = null },
        )
    }
}
