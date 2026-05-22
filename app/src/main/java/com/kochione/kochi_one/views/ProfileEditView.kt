package com.kochione.kochi_one.views

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.kochione.kochi_one.R

// Removed local CountryCode definitions, now using ProfileManager.CountryCode

@Composable
fun ProfileEditView(isDarkTheme: Boolean = false, onBack: () -> Unit, onSave: () -> Unit, onReport: () -> Unit = {}) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("kochi_one_prefs", Context.MODE_PRIVATE) }

    val snapshot = remember { com.kochione.kochi_one.utils.ProfileManager.snapshot() }
    
    var name by remember { mutableStateOf(snapshot.username) }
    var mobile by remember { mutableStateOf(snapshot.mobile) }
    var imageData by remember { mutableStateOf(snapshot.imageData) }
    
    var selectedCountryCode by remember { 
        val savedCode = snapshot.countryCode
        mutableStateOf(com.kochione.kochi_one.utils.ProfileManager.countryCodes.find { it.code == savedCode } ?: com.kochione.kochi_one.utils.ProfileManager.countryCodes[0])
    }
    
    val coroutineScope = rememberCoroutineScope()
    val deviceId = remember { android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID) ?: "unknown_device" }

    var expandedCountryDropdown by remember { mutableStateOf(false) }

    var cropImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            cropImageUri = uri
        }
    }
    
    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val textFieldBg = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF0F2F5)
    val labelColor = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Gray
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val dividerColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.2f)
    
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    var showReportDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // --- Avatar Section ---
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color(0xFFFFD54F) // Yellow background matching screenshot
                ) {
                    if (imageData != null) {
                        AsyncImage(
                            model = imageData,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.profile_image),
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Camera Edit Button
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    color = Color(0xFF007AFF), // Blue matching screenshot
                    shape = CircleShape,
                    tonalElevation = 4.dp
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = "Edit Photo",
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize(),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- Name Input ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Name",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = labelColor,
                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = textFieldBg
                ) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Your name", color = textColor.copy(alpha = 0.3f)) },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = textColor),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Mobile Input ---
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Mobile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = labelColor,
                    modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = textFieldBg
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Country Code Selector
                        Box {
                            Row(
                                modifier = Modifier
                                    .padding(start = 24.dp)
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = null
                                    ) { expandedCountryDropdown = true },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedCountryCode.code,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF007AFF),
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFF007AFF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            MaterialTheme(
                                shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(24.dp))
                            ) {
                                DropdownMenu(
                                    expanded = expandedCountryDropdown,
                                    onDismissRequest = { expandedCountryDropdown = false },
                                    modifier = Modifier
                                        .background(if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF4F5F8))
                                        .heightIn(max = 500.dp)
                                        .width(260.dp)
                                ) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    com.kochione.kochi_one.utils.ProfileManager.countryCodes.forEach { country ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(
                                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    selectedCountryCode = country
                                                    expandedCountryDropdown = false
                                                }
                                                .padding(horizontal = 24.dp, vertical = 12.dp)
                                        ) {
                                            Text(
                                                text = "${country.name} (${country.code})", 
                                                color = textColor,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        VerticalDivider(
                            modifier = Modifier
                                .height(24.dp)
                                .padding(horizontal = 16.dp),
                            color = Color.LightGray.copy(alpha = 0.3f)
                        )

                        TextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            placeholder = { Text("Mobile number", color = textColor.copy(alpha = 0.3f)) },
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(color = textColor),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp)) // Extra space for bottom nav
        }

        // --- Bottom Action Bar ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 16.dp)
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
                // Back Icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onBack() },
                    tint = textColor
                )

                Box(modifier = Modifier.width(1.dp).height(20.dp).background(dividerColor))

                // Save / Tick Mark
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "Save",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            com.kochione.kochi_one.utils.ProfileManager.updateAndSyncToBackend(
                                context = context,
                                username = name,
                                mobile = mobile,
                                imageData = imageData,
                                countryCode = selectedCountryCode.code
                            )
                            onSave()
                        },
                    tint = textColor
                )

                Box(modifier = Modifier.width(1.dp).height(20.dp).background(dividerColor))

                // Report Icon
                Icon(
                    painter = painterResource(id = R.drawable.ic_report_pill),
                    contentDescription = "Report",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { showReportDialog = true },
                    tint = textColor
                )
            }
        } // Closes the bottom action bar Box
    } // Closes main Box

    if (cropImageUri != null) {
        ImageCropView(
            uri = cropImageUri!!,
            isDarkTheme = isDarkTheme,
            onCancel = { cropImageUri = null },
            onCropSuccess = { croppedUri ->
                imageData = croppedUri
                cropImageUri = null
                // Optional: instantly update global manager so other parts of the app show the change
                com.kochione.kochi_one.utils.ProfileManager.updateProfileImage(context, croppedUri)
            }
        )
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
                        color = textColor.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
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
