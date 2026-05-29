package com.example.view3d.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.view3d.model.ModelItem
import com.example.view3d.model.ModelRegistry

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF1A1B1F),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121317)),
                title = { Text("View 3D", fontSize = 24.sp, color = Color(0xFF00D1FF)) })
        }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                onClick = { showPicker = true }) {
                Text("+", fontSize = 24.sp)
            }
            if (showPicker) {
                Popup(properties = PopupProperties(focusable = false)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x66000000))
                            .clickable { showPicker = false }
                    )
                }
                Popup(
                    alignment = Alignment.BottomCenter,
                    properties = PopupProperties(focusable = true)
                ) {
                    AnimatedVisibility(
                        visible = showPicker,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it })
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 350.dp)
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                .background(Color(0xFF1E1F24))
                                .navigationBarsPadding()
                        ) {
                            // Handle
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 12.dp)
                                    .size(width = 40.dp, height = 4.dp)
                                    .align(Alignment.CenterHorizontally)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0xFF444444))
                                    .padding(horizontal = 20.dp, vertical = 2.dp)
                            )

                            Text(
                                "Choose a Model",
                                color = Color(0xFF00D1FF),
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )

                            HorizontalDivider(color = Color(0xFF2A2B30))

                            LazyColumn(modifier = Modifier.weight(1f)) {
                                itemsIndexed(ModelRegistry.models) { _, def ->
                                    ListItem(
                                        headlineContent = {
                                            Text(def.label, color = Color(0xFFE0E0E0))
                                        },
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
                                        }
                                    )
                                    HorizontalDivider(color = Color(0xFF2A2B30))
                                }
                            }
                        }
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