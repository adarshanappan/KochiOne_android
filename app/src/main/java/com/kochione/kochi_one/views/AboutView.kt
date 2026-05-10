package com.kochione.kochi_one.views

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kochione.kochi_one.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@Composable
fun AboutView(
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onReport: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    val context = LocalContext.current
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val secondaryText = textColor.copy(alpha = 0.6f)
    val inputBgColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF2F2F2)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
    val accentColor = Color(0xFF007AFF)

    var nameText by remember { mutableStateOf("") }
    var emailText by remember { mutableStateOf("") }
    var phoneText by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }

    var showReportDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    val isFormComplete = nameText.isNotBlank() && emailText.isNotBlank() && messageText.isNotBlank()

    val coroutineScope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Kochi One Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .background(cardBgColor)
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Kochi One",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your handbook to Kochi city. Discover places, follow updates, and stay connected with what is happening around you.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryText,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Connect Section
            Text(
                text = "Connect",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Connect Items
            ConnectItem(
                iconRes = R.drawable.ic_camera, // Placeholder for Instagram
                title = "Follow on Instagram",
                subtitle = "@kochioneapp",
                isDarkTheme = isDarkTheme,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/kochioneapp"))
                    context.startActivity(intent)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ConnectItem(
                iconRes = R.drawable.ic_browser_chrome,
                title = "Visit Website",
                subtitle = "www.kochi.one",
                isDarkTheme = isDarkTheme,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.kochi.one"))
                    context.startActivity(intent)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ConnectItem(
                iconRes = R.drawable.ic_email,
                title = "Email Us",
                subtitle = "mail@kochi.one",
                isDarkTheme = isDarkTheme,
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:mail@kochi.one")
                    }
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Enquiry Form Section
            Text(
                text = "Enquiry Form",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .background(cardBgColor)
                    .padding(20.dp)
            ) {
                Column {
                    FormLabel(text = "Your name", textColor = textColor)
                    FormTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        placeholder = "Enter your name",
                        inputBgColor = inputBgColor,
                        textColor = textColor,
                        secondaryText = secondaryText
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    FormLabel(text = "Email address", textColor = textColor)
                    FormTextField(
                        value = emailText,
                        onValueChange = { emailText = it },
                        placeholder = "Enter your email address",
                        inputBgColor = inputBgColor,
                        textColor = textColor,
                        secondaryText = secondaryText
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    FormLabel(text = "Phone number (optional)", textColor = textColor)
                    FormTextField(
                        value = phoneText,
                        onValueChange = { phoneText = it },
                        placeholder = "Enter your phone number",
                        inputBgColor = inputBgColor,
                        textColor = textColor,
                        secondaryText = secondaryText
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    FormLabel(text = "Message", textColor = textColor)
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text(text = "", color = secondaryText) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = inputBgColor,
                            unfocusedContainerColor = inputBgColor,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        textStyle = TextStyle(color = textColor)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Submit button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(if (isFormComplete && !isSubmitting) accentColor else inputBgColor)
                            .clickable(enabled = isFormComplete && !isSubmitting) {
                                coroutineScope.launch {
                                    isSubmitting = true
                                    try {
                                        val result = withContext(Dispatchers.IO) {
                                            val client = OkHttpClient.Builder()
                                                .connectTimeout(30, TimeUnit.SECONDS)
                                                .writeTimeout(60, TimeUnit.SECONDS)
                                                .readTimeout(30, TimeUnit.SECONDS)
                                                .build()

                                            val jsonObject = JSONObject().apply {
                                                put("name", nameText.trim())
                                                put("email", emailText.trim())
                                                put("phone", phoneText.trim())
                                                put("message", messageText.trim())
                                            }

                                            val request = Request.Builder()
                                                .url("https://api.kochi.one/api/enquiries")
                                                .post(jsonObject.toString().toRequestBody("application/json".toMediaType()))
                                                .build()

                                            client.newCall(request).execute().use { response ->
                                                response.isSuccessful
                                            }
                                        }
                                        if (result) {
                                            Toast.makeText(context, "Enquiry sent successfully!", Toast.LENGTH_SHORT).show()
                                            nameText = ""
                                            emailText = ""
                                            phoneText = ""
                                            messageText = ""
                                        } else {
                                            Toast.makeText(context, "Failed to send enquiry. Please try again.", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            },
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
                                text = "Send Enquiry",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isFormComplete) Color.White else secondaryText.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }

        // Bottom floating pill with Back, Report, and Share
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(50))
                .background(if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF2F2F2))
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isDarkTheme) Color(0xFF3A3A3A) else Color(0xFFE5E5E5))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_left),
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp),
                        tint = textColor
                    )
                }
                Box(modifier = Modifier.width(1.dp).height(20.dp).background(secondaryText.copy(alpha = 0.2f)))
                Icon(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = "Report",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { showReportDialog = true },
                    tint = textColor
                )
                Box(modifier = Modifier.width(1.dp).height(20.dp).background(secondaryText.copy(alpha = 0.2f)))
                Icon(
                    painter = painterResource(id = R.drawable.ic_share),
                    contentDescription = "Share",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onShare() },
                    tint = textColor
                )
            }
        }
    }

    if (showReportDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showReportDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF2F2F2))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Report an issue",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Open the report form to send feedback or report a problem.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryText
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (isDarkTheme) Color(0xFF444444) else Color(0xFFE5E5E5))
                                .clickable { showReportDialog = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Cancel", color = textColor, fontWeight = FontWeight.Medium)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (isDarkTheme) Color(0xFF444444) else Color(0xFFE5E5E5))
                                .clickable {
                                    showReportDialog = false
                                    onReport()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Report", color = textColor, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val secondaryText = textColor.copy(alpha = 0.5f)
    val cardBgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val borderColor = if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(textColor)
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = cardBgColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText
                )
            }
            Icon(
                imageVector = Icons.Default.CallMade,
                contentDescription = null,
                tint = secondaryText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun FormLabel(text: String, textColor: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    inputBgColor: Color,
    textColor: Color,
    secondaryText: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        placeholder = { Text(text = placeholder, color = secondaryText, style = MaterialTheme.typography.bodyMedium) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = inputBgColor,
            unfocusedContainerColor = inputBgColor,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        textStyle = TextStyle(color = textColor, fontSize = 14.sp),
        singleLine = true
    )
}
