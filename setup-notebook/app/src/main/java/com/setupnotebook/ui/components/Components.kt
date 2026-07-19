package com.setupnotebook.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.setupnotebook.data.FieldDef
import com.setupnotebook.data.SetupFields
import com.setupnotebook.ui.theme.MonoValue
import com.setupnotebook.ui.theme.MonoValueSmall
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

/** Icon button that repeats its action while held down. */
@Composable
private fun RepeatIconButton(
    onStep: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    small: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    var consumedByRepeat by remember { mutableStateOf(false) }

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(400)
            consumedByRepeat = true
            while (true) {
                onStep()
                delay(60)
            }
        }
    }

    IconButton(
        onClick = {
            if (consumedByRepeat) consumedByRepeat = false else onStep()
        },
        interactionSource = interactionSource,
        modifier = Modifier.size(if (small) 32.dp else 40.dp),
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(if (small) 16.dp else 20.dp),
        )
    }
}

/** The animated numeric value readout: flashes accent + bumps scale on change. */
@Composable
fun AnimatedValueText(text: String, small: Boolean = false, highlight: Boolean = false) {
    var flash by remember { mutableStateOf(false) }
    var first by remember { mutableStateOf(true) }
    LaunchedEffect(text) {
        if (first) {
            first = false
        } else {
            flash = true
            delay(180)
            flash = false
        }
    }
    val color by animateColorAsState(
        targetValue = when {
            flash || highlight -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(220),
        label = "valueColor",
    )
    val scale by animateFloatAsState(
        targetValue = if (flash) 1.14f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
        label = "valueScale",
    )
    Text(
        text = text,
        style = if (small) MonoValueSmall else MonoValue,
        color = color,
        maxLines = 1,
        modifier = Modifier.scale(scale),
    )
}

/** Stepper row for a single numeric field: label, unit, minus / value / plus. */
@Composable
fun FieldStepper(
    field: FieldDef,
    value: Double?,
    onChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    compactLabel: String? = null,
) {
    val haptics = LocalHapticFeedback.current
    val current = value ?: field.default
    val compact = compactLabel != null

    fun step(direction: Int) {
        val next = min(field.max, max(field.min, current + direction * field.step))
        if (next != current || value == null) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onChange(next)
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = compactLabel ?: field.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (field.unit.isNotEmpty() && !compact) {
                Text(
                    text = field.unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
        RepeatIconButton(onStep = { step(-1) }, icon = Icons.Default.Remove, contentDescription = "Decrease", small = compact)
        Box(Modifier.width(if (compact) 56.dp else 72.dp), contentAlignment = Alignment.Center) {
            AnimatedValueText(
                text = SetupFields.format(field, current),
                small = compact,
                highlight = value == null,
            )
        }
        RepeatIconButton(onStep = { step(1) }, icon = Icons.Default.Add, contentDescription = "Increase", small = compact)
    }
}

/** Collapsible card section with spring-animated expand / collapse. */
@Composable
fun CollapsibleSection(
    title: String,
    subtitle: String? = null,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "chevron",
    )
    val borderColor by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(250),
        label = "sectionBorder",
    )

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, MaterialTheme.shapes.medium),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggle()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(
                            if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            CircleShape,
                        ),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    if (subtitle != null) {
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(chevronRotation),
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(spring(dampingRatio = 0.75f, stiffness = 380f)) + fadeIn(tween(200)),
                exit = shrinkVertically(spring(dampingRatio = 1f, stiffness = 500f)) + fadeOut(tween(120)),
            ) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 14.dp)) {
                    content()
                }
            }
        }
    }
}

/** Read-only field that opens a dropdown of options. */
@Composable
fun PickerField(
    label: String,
    options: List<Pair<Long, String>>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedTextField(
            value = options.firstOrNull { it.first == selectedId }?.second ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.ExpandMore, contentDescription = null) },
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable { open = true },
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            if (options.isEmpty()) {
                DropdownMenuItem(text = { Text("Nothing here yet") }, onClick = { open = false })
            }
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(id)
                        open = false
                    },
                )
            }
        }
    }
}

/** Small accent-tinted label chip, used for sim titles. */
@Composable
fun SimChip(text: String, selected: Boolean = false, onClick: (() -> Unit)? = null) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        tween(200),
        label = "chipBg",
    )
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        color = bg,
        contentColor = fg,
        shape = MaterialTheme.shapes.small,
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
fun EmptyState(title: String, hint: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 96.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Text(
            hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}
