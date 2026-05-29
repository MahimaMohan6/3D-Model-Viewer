# Android Developer Task – 3D Model Viewer

## Overview

This project is a single-activity Android application built using Kotlin and Jetpack Compose. The application allows users to load multiple GLB 3D models, place them on the screen, resize them, move them, and interact with them independently.

## Technology Stack

* Language: Kotlin
* UI: Jetpack Compose
* Min SDK: 24
* 3D Engine: SceneView (Filament-based renderer)

## Why SceneView

I selected SceneView because it provides a simple integration layer on top of Google's Filament rendering engine while still offering good rendering performance and support for GLB models. It allowed rapid implementation of model loading, rendering, rotation, and camera control while keeping the codebase relatively small.

## Implemented Features

* Single Activity architecture
* Multiple GLB models bundled with the application
* Model picker using a bottom sheet
* Multiple models displayed simultaneously
* Draggable model containers
* Resizable model containers using pinch gestures
* Interaction mode for model rotation and zoom
* Close button to remove models
* Independent interaction state per model
* Separation between layout manipulation mode and model interaction mode

## Performance Optimizations

The task requirements emphasized performance, so the following optimizations were applied:

* Models are loaded only when selected by the user.
* UI state is maintained per model to avoid unnecessary recompositions.
* Model scale normalization is applied so different GLB assets appear consistently sized.
* Interaction mode and container manipulation mode are separated to prevent conflicting gesture processing.
* Android Studio CPU and Memory Profilers were used during testing.
* The application was tested with up to 5 simultaneously loaded models.

## Profiling Results

Testing was performed using Android Studio Profiler.

Observed results:

* Stable memory usage during model addition and removal.
* No memory leaks detected during profiling.
* Smooth drag, resize, rotate, and zoom interactions with up to 5 loaded models.
* CPU usage remained within acceptable limits during interaction testing.

## Trade-offs

To simplify independent model manipulation and interaction handling, each model is currently hosted inside its own SceneView instance.

This approach provides:

* Clear separation of model state
* Simpler gesture handling
* Easier lifecycle management

A future optimization would be to use a single shared SceneView containing multiple ModelNodes. This would reduce renderer count and memory usage, especially on very low-end devices.

## Known Limitations

* A minor rendering artifact may occasionally appear at extreme zoom-out levels for certain GLB assets.
* The current implementation prioritizes feature isolation and maintainability over maximum rendering efficiency.

## Future Improvements

* Single SceneView architecture with multiple ModelNodes
* Asset caching and pooling
* Additional model interaction controls
* Better low-end device profiling and benchmarking
* More advanced model placement and alignment features
