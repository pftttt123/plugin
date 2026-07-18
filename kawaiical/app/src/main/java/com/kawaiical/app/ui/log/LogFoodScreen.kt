package com.kawaiical.app.ui.log

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kawaiical.app.data.db.FoodItem
import com.kawaiical.app.data.db.MealType
import com.kawaiical.app.ui.components.PillSelector
import kotlinx.coroutines.delay

@Composable
fun LogFoodScreen(
    initialMeal: MealType,
    onDone: () -> Unit,
    viewModel: LogFoodViewModel = viewModel(factory = LogFoodViewModel.factory(initialMeal)),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val selectedMeal by viewModel.selectedMeal.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()

    var servingFood by remember { mutableStateOf<FoodItem?>(null) }
    var showQuickAdd by remember { mutableStateOf(false) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDone) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
                Text("Add food", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.weight(1f))
                FilledTonalButton(
                    onClick = { showQuickAdd = true },
                    shape = RoundedCornerShape(50),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Quick add")
                }
            }

            Spacer(Modifier.height(12.dp))

            PillSelector(
                options = MealType.entries.toList(),
                selected = selectedMeal,
                onSelect = { viewModel.selectedMeal.value = it },
                label = { "${it.emoji} ${it.label.take(9)}" },
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.query.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search foods…") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                shape = RoundedCornerShape(50),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
            ) {
                itemsIndexed(results, key = { _, food -> food.id }) { index, food ->
                    // Staggered entrance: each row slides in slightly after the last
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(minOf(index, 12) * 45L)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn() + slideInVertically(
                            spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            ),
                            initialOffsetY = { it / 3 },
                        ),
                        modifier = Modifier.animateItem(),
                    ) {
                        FoodRow(food = food, onClick = { servingFood = food })
                    }
                }
            }
        }
    }

    servingFood?.let { food ->
        ServingDialog(
            food = food,
            onDismiss = { servingFood = null },
            onConfirm = { servings ->
                viewModel.log(food, servings) {
                    servingFood = null
                    onDone()
                }
            },
        )
    }

    if (showQuickAdd) {
        QuickAddDialog(
            onDismiss = { showQuickAdd = false },
            onConfirm = { name, serving, cal, p, c, f ->
                viewModel.quickAdd(name, serving, cal, p, c, f) {
                    showQuickAdd = false
                    onDone()
                }
            },
        )
    }
}

@Composable
private fun FoodRow(food: FoodItem, onClick: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    (if (food.isCustom) "⭐ " else "") + food.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    "${food.serving} · P ${food.protein.toInt()} C ${food.carbs.toInt()} F ${food.fat.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "${food.calories}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                " kcal",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ServingDialog(
    food: FoodItem,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit,
) {
    var servings by remember { mutableFloatStateOf(1f) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text(food.name, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${food.serving} · ${food.calories} kcal each",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledIconButton(
                        onClick = { servings = (servings - 0.5f).coerceAtLeast(0.5f) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Icon(Icons.Rounded.Remove, contentDescription = "Fewer servings")
                    }
                    Text(
                        text = if (servings % 1f == 0f) {
                            "${servings.toInt()}"
                        } else {
                            "%.1f".format(servings)
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                    FilledIconButton(
                        onClick = { servings = (servings + 0.5f).coerceAtMost(20f) },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "More servings")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "= ${(food.calories * servings).toInt()} kcal",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(servings) }, shape = RoundedCornerShape(50)) {
                Text("Add 🎀")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun QuickAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, serving: String, cal: Int, p: Float, c: Float, f: Float) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var serving by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }

    val valid = name.isNotBlank() && calories.toIntOrNull() != null

    @Composable
    fun numberField(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= 6) onChange(it.filter { ch -> ch.isDigit() || ch == '.' }) },
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = MaterialTheme.shapes.medium,
            modifier = modifier,
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        title = { Text("Quick add ✨", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food name") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = serving,
                    onValueChange = { serving = it },
                    label = { Text("Serving (e.g. 1 bowl)") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
                numberField(calories, { calories = it }, "kcal", Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    numberField(protein, { protein = it }, "P (g)", Modifier.weight(1f))
                    numberField(carbs, { carbs = it }, "C (g)", Modifier.weight(1f))
                    numberField(fat, { fat = it }, "F (g)", Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        name.trim(),
                        serving.trim(),
                        calories.toIntOrNull() ?: 0,
                        protein.toFloatOrNull() ?: 0f,
                        carbs.toFloatOrNull() ?: 0f,
                        fat.toFloatOrNull() ?: 0f,
                    )
                },
                enabled = valid,
                shape = RoundedCornerShape(50),
            ) {
                Text("Save & log 💖")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
