package com.kawaiical.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import com.kawaiical.app.ui.theme.BabyBlue
import com.kawaiical.app.ui.theme.DeepBlue
import kotlin.math.sin

/**
 * A cute glass of water that fills up with a gentle lapping wave as
 * glasses are logged.
 */
@Composable
fun WaterGlass(
    glasses: Int,
    goal: Int,
    modifier: Modifier = Modifier,
) {
    val fill by animateFloatAsState(
        targetValue = (glasses.toFloat() / goal.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "waterFill",
    )

    val wave = rememberInfiniteTransition(label = "wave")
    val phase by wave.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "wavePhase",
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Glass silhouette: slightly tapered tumbler
        val topInset = w * 0.08f
        val bottomInset = w * 0.2f
        val glassPath = Path().apply {
            moveTo(topInset, 0f)
            lineTo(w - topInset, 0f)
            lineTo(w - bottomInset, h - w * 0.1f)
            quadraticBezierTo(w - bottomInset, h, w - bottomInset - w * 0.1f, h)
            lineTo(bottomInset + w * 0.1f, h)
            quadraticBezierTo(bottomInset, h, bottomInset, h - w * 0.1f)
            close()
        }

        // Water inside, clipped to the glass
        clipPath(glassPath) {
            if (fill > 0.01f) {
                val levelY = h * (1f - fill * 0.94f)
                val amp = h * 0.018f * (0.4f + fill)
                val water = Path().apply {
                    moveTo(0f, levelY + amp * sin(phase))
                    var x = 0f
                    while (x <= w) {
                        val yy = levelY + amp * sin(phase + (x / w) * 2f * Math.PI.toFloat())
                        lineTo(x, yy)
                        x += w / 24f
                    }
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    water,
                    Brush.verticalGradient(
                        colors = listOf(BabyBlue.copy(alpha = 0.85f), DeepBlue.copy(alpha = 0.9f)),
                        startY = levelY,
                        endY = h,
                    ),
                )
                // Bubbles rising near the bottom
                val bubblePhase = phase * 1.4f
                listOf(0.35f, 0.55f, 0.7f).forEachIndexed { i, bx ->
                    val by = h - ((bubblePhase / (2f * Math.PI.toFloat()) + i * 0.33f) % 1f) * (h - levelY) * 0.8f
                    drawCircle(
                        Color.White.copy(alpha = 0.35f),
                        radius = w * (0.02f + i * 0.008f),
                        center = Offset(w * bx, by),
                    )
                }
            }
        }

        // Glass outline + shine
        drawPath(glassPath, DeepBlue.copy(alpha = 0.5f), style = Stroke(width = w * 0.035f, cap = StrokeCap.Round))
        drawLine(
            Color.White.copy(alpha = 0.55f),
            start = Offset(w * 0.82f, h * 0.12f),
            end = Offset(w * 0.76f, h * 0.5f),
            strokeWidth = w * 0.05f,
            cap = StrokeCap.Round,
        )
    }
}
