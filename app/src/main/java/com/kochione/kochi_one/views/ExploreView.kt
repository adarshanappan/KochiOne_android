package com.kochione.kochi_one.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kochione.kochi_one.models.ExplorePost
import com.kochione.kochi_one.ui.components.shimmerEffect
import com.kochione.kochi_one.viewmodels.ExploreViewModel

@Composable
fun ExploreView(
    isDarkTheme: Boolean,
    viewModel: ExploreViewModel = viewModel(),
    externalSelectedPost: ExplorePost? = null,
    onPostSelected: (ExplorePost?) -> Unit = {}
) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()


    var selectedPost by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<ExplorePost?>(null) }
    
    // Sync internal state with external parameter (e.g. when global back is pressed)
    androidx.compose.runtime.LaunchedEffect(externalSelectedPost) {
        if (externalSelectedPost != selectedPost) {
            selectedPost = externalSelectedPost
        }
    }

    // Sync with external listener (e.g. when card is clicked)
    androidx.compose.runtime.LaunchedEffect(selectedPost) {
        onPostSelected(selectedPost)
    }

    // Theme handled by parameter
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
                    initialOffsetY = { it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                        fadeOut(animationSpec = tween(durationMillis = 150))
            } else {
                // Collapsing back to list
                fadeIn(animationSpec = tween(durationMillis = 300)) togetherWith
                        slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + fadeOut(animationSpec = tween(durationMillis = 300))
            }
        },
        label = "ExploreTransition"
    ) { postToDisplay ->
        if (postToDisplay != null) {
            // Detailed View
            ExplorePostDetail(
                post = postToDisplay,
                isDarkTheme = isDarkTheme,
                onClose = { selectedPost = null }
            )
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
                if (isLoading && posts.isEmpty()) {
                    items(3) { // Show 3 skeleton cards while loading
                        ExploreSkeletonCard(isDarkTheme = isDarkTheme)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else if (!isLoading && errorMessage != null) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = textColor.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Oops! Something went wrong",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.fetchPosts() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = cardBgColor,
                                    contentColor = textColor
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Refresh", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                } else if (!isLoading && posts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Refresh, // Placeholder for empty
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = textColor.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No happenings right now.",
                                style = MaterialTheme.typography.titleMedium,
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Check back later for new events!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = subtleTextColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(posts) { post ->
                        ExploreSummaryCard(
                            post = post,
                            isDarkTheme = isDarkTheme,
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
    isDarkTheme: Boolean,
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
                    model = post.bannerImageUrl,
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
                if (post.accountLogoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = post.accountLogoUrl,
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    if (post.accountName.isNotEmpty()) {
                        Text(
                            text = post.accountName.uppercase(),
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
                        .background(if (isDarkTheme) Color(0xFF333333) else Color(0xFFF0F0F0)), 
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = post.buttonLabel,
                        tint = textColor,
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

//}
