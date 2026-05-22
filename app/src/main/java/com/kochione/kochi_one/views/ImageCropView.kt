package com.kochione.kochi_one.views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun ImageCropView(
    uri: Uri,
    isDarkTheme: Boolean = false,
    onCancel: () -> Unit,
    onCropSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(uri) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val originalBitmap = BitmapFactory.decodeStream(stream)
                    // Downscale if too large to prevent OOM
                    val maxDim = 2048
                    if (originalBitmap.width > maxDim || originalBitmap.height > maxDim) {
                        val ratio = maxDim.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
                        bitmap = Bitmap.createScaledBitmap(
                            originalBitmap,
                            (originalBitmap.width * ratio).toInt(),
                            (originalBitmap.height * ratio).toInt(),
                            true
                        )
                    } else {
                        bitmap = originalBitmap
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFF2F2F2)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val overlayColor = if (isDarkTheme) Color(0xFF1E1E1E).copy(alpha = 0.85f) else Color(0xFFF2F2F2).copy(alpha = 0.85f)

    if (bitmap == null) {
        Box(modifier = Modifier.fillMaxSize().background(bgColor), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = textColor)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Image with panning and zooming
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Crop Image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offset = offset + pan
                    }
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )

        // Overlay with circle cutout
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val cutoutSize = canvasWidth * 0.9f
            val cutoutRect = Rect(
                left = (canvasWidth - cutoutSize) / 2f,
                top = (canvasHeight - cutoutSize) / 2f,
                right = (canvasWidth + cutoutSize) / 2f,
                bottom = (canvasHeight + cutoutSize) / 2f
            )

            clipPath(Path().apply { addOval(cutoutRect) }, clipOp = androidx.compose.ui.graphics.ClipOp.Difference) {
                drawRect(color = overlayColor)
            }
            // Draw border
            drawCircle(
                color = textColor.copy(alpha = 0.5f),
                radius = cutoutSize / 2f,
                center = Offset(canvasWidth / 2f, canvasHeight / 2f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )
        }

        // Bottom Actions
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(bgColor.copy(alpha = 0.9f))
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = textColor, fontSize = 18.sp)
            }
            TextButton(onClick = {
                val b = bitmap ?: return@TextButton
                Thread {
                    try {
                        val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
                        val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()
                        val imgRatio = b.width.toFloat() / b.height.toFloat()
                        val screenRatio = screenWidth / screenHeight
                        
                        var visualWidth = screenWidth
                        var visualHeight = screenHeight
                        if (imgRatio > screenRatio) {
                            visualHeight = screenWidth / imgRatio
                        } else {
                            visualWidth = screenHeight * imgRatio
                        }

                        val cutoutSize = screenWidth * 0.9f
                        val scaleFactor = b.width / visualWidth
                        
                        val centerX = (screenWidth / 2f) - offset.x
                        val centerY = (screenHeight / 2f) - offset.y
                        
                        val bmpCenterX = (b.width / 2f) + (centerX - screenWidth / 2f) * scaleFactor / scale
                        val bmpCenterY = (b.height / 2f) + (centerY - screenHeight / 2f) * scaleFactor / scale
                        
                        val cropHalf = (cutoutSize / 2f) * scaleFactor / scale
                        
                        val left = (bmpCenterX - cropHalf).toInt().coerceIn(0, b.width)
                        val top = (bmpCenterY - cropHalf).toInt().coerceIn(0, b.height)
                        val right = (bmpCenterX + cropHalf).toInt().coerceIn(0, b.width)
                        val bottom = (bmpCenterY + cropHalf).toInt().coerceIn(0, b.height)
                        
                        val width = right - left
                        val height = bottom - top
                        val size = minOf(width, height)
                        
                        if (size > 0) {
                            val cropped = Bitmap.createBitmap(b, left, top, size, size)
                            val file = File(context.filesDir, "profile_crop_${System.currentTimeMillis()}.jpg")
                            FileOutputStream(file).use { out ->
                                cropped.compress(Bitmap.CompressFormat.JPEG, 90, out)
                            }
                            onCropSuccess(Uri.fromFile(file).toString())
                        } else {
                            onCancel()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onCancel()
                    }
                }.start()
            }) {
                Text("Done", color = Color(0xFF007AFF), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
