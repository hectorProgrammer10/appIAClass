// app/src/main/java/com/example/myapplication/api/ObjectDetectionApi.kt
package com.example.myapplication.api

import android.graphics.Bitmap

interface ObjectDetectionApi {
    suspend fun classifyImage(image: Bitmap): DetectionResult
}