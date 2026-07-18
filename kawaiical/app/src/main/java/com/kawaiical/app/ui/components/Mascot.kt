package com.kawaiical.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.kawaiical.app.ui.theme.BabyBlue
import com.kawaiical.app.ui.theme.InkDark
import com.kawaiical.app.ui.theme.PastelPink
import com.kawaiical.app.ui.theme.RosePink
import kotlin.math.sin

enum class MascotMood { NEUTRAL, HAPPY, CHEER, SLEEPY }

private val SkinTone = Color(0xFFFFEDE2)
private val HairPink = Color(0xFFFFBCD3)
private val HairShadow = Color(0xFFF79FBE)
private val BlushColor = Color(0x55FF7BA9)
private val SockWhite = Color(0xFFFDFDFD)

/**
 * Mochi, the KawaiiCal mascot: a chibi in striped thigh-high socks.
 * Breathes and blinks on an idle loop, bounces when cheering, and dozes
 * off at night.
 */
@Composable
fun Mascot(
    mood: MascotMood,
    modifier: Modifier = Modifier,
) {
    val idle = rememberInfiniteTransition(label = "mascotIdle")

    val breath by idle.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breath",
    )

    // Eyes stay open, then snap shut and reopen every few seconds
    val blink by idle.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3600
                1f at 0
                1f at 3150
                0.08f at 3300 using LinearEasing
                1f at 3450
                1f at 3600
            },
        ),
        label = "blink",
    )

    val wave by idle.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
        ),
        label = "wave",
    )

    // A springy hop each time the mood flips to CHEER
    val hop = remember { Animatable(0f) }
    LaunchedEffect(mood) {
        if (mood == MascotMood.CHEER) {
            hop.snapTo(0f)
            hop.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessLow),
            )
            hop.animateTo(0f, spring(stiffness = Spring.StiffnessLow))
        } else {
            hop.animateTo(0f, spring())
        }
    }

    Canvas(modifier = modifier) {
        val u = size.minDimension / 100f // mascot design grid is 100x100
        val hopOffset = -hop.value * 6f * u
        val breathScale = 1f + breath * 0.022f

        translate(top = hopOffset) {
            scale(scale = breathScale, pivot = Offset(size.width / 2f, size.height)) {
                drawMascot(
                    u = u,
                    mood = mood,
                    eyeOpen = if (mood == MascotMood.SLEEPY) 0f else blink,
                    armWave = if (mood == MascotMood.CHEER) sin(wave) else 0f,
                )
            }
        }
    }
}

private fun DrawScope.drawMascot(
    u: Float,
    mood: MascotMood,
    eyeOpen: Float,
    armWave: Float,
) {
    val cx = size.width / 2f

    fun x(v: Float) = cx + (v - 50f) * u
    fun y(v: Float) = v * u

    // ---- Legs with striped thigh-highs (drawn first, behind the body) ----
    drawStripedSock(u, centerX = x(42f), topY = y(66f))
    drawStripedSock(u, centerX = x(58f), topY = y(66f))

    // ---- Arms ----
    val armColor = SkinTone
    if (mood == MascotMood.CHEER) {
        // Arms up, waving
        rotate(degrees = -25f + armWave * 8f, pivot = Offset(x(36f), y(66f))) {
            drawLine(armColor, Offset(x(36f), y(66f)), Offset(x(30f), y(54f)), strokeWidth = 6f * u, cap = StrokeCap.Round)
        }
        rotate(degrees = 25f - armWave * 8f, pivot = Offset(x(64f), y(66f))) {
            drawLine(armColor, Offset(x(64f), y(66f)), Offset(x(70f), y(54f)), strokeWidth = 6f * u, cap = StrokeCap.Round)
        }
    } else {
        drawLine(armColor, Offset(x(37f), y(66f)), Offset(x(31f), y(74f)), strokeWidth = 6f * u, cap = StrokeCap.Round)
        drawLine(armColor, Offset(x(63f), y(66f)), Offset(x(69f), y(74f)), strokeWidth = 6f * u, cap = StrokeCap.Round)
    }

    // ---- Body: an oversized white sweater ----
    val bodyPath = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(x(34f), y(58f), x(66f), y(84f)),
                cornerRadius = CornerRadius(11f * u, 11f * u),
            )
        )
    }
    drawPath(bodyPath, SockWhite)
    // Sweater collar in baby blue
    drawRoundRect(
        color = BabyBlue,
        topLeft = Offset(x(40f), y(58f)),
        size = Size(20f * u, 5f * u),
        cornerRadius = CornerRadius(3f * u, 3f * u),
    )
    // Tiny heart on the sweater
    drawHeart(center = Offset(x(50f), y(72f)), r = 4.2f * u, color = PastelPink)

    // ---- Hair behind the head ----
    drawCircle(HairShadow, radius = 25f * u, center = Offset(x(50f), y(35f)))
    // Side tufts
    drawCircle(HairPink, radius = 8f * u, center = Offset(x(27f), y(42f)))
    drawCircle(HairPink, radius = 8f * u, center = Offset(x(73f), y(42f)))

    // ---- Head ----
    drawCircle(SkinTone, radius = 21f * u, center = Offset(x(50f), y(38f)))

    // Fringe: scalloped bangs over the forehead
    val bangs = Path().apply {
        moveTo(x(29f), y(38f))
        quadraticBezierTo(x(28f), y(16f), x(50f), y(15f))
        quadraticBezierTo(x(72f), y(16f), x(71f), y(38f))
        quadraticBezierTo(x(68f), y(29f), x(62f), y(31f))
        quadraticBezierTo(x(58f), y(25f), x(50f), y(28f))
        quadraticBezierTo(x(42f), y(24f), x(38f), y(31f))
        quadraticBezierTo(x(32f), y(29f), x(29f), y(38f))
        close()
    }
    drawPath(bangs, HairPink)

    // ---- Blush ----
    drawCircle(BlushColor, radius = 3.6f * u, center = Offset(x(38f), y(43f)))
    drawCircle(BlushColor, radius = 3.6f * u, center = Offset(x(62f), y(43f)))

    // ---- Eyes ----
    if (mood == MascotMood.SLEEPY || eyeOpen < 0.15f) {
        // Closed: gentle down-curved lashes
        val stroke = Stroke(width = 1.8f * u, cap = StrokeCap.Round)
        val left = Path().apply {
            moveTo(x(39f), y(39f))
            quadraticBezierTo(x(43f), y(42f), x(47f), y(39f))
        }
        val right = Path().apply {
            moveTo(x(53f), y(39f))
            quadraticBezierTo(x(57f), y(42f), x(61f), y(39f))
        }
        drawPath(left, InkDark, style = stroke)
        drawPath(right, InkDark, style = stroke)
    } else {
        val eyeH = 6.5f * u * eyeOpen
        val eyeW = 4.6f * u
        listOf(x(43f), x(57f)).forEach { ex ->
            drawOval(
                color = InkDark,
                topLeft = Offset(ex - eyeW / 2f, y(39f) - eyeH / 2f),
                size = Size(eyeW, eyeH),
            )
            // Sparkle highlight
            drawCircle(
                Color.White,
                radius = 1.3f * u,
                center = Offset(ex + 1.1f * u, y(39f) - eyeH * 0.22f),
            )
        }
    }

    // ---- Mouth ----
    when (mood) {
        MascotMood.CHEER -> {
            // Big open happy mouth
            val mouth = Path().apply {
                moveTo(x(46f), y(46f))
                quadraticBezierTo(x(50f), y(52f), x(54f), y(46f))
                close()
            }
            drawPath(mouth, Color(0xFFB35A72))
        }
        MascotMood.SLEEPY -> {
            // Tiny sleepy "o"
            drawCircle(Color(0xFFB35A72), radius = 1.7f * u, center = Offset(x(50f), y(47f)))
            // Zzz
            drawZ(Offset(x(72f), y(22f)), 4.5f * u)
            drawZ(Offset(x(78f), y(15f)), 3f * u)
        }
        else -> {
            val smile = Path().apply {
                moveTo(x(46f), y(46f))
                quadraticBezierTo(x(50f), y(49.5f), x(54f), y(46f))
            }
            drawPath(smile, InkDark, style = Stroke(width = 1.8f * u, cap = StrokeCap.Round))
        }
    }
}

/** One thigh-high sock in the femboy flag stripes: pink / white / blue. */
private fun DrawScope.drawStripedSock(u: Float, centerX: Float, topY: Float) {
    val w = 9f * u
    val h = 30f * u
    val sock = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(centerX - w / 2f, topY, centerX + w / 2f, topY + h),
                cornerRadius = CornerRadius(4.5f * u, 4.5f * u),
            )
        )
    }
    clipPath(sock) {
        // Skin peeking at the very top, then flag stripes down the sock
        drawRect(SkinTone, topLeft = Offset(centerX - w / 2f, topY), size = Size(w, 4f * u))
        val stripes = listOf(
            PastelPink, SockWhite, BabyBlue, SockWhite,
            PastelPink, SockWhite, BabyBlue, SockWhite, PastelPink,
        )
        val stripeH = (h - 4f * u) / stripes.size
        stripes.forEachIndexed { i, color ->
            drawRect(
                color = color,
                topLeft = Offset(centerX - w / 2f, topY + 4f * u + i * stripeH),
                size = Size(w, stripeH + 1f),
            )
        }
    }
    // Little shoe
    drawRoundRect(
        color = RosePink,
        topLeft = Offset(centerX - w / 2f - 1f * u, topY + h - 3f * u),
        size = Size(w + 2f * u, 4.5f * u),
        cornerRadius = CornerRadius(2.5f * u, 2.5f * u),
    )
}

private fun DrawScope.drawHeart(center: Offset, r: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y + r)
        cubicTo(
            center.x - 1.9f * r, center.y - 0.1f * r,
            center.x - 0.9f * r, center.y - 1.3f * r,
            center.x, center.y - 0.4f * r,
        )
        cubicTo(
            center.x + 0.9f * r, center.y - 1.3f * r,
            center.x + 1.9f * r, center.y - 0.1f * r,
            center.x, center.y + r,
        )
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawZ(topLeft: Offset, s: Float) {
    val stroke = Stroke(width = s * 0.28f, cap = StrokeCap.Round)
    val path = Path().apply {
        moveTo(topLeft.x, topLeft.y)
        lineTo(topLeft.x + s, topLeft.y)
        lineTo(topLeft.x, topLeft.y + s)
        lineTo(topLeft.x + s, topLeft.y + s)
    }
    drawPath(path, BabyBlue, style = stroke)
}
