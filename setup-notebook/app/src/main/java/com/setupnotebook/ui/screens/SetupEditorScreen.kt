package com.setupnotebook.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.setupnotebook.data.SetupFields
import com.setupnotebook.data.SetupRow
import com.setupnotebook.data.formatLapTime
import com.setupnotebook.ui.AppViewModel
import com.setupnotebook.ui.ShareUtil
import com.setupnotebook.ui.components.AddLapDialog
import com.setupnotebook.ui.components.CollapsibleSection
import com.setupnotebook.ui.components.FieldStepper
import com.setupnotebook.ui.components.TextInputDialog
import com.setupnotebook.ui.theme.MonoValue
import com.setupnotebook.ui.theme.MonoValueSmall
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupEditorScreen(vm: AppViewModel, setupId: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val setup by vm.setupById(setupId).collectAsStateWithLifecycle(initialValue = null)
    val laps by vm.lapsFor(setupId).collectAsStateWithLifecycle(initialValue = emptyList())
    val setupItems by vm.setupItems.collectAsStateWithLifecycle()

    val expanded = remember { mutableStateMapOf(SetupFields.sections.first().key to true) }
    var renaming by remember { mutableStateOf(false) }
    var addingLap by remember { mutableStateOf(false) }

    // Notes are edited locally and persisted debounced, so typing stays smooth.
    var notes by remember(setupId) { mutableStateOf("") }
    var notesLoaded by remember(setupId) { mutableStateOf(false) }
    LaunchedEffect(setup) {
        val s = setup
        if (s != null && !notesLoaded) {
            notes = s.notes
            notesLoaded = true
        }
    }
    LaunchedEffect(notes) {
        if (notesLoaded) {
            delay(600)
            vm.updateNotes(setupId, notes)
        }
    }

    val s = setup
    val item = setupItems.firstOrNull { it.setup.id == setupId }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Column {
                        Text(
                            s?.name ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val subtitle = item?.let { "${it.car?.name ?: "—"} · ${it.track?.name ?: "—"}" }
                        if (subtitle != null) {
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { renaming = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Rename", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = {
                        item?.let { ShareUtil.shareJson(context, it.setup.name, vm.exportJsonFor(it)) }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
            )
        },
    ) { padding ->
        if (s == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }
        val values = remember(s.valuesJson) { SetupFields.parseValues(s.valuesJson) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SetupFields.sections.forEach { section ->
                item(key = section.key) {
                    val setCount = section.fields.count { values.containsKey(it.key) }
                    CollapsibleSection(
                        title = section.title,
                        subtitle = "$setCount of ${section.fields.size} values set",
                        expanded = expanded[section.key] == true,
                        onToggle = { expanded[section.key] = expanded[section.key] != true },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            section.rows.forEach { row ->
                                when (row) {
                                    is SetupRow.Single -> FieldStepper(
                                        field = row.field,
                                        value = values[row.field.key],
                                        onChange = { vm.setFieldValue(setupId, row.field.key, it) },
                                    )
                                    is SetupRow.Corners -> CornerGroup(
                                        row = row,
                                        values = values,
                                        onChange = { key, v -> vm.setFieldValue(setupId, key, v) },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item(key = "notes") {
                CollapsibleSection(
                    title = "Notes",
                    subtitle = if (notes.isBlank()) "Empty" else "${notes.length} chars",
                    expanded = expanded["notes"] == true,
                    onToggle = { expanded["notes"] = expanded["notes"] != true },
                ) {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Balance, kerb behaviour, what to try next…") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            item(key = "laps") {
                val bestMs = laps.minOfOrNull { it.lapTimeMs }
                val dateFormat = remember { SimpleDateFormat("d MMM HH:mm", Locale.getDefault()) }
                CollapsibleSection(
                    title = "Lap log",
                    subtitle = if (bestMs != null) "Best ${formatLapTime(bestMs)}" else "No laps logged",
                    expanded = expanded["laps"] == true,
                    onToggle = { expanded["laps"] = expanded["laps"] != true },
                ) {
                    Column {
                        TextButton(onClick = { addingLap = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.width(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Log lap")
                        }
                        laps.forEachIndexed { index, lap ->
                            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            formatLapTime(lap.lapTimeMs),
                                            style = MonoValue,
                                            color = if (lap.lapTimeMs == bestMs) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface,
                                        )
                                        if (lap.lapTimeMs == bestMs) {
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "BEST",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    }
                                    val details = buildList {
                                        if (lap.conditions.isNotBlank()) add(lap.conditions)
                                        lap.airTemp?.let { add("air ${it}°") }
                                        lap.trackTemp?.let { add("track ${it}°") }
                                        add(dateFormat.format(Date(lap.loggedAt)))
                                    }.joinToString("  ·  ")
                                    Text(
                                        details,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                IconButton(onClick = { vm.deleteLap(lap) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete lap",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (renaming && s != null) {
        TextInputDialog(
            title = "Rename setup",
            label = "Setup name",
            initial = s.name,
            onSave = { vm.renameSetup(setupId, it) },
            onDismiss = { renaming = false },
        )
    }
    if (addingLap) {
        AddLapDialog(
            onSave = { ms, cond, air, track -> vm.addLap(setupId, ms, cond, air, track) },
            onDismiss = { addingLap = false },
        )
    }
}

/** 2x2 grid of compact steppers for FL / FR / RL / RR fields. */
@Composable
private fun CornerGroup(
    row: SetupRow.Corners,
    values: Map<String, Double>,
    onChange: (String, Double) -> Unit,
) {
    Column(Modifier.padding(top = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                row.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (row.unit.isNotEmpty()) {
                Spacer(Modifier.width(6.dp))
                Text(
                    row.unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        val (fl, fr, rl, rr) = row.fields
        listOf(fl to fr, rl to rr).forEach { (left, right) ->
            Row {
                FieldStepper(
                    field = left,
                    value = values[left.key],
                    onChange = { onChange(left.key, it) },
                    compactLabel = left.key.takeLast(2).uppercase(),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                FieldStepper(
                    field = right,
                    value = values[right.key],
                    onChange = { onChange(right.key, it) },
                    compactLabel = right.key.takeLast(2).uppercase(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
