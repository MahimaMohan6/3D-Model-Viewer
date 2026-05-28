package com.example.view3d.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ModelItem(
    val id: Int,
    val assetFile: String,
    offsetX: Float,
    offsetY: Float
) {
    var offsetX by mutableFloatStateOf(offsetX)
    var offsetY by mutableFloatStateOf(offsetY)
    var scale by mutableFloatStateOf(1f)
    var isInteractionMode by mutableStateOf(false)
    var modelRotation by mutableFloatStateOf(0f)
    var cameraZoom by mutableFloatStateOf(3.5f)
}

data class ModelDefinition(val label: String, val assetFile: String)

object ModelRegistry {
    val models = listOf(
        ModelDefinition("Chair",    "models/chair.glb"),
        ModelDefinition("Laptop",    "models/laptop.glb"),
        ModelDefinition("Tree",     "models/tree.glb"),
        ModelDefinition("Robot",     "models/robot.glb"),
        ModelDefinition("WaterMelon",    "models/watermelon.glb"),
    )
}