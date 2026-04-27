package com.kochione.kochi_one.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kochione.kochi_one.R
import com.kochione.kochi_one.models.Restaurant
import com.kochione.kochi_one.ui.components.shimmerEffect
import com.kochione.kochi_one.utils.LocationRepository
import com.kochione.kochi_one.utils.distanceInKm
import com.kochione.kochi_one.utils.isOpenAtNow
import com.kochione.kochi_one.utils.minutesUntilCloseIfOpen
import kotlinx.coroutines.launch

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val secondaryTextColor = if (isDarkTheme) Color.LightGray else Color.Gray

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo Image
            AsyncImage(
                model = restaurant.logo?.url ?: "https://via.placeholder.com/150",
                contentDescription = "Restaurant Logo",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isDarkTheme) Color(0xFF333333) else Color.White),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val todayHours = androidx.compose.runtime.remember(restaurant.operatingHours) {
                        val cal = java.util.Calendar.getInstance()
                        when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
                            java.util.Calendar.MONDAY -> restaurant.operatingHours.monday
                            java.util.Calendar.TUESDAY -> restaurant.operatingHours.tuesday
                            java.util.Calendar.WEDNESDAY -> restaurant.operatingHours.wednesday
                            java.util.Calendar.THURSDAY -> restaurant.operatingHours.thursday
                            java.util.Calendar.FRIDAY -> restaurant.operatingHours.friday
                            java.util.Calendar.SATURDAY -> restaurant.operatingHours.saturday
                            java.util.Calendar.SUNDAY -> restaurant.operatingHours.sunday
                            else -> restaurant.operatingHours.monday
                        }
                    }

                    val closeTime24 = todayHours.close
                    val timeStatus = run {
                        val calendar = java.util.Calendar.getInstance()
                        val nowMinutes =
                            calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                                calendar.get(java.util.Calendar.MINUTE)
                        try {
                            when {
                                todayHours.closed ->
                                    Pair("Closed", Color(0xFFFF6B6B))
                                !todayHours.isOpenAtNow(nowMinutes) ->
                                    Pair("Closed", Color(0xFFFF6B6B))
                                else -> {
                                    val untilClose = todayHours.minutesUntilCloseIfOpen(nowMinutes)
                                    if (untilClose != null && untilClose in 0..60) {
                                        Pair("Closes soon", Color(0xFFFFD54F))
                                    } else {
                                        Pair("Open", Color(0xFF00C853))
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Pair("Closed", Color(0xFFFF6B6B))
                        }
                    }

                    // Open indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(timeStatus.second)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timeStatus.first,
                        fontWeight = FontWeight.Bold,
                        color = timeStatus.second,
                        fontSize = 14.sp
                    )
                    val formattedCloseTime = androidx.compose.runtime.remember(closeTime24) {
                        try {
                            if (closeTime24 != null) {
                                val parts = closeTime24.split(":")
                                if (parts.size >= 2) {
                                    val hour24 = parts[0].toIntOrNull() ?: 0
                                    val minute = parts[1]
                                    val amPm = if (hour24 >= 12) "PM" else "AM"
                                    val hour12 = when {
                                        hour24 == 0 -> 12
                                        hour24 > 12 -> hour24 - 12
                                        else -> hour24
                                    }
                                    "$hour12:$minute $amPm"
                                } else {
                                    closeTime24
                                }
                            } else {
                                "11:00 PM"
                            }
                        } catch (e: Exception) {
                            closeTime24 ?: "11:00 PM"
                        }
                    }

                    if (timeStatus.first != "Closed") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "· closes $formattedCloseTime",
                            color = secondaryTextColor,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Distance from user's GPS (hidden when location not yet available)
            val userLocation by LocationRepository.location.collectAsState()
            val distanceKm = userLocation?.let { loc ->
                distanceInKm(
                    loc.first, loc.second,
                    restaurant.location.latitude,
                    restaurant.location.longitude
                )
            }
            if (distanceKm != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "%.1f km".format(distanceKm),
                        color = secondaryTextColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_transit),
                        contentDescription = "Distance",
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = restaurant.description,
            style = MaterialTheme.typography.bodyLarge,
            color = secondaryTextColor,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of 4 Images (Assuming top 4 images fit the 2x2 grid)
        val displayImages = restaurant.coverImages.take(4)
        if (displayImages.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row
                Row(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    if (displayImages.isNotEmpty()) {
                        AsyncImage(
                            model = displayImages[0].url,
                            contentDescription = "Cover Image 1",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    if (displayImages.size > 1) {
                        AsyncImage(
                            model = displayImages[1].url,
                            contentDescription = "Cover Image 2",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Bottom Row
                Row(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    if (displayImages.size > 2) {
                        AsyncImage(
                            model = displayImages[2].url,
                            contentDescription = "Cover Image 3",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 4.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    if (displayImages.size > 3) {
                        AsyncImage(
                            model = displayImages[3].url,
                            contentDescription = "Cover Image 4",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconAction(painterResId = R.drawable.ic_call, contentDescription = "Call", isDarkTheme = isDarkTheme)
            IconAction(painterResId = R.drawable.ic_location_custom, contentDescription = "Directions", isDarkTheme = isDarkTheme)
            var isFavorited by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            val heartIcon = if (isFavorited) R.drawable.ic_heart_filled else R.drawable.ic_heart
            val heartTint = if (isFavorited) Color.Red else (if (isDarkTheme) Color.LightGray else Color.Gray)
            
            val scale = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(1f) }
            val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
            
            Icon(
                painter = painterResource(id = heartIcon),
                contentDescription = "Favorite",
                tint = heartTint,
                modifier = Modifier
                    .size(48.dp)
                    .scale(scale.value)
                    .clickable(
                        interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null // Removes standard ripple for cleaner animation
                    ) { 
                        isFavorited = !isFavorited
                        if (isFavorited) {
                            coroutineScope.launch {
                                scale.animateTo(
                                    targetValue = 1.3f,
                                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 150)
                                )
                                scale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = androidx.compose.animation.core.spring(
                                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                    )
                                )
                            }
                        } else {
                            coroutineScope.launch {
                                scale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 150)
                                )
                            }
                        }
                    }
                    .padding(12.dp)
            )
            var isSaved by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            val saveIcon = if (isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark
            val saveTint = if (isSaved) (if (isDarkTheme) Color.White else Color.Black) else (if (isDarkTheme) Color.LightGray else Color.Gray)
            val saveScale = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(1f) }
            
            Icon(
                painter = painterResource(id = saveIcon),
                contentDescription = "Save",
                tint = saveTint,
                modifier = Modifier
                    .size(48.dp)
                    .scale(saveScale.value)
                    .clickable(
                        interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) { 
                        isSaved = !isSaved
                        if (isSaved) {
                            coroutineScope.launch {
                                saveScale.animateTo(
                                    targetValue = 1.2f,
                                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 100)
                                )
                                saveScale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = androidx.compose.animation.core.spring(
                                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                    )
                                )
                            }
                        } else {
                            coroutineScope.launch {
                                saveScale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 100)
                                )
                            }
                        }
                    }
                    .padding(12.dp)
            )
            IconAction(painterResId = R.drawable.ic_share, contentDescription = "Share", isDarkTheme = isDarkTheme)
        }
        androidx.compose.material3.HorizontalDivider(
            color = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.12f),
            thickness = 0.5.dp,
            modifier = Modifier.padding(
                top = 12.dp,
                bottom = 2.dp
            )
        )
    }
}

@Composable
fun IconAction(painterResId: Int, contentDescription: String, isDarkTheme: Boolean) {
    val tint = if (isDarkTheme) Color.LightGray else Color.Gray
    Icon(
        painter = painterResource(id = painterResId),
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier
            .size(48.dp)
            .clickable(
                interactionSource = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { /* Action */ }
            .padding(12.dp)
    )
}

@Composable
fun RestaurantSkeletonCard(isDarkTheme: Boolean, modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo Image
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .shimmerEffect(isDarkTheme)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect(isDarkTheme)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .shimmerEffect(isDarkTheme)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect(isDarkTheme)
                    )
                }
            }

            // Distance
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect(isDarkTheme)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect(isDarkTheme)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect(isDarkTheme)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect(isDarkTheme)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of 4 Images (Assuming top 4 images fit the 2x2 grid)
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Row
            Row(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                        .shimmerEffect(isDarkTheme)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                        .shimmerEffect(isDarkTheme)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Bottom Row
            Row(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 4.dp))
                        .shimmerEffect(isDarkTheme)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                        .shimmerEffect(isDarkTheme)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(12.dp)
                        .clip(CircleShape)
                        .shimmerEffect(isDarkTheme)
                )
            }
        }
        androidx.compose.material3.HorizontalDivider(
            color = Color.Gray.copy(alpha = 0.3f),
            thickness = 0.5.dp,
            modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
        )
    }
}
