package com.kochione.kochi_one.views
 
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.graphicsLayer
import coil.compose.AsyncImage
import com.kochione.kochi_one.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val LocalProfileScrollState = androidx.compose.runtime.compositionLocalOf<androidx.compose.foundation.ScrollState> { error("No Profile Scroll State provided") }

@Composable
fun ProfileView(
    isDarkTheme: Boolean = false,
    selectedTheme: String,
    onThemeSelected: (String, androidx.compose.ui.geometry.Offset) -> Unit,
    isAutomatic: Boolean,
    onAutomaticToggle: (Boolean, androidx.compose.ui.geometry.Offset?) -> Unit,
    onEditClick: () -> Unit,
    onLikedClick: () -> Unit,
    onSavedClick: () -> Unit,
    onReportClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onBack: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    val scrollState = LocalProfileScrollState.current
    var showReportDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("kochi_one_prefs", android.content.Context.MODE_PRIVATE) }
    
    // Read profile data
    val profileName = prefs.getString("profile_username", "") ?: ""
    val displayName = if (profileName.isNotBlank()) profileName else "Name"
    val profileImageData = prefs.getString("profile_imageData", null)

    // isDarkTheme here = sheetIsDark (for visual consistency with the sheet)
    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val cardColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val secondaryTextColor = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.6f)
    // For theme SELECTION STATE: use the actual effective theme (system when automatic)
    val systemDark = isSystemInDarkTheme()
    val effectiveForSelection = if (isAutomatic) systemDark else (selectedTheme == "Dark")

    // Entrance animations removed as requested by user

    // Disable overscroll stretch effect (shaking at the top/bottom)
    val overscrollDisabler = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPostScroll(
                consumed: androidx.compose.ui.geometry.Offset,
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                return available // Consume unconsumed scroll to prevent stretch effect
            }
            override suspend fun onPostFling(
                consumed: androidx.compose.ui.unit.Velocity,
                available: androidx.compose.ui.unit.Velocity
            ): androidx.compose.ui.unit.Velocity {
                return available // Consume unconsumed fling to prevent stretch effect
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .nestedScroll(overscrollDisabler)
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
                    if (profileImageData != null) {
                        AsyncImage(
                            model = profileImageData,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.profile_image),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
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
            modifier = Modifier
                .padding(bottom = 8.dp)
        )
        Text(
            text = "Choose whether Kochi One follows your device appearance or stays in a fixed theme.",
            style = MaterialTheme.typography.bodyMedium,
            color = secondaryTextColor,
            modifier = Modifier
                .padding(bottom = 24.dp)
        )

        // Theme Selection Cards
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
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
                        isSelected = !isAutomatic && !effectiveForSelection,
                        onSelect = { offset ->
                            onThemeSelected("Light", offset)
                        },
                        modifier = Modifier.weight(1f),
                        isDarkTheme = isDarkTheme
                    )

                    // Dark Theme Preview
                    ThemeOption(
                        label = "Dark",
                        isSelected = !isAutomatic && effectiveForSelection,
                        onSelect = { offset ->
                            onThemeSelected("Dark", offset)
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
                            text = "Automatic",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "Follow device appearance",
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
                    onClick = onLikedClick
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
                    onClick = onSavedClick
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
                    onClick = onReportClick
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
                    onClick = onHelpClick
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
                    onClick = onAboutClick
                )
            }
        }

        
        Spacer(modifier = Modifier.height(160.dp)) // Extra space for bottom nav and capsule
        } // Closes Column

        // Bottom floating pill with Back, Report, and Share
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
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
                Box(modifier = Modifier.width(1.dp).height(20.dp).background(secondaryTextColor.copy(alpha = 0.2f)))
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
                Box(modifier = Modifier.width(1.dp).height(20.dp).background(secondaryTextColor.copy(alpha = 0.2f)))
                Icon(
                    painter = painterResource(id = R.drawable.ic_share),
                    contentDescription = "Share",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onShare() },
                    tint = textColor
                )
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
                            color = secondaryTextColor
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
                                        onReportClick()
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
    } // Closes Box
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
    val unselectedTint = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.3f)

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF2196F3) else Color.Transparent,
        animationSpec = tween(300),
        label = "borderColor"
    )
    val tintColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF2196F3) else unselectedTint,
        animationSpec = tween(300),
        label = "tintColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

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
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = if (isSelected) 8.dp else 4.dp
        ) {
            val previewRes = if (label == "Light") {
                R.drawable.theme_preview_light_map
            } else {
                R.drawable.theme_preview_dark_map
            }
            Image(
                painter = painterResource(id = previewRes),
                contentDescription = "$label theme preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
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
