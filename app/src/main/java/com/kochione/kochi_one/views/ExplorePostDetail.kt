package com.kochione.kochi_one.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kochione.kochi_one.models.ExploreContentBlock
import com.kochione.kochi_one.models.ExploreContentParser
import com.kochione.kochi_one.models.ExplorePost

@Composable
fun ExplorePostDetail(post: ExplorePost, onClose: () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val subtleTextColor = if (isDarkTheme) Color.LightGray else Color.DarkGray
    
    val scrollState = rememberScrollState()
    // Calculate alpha for sticky header based on scroll (max at 400px equivalent scroll)
    val headerAlpha = (scrollState.value / 400f).coerceIn(0f, 1f)
    val headerBgColor = bgColor.copy(alpha = headerAlpha)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
    ) {
        // Main Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Banner Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            ) {
                AsyncImage(
                    model = post.bannerImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Gradient Overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 400f
                            )
                        )
                )

                // Text at bottom left of image
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    if (post.eyebrow.isNotEmpty()) {
                        Text(
                            text = post.eyebrow.uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = post.title,
                        color = Color.White,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info Section below image
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                if (post.accountName.isNotEmpty()) {
                    Text(
                        text = post.accountName.uppercase(),
                        color = Color.Gray,
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 1.sp
                    )
                }
                
                Text(
                    text = post.title,
                    color = textColor,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (post.description.isNotEmpty()) {
                        Text(
                            text = post.description,
                            color = subtleTextColor,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Account Logo and Arrow Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        if (post.accountLogoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = post.accountLogoUrl,
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isDarkTheme) Color(0xFF333333) else Color(0xFFF0F0F0))
                                .clickable { /* action */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = post.buttonLabel,
                                tint = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Detail Body Parsed Content
                val contentBlocks = remember(post.detailBody) {
                    ExploreContentParser.parse(post.detailBody)
                }

                contentBlocks.forEach { block ->
                    RenderContentBlock(block, textColor)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun RenderContentBlock(block: ExploreContentBlock, textColor: Color) {
    when (block) {
        is ExploreContentBlock.Title -> {
            Text(
                text = block.text,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
        is ExploreContentBlock.SectionHeading -> {
            Text(
                text = block.text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        is ExploreContentBlock.Subheading -> {
            Text(
                text = block.text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        is ExploreContentBlock.Paragraph -> {
            Text(
                text = block.text,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 26.sp,
                color = textColor
            )
        }
        is ExploreContentBlock.BulletPoint -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = block.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    lineHeight = 24.sp
                )
            }
        }
    }
}
