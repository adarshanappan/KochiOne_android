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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import coil.compose.AsyncImage
import com.kochione.kochi_one.models.ExploreContentBlock
import com.kochione.kochi_one.models.ExploreContentParser
import com.kochione.kochi_one.models.ExplorePost
import com.kochione.kochi_one.ui.components.shimmerEffect

@Composable
fun ExplorePostDetail(post: ExplorePost, isDarkTheme: Boolean, onClose: () -> Unit) {
    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val subtleTextColor = if (isDarkTheme) Color.LightGray else Color.DarkGray
    val uriHandler = LocalUriHandler.current
    
    val scrollState = rememberScrollState()
    
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
                    modifier = Modifier
                        .fillMaxSize()
                        .shimmerEffect(isDarkTheme)
                )

                // Gradient Overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                                endY = 400f
                            )
                        )
                )

                // Text at top left of image
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
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
                        
                        if (post.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = post.description,
                                color = subtleTextColor,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (post.accountLogoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = post.accountLogoUrl,
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .shimmerEffect(isDarkTheme),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        
                        if (post.redirectUrl.isNotBlank() && post.redirectUrl != "null") {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isDarkTheme) Color(0xFF333333) else Color(0xFFF0F0F0))
                                    .clickable {
                                        try {
                                            uriHandler.openUri(post.redirectUrl)
                                        } catch (e: Exception) {
                                            // Ignore if URL is invalid
                                        }
                                    },
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

                if (post.galleryImages.isNotEmpty()) {
                    post.galleryImages.forEach { galleryImage ->
                        if (galleryImage.url.isNotBlank()) {
                            AsyncImage(
                                model = galleryImage.url,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                val displayButtonLabel = post.buttonLabel.ifEmpty { "Learn More" }

                if (post.redirectUrl.isNotBlank() && post.redirectUrl != "null") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            try {
                                uriHandler.openUri(post.redirectUrl)
                            } catch (e: Exception) {
                                // Ignore if URL is invalid
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkTheme) Color(0xFF333333) else Color(0xFFF0F0F0),
                            contentColor = textColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = displayButtonLabel,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
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
