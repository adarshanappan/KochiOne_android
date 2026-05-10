package com.kochione.kochi_one.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kochione.kochi_one.R
import com.kochione.kochi_one.utils.LikedSavedStore
import com.kochione.kochi_one.utils.SavedBucket
import com.kochione.kochi_one.utils.SavedItem
import com.kochione.kochi_one.utils.SavedSection

@Composable
fun LikedSavedItemsView(
    isDarkTheme: Boolean,
    bucket: SavedBucket,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val items = remember(bucket) { mutableStateListOf<SavedItem>().apply { addAll(LikedSavedStore.all(context, bucket)) } }
    var openedItem by remember(bucket) { mutableStateOf<SavedItem?>(null) }
    val bg = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val text = if (isDarkTheme) Color.White else Color.Black
    val subText = text.copy(alpha = 0.66f)

    AnimatedContent(
        targetState = openedItem,
        transitionSpec = {
            fadeIn(animationSpec = tween(260)) togetherWith fadeOut(animationSpec = tween(220))
        },
        label = "liked_saved_open_transition"
    ) { selected ->
        if (selected != null) {
            OpenedSavedItemFullView(
                item = selected,
                bucket = bucket,
                isDarkTheme = isDarkTheme,
                context = context,
                onBack = { openedItem = null },
                onRemove = {
                    LikedSavedStore.setInBucket(context, bucket, selected, false)
                    items.remove(selected)
                    openedItem = null
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF0F0F0))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chevron_left),
                            contentDescription = "Back",
                            tint = text,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = if (bucket == SavedBucket.LIKED) "Liked" else "Saved",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = text
                    )
                }

                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No ${if (bucket == SavedBucket.LIKED) "liked" else "saved"} items yet",
                            color = subText
                        )
                    }
                } else {
                    val grouped = items.groupBy { it.section }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        grouped.forEach { (section, sectionItems) ->
                            item { Text(text = sectionLabel(section), color = subText, style = MaterialTheme.typography.titleLarge) }
                            items(sectionItems, key = { "${it.section}:${it.id}" }) { item ->
                                SavedSheetCard(
                                    item = item,
                                    bucket = bucket,
                                    isDarkTheme = isDarkTheme,
                                    context = context,
                                    onOpen = { openedItem = item },
                                    onRemove = {
                                        LikedSavedStore.setInBucket(context, bucket, item, false)
                                        items.remove(item)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
private fun sectionLabel(section: SavedSection): String = when (section) {
    SavedSection.EATS -> "Eats"
    SavedSection.PLAY -> "Play"
    SavedSection.FITNESS -> "Fitness"
}

@Composable
private fun SavedSheetCard(
    item: SavedItem,
    bucket: SavedBucket,
    isDarkTheme: Boolean,
    context: android.content.Context,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    val primary = if (isDarkTheme) Color.White else Color(0xFF111418)
    val secondary = primary.copy(alpha = 0.68f)
    val cardColor = Color.Transparent
    val actionTint = if (isDarkTheme) Color(0xFFAAB2BD) else Color(0xFF6B7280)
    val image = item.imageUrl
    val gallery = (item.galleryImages ?: emptyList()).filter { it.isNotBlank() }
    val previewImages = if (gallery.isNotEmpty()) gallery.take(4) else List(4) { image }
    val logo = item.logoUrl?.takeIf { it.isNotBlank() } ?: image

    val normalizedStatus = when (item.statusLabel?.trim()?.lowercase()) {
        "open" -> "Open"
        "closes soon" -> "Closes soon"
        "closed" -> "Closed"
        else -> "Closed"
    }
    val statusColor = when (normalizedStatus) {
        "Open" -> Color(0xFF00C853)
        "Closes soon" -> Color(0xFFFFD54F)
        else -> Color(0xFFFF6B6B)
    }
    var likedState by remember(item.id, item.section) {
        mutableStateOf(
            LikedSavedStore.isInBucket(context, SavedBucket.LIKED, item.section, item.id)
        )
    }
    var savedState by remember(item.id, item.section) {
        mutableStateOf(
            LikedSavedStore.isInBucket(context, SavedBucket.SAVED, item.section, item.id)
        )
    }
    val heartScale = remember(item.id, item.section) { Animatable(1f) }
    val bookmarkScale = remember(item.id, item.section, "bookmark_list") { Animatable(1f) }
    LaunchedEffect(likedState) {
        if (likedState) {
            heartScale.animateTo(1.22f, animationSpec = tween(durationMillis = 120))
            heartScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            heartScale.animateTo(1f, animationSpec = tween(durationMillis = 100))
        }
    }
    LaunchedEffect(savedState) {
        if (savedState) {
            bookmarkScale.animateTo(1.18f, animationSpec = tween(durationMillis = 110))
            bookmarkScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            bookmarkScale.animateTo(1f, animationSpec = tween(durationMillis = 90))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onOpen() }
            .padding(horizontal = 2.dp, vertical = 10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AsyncImage(
                    model = logo,
                    contentDescription = item.title,
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(28.dp)).background(Color.DarkGray),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.title,
                        color = primary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = normalizedStatus,
                            color = statusColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!item.statusSuffix.isNullOrBlank()) {
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(text = "· ${item.statusSuffix}", color = secondary, fontSize = 14.sp)
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.distanceLabel ?: "",
                    color = secondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.size(6.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_near_me),
                    contentDescription = null,
                    tint = secondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.description.ifBlank { "Description not available." },
            color = secondary,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(12.dp))
        SavedImageGrid(previewImages)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(R.drawable.ic_call), contentDescription = null, tint = actionTint, modifier = Modifier.size(24.dp))
            Icon(painterResource(R.drawable.ic_near_me), contentDescription = null, tint = actionTint, modifier = Modifier.size(24.dp))
            Icon(
                painter = painterResource(if (likedState) R.drawable.ic_heart_filled else R.drawable.ic_heart),
                contentDescription = null,
                tint = if (likedState) Color(0xFFFF4B4B) else actionTint,
                modifier = Modifier
                    .size(24.dp)
                    .scale(heartScale.value)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (bucket == SavedBucket.LIKED) {
                            onRemove()
                        } else {
                            likedState = !likedState
                            LikedSavedStore.setInBucket(context, SavedBucket.LIKED, item, likedState)
                        }
                    }
            )
            Icon(
                painter = painterResource(if (savedState) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark),
                contentDescription = null,
                tint = if (savedState) Color(0xFF0095FF) else actionTint,
                modifier = Modifier
                    .size(24.dp)
                    .scale(bookmarkScale.value)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (bucket == SavedBucket.SAVED) {
                            onRemove()
                        } else {
                            savedState = !savedState
                            LikedSavedStore.setInBucket(context, SavedBucket.SAVED, item, savedState)
                        }
                    }
            )
            Icon(painterResource(R.drawable.ic_share), contentDescription = null, tint = actionTint, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun SavedImageGrid(images: List<String>) {
    val top = images.take(2)
    val bottom = images.drop(2).take(2)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        top.forEachIndexed { index, url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(
                        if (index == 0) RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                        else RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                    )
                    .background(Color.DarkGray),
                contentScale = ContentScale.Crop
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        bottom.forEachIndexed { index, url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(
                        if (index == 0) RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
                        else RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
                    )
                    .background(Color.DarkGray),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun OpenedSavedItemFullView(
    item: SavedItem,
    bucket: SavedBucket,
    isDarkTheme: Boolean,
    context: android.content.Context,
    onBack: () -> Unit,
    onRemove: () -> Unit
) {
    val bg = if (isDarkTheme) Color(0xFF1A1C21) else Color.White
    val primary = if (isDarkTheme) Color.White else Color(0xFF111418)
    val secondary = primary.copy(alpha = 0.72f)
    val scrollState = rememberScrollState()
    val gallery = (item.galleryImages ?: emptyList()).filter { it.isNotBlank() }
    val preview = if (gallery.isNotEmpty()) gallery.take(4) else List(4) { item.imageUrl }
    val logo = item.logoUrl?.takeIf { it.isNotBlank() } ?: item.imageUrl
    var likedState by remember(item.id, item.section) {
        mutableStateOf(
            LikedSavedStore.isInBucket(context, SavedBucket.LIKED, item.section, item.id)
        )
    }
    var savedState by remember(item.id, item.section) {
        mutableStateOf(
            LikedSavedStore.isInBucket(context, SavedBucket.SAVED, item.section, item.id)
        )
    }
    val heartScale = remember(item.id, item.section) { Animatable(1f) }
    val bookmarkScale = remember(item.id, item.section, "bookmark_full") { Animatable(1f) }
    LaunchedEffect(likedState) {
        if (likedState) {
            heartScale.animateTo(1.26f, animationSpec = tween(durationMillis = 120))
            heartScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            heartScale.animateTo(1f, animationSpec = tween(durationMillis = 100))
        }
    }
    LaunchedEffect(savedState) {
        if (savedState) {
            bookmarkScale.animateTo(1.2f, animationSpec = tween(durationMillis = 110))
            bookmarkScale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            bookmarkScale.animateTo(1f, animationSpec = tween(durationMillis = 90))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(top = 64.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 44.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(secondary.copy(alpha = 0.35f))
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF0F0F0))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_left),
                        contentDescription = "Back",
                        tint = primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = logo,
                        contentDescription = item.title,
                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(36.dp)).background(Color.DarkGray),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            color = primary,
                            fontSize = 24.sp,
                            lineHeight = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(if ((item.statusLabel ?: "").equals("Closed", true)) Color(0xFFFF6B6B) else Color(0xFFF4D35E))
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = item.statusLabel ?: item.subtitle.ifBlank { sectionLabel(item.section) },
                                color = if ((item.statusLabel ?: "").equals("Closed", true)) Color(0xFFFF6B6B) else Color(0xFFF4D35E),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.distanceLabel ?: "",
                        color = secondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_near_me),
                        contentDescription = null,
                        tint = secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.description.ifBlank { "Description not available." },
                color = secondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))
            SavedImageGrid(preview)

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(R.drawable.ic_call), contentDescription = null, tint = secondary, modifier = Modifier.size(34.dp))
                Icon(painterResource(R.drawable.ic_near_me), contentDescription = null, tint = secondary, modifier = Modifier.size(34.dp))
                Icon(
                    painter = painterResource(id = if (likedState) R.drawable.ic_heart_filled else R.drawable.ic_heart),
                    contentDescription = null,
                    tint = if (likedState) Color(0xFFFF2D2D) else secondary,
                    modifier = Modifier
                        .size(34.dp)
                        .scale(heartScale.value)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (bucket == SavedBucket.LIKED) {
                                onRemove()
                            } else {
                                likedState = !likedState
                                LikedSavedStore.setInBucket(context, SavedBucket.LIKED, item, likedState)
                            }
                        }
                )
                Icon(
                    painter = painterResource(id = if (savedState) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark),
                    contentDescription = null,
                    tint = if (savedState) Color(0xFF2F95FF) else secondary,
                    modifier = Modifier
                        .size(34.dp)
                        .scale(bookmarkScale.value)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (bucket == SavedBucket.SAVED) {
                                onRemove()
                            } else {
                                savedState = !savedState
                                LikedSavedStore.setInBucket(context, SavedBucket.SAVED, item, savedState)
                            }
                        }
                )
                Icon(painterResource(R.drawable.ic_share), contentDescription = null, tint = secondary, modifier = Modifier.size(34.dp))
            }
        }
    }
}
}
