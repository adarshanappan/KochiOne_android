package com.kochione.kochi_one.views
 
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Circle
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
import coil.compose.AsyncImage
import com.kochione.kochi_one.R

@Composable
fun ProfileView(
    isDarkTheme: Boolean = false,
    selectedTheme: String,
    onThemeSelected: (String, androidx.compose.ui.geometry.Offset) -> Unit,
    isAutomatic: Boolean,
    onAutomaticToggle: (Boolean, androidx.compose.ui.geometry.Offset?) -> Unit,
    onEditClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val cardColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val secondaryTextColor = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.6f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // --- Profile Header ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .clickable { onEditClick() },
            shape = RoundedCornerShape(24.dp),
            color = cardColor,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD54F)), // Yellow-ish background for avatar
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profile_image),
                        contentDescription = "Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Name",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Tap to edit profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryTextColor
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Edit Profile",
                    tint = secondaryTextColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // --- Appearance Section ---
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Choose whether Kochi One follows your device appearance or stays in a fixed theme.",
            style = MaterialTheme.typography.bodyMedium,
            color = secondaryTextColor,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Theme Selection Cards
        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            shape = RoundedCornerShape(24.dp),
            color = cardColor,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Light Theme Preview
                    ThemeOption(
                        label = "Light",
                        isSelected = if (isAutomatic) !isDarkTheme else selectedTheme == "Light",
                        onSelect = { offset ->
                            onThemeSelected("Light", offset)
                            onAutomaticToggle(false, offset)
                        },
                        modifier = Modifier.weight(1f),
                        isDarkTheme = isDarkTheme
                    )

                    // Dark Theme Preview
                    ThemeOption(
                        label = "Dark",
                        isSelected = if (isAutomatic) isDarkTheme else selectedTheme == "Dark",
                        onSelect = { offset ->
                            onThemeSelected("Dark", offset)
                            onAutomaticToggle(false, offset)
                        },
                        modifier = Modifier.weight(1f),
                        isDarkTheme = isDarkTheme
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = secondaryTextColor.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                // Automatic Toggle
                var autoTogglePosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            autoTogglePosition = coordinates.positionInRoot()
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                onAutomaticToggle(!isAutomatic, autoTogglePosition + offset)
                            }
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "Sunset to Sunrise (6 PM - 6 AM)",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryTextColor
                        )
                    }
                    var switchPosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
                    Switch(
                        modifier = Modifier.onGloballyPositioned { coords ->
                            switchPosition = coords.positionInRoot() + androidx.compose.ui.geometry.Offset(coords.size.width / 2f, coords.size.height / 2f)
                        },
                        checked = isAutomatic,
                        onCheckedChange = { checked ->
                            onAutomaticToggle(checked, switchPosition) 
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2196F3)
                        )
                    )
                }
            }
        }

        // --- Preview Section ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(28.dp),
            color = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFECEEF1)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kochi One will always open in the theme you choose.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = secondaryTextColor,
                    lineHeight = 22.sp
                )
            }
        }

        // --- Liked & Saved Section ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            shape = RoundedCornerShape(28.dp),
            color = cardColor,
            tonalElevation = 2.dp
        ) {
            Column {
                ActionRow(
                    icon = R.drawable.ic_heart_filled,
                    iconBgColor = Color(0xFFFF3B30),
                    label = "Liked",
                    isDarkTheme = isDarkTheme,
                    onClick = { /* Handle Liked */ }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                    color = secondaryTextColor.copy(alpha = 0.1f)
                )
                ActionRow(
                    icon = R.drawable.ic_bookmark_filled,
                    iconBgColor = Color(0xFF007AFF),
                    label = "Saved",
                    isDarkTheme = isDarkTheme,
                    onClick = { /* Handle Saved */ }
                )
            }
        }

        // --- Support & Help Section ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            shape = RoundedCornerShape(28.dp),
            color = cardColor,
            tonalElevation = 2.dp
        ) {
            Column {
                ActionRow(
                    icon = R.drawable.ic_chat_bubble,
                    iconBgColor = Color(0xFFFF9500),
                    label = "Report a problem",
                    isDarkTheme = isDarkTheme,
                    onClick = { /* Handle Report */ }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                    color = secondaryTextColor.copy(alpha = 0.1f)
                )
                ActionRow(
                    icon = R.drawable.ic_help,
                    iconBgColor = Color(0xFFFF9500),
                    label = "Help",
                    isDarkTheme = isDarkTheme,
                    onClick = { /* Handle Help */ }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                    color = secondaryTextColor.copy(alpha = 0.1f)
                )
                ActionRow(
                    icon = R.drawable.ic_download,
                    iconBgColor = Color(0xFF007AFF),
                    label = "Check for update",
                    isDarkTheme = isDarkTheme,
                    onClick = { /* Handle Update */ }
                )
            }
        }

        // --- Settings & About Section ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            shape = RoundedCornerShape(28.dp),
            color = cardColor,
            tonalElevation = 2.dp
        ) {
            Column {
                ActionRow(
                    icon = R.drawable.ic_notifications,
                    iconBgColor = Color(0xFFFF3B30),
                    label = "Notification settings",
                    isDarkTheme = isDarkTheme,
                    onClick = { /* Handle Notifications */ }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(start = 64.dp, end = 16.dp),
                    color = secondaryTextColor.copy(alpha = 0.1f)
                )
                ActionRow(
                    icon = R.drawable.ic_info,
                    iconBgColor = Color(0xFF8E8E93),
                    label = "About",
                    isDarkTheme = isDarkTheme,
                    onClick = { /* Handle About */ }
                )
            }
        }

        
        Spacer(modifier = Modifier.height(100.dp)) // Extra space for bottom nav
    }
}

@Composable
fun ActionRow(
    icon: Int,
    iconBgColor: Color,
    label: String,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Container
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = if (isDarkTheme) Color.White else Color.Black,
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.2f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ThemeOption(
    label: String,
    isSelected: Boolean,
    onSelect: (androidx.compose.ui.geometry.Offset) -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean
) {
    val borderColor = if (isSelected) Color(0xFF2196F3) else Color.Transparent
    val tintColor = if (isSelected) Color(0xFF2196F3) else (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.5f)

    var globalPosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    Column(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                globalPosition = coordinates.positionInRoot()
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onSelect(globalPosition + offset)
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Preview Card
        Surface(
            modifier = Modifier
                .aspectRatio(0.75f)
                .fillMaxWidth()
                .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp
        ) {
            val previewBg = if (label == "Light") Color(0xFFF8F9FA) else Color(0xFF1E1E1E)
            val previewCard = if (label == "Light") Color.White else Color(0xFF2C2C2C)
            val previewAccent = Color(0xFF2196F3).copy(alpha = 0.4f)
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(previewBg)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mock Header
                Box(modifier = Modifier.fillMaxWidth().height(14.dp).clip(CircleShape).background(previewCard))
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Mock Content Card
                Box(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp)).background(previewCard)) {
                    // Mock Inner Accent
                    Box(modifier = Modifier.padding(8.dp).size(20.dp).clip(RoundedCornerShape(6.dp)).background(previewAccent))
                }
                
                // Mock Content Card 2
                Box(modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(12.dp)).background(previewCard))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color.White else Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Radio-style button
        Icon(
            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = tintColor,
            modifier = Modifier.size(28.dp)
        )
    }
}
