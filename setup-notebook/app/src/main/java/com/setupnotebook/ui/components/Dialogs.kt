package com.setupnotebook.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.setupnotebook.data.Car
import com.setupnotebook.data.SetupFields
import com.setupnotebook.data.Track
import com.setupnotebook.data.parseLapTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CarDialog(
    existing: Car?,
    onSave: (name: String, simTitle: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var sim by remember { mutableStateOf(existing?.simTitle ?: SetupFields.simTitles.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New car" else "Edit car") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Car name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = sim,
                    onValueChange = { sim = it },
                    label = { Text("Sim title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    SetupFields.simTitles.forEach { title ->
                        SimChip(title, selected = sim == title, onClick = { sim = title })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, sim); onDismiss() },
                enabled = name.isNotBlank() && sim.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun TrackDialog(
    existing: Track?,
    onSave: (name: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New track" else "Edit track") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Track name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name); onDismiss() }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Delete",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = { onConfirm(); onDismiss() }) {
                Text(confirmLabel, color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun TextInputDialog(
    title: String,
    label: String,
    initial: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text); onDismiss() }, enabled = text.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun NewSetupDialog(
    cars: List<Car>,
    tracks: List<Track>,
    onCreate: (name: String, carId: Long, trackId: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var carId by remember { mutableStateOf<Long?>(cars.firstOrNull()?.id) }
    var trackId by remember { mutableStateOf<Long?>(tracks.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New setup") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Setup name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                PickerField(
                    label = "Car",
                    options = cars.map { it.id to "${it.name} (${it.simTitle})" },
                    selectedId = carId,
                    onSelect = { carId = it },
                )
                Spacer(Modifier.height(12.dp))
                PickerField(
                    label = "Track",
                    options = tracks.map { it.id to it.name },
                    selectedId = trackId,
                    onSelect = { trackId = it },
                )
                if (cars.isEmpty() || tracks.isEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Add at least one car and one track first.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, carId!!, trackId!!); onDismiss() },
                enabled = name.isNotBlank() && carId != null && trackId != null,
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun AddLapDialog(
    onSave: (lapTimeMs: Long, conditions: String, airTemp: Double?, trackTemp: Double?) -> Unit,
    onDismiss: () -> Unit,
) {
    var time by remember { mutableStateOf("") }
    var conditions by remember { mutableStateOf("") }
    var air by remember { mutableStateOf("") }
    var track by remember { mutableStateOf("") }
    val parsed = parseLapTime(time)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log lap") },
        text = {
            Column {
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Lap time") },
                    placeholder = { Text("1:23.456", fontFamily = FontFamily.Monospace) },
                    singleLine = true,
                    isError = time.isNotBlank() && parsed == null,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = conditions,
                    onValueChange = { conditions = it },
                    label = { Text("Track conditions") },
                    placeholder = { Text("Dry, rubbered in") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    OutlinedTextField(
                        value = air,
                        onValueChange = { air = it },
                        label = { Text("Air °C") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = track,
                        onValueChange = { track = it },
                        label = { Text("Track °C") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(parsed!!, conditions, air.toDoubleOrNull(), track.toDoubleOrNull())
                    onDismiss()
                },
                enabled = parsed != null,
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
