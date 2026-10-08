package com.aura.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.aura.app.core.OrbState
import com.aura.app.ui.theme.orbColorFor

@Composable
fun AuraOrb(state: OrbState, modifier: Modifier = Modifier) {
    val color by animateColorAsState(
        targetValue = orbColorFor(state),
        animationSpec = tween(600),
        label = "orbColor"
    )

    val transition = rememberInfiniteTransition(label = "orb")
    val slowPulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "slowPulse"
    )
    val fastPulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "fastPulse"
    )
    val spinSlow by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart),
        label = "spinSlow"
    )
    val spinFast by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "spinFast"
    )
    val ripple by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1900, easing = LinearEasing), RepeatMode.Restart),
        label = "ripple"
    )

    Canvas(modifier = modifier) {
        val c = center
        val base = size.minDimension / 2f
        if (base <= 1f) return@Canvas

        val boost = when (state) {
            OrbState.IDLE -> 0.05f * slowPulse
            OrbState.LISTENING -> 0.08f * slowPulse
            OrbState.THINKING -> 0.03f * fastPulse
            OrbState.SPEAKING -> 0.14f * fastPulse
        }
        val coreR = base * 0.40f * (1f + boost)

        // Soft outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.35f), Color.Transparent),
                center = c,
                radius = base
            ),
            radius = base,
            center = c
        )

        // Thin outer ring
        drawCircle(
            color = color.copy(alpha = 0.30f),
            radius = base * 0.80f,
            center = c,
            style = Stroke(width = 2.dp.toPx())
        )

        // Rotating arcs (fast while thinking)
        val arcRadius = base * 0.62f
        val arcTopLeft = Offset(c.x - arcRadius, c.y - arcRadius)
        val arcSize = Size(arcRadius * 2f, arcRadius * 2f)
        val spin = if (state == OrbState.THINKING) spinFast else spinSlow
        val sweep = if (state == OrbState.THINKING) 110f else 60f
        val arcAlpha = when (state) {
            OrbState.THINKING -> 1f
            OrbState.IDLE -> 0.35f
            else -> 0.6f
        }
        rotate(degrees = spin, pivot = c) {
            drawArc(
                color = color.copy(alpha = arcAlpha),
                startAngle = 0f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = color.copy(alpha = arcAlpha),
                startAngle = 180f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Expanding ripples while listening
        if (state == OrbState.LISTENING) {
            for (i in 0..1) {
                val p = (ripple + i * 0.5f) % 1f
                drawCircle(
                    color = color.copy(alpha = (1f - p) * 0.5f),
                    radius = coreR + (base * 0.95f - coreR) * p,
                    center = c,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Glowing core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.95f), color, color.copy(alpha = 0.25f)),
                center = c,
                radius = coreR
            ),
            radius = coreR,
            center = c
        )
    }
}
