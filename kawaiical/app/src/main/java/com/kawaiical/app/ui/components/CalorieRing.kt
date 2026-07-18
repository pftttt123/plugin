package com.kawaiical.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kawaiical.app.ui.theme.BabyBlue
import com.kawaiical.app.ui.theme.Lavender
import com.kawaiical.app.ui.theme.PastelPink
import com.kawaiical.app.ui.theme.PeachGold
import kotlin.math.cos
import kotlin.math.sin

/**
 * The hero of the home screen: a gradient progress ring that springs to
 * its new value whenever calories are logged, with a little bead riding
 * the leading edge.
 */
@Composable
fun CalorieRing(
    progress: Float,
    modifier: Modifier = Modifier,
    ringWidth: Dp = 22.dp,
    trackColor: Color = PastelPink.copy(alpha = 0.18f),
    overGoal: Boolean = false,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val animated = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animated.animateTo(
            progress.coerceIn(0f, 1f),
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessVeryLow,
            ),
        )
    }

    val gradientColors =
        if (overGoal) listOf(PeachGold, PastelPink, PeachGold)
        else listOf(PastelPink, Lavender, BabyBlue, PastelPink)

    val strokePx = with(LocalDensity.current) { ringWidth.toPx() }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val inset = strokePx / 2f + 4.dp.toPx()
            val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
            val topLeft = Offset(inset, inset)
            val sweep = animated.value * 360f

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            if (sweep > 0.5f) {
                // Rotate so the gradient (and the arc) starts at 12 o'clock
                rotate(degrees = -90f) {
                    // Soft glow underneath
                    drawArc(
                        brush = Brush.sweepGradient(gradientColors),
                        startAngle = 0f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        alpha = 0.25f,
                        style = Stroke(width = strokePx * 1.55f, cap = StrokeCap.Round),
                    )
                    drawArc(
                        brush = Brush.sweepGradient(gradientColors),
                        startAngle = 0f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    )
                }

                // Bead riding the leading edge of the arc
                val angleRad = Math.toRadians(sweep - 90.0)
                val radius = arcSize.width / 2f
                val bead = Offset(
                    x = center.x + radius * cos(angleRad).toFloat(),
                    y = center.y + radius * sin(angleRad).toFloat(),
                )
                drawCircle(Color.White, radius = strokePx * 0.42f, center = bead)
                drawCircle(
                    if (overGoal) PeachGold else PastelPink,
                    radius = strokePx * 0.2f,
                    center = bead,
                )
            }
        }
        content()
    }
}
