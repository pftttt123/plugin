package com.kawaiical.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.geometry.CornerRadius
import com.kawaiical.app.ui.theme.BabyBlue
import com.kawaiical.app.ui.theme.Lavender
import com.kawaiical.app.ui.theme.PastelPink
import com.kawaiical.app.ui.theme.PeachGold
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var spin: Float,
    var life: Float,
    val maxLife: Float,
    val size: Float,
    val color: Color,
    val isCircle: Boolean,
)

/**
 * Full-screen confetti burst. Every time [burstKey] changes to a new
 * non-zero value, a shower of pastel confetti erupts from the top of the
 * screen and flutters down.
 */
@Composable
fun ConfettiOverlay(
    burstKey: Int,
    modifier: Modifier = Modifier,
) {
    val particles = remember { mutableStateListOf<Particle>() }
    var frame by remember { mutableLongStateOf(0L) }
    val colors = listOf(PastelPink, BabyBlue, Color.White, PeachGold, Lavender)

    LaunchedEffect(burstKey) {
        if (burstKey == 0) return@LaunchedEffect
        particles.clear()
        val rng = Random(burstKey)
        repeat(140) {
            val angle = rng.nextFloat() * Math.PI.toFloat() // downward fan
            val speed = 400f + rng.nextFloat() * 900f
            val life = 1.6f + rng.nextFloat() * 1.4f
            particles.add(
                Particle(
                    x = 0.15f + rng.nextFloat() * 0.7f, // fraction of width
                    y = -0.05f,
                    vx = cos(angle) * speed * 0.5f,
                    vy = sin(angle) * speed * 0.3f + 150f,
                    rotation = rng.nextFloat() * 360f,
                    spin = (rng.nextFloat() - 0.5f) * 720f,
                    life = life,
                    maxLife = life,
                    size = 10f + rng.nextFloat() * 14f,
                    color = colors[rng.nextInt(colors.size)],
                    isCircle = rng.nextBoolean(),
                )
            )
        }
        var last = 0L
        while (particles.isNotEmpty()) {
            androidx.compose.runtime.withFrameNanos { now ->
                val dt = if (last == 0L) 0.016f else ((now - last) / 1_000_000_000f).coerceAtMost(0.05f)
                last = now
                val iterator = particles.listIterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    p.x += p.vx * dt / 1000f
                    p.y += p.vy * dt / 1000f
                    p.vy += 900f * dt // gravity
                    p.vx *= (1f - 0.6f * dt) // drag
                    p.rotation += p.spin * dt
                    p.life -= dt
                    if (p.life <= 0f) iterator.remove()
                }
                frame = now
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        @Suppress("UNUSED_EXPRESSION")
        frame // state read -> redraw every physics frame
        particles.forEach { p ->
            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            val px = p.x * size.width
            val py = p.y * size.height
            if (py < size.height + 40f) {
                translate(left = px, top = py) {
                    rotate(degrees = p.rotation, pivot = Offset.Zero) {
                        if (p.isCircle) {
                            drawCircle(p.color, radius = p.size / 2f, center = Offset.Zero, alpha = alpha)
                        } else {
                            drawRoundRect(
                                color = p.color,
                                topLeft = Offset(-p.size / 2f, -p.size / 3.4f),
                                size = Size(p.size, p.size / 1.7f),
                                cornerRadius = CornerRadius(p.size / 5f),
                                alpha = alpha,
                            )
                        }
                    }
                }
            }
        }
    }
}
