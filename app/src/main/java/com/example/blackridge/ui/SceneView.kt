package com.example.blackridge.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blackridge.model.GameState
import com.example.blackridge.model.Npc
import com.example.blackridge.model.TurnOutput
import kotlin.random.Random

/** Accent colour and skyline shape for each district. */
private data class DistrictLook(val neon: Color, val haze: Color, val towerHeight: Float)

private fun districtLook(district: String): DistrictLook = when {
    district.contains("Downtown", true) -> DistrictLook(Color(0xFF38BDF8), Color(0xFF1E3A5F), 0.85f)
    district.contains("Southside", true) -> DistrictLook(Color(0xFFF59E0B), Color(0xFF4A2C12), 0.45f)
    district.contains("Mill", true) -> DistrictLook(Color(0xFFEA580C), Color(0xFF3F1F14), 0.55f)
    district.contains("Yards", true) -> DistrictLook(Color(0xFF10B981), Color(0xFF14332B), 0.6f)
    else -> DistrictLook(Color(0xFFA855F7), Color(0xFF2A1840), 0.3f)
}

/** Sky gradient for the in-game clock: day, dusk/dawn, night. */
private fun skyColors(minutes: Int): Pair<Color, Color> {
    val hour = (minutes / 60) % 24
    return when (hour) {
        in 7..16 -> Color(0xFF5B7A99) to Color(0xFFA9B8C6)
        in 17..19, in 5..6 -> Color(0xFF2B1F4A) to Color(0xFFD9774B)
        else -> Color(0xFF05060A) to Color(0xFF151A2B)
    }
}

/** NPCs on screen: anyone at the player's location or named in this turn's NPC action. */
fun npcsInScene(state: GameState, turn: TurnOutput?): List<Npc> {
    val action = turn?.npcAction.orEmpty()
    return state.npcs.filter {
        it.currentLocation.equals(state.locationName, ignoreCase = true) ||
            (action.isNotBlank() && action.contains(it.name, ignoreCase = true))
    }.take(4)
}

/**
 * Animated 2.5D street view of the player's current location: layered parallax skyline,
 * perspective street, rain, a neon sign, and silhouettes for the player and NPCs present.
 */
@Composable
fun SceneView(state: GameState, turn: TurnOutput?, modifier: Modifier = Modifier) {
    val look = districtLook(state.locationDistrict)
    val (skyTop, skyBottom) = skyColors(state.timeMinutes)
    val isNight = (state.timeMinutes / 60) % 24 !in 7..16
    val npcs = npcsInScene(state, turn)
    val seed = state.locationName.hashCode()
    val textMeasurer = rememberTextMeasurer()

    val transition = rememberInfiniteTransition(label = "scene")
    val sway by transition.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sway"
    )
    val rainPhase by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "rain"
    )
    val flicker by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2300, easing = LinearEasing)),
        label = "flicker"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(10.dp))
    ) {
        val w = size.width
        val h = size.height
        val horizon = h * 0.58f

        // Sky
        drawRect(Brush.verticalGradient(listOf(skyTop, skyBottom, look.haze), endY = horizon))

        // Far skyline (moves least) and near buildings (move most) give the parallax depth
        drawSkyline(seed, horizon, look.towerHeight * 0.75f, Color(0xFF0E1220), sway * 4f, 14, isNight, look.neon.copy(alpha = 0.25f))
        drawSkyline(seed + 7, horizon, look.towerHeight, Color(0xFF080A12), sway * 10f, 8, isNight, Color(0xFFFFD27A).copy(alpha = 0.55f))

        // Perspective street converging on a vanishing point
        val vanishX = w / 2 + sway * 6f
        drawRect(Color(0xFF0B0D14), topLeft = Offset(0f, horizon), size = Size(w, h - horizon))
        val road = Path().apply {
            moveTo(vanishX - w * 0.04f, horizon)
            lineTo(vanishX + w * 0.04f, horizon)
            lineTo(w * 0.95f, h)
            lineTo(w * 0.05f, h)
            close()
        }
        drawPath(road, Brush.verticalGradient(listOf(Color(0xFF151925), Color(0xFF232838)), startY = horizon, endY = h))
        // Centre dashes get longer toward the viewer
        for (i in 0 until 6) {
            val t0 = i / 6f
            val t1 = t0 + 0.07f
            val y0 = horizon + (h - horizon) * t0 * t0
            val y1 = horizon + (h - horizon) * t1 * t1
            drawLine(look.neon.copy(alpha = 0.35f), Offset(vanishX, y0), Offset(vanishX, y1), strokeWidth = 1f + 4f * t1)
        }
        // Neon glow reflected on the wet street
        drawRect(
            Brush.verticalGradient(listOf(look.neon.copy(alpha = 0.18f), Color.Transparent), startY = horizon, endY = h),
            topLeft = Offset(0f, horizon), size = Size(w, h - horizon)
        )

        // Neon location sign, with an occasional flicker
        val signOn = !(flicker in 0.62f..0.66f || flicker in 0.70f..0.72f)
        val signText = textMeasurer.measure(
            state.locationName.uppercase(),
            TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                color = if (signOn) look.neon else look.neon.copy(alpha = 0.25f))
        )
        val signTopLeft = Offset(w * 0.06f + sway * 10f, horizon * 0.42f)
        drawRoundRect(
            Color.Black.copy(alpha = 0.6f), topLeft = signTopLeft - Offset(8f, 6f),
            size = Size(signText.size.width + 16f, signText.size.height + 12f), cornerRadius = CornerRadius(6f)
        )
        drawRoundRect(
            look.neon.copy(alpha = if (signOn) 0.9f else 0.2f), topLeft = signTopLeft - Offset(8f, 6f),
            size = Size(signText.size.width + 16f, signText.size.height + 12f), cornerRadius = CornerRadius(6f),
            style = Stroke(width = 2f)
        )
        drawText(signText, topLeft = signTopLeft)

        // Characters: NPCs spread across the middle distance, player in the foreground
        npcs.forEachIndexed { i, npc ->
            val depth = 0.35f + 0.12f * (i % 2)
            val x = w * (0.22f + 0.56f * (i + 0.5f) / npcs.size) + sway * 14f * depth
            val y = horizon + (h - horizon) * depth
            drawFigure(Offset(x, y), h * 0.32f * (0.6f + depth), factionColor(npc.faction))
            val tag = textMeasurer.measure(
                npc.name,
                TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White)
            )
            val tagTop = Offset(x - tag.size.width / 2f, y - h * 0.32f * (0.6f + depth) - tag.size.height - 6f)
            drawRoundRect(
                Color.Black.copy(alpha = 0.65f), topLeft = tagTop - Offset(4f, 2f),
                size = Size(tag.size.width + 8f, tag.size.height + 4f), cornerRadius = CornerRadius(4f)
            )
            drawText(tag, topLeft = tagTop)
        }
        drawFigure(Offset(w * 0.5f + sway * 20f, h * 1.02f), h * 0.42f, Color(0xFFF59E0B))

        // Rain
        val rng = Random(seed)
        repeat(70) {
            val rx = rng.nextFloat() * w
            val speed = 0.6f + rng.nextFloat() * 0.8f
            val ry = ((rng.nextFloat() + rainPhase * speed) % 1f) * h
            drawLine(Color(0xFFB6C6E0).copy(alpha = 0.25f), Offset(rx, ry), Offset(rx - 3f, ry + 12f), strokeWidth = 1.2f)
        }

        // Vignette
        drawRect(
            Brush.radialGradient(
                listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                center = Offset(w / 2, h / 2), radius = w * 0.75f
            )
        )
    }
}

private fun factionColor(faction: String): Color = when {
    faction.contains("Southside", true) -> Color(0xFFEF4444)
    faction.contains("East Yards", true) || faction.contains("Cartel", true) -> Color(0xFF10B981)
    faction.contains("Mill", true) -> Color(0xFFFB923C)
    faction.contains("Downtown", true) -> Color(0xFF38BDF8)
    else -> Color(0xFFA855F7)
}

/** A row of building silhouettes along the horizon, with lit windows at night. */
private fun DrawScope.drawSkyline(
    seed: Int, horizon: Float, heightScale: Float, color: Color,
    shift: Float, count: Int, isNight: Boolean, windowColor: Color
) {
    val rng = Random(seed)
    val w = size.width
    val slot = w / count
    for (i in -1..count) {
        val bw = slot * (0.7f + rng.nextFloat() * 0.5f)
        val bh = horizon * heightScale * (0.35f + rng.nextFloat() * 0.65f)
        val x = i * slot + shift
        drawRect(color, topLeft = Offset(x, horizon - bh), size = Size(bw, bh))
        if (isNight) {
            val cols = (bw / 9f).toInt().coerceAtLeast(1)
            val rows = (bh / 12f).toInt()
            for (r in 0 until rows) for (c in 0 until cols) {
                if (rng.nextFloat() < 0.22f) {
                    drawRect(windowColor, topLeft = Offset(x + 3f + c * 9f, horizon - bh + 4f + r * 12f), size = Size(4f, 5f))
                }
            }
        }
    }
}

/** Simple standing silhouette (head, body, legs) with a coloured rim light. */
private fun DrawScope.drawFigure(feet: Offset, height: Float, rim: Color) {
    val headR = height * 0.09f
    val bodyW = height * 0.26f
    val body = Path().apply {
        moveTo(feet.x - bodyW * 0.5f, feet.y - height * 0.45f)
        lineTo(feet.x + bodyW * 0.5f, feet.y - height * 0.45f)
        lineTo(feet.x + bodyW * 0.38f, feet.y - height * 0.8f)
        quadraticTo(feet.x, feet.y - height * 0.86f, feet.x - bodyW * 0.38f, feet.y - height * 0.8f)
        close()
    }
    val legs = Path().apply {
        moveTo(feet.x - bodyW * 0.42f, feet.y - height * 0.46f)
        lineTo(feet.x - bodyW * 0.32f, feet.y)
        lineTo(feet.x - bodyW * 0.06f, feet.y)
        lineTo(feet.x, feet.y - height * 0.3f)
        lineTo(feet.x + bodyW * 0.06f, feet.y)
        lineTo(feet.x + bodyW * 0.32f, feet.y)
        lineTo(feet.x + bodyW * 0.42f, feet.y - height * 0.46f)
        close()
    }
    val head = Offset(feet.x, feet.y - height * 0.8f - headR * 1.1f)
    // Shadow on the street
    drawOval(Color.Black.copy(alpha = 0.45f), topLeft = Offset(feet.x - bodyW * 0.7f, feet.y - 4f), size = Size(bodyW * 1.4f, 8f))
    val fill = Color(0xFF05060A)
    drawPath(legs, fill)
    drawPath(body, fill)
    drawCircle(fill, headR, head)
    drawPath(body, rim.copy(alpha = 0.8f), style = Stroke(width = 1.6f))
    drawCircle(rim.copy(alpha = 0.8f), headR, head, style = Stroke(width = 1.6f))
    // Soft halo so figures read against dark backgrounds
    drawCircle(rim.copy(alpha = 0.12f), headR * 2.2f, head)
}
