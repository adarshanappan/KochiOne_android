package com.kochione.kochi_one.views

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kochione.kochi_one.models.ExplorePost
import com.kochione.kochi_one.viewmodels.ExploreViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState

@Composable
fun ExploreView(viewModel: ExploreViewModel = viewModel()) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()


    var selectedPost by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<ExplorePost?>(null) }

    val isDarkTheme = isSystemInDarkTheme()
    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val cardBgColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFE0E0E0)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val subtleTextColor = if (isDarkTheme) Color.LightGray else Color.DarkGray

    AnimatedContent(
        targetState = selectedPost,
        transitionSpec = {
            if (targetState != null) {
                // Expanding into detail
                slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = 150))
            } else {
                // Collapsing back to list
                fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                        slideOutVertically(
                            targetOffsetY = { 100 },
                            animationSpec = tween(durationMillis = 300)
                        ) + fadeOut(animationSpec = tween(durationMillis = 300))
            }
        },
        label = "ExploreTransition"
    ) { postToDisplay ->
        if (postToDisplay != null) {
            // Detailed View
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(bgColor),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    ExplorePostDetail(
                        post = postToDisplay,
                        onClose = { selectedPost = null }
                    )
                }
            }
        } else {
            // List View
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(bgColor),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                item {
                    Text(
                        text = "Kochi Happenings",
                        style = MaterialTheme.typography.headlineLarge,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (isLoading) {
                    items(3) { // Show 3 skeleton cards while loading
                        ExploreSkeletonCard(isDarkTheme = isDarkTheme)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else {
                    items(posts) { post ->
                        ExploreSummaryCard(
                            post = post,
                            cardBgColor = cardBgColor,
                            textColor = textColor,
                            subtleTextColor = subtleTextColor,
                            onClick = { selectedPost = post }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreSummaryCard(
    post: ExplorePost, 
    cardBgColor: Color, 
    textColor: Color, 
    subtleTextColor: Color, 
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBgColor)
            .clickable(onClick = onClick)
    ) {
        Column {
            // Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 150f
                            )
                        )
                )
                
                // Text at bottom left of image
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Details Strip (Logo, titles, arrow)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (post.logoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = post.logoUrl,
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
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
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF333333)), // Arrow background color
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = post.buttonLabel,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExploreSkeletonCard(isDarkTheme: Boolean) {
    val cardBgColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFE0E0E0)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBgColor)
    ) {
        Column {
            // Skeleton Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .shimmerEffect(isDarkTheme)
            )
            
            // Skeleton Details Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skeleton Logo
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmerEffect(isDarkTheme)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Skeleton Texts
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .shimmerEffect(isDarkTheme)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .shimmerEffect(isDarkTheme)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .shimmerEffect(isDarkTheme)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Skeleton Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .shimmerEffect(isDarkTheme)
                )
            }
        }
    }
}

fun Modifier.shimmerEffect(isDarkTheme: Boolean): Modifier = composed {
    var size by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(androidx.compose.ui.unit.IntSize.Zero)
    }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500)
        ),
        label = "shimmer_offset"
    )

    val shimmerColors = if (isDarkTheme) {
        listOf(
            Color(0xFF333333),
            Color(0xFF444444),
            Color(0xFF333333)
        )
    } else {
        listOf(
            Color(0xFFCCCCCC),
            Color(0xFFEEEEEE),
            Color(0xFFCCCCCC)
        )
    }

    background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    )
    .onGloballyPositioned {
        size = it.size
    }
}
