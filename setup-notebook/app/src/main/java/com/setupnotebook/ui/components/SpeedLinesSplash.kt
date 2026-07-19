package com.setupnotebook.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.setupnotebook.ui.theme.RacingRed
import kotlin.random.Random

private class SpeedLine(seed: Random) {
    val y = seed.nextFloat()
    val length = 0.08f + seed.nextFloat() * 0.22f
    val speed = 0.6f + seed.nextFloat() * 1.4f
    val phase = seed.nextFloat()
    val thickness = 1.5f + seed.nextFloat() * 2.5f
    val red = seed.nextFloat() < 0.35f
}

/** Full-screen splash: dark void with horizontal speed lines streaking past the app title. */
@Composable
fun SpeedLinesSplash() {
    val lines = remember { Random(42).let { rnd -> List(26) { SpeedLine(rnd) } } }
    val transition = rememberInfiniteTransition(label = "speed")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "speedT",
    )

    var titleIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { titleIn = true }
    val titleScale by animateFloatAsState(
        targetValue = if (titleIn) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 220f),
        label = "titleScale",
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (titleIn) 1f else 0f,
        animationSpec = tween(400),
        label = "titleAlpha",
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            lines.forEach { line ->
                val progress = ((t * line.speed + line.phase) % 1f)
                // travel from just off-screen right to off-screen left
                val startX = size.width * (1.3f - progress * (1.6f + line.length))
                val y = size.height * line.y
                val alpha = (0.15f + 0.5f * progress).coerceAtMost(0.65f)
                drawLine(
                    color = if (line.red) RacingRed.copy(alpha = alpha) else Color.White.copy(alpha = alpha * 0.5f),
                    start = Offset(startX, y),
                    end = Offset(startX + size.width * line.length, y),
                    strokeWidth = line.thickness,
                    cap = StrokeCap.Round,
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(titleScale)
                .alpha(titleAlpha),
        ) {
            Text(
                text = "SETUP",
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                letterSpacing = 8.sp,
            )
            Text(
                text = "NOTEBOOK",
                color = RacingRed,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                letterSpacing = 6.sp,
            )
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier
                    .width(120.dp)
                    .height(3.dp)
                    .background(RacingRed),
            )
        }
    }
}
