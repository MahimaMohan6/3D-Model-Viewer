package com.example.view3d.ui.screens

import android.graphics.Color.TRANSPARENT
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.view3d.model.ModelItem
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.max
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlin.math.abs

private const val BASE_CARD_DP = 160

@Composable
fun ModelContainer(
    model: ModelItem,
    onRemove: () -> Unit,
) {

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()
    val density = LocalDensity.current.density
    val modelNodeRef = remember {
        mutableStateOf<ModelNode?>(null)
    }
    val cardSizeDp =
        (BASE_CARD_DP * model.scale)
            .coerceIn(80f, 400f)

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    model.offsetX.dp.roundToPx(),
                    model.offsetY.dp.roundToPx()
                )
            }
            .pointerInput(model.isInteractionMode) {
                if (!model.isInteractionMode) {
                    awaitEachGesture {
                        awaitFirstDown(
                            requireUnconsumed = false
                        )
                        do {
                            val event =
                                awaitPointerEvent()
                            val active =
                                event.changes.filter {
                                    it.pressed
                                }
                            when {
                                active.size >= 2 -> {
                                    val rawZoom = event.calculateZoom()
                                    if (abs(rawZoom - 1f) > 0.01f) {
                                        model.scale = (model.scale * rawZoom).coerceIn(0.5f, 2.5f)
                                    }
                                    event.changes.forEach {
                                        it.consume()
                                    }
                                }

                                active.size == 1 -> {
                                    val change = active.first()
                                    if (change.positionChanged()) {
                                        val drag = change.position - change.previousPosition
                                        model.offsetX = (model.offsetX + drag.x / density).coerceIn(
                                            0f,
                                            screenWidth - cardSizeDp
                                        )
                                        model.offsetY = (model.offsetY + drag.y / density).coerceIn(
                                            0f,
                                            screenHeight - cardSizeDp
                                        )
                                        change.consume()
                                    }
                                }
                            }
                        } while (
                            event.changes.any {
                                it.pressed
                            }
                        )
                    }
                }
            }
            .size(cardSizeDp.dp)
            .clipToBounds()
            .background(Color(0xFF1A1A1A))
            .border(
                2.dp,
                if (model.isInteractionMode) Color.Green else Color.DarkGray
            ),
        contentAlignment = Alignment.Center
    ) {
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
                                            model.cameraZoom = (model.cameraZoom / rawZoom)
                                                .coerceIn(1.5f, 8f)
                                        }
                                        event.changes.forEach {
                                            it.consume()
                                        }
                                    }

                                    active.size == 1 -> {
                                        val change = active.first()
                                        if (change.positionChanged()) {
                                            val drag = change.position - change.previousPosition
                                            model.modelRotation += drag.x * 0.7f
                                            change.consume()
                                        }
                                    }
                                }
                            } while (
                                event.changes.any {
                                    it.pressed
                                }
                            )
                        }
                    }
                },
            factory = { context ->
                SceneView(
                    context = context,
                    cameraManipulator = null
                ).apply {
                    setBackgroundColor(TRANSPARENT)
                    val node = ModelNode(
                        modelInstance =
                            modelLoader.createModelInstance(
                                assetFileLocation =
                                    model.assetFile
                            ),
                        scaleToUnits = 1f,
                        centerOrigin = null
                    )
                    addChildNode(node)
                    modelNodeRef.value = node
                    post {
                        val maxHalf = max(node.halfExtent)
                        val camZ = if (maxHalf > 0f && !maxHalf.isNaN())
                            (maxHalf * 3.5f).coerceIn(1.5f, 6f)
                        else 2.5f
                        model.cameraZoom = camZ
                        cameraNode.position = Position(
                            x = node.center.x,
                            y = node.center.y,
                            z = camZ
                        )
                    }
                }
            },
            update = { sceneView ->
                modelNodeRef.value?.apply {
                    rotation = Float3(
                        0f,
                        model.modelRotation,
                        0f
                    )
                }
                sceneView.cameraNode.position =
                    Position(
                        0f,
                        0.5f,
                        model.cameraZoom
                    )
            }
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White
            )
        }
        Button(
            onClick = {
                model.isInteractionMode =
                    !model.isInteractionMode
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor =
                    if (model.isInteractionMode)
                        MaterialTheme.colorScheme.primary
                    else
                        Color(0xFF48484A)
            )
        ) {
            Text(
                if (model.isInteractionMode) "Done" else "Interact"
            )
        }
    }
}