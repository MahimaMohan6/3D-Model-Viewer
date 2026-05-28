package com.example.view3d.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.view3d.model.ModelItem
import com.example.view3d.model.ModelRegistry
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode

private const val CARD_SIZE_DP = 160
private const val CARD_GAP_DP = 8

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    val modelList = remember { mutableStateListOf<ModelItem>() }
    var showPicker by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    val columns = (screenWidthDp / (CARD_SIZE_DP + CARD_GAP_DP)).coerceAtLeast(1)
    val cardStep = CARD_SIZE_DP + CARD_GAP_DP
    Scaffold(modifier = Modifier.fillMaxSize(), containerColor = Color(0xFF1A1B1F), topBar = {
        TopAppBar(colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121317)), title = {Text("View 3D", fontSize = 24.sp, color = Color(0xFF00D1FF))})
    }){
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            modelList.forEach { model ->
                key(model.id) {
                    ModelContainer(model, onRemove = {
                        modelList.remove(model)
                    })
                }
            }

            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 16.dp, vertical = 16.dp), onClick = { showPicker = true }) {
                Text("+", fontSize = 24.sp)
            }
        }
        if (showPicker) {
            ModalBottomSheet(onDismissRequest = { showPicker = false }) {
                Text("Choose a Model", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                LazyColumn(modifier = Modifier.navigationBarsPadding()) {
                    itemsIndexed(ModelRegistry.models) { _, def ->
                        ListItem(
                            headlineContent = { Text(def.label) },
                            modifier = Modifier.clickable {
                                val index = modelList.size
                                val col = index % columns
                                val row = index / columns
                                modelList.add(
                                    ModelItem(
                                        id = index,
                                        assetFile = def.assetFile,
                                        offsetX = (col * cardStep + CARD_GAP_DP).toFloat(),
                                        offsetY = (row * cardStep + CARD_GAP_DP).toFloat()
                                    )
                                )
                                showPicker = false
                            })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MainScreenPreview() {
    MainScreen()
}