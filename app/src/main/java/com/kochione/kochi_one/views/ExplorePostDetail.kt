package com.kochione.kochi_one.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.kochione.kochi_one.models.ExplorePost

@Composable
fun ExplorePostDetail(post: ExplorePost, onClose: () -> Unit) {
    val isDarkTheme = isSystemInDarkTheme()
    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val subtleTextColor = if (isDarkTheme) Color.LightGray else Color.DarkGray
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor) 
    ) {
        // Top Image Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp) // Large image spanning top half
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
        ) {
            AsyncImage(
                model = post.mediaUrl,
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
                            startY = 400f // Starts gradient midway
                        )
                    )
            )
            
            // X button Top Right
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
            
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
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Detail section below image
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Row with Logo, Titles, Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (post.logoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = post.logoUrl,
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    if (post.subtitle.isNotEmpty()) {
                        Text(
                            text = post.subtitle.uppercase(),
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Text(
                        text = post.title,
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (post.description.isNotEmpty()) {
                        Text(
                            text = post.description,
                            color = subtleTextColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Button placeholder (Right Arrow)
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF333333))
                        .clickable { /* action */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = post.buttonLabel,
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Detail Body
            Text(
                text = post.detailBody,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 26.sp
            )
        }
    }
}
