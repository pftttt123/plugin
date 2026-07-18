package com.kawaiical.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset

/**
 * A number whose digits roll up (or down) like a slot machine whenever
 * the value changes.
 */
@Composable
fun RollingNumber(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    var oldValue by remember { mutableIntStateOf(value) }
    val increasing = value >= oldValue
    SideEffect { oldValue = value }

    val newText = value.toString()
    val oldText = oldValue.toString()

    Row(modifier) {
        // Align digits from the right so "999 -> 1000" only rolls what changed
        val pad = newText.length - oldText.length
        newText.forEachIndexed { i, newChar ->
            val oldChar = oldText.getOrNull(i - pad)
            val char = if (oldChar == newChar) oldChar else newChar
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    val slide = spring<IntOffset>(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    )
                    if (increasing) {
                        (slideInVertically(slide) { it } + fadeIn()) togetherWith
                            (slideOutVertically(slide) { -it } + fadeOut())
                    } else {
                        (slideInVertically(slide) { -it } + fadeIn()) togetherWith
                            (slideOutVertically(slide) { it } + fadeOut())
                    }
                },
                label = "digit$i",
            ) { c ->
                Text(text = c.toString(), style = style, color = color, softWrap = false)
            }
        }
    }
}
