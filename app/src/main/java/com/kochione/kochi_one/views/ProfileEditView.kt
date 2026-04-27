package com.kochione.kochi_one.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kochione.kochi_one.R

@Composable
fun ProfileEditView(isDarkTheme: Boolean = false, onBack: () -> Unit, onSave: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    
    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFECEEF1)
    val textFieldBg = if (isDarkTheme) Color(0xFF2C2C2C) else Color.White
    val labelColor = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color.Gray
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val bottomBarBg = if (isDarkTheme) Color(0xFF2C2C2C).copy(alpha = 0.95f) else Color(0xFFF0F2F5).copy(alpha = 0.95f)
    val dividerColor = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.5f)
    val iconTint = if (isDarkTheme) Color.White else Color.DarkGray

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    Image(
                        painter = painterResource(id = R.drawable.profile_image),
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Camera Edit Button
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { /* Handle photo edit */ },
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
                        Row(
                            modifier = Modifier
                                .padding(start = 24.dp)
                                .clickable { /* Open country selector */ },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "+91",
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
        }

        // --- Bottom Action Bar ---
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .width(280.dp)
                .height(72.dp),
            shape = RoundedCornerShape(36.dp),
            color = bottomBarBg,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Save
                IconButton(onClick = onSave) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = "Save",
                        modifier = Modifier.size(28.dp),
                        tint = iconTint
                    )
                }

                VerticalDivider(
                    modifier = Modifier.height(32.dp),
                    color = dividerColor
                )

                // Info
                IconButton(onClick = { /* Handle Info */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = "Info",
                        modifier = Modifier.size(28.dp),
                        tint = iconTint
                    )
                }
            }
        }
    }
}
