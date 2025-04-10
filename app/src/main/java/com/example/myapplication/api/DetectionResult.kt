// app/src/main/java/com/example/myapplication/api/DetectionResult.kt
package com.example.myapplication.api

import kotlinx.serialization.Serializable

@Serializable
data class DetectionResult(
    val `class`: String,
    val confidence: String,
    val all_predictions: Map<String, Float>? = null,
    val processing_time_sec: Float? = null,
    val model_prediction_time_sec: Float? = null,
    val timestamp: String? = null
)