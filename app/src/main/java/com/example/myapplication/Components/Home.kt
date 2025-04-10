package com.example.myapplication.Components

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.api.DetectionResult
import com.example.myapplication.api.ObjectDetectionApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectDetectionScreen(
    api: ObjectDetectionApi,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var detectionResult by remember { mutableStateOf<DetectionResult?>(null) }

    // Launcher para la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedImage = bitmap
            saveImageToGallery(context, bitmap)

            // Procesar imagen automáticamente
            scope.launch {
                isLoading = true
                try {
                    detectionResult = api.classifyImage(bitmap)
                    snackbarHostState.showSnackbar("Análisis completado")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar(
                        "Error: ${e.message ?: "Error desconocido"}",
                        duration = SnackbarDuration.Long
                    )
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Row (modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .border(1.dp, color = Color.DarkGray)
                .padding(33.dp),
                horizontalArrangement = Arrangement.Center
            ){
                Text(
                    text = "IA Clasificador",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Cursive,
                    fontSize = 33.sp,
                    color = Color.Black,

                )
            }

            // Imagen de fondo
            Image(
                painter = painterResource(id = R.drawable.fondo),
                contentDescription = "Fondo de pantalla",
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Botón para capturar imagen
                Button(
                    onClick = { cameraLauncher.launch(null) },
                    enabled = !isLoading,
                    modifier = Modifier.width(200.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(text = if (isLoading) "Procesando..." else "Capturar Imagen")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Mostrar imagen capturada
                capturedImage?.let { bitmap ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Imagen capturada",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Mostrar resultados
                    detectionResult?.let { result ->
                        DetectionResultCard(result = result)
                    }
                }
            }
        }
    }
}

@Composable
fun DetectionResultCard(result: DetectionResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Resultado del Análisis",
                style = MaterialTheme.typography.headlineSmall
            )

            Divider()

            // Información principal
            ResultItem("Objeto detectado:", result.`class`)
            ResultItem("Confianza:", result.confidence)

            // Tiempos de procesamiento
            result.processing_time_sec?.let {
                ResultItem("Tiempo total:", "${it}s")
            }
            result.model_prediction_time_sec?.let {
                ResultItem("Tiempo modelo:", "${it}s")
            }

            // Predicciones adicionales
            result.all_predictions?.takeIf { it.isNotEmpty() }?.let { predictions ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Otras predicciones:",
                    style = MaterialTheme.typography.labelLarge
                )
                predictions.forEach { (key, value) ->
                    if (value > 0.1) { // Mostrar solo predicciones con >10% de confianza
                        ResultItem(
                            label = key,
                            value = "${(value * 100).toInt()}%",
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResultItem(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// Función para guardar la imagen (sin cambios)
fun saveImageToGallery(context: Context, bitmap: Bitmap): Uri? {
    val uri = MediaStore.Images.Media.insertImage(
        context.contentResolver,
        bitmap,
        "detected_object_${System.currentTimeMillis()}",
        "Imagen analizada por el modelo de detección"
    )
    return uri?.let { Uri.parse(it) }
}