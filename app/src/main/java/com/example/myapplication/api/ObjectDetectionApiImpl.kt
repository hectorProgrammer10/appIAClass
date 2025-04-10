package com.example.myapplication.api

import android.graphics.Bitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

class ObjectDetectionApiImpl : ObjectDetectionApi {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    override suspend fun classifyImage(image: Bitmap): DetectionResult {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()

        return client.submitFormWithBinaryData(
            //url = "http://10.0.2.2:3000/classify",
            url = "http://192.168.0.109:3000/classify",
            formData = formData {
                append(
                    key = "image",
                    value = imageBytes,
                    headers = Headers.build {
                        // Versión corregida sin usar APIs internas
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"upload.jpg\"")
                    }
                )
            }
        ).body()
    }
}