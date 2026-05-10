package com.kochione.kochi_one.views

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.kochione.kochi_one.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import coil.ImageLoader
import coil.decode.VideoFrameDecoder

private const val REPORT_API_URL = "https://api.kochi.one/api/reports"

@Composable
fun ReportView(
    isDarkTheme: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val textColor = if (isDarkTheme) Color.White else Color.Black
    val secondaryText = textColor.copy(alpha = 0.6f)
    val inputBgColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF0F0F0)
    val borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    val accentColor = Color(0xFF007AFF)

    var issueText by remember { mutableStateOf("") }
    var contactText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val selectedImageUris = remember { mutableStateListOf<Uri>() }
    val maxImages = 5

    // Media launcher — supports both images and videos via OpenMultipleDocuments
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val remaining = maxImages - selectedImageUris.size
        uris.take(remaining).forEach { uri ->
            if (!selectedImageUris.contains(uri)) selectedImageUris.add(uri)
        }
    }

    // ── Staggered entrance animations ───────────────────────────────────────
    val headerAlpha = remember { Animatable(0f) }
    val headerOffsetY = remember { Animatable(-30f) }
    val field1Alpha = remember { Animatable(0f) }
    val field1OffsetY = remember { Animatable(40f) }
    val field2Alpha = remember { Animatable(0f) }
    val field2OffsetY = remember { Animatable(40f) }
    val mediaAlpha = remember { Animatable(0f) }
    val mediaOffsetY = remember { Animatable(40f) }
    val buttonAlpha = remember { Animatable(0f) }
    val buttonOffsetY = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        launch { headerAlpha.animateTo(1f, tween(350, easing = EaseOutCubic)) }
        launch { headerOffsetY.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)) }
        delay(80)
        launch { field1Alpha.animateTo(1f, tween(400, easing = EaseOutCubic)) }
        launch { field1OffsetY.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) }
        delay(80)
        launch { field2Alpha.animateTo(1f, tween(400, easing = EaseOutCubic)) }
        launch { field2OffsetY.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) }
        delay(80)
        launch { mediaAlpha.animateTo(1f, tween(350, easing = EaseOutCubic)) }
        launch { mediaOffsetY.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) }
        delay(80)
        launch { buttonAlpha.animateTo(1f, tween(350, easing = EaseOutCubic)) }
        launch { buttonOffsetY.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) }
    }

    // ── Submit function ──────────────────────────────────────────────────────
    fun submitReport() {
        if (issueText.isBlank()) {
            Toast.makeText(context, "Please describe the issue", Toast.LENGTH_SHORT).show()
            return
        }
        coroutineScope.launch {
            isSubmitting = true
            try {
                val result = withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build()

                    val bodyBuilder = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("message", issueText.trim())
                        .addFormDataPart("contact", contactText.trim())

                    // Attach each selected image/video
                    selectedImageUris.forEachIndexed { idx, uri ->
                        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                        val ext = if (mimeType.startsWith("video")) "mp4" else "jpg"
                        val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                        if (bytes != null) {
                            bodyBuilder.addFormDataPart(
                                name = "media",
                                filename = "media_$idx.$ext",
                                body = bytes.toRequestBody(mimeType.toMediaType())
                            )
                        }
                    }

                    val request = Request.Builder()
                        .url(REPORT_API_URL)
                        .post(bodyBuilder.build())
                        .build()

                    client.newCall(request).execute().use { response ->
                        response.isSuccessful
                    }
                }
                if (result) {
                    Toast.makeText(context, "Report submitted. Thank you!", Toast.LENGTH_SHORT).show()
                    onBack()
                } else {
                    Toast.makeText(context, "Submission failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isSubmitting = false
            }
        }
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .graphicsLayer {
                    alpha = headerAlpha.value
                    translationY = headerOffsetY.value
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(inputBgColor)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = "Back",
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Report a problem",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.size(44.dp)) // centering spacer
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Describe the issue
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    alpha = field1Alpha.value
                    translationY = field1OffsetY.value
                }
        ) {
            Text(
                text = "Describe the issue",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = issueText,
                onValueChange = { issueText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(16.dp),
                placeholder = {
                    Text(text = "What went wrong? How can we help?", color = secondaryText)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBgColor,
                    unfocusedContainerColor = inputBgColor,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                textStyle = TextStyle(color = textColor)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Contact
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    alpha = field2Alpha.value
                    translationY = field2OffsetY.value
                }
        ) {
            Text(
                text = "Contact (email or phone)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = contactText,
                onValueChange = { contactText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                placeholder = {
                    Text(text = "Email or phone number", color = secondaryText)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBgColor,
                    unfocusedContainerColor = inputBgColor,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                textStyle = TextStyle(color = textColor),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Attach media
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    alpha = mediaAlpha.value
                    translationY = mediaOffsetY.value
                }
        ) {
            Text(
                text = "Attach photos or videos (optional)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Horizontal thumbnail/video strip — only visible when items are selected
            if (selectedImageUris.isNotEmpty()) {
                val imageLoader = remember {
                    ImageLoader.Builder(context)
                        .components {
                            add(VideoFrameDecoder.Factory())
                        }
                        .build()
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    items(selectedImageUris.size) { index ->
                        val uri = selectedImageUris[index]
                        val mimeType = context.contentResolver.getType(uri) ?: ""
                        val isVideo = mimeType.startsWith("video")
                        Box(modifier = Modifier.size(80.dp)) {
                            // Thumbnail — Coil handles both images and video frames
                            AsyncImage(
                                model = uri,
                                imageLoader = imageLoader,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            // Dark overlay + play icon for videos
                            if (isVideo) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.35f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_play),
                                        contentDescription = "Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            // Remove button
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .clickable { selectedImageUris.removeAt(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "✕",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // Add photos / Add more button
            val isFull = selectedImageUris.size >= maxImages
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(inputBgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .then(
                        if (!isFull) Modifier.clickable {
                            galleryLauncher.launch(arrayOf("image/*", "video/*"))
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = "Add photos",
                        tint = if (isFull) textColor.copy(alpha = 0.3f) else accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedImageUris.isEmpty()) "Add Media"
                               else "Add more (${selectedImageUris.size}/$maxImages)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isFull) textColor.copy(alpha = 0.3f) else accentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Submit button
        val isFormComplete = issueText.isNotBlank() && contactText.length >= 10
        val submitEnabled = isFormComplete && !isSubmitting

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    alpha = buttonAlpha.value
                    translationY = buttonOffsetY.value
                }
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (submitEnabled) accentColor else inputBgColor)
                .clickable(enabled = submitEnabled) { submitReport() },
            contentAlignment = Alignment.Center
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = accentColor,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Submit report",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (submitEnabled) Color.White else secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
