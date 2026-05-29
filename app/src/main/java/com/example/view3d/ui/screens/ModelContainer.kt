@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.example.view3d.ui.screens

import android.graphics.Color.TRANSPARENT
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.view3d.model.ModelItem
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlin.math.abs
import kotlin.math.max

private const val BASE_CARD_DP = 160
private const val INITIAL_CAM_Z = 3.2f

@Composable
fun ModelContainer(
    model: ModelItem,
    onRemove: () -> Unit,
) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()
    val density = LocalDensity.current.density

    val modelNodeRef = remember { mutableStateOf<ModelNode?>(null) }
    val sceneViewRef = remember { mutableStateOf<SceneView?>(null) }

    val cardSizeDp = (BASE_CARD_DP * model.scale).coerceIn(80f, 400f)

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    model.offsetX.dp.roundToPx(),
                    model.offsetY.dp.roundToPx()
                )
            }
            .size(cardSizeDp.dp)
            .shadow(
                elevation = 32.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (model.isInteractionMode) Color(0xFFFF00FF) else Color(0xFF00D1FF)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1B1F))
            .border(
                2.dp,
                if (model.isInteractionMode) Color(0xFFFF00FF) else Color(0xFF00D1FF),
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(model.isInteractionMode) {
                if (!model.isInteractionMode) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val active = event.changes.filter { it.pressed }
                            when {
                                active.size >= 2 -> {
                                    val rawZoom = event.calculateZoom()
                                    if (abs(rawZoom - 1f) > 0.01f) {
                                        val maxScale = (minOf(
                                            screenWidth,
                                            screenHeight
                                        ) / BASE_CARD_DP).coerceAtMost(2.5f)
                                        model.scale =
                                            (model.scale * rawZoom).coerceIn(0.5f, maxScale)
                                    }
                                    event.changes.forEach { it.consume() }
                                }

                                active.size == 1 -> {
                                    val change = active.first()
                                    if (change.positionChanged()) {
                                        val drag = change.position - change.previousPosition
                                        val maxOffsetX =
                                            (screenWidth - cardSizeDp).coerceAtLeast(0f)
                                        val maxOffsetY =
                                            (screenHeight - cardSizeDp).coerceAtLeast(0f)
                                        model.offsetX = (model.offsetX + drag.x / density).coerceIn(
                                            0f,
                                            maxOffsetX
                                        )
                                        model.offsetY = (model.offsetY + drag.y / density).coerceIn(
                                            0f,
                                            maxOffsetY
                                        )
                                        change.consume()
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)) {
                AndroidView(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(model.isInteractionMode) {
                            if (model.isInteractionMode) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    do {
                                        val event = awaitPointerEvent()
                                        val active = event.changes.filter { it.pressed }
                                        when {
                                            active.size >= 2 -> {
                                                val rawZoom = event.calculateZoom()
                                                if (abs(rawZoom - 1f) > 0.01f) {
                                                    val zoomDelta = (1f - rawZoom) * 1.2f
                                                    val newZoom =
                                                        (model.cameraZoom + zoomDelta).coerceIn(
                                                            1.8f,
                                                            5.5f
                                                        )
                                                    model.cameraZoom = newZoom
                                                    sceneViewRef.value?.cameraNode?.position =
                                                        Position(0f, 0f, newZoom)
                                                }
                                                event.changes.forEach { it.consume() }
                                            }

                                            active.size == 1 -> {
                                                val change = active.first()
                                                if (change.positionChanged()) {
                                                    val drag =
                                                        change.position - change.previousPosition
                                                    model.modelRotation += drag.x * 0.7f
                                                    change.consume()
                                                }
                                            }
                                        }
                                    } while (event.changes.any { it.pressed })
                                }
                            }
                        },
                    factory = { context ->
                        SceneView(
                            context = context,
                            cameraManipulator = null,
//                            isOpaque = false
                        ).apply {
                            setBackgroundColor(TRANSPARENT)
                            val node = ModelNode(
                                modelInstance =
                                    modelLoader.createModelInstance(assetFileLocation = model.assetFile),
                                scaleToUnits = 1f,
                                centerOrigin = Position(0f, 0f, 0f)
                            )
                            addChildNode(node)
                            modelNodeRef.value = node
                            sceneViewRef.value = this
                            post {
                                val maxDimension =
                                    max(node.extents.x, max(node.extents.y, node.extents.z))
                                val normalizedScale = 1.8f / maxDimension
                                node.scale =
                                    Float3(normalizedScale, normalizedScale, normalizedScale)
                                node.position = Position(
                                    -node.center.x * normalizedScale,
                                    -node.center.y * normalizedScale,
                                    -node.center.z * normalizedScale
                                )
                                model.cameraZoom = INITIAL_CAM_Z
                                cameraNode.position = Position(0f, 0f, INITIAL_CAM_Z)
                            }
                        }
                    },
                    update = {
                        modelNodeRef.value?.rotation = Float3(0f, model.modelRotation, 0f)
                    }
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.White
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color(0xFF13141A))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { model.isInteractionMode = !model.isInteractionMode },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (model.isInteractionMode)
                            Color(0x1FFF00FF) else Color(0x1F00D1FF)
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        if (model.isInteractionMode) Color(0xFFFF00FF).copy(alpha = 0.5f) else Color(
                            0xFF00D1FF
                        ).copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = if (model.isInteractionMode) "Done" else "Interact",
                        fontSize = 11.sp,
                        color = if (model.isInteractionMode) Color(0xFFFF00FF) else Color(0xFF00D1FF)
                    )
                }
            }
        }
    }
}