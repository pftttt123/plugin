package com.kawaiical.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kawaiical.app.ui.theme.BabyBlue
import com.kawaiical.app.ui.theme.Lavender
import com.kawaiical.app.ui.theme.PastelPink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val MacroColors = listOf(PastelPink, BabyBlue, Lavender)
private val MacroNames = listOf("Protein", "Carbs", "Fat")

/**
 * Animated stacked bar of today's macros, plus a legend with rolling
 * gram counters. Segments spring into place with a small stagger.
 */
@Composable
fun MacroBreakdown(
    protein: Float,
    carbs: Float,
    fat: Float,
    modifier: Modifier = Modifier,
) {
    // Weight by calorie contribution (4/4/9 kcal per gram)
    val kcals = listOf(protein * 4f, carbs * 4f, fat * 9f)
    val total = kcals.sum()
    val fractions = if (total <= 0f) listOf(0f, 0f, 0f) else kcals.map { it / total }

    val animated = remember { List(3) { Animatable(0f) } }
    LaunchedEffect(fractions) {
        // Stagger the three segments so the bar assembles left-to-right
        animated.forEachIndexed { i, anim ->
            launch {
                if (anim.value == 0f && fractions[i] > 0f) delay(i * 110L)
                anim.animateTo(
                    fractions[i],
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                )
            }
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Row(Modifier.fillMaxWidth().fillMaxHeight()) {
                animated.forEachIndexed { i, anim ->
                    val weight = anim.value.coerceAtLeast(0.0001f)
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .fillMaxHeight()
                            .background(if (anim.value > 0.002f) MacroColors[i] else Color.Transparent),
                    )
                }
                // Filler for the un-eaten remainder keeps segments proportional
                val used = animated.sumOf { it.value.toDouble() }.toFloat()
                if (used < 0.999f) {
                    Spacer(Modifier.weight((1f - used).coerceAtLeast(0.0001f)))
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        val grams = listOf(protein, carbs, fat)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            repeat(3) { i ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MacroColors[i]),
                    )
                    Spacer(Modifier.width(6.dp))
                    Column {
                        Text(
                            MacroNames[i],
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            RollingNumber(
                                value = grams[i].roundToInt(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                " g",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
