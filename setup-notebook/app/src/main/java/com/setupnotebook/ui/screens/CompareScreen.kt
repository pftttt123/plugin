package com.setupnotebook.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.setupnotebook.data.FieldDef
import com.setupnotebook.data.SetupFields
import com.setupnotebook.data.formatLapTime
import com.setupnotebook.ui.AppViewModel
import com.setupnotebook.ui.components.EmptyState
import com.setupnotebook.ui.components.PickerField
import com.setupnotebook.ui.theme.MonoValueSmall

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(vm: AppViewModel) {
    val items by vm.setupItems.collectAsStateWithLifecycle()
    var aId by rememberSaveable { mutableStateOf<Long?>(null) }
    var bId by rememberSaveable { mutableStateOf<Long?>(null) }
    var diffOnly by rememberSaveable { mutableStateOf(false) }

    val a = items.firstOrNull { it.setup.id == aId }
    val b = items.firstOrNull { it.setup.id == bId }
    val options = items.map { it.setup.id to "${it.setup.name} · ${it.car?.name ?: "—"}" }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Compare") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Row {
                PickerField(
                    label = "Setup A",
                    options = options,
                    selectedId = aId,
                    onSelect = { aId = it },
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(10.dp))
                PickerField(
                    label = "Setup B",
                    options = options,
                    selectedId = bId,
                    onSelect = { bId = it },
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Text(
                    "Differences only",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = diffOnly,
                    onCheckedChange = { diffOnly = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                )
            }

            if (a == null || b == null) {
                EmptyState("Pick two setups", "Choose Setup A and Setup B above to see the differences.")
            } else {
                val valuesA = remember(a.setup.valuesJson) { SetupFields.parseValues(a.setup.valuesJson) }
                val valuesB = remember(b.setup.valuesJson) { SetupFields.parseValues(b.setup.valuesJson) }

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    item(key = "header") {
                        Row(Modifier.padding(vertical = 6.dp)) {
                            listOf(a, b).forEachIndexed { index, item ->
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Column(Modifier.padding(10.dp)) {
                                        Text(
                                            if (index == 0) "A · ${item.setup.name}" else "B · ${item.setup.name}",
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            "${item.car?.name ?: "—"} · ${item.track?.name ?: "—"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            item.bestLapMs?.let { "Best " + formatLapTime(it) } ?: "No laps",
                                            style = MonoValueSmall,
                                            color = if (item.bestLapMs != null) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                if (index == 0) Spacer(Modifier.width(10.dp))
                            }
                        }
                    }

                    SetupFields.sections.forEach { section ->
                        val rows = section.fields.mapNotNull { field ->
                            val va = valuesA[field.key]
                            val vb = valuesB[field.key]
                            if (va == null && vb == null) return@mapNotNull null
                            val differs = va != vb
                            if (diffOnly && !differs) return@mapNotNull null
                            Triple(field, va to vb, differs)
                        }
                        if (rows.isNotEmpty()) {
                            item(key = "section-${section.key}") {
                                Text(
                                    section.title.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 14.dp, bottom = 4.dp),
                                )
                            }
                            items(rows.size, key = { i -> "row-${section.key}-${rows[i].first.key}" }) { i ->
                                val (field, pair, differs) = rows[i]
                                CompareRow(field, pair.first, pair.second, differs)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareRow(field: FieldDef, va: Double?, vb: Double?, differs: Boolean) {
    val bg by animateColorAsState(
        targetValue = if (differs) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent,
        animationSpec = tween(250),
        label = "diffBg",
    )
    val valueColor = if (differs) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Column(Modifier.weight(1.3f)) {
            Text(
                field.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (field.unit.isNotEmpty()) {
                Text(
                    field.unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                )
            }
        }
        Text(
            va?.let { SetupFields.format(field, it) } ?: "—",
            style = MonoValueSmall,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(18.dp))
        Text(
            vb?.let { SetupFields.format(field, it) } ?: "—",
            style = MonoValueSmall,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}
