package com.kawaiical.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kawaiical.app.ui.theme.BabyBlue
import com.kawaiical.app.ui.theme.DeepPink
import com.kawaiical.app.ui.theme.PastelPink

/**
 * Smooth line chart of daily calories that draws itself in from left to
 * right, with a dashed goal line and pop-in data dots.
 */
@Composable
fun WeeklyLineChart(
    values: List<Int>,
    goal: Int,
    labels: List<String>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 180.dp,
) {
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(values) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, tween(durationMillis = 1100, easing = FastOutSlowInEasing))
    }

    val goalColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dotFill = MaterialTheme.colorScheme.surface

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
        ) {
            if (values.isEmpty()) return@Canvas
            val maxY = maxOf(values.max(), goal).coerceAtLeast(1) * 1.2f
            val stepX = if (values.size == 1) 0f else size.width / (values.size - 1)
            val points = values.mapIndexed { i, v ->
                Offset(i * stepX, size.height * (1f - v / maxY))
            }

            // Dashed goal line
            val goalY = size.height * (1f - goal / maxY)
            drawLine(
                color = goalColor.copy(alpha = 0.5f),
                start = Offset(0f, goalY),
                end = Offset(size.width, goalY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)),
            )

            // Smooth path through the points
            val line = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val midX = (prev.x + curr.x) / 2f
                    cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                }
            }

            val measure = PathMeasure().apply { setPath(line, false) }
            val revealed = Path()
            measure.getSegment(0f, measure.length * reveal.value, revealed, true)

            // Gradient fill under the revealed part of the curve
            if (reveal.value > 0.01f) {
                val head = measure.getPosition(measure.length * reveal.value)
                val fill = Path().apply {
                    addPath(revealed)
                    lineTo(head.x, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(
                    fill,
                    Brush.verticalGradient(
                        listOf(PastelPink.copy(alpha = 0.35f), BabyBlue.copy(alpha = 0.02f)),
                    ),
                )
            }

            drawPath(
                revealed,
                Brush.horizontalGradient(listOf(DeepPink, PastelPink, BabyBlue)),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
            )

            // Dots pop in as the line sweeps past them
            points.forEachIndexed { i, p ->
                val fraction = if (points.size == 1) 0f else i.toFloat() / (points.size - 1)
                if (reveal.value >= fraction) {
                    drawCircle(dotFill, radius = 6.dp.toPx(), center = p)
                    drawCircle(
                        if (values[i] > goal) DeepPink else BabyBlue,
                        radius = 6.dp.toPx(),
                        center = p,
                        style = Stroke(width = 3.dp.toPx()),
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            labels.forEach { label ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
