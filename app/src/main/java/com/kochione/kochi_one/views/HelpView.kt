package com.kochione.kochi_one.views

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kochione.kochi_one.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HelpView(
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onReport: () -> Unit = {},
    onShare: () -> Unit = {},
    onAskLilly: (String) -> Unit = {}
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val secondaryText = textColor.copy(alpha = 0.55f)
    val inputBgColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF0F0F0)
    val cardColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF5F5F5)
    val bulletColor = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.45f)
    val chipBg = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF0F0F0)
    
    var showReportDialog by remember { mutableStateOf(false) }

    // Entrance animations
    
    val headerAlpha = remember { Animatable(0f) }
    val headerOffset = remember { Animatable(-30f) }
    val heroAlpha = remember { Animatable(0f) }
    val heroOffset = remember { Animatable(40f) }
    val contentAlpha = remember { Animatable(0f) }
    val contentOffset = remember { Animatable(50f) }

    LaunchedEffect(Unit) {
        launch { headerAlpha.animateTo(1f, tween(350, easing = EaseOutCubic)) }
        launch { headerOffset.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessLow)) }
        delay(80)
        launch { heroAlpha.animateTo(1f, tween(400, easing = EaseOutCubic)) }
        launch { heroOffset.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) }
        delay(100)
        launch { contentAlpha.animateTo(1f, tween(450, easing = EaseOutCubic)) }
        launch { contentOffset.animateTo(0f, spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

        // Hero Card — purple/lavender gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    alpha = heroAlpha.value
                    translationY = heroOffset.value
                }
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = if (isDarkTheme) listOf(
                            Color(0xFF2A1F4E),
                            Color(0xFF1E2A4A),
                            Color(0xFF2C2C2C)
                        ) else listOf(
                            Color(0xFFE8DEF8),
                            Color(0xFFD0BCFF).copy(alpha = 0.5f),
                            Color(0xFFF3EDF7)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF007AFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_help),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Help Centre",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "Everything you need to understand Kochi One.",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryText
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Learn the full app flow, discover what each tab does, and jump straight into chat whenever you want guided help.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryText,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Everything below uses content animation
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = contentAlpha.value
                    translationY = contentOffset.value
                }
        ) {
            // — Get Help Fast —
            Text(
                text = "Get Help Fast",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Message Lilly card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor)
                    .clickable { onAskLilly("Hey Lilly!") }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isDarkTheme) Color(0xFF3A3A3A) else Color(0xFFE8E8E8)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chat_bubble),
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Message Lilly",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Open chat with guided usage questions and app support.",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_near_me),
                    contentDescription = null,
                    tint = secondaryText,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick question chips — horizontal scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    "How do I use Kochi One?",
                    "How do I find nearby spots?",
                    "How does metro tracking work?",
                    "How to change theme?"
                ).forEach { question ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(chipBg)
                            .clickable { onAskLilly(question) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = question,
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // — App Guide —
            Text(
                text = "App Guide",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Getting Started
            GuideSection(
                icon = R.drawable.ic_star_new,
                iconBgColor = Color(0xFF007AFF),
                title = "Getting Started",
                subtitle = "Your first flow through the app.",
                bullets = listOf(
                    "Open Kochi One and complete the onboarding screens.",
                    "Allow location access so nearby places can be shown correctly.",
                    "Allow notifications if you want updates and reminders.",
                    "After that, the app opens to the map with the main bottom sheet."
                ),
                isDarkTheme = isDarkTheme,
                bulletColor = bulletColor,
                textColor = textColor,
                secondaryText = secondaryText
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Finding Places
            GuideSection(
                icon = R.drawable.ic_explore,
                iconBgColor = Color(0xFFFF6B35),
                title = "Finding Places",
                subtitle = "Use the right tab for the right kind of discovery.",
                bullets = listOf(
                    "Use Explore for story-style featured posts and happenings.",
                    "Use Eats for cafes, restaurants, pubs, bars, and food spots.",
                    "Use Play for sports, gaming, snooker, turf, and fun activities.",
                    "Use Fitness for gyms, yoga, MMA, studios, and health clubs."
                ),
                isDarkTheme = isDarkTheme,
                bulletColor = bulletColor,
                textColor = textColor,
                secondaryText = secondaryText
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Transit & Map Tools
            GuideSection(
                icon = R.drawable.ic_transit,
                iconBgColor = Color(0xFF34C759),
                title = "Transit & Map Tools",
                subtitle = "Use the live map and transit tools.",
                bullets = listOf(
                    "Use Transit to view metro-related information and route flow.",
                    "The floating tool buttons help with QR / transit actions and centering on your current location.",
                    "If metro mode is active, the floating action changes to the train action."
                ),
                isDarkTheme = isDarkTheme,
                bulletColor = bulletColor,
                textColor = textColor,
                secondaryText = secondaryText
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Profile & Support
            GuideSection(
                icon = R.drawable.ic_profile_placeholder,
                iconBgColor = Color(0xFFAF52DE),
                title = "Profile & Support",
                subtitle = "Everything personal and app-related lives here.",
                bullets = listOf(
                    "Open Profile to edit your account and image.",
                    "Liked and Saved help you revisit places quickly.",
                    "Report a problem, check for updates, and change Appearance from Profile.",
                    "About includes Instagram, website, email, and the enquiry form."
                ),
                isDarkTheme = isDarkTheme,
                bulletColor = bulletColor,
                textColor = textColor,
                secondaryText = secondaryText
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Best Ways to Use Kochi One — card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardColor)
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Best Ways to Use Kochi One",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val tips = listOf(
                        "Tap the message capsule in the header anytime to ask Lilly what to do next.",
                        "Use Likes and Saves while browsing so you can return to places later.",
                        "Switch tabs depending on what you need: editorial discovery, food, activities, fitness, or transit.",
                        "If you are unsure where something lives, ask Lilly directly from chat."
                    )

                    tips.forEach { tip ->
                        Row(
                            modifier = Modifier.padding(bottom = 14.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = null,
                                tint = Color(0xFF34C759),
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF34C759).copy(alpha = 0.15f))
                                    .padding(4.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                                lineHeight = 22.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        } // Closes inner Column
    } // Closes outer Column

    // Bottom floating pill with Back, Report, and Share
    val interactionSource = remember { MutableInteractionSource() }
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
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_left),
                contentDescription = "Back",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onBack() },
                tint = textColor
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(secondaryText.copy(alpha = 0.2f))
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_info), // Representing Report (!)
                contentDescription = "Report",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { showReportDialog = true },
                tint = textColor
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(secondaryText.copy(alpha = 0.2f))
            )
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
} // Closes outer Box
} // Closes HelpView


@Composable
private fun GuideSection(
    icon: Int,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    bullets: List<String>,
    isDarkTheme: Boolean,
    bulletColor: Color,
    textColor: Color,
    secondaryText: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Icon + Title
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = iconBgColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bullet points
        bullets.forEach { bullet ->
            Row(
                modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(bulletColor)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = bullet,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
