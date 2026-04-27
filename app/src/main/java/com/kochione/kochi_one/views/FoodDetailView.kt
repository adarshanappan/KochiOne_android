package com.kochione.kochi_one.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.kochione.kochi_one.R
import com.kochione.kochi_one.models.DayHours
import com.kochione.kochi_one.models.Restaurant
import com.kochione.kochi_one.utils.KochiLinkType
import com.kochione.kochi_one.utils.buildKochiDeepLink
import com.kochione.kochi_one.utils.isOpenAtNow
import com.kochione.kochi_one.utils.minutesUntilCloseIfOpen
import kotlin.math.abs
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FoodDetailView(
    restaurant: Restaurant,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val pageBg = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val images = remember(restaurant) {
        restaurant.coverImages.map { it.url }.ifEmpty {
            listOfNotNull(restaurant.logo?.url)
        }
    }
    var selectedIndex by remember(images) { mutableIntStateOf(0) }
    var showFullGallery by remember { mutableStateOf(false) }
    val fullscreenPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { images.size.coerceAtLeast(1) }
    )

    fun closeFullGallery() {
        if (images.isNotEmpty()) {
            selectedIndex = fullscreenPagerState.currentPage.coerceIn(0, images.lastIndex)
        }
        showFullGallery = false
    }

    fun openFullGallery(atIndex: Int) {
        if (images.isEmpty()) return
        selectedIndex = atIndex.coerceIn(0, images.lastIndex)
        showFullGallery = true
    }

    LaunchedEffect(showFullGallery) {
        if (showFullGallery && images.isNotEmpty()) {
            fullscreenPagerState.scrollToPage(selectedIndex.coerceIn(0, images.lastIndex))
        }
    }

    BackHandler(enabled = showFullGallery) { closeFullGallery() }

    if (showFullGallery && images.isNotEmpty()) {
        val overlayPage by remember(fullscreenPagerState) {
            derivedStateOf { fullscreenPagerState.currentPage }
        }
        val fullscreenThumbState = rememberLazyListState()
        val fullscreenScope = rememberCoroutineScope()
        val fullscreenThumbShape = RoundedCornerShape(12.dp)

        LaunchedEffect(overlayPage) {
            val i = overlayPage.coerceIn(0, images.lastIndex)
            fullscreenThumbState.animateScrollToItem(i)
        }

        Dialog(
            onDismissRequest = ::closeFullGallery,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3D3D3D))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { closeFullGallery() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Close gallery",
                            modifier = Modifier.size(22.dp),
                            tint = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xB32A2A2A))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${overlayPage + 1} / ${images.size}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                HorizontalPager(
                    state = fullscreenPagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    AsyncImage(
                        model = images[page],
                        contentDescription = "Restaurant photo ${page + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                LazyRow(
                    state = fullscreenThumbState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(top = 12.dp, bottom = 20.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(images) { index, url ->
                        val isSelected = index == overlayPage
                        val thumbAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0.42f,
                            animationSpec = tween(280, easing = FastOutSlowInEasing),
                            label = "fullscreen_thumb_alpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(width = 64.dp, height = 64.dp)
                                .alpha(thumbAlpha)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, Color.White, fullscreenThumbShape)
                                    } else {
                                        Modifier
                                    }
                                )
                                .clip(fullscreenThumbShape)
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) {
                                    fullscreenScope.launch {
                                        fullscreenPagerState.animateScrollToPage(index)
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = "Thumbnail ${index + 1}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF1A1A1A)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            FoodDetailTopSection(restaurant = restaurant, isDarkTheme = isDarkTheme)
            FoodDetailStatsRow(restaurant = restaurant, isDarkTheme = isDarkTheme)
            if (images.isNotEmpty()) {
                FoodDetailGallerySection(
                    images = images,
                    selectedIndex = selectedIndex,
                    isDarkTheme = isDarkTheme,
                    onSelectedIndexChange = { selectedIndex = it },
                    onRequestFullGallery = { openFullGallery(selectedIndex) }
                )
            }
            FoodDetailInfoSection(restaurant = restaurant, isDarkTheme = isDarkTheme)
        }

        FoodDetailActionBar(
            restaurant = restaurant,
            isDarkTheme = isDarkTheme,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun FoodDetailSheetTop(isDarkTheme: Boolean, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDarkTheme) Color(0xFF1E1E1E) else Color.White)
//            .padding(top = 10.dp, bottom = 8.dp),
//        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isDarkTheme) Color(0xFF555D69) else Color(0xFFB9BEC7))
        )
    }
}

@Composable
fun FoodDetailTopSection(
    restaurant: Restaurant,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val todayHours = getTodayHours(restaurant)
    val status = getOpenStatus(todayHours)
    val closeLabel = formatTo12Hour(todayHours.close)
    val statusText = when {
        status == "Closed" -> "Closed"
        closeLabel != null -> "$status until $closeLabel"
        else -> status
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp)
    ) {
        AsyncImage(
            model = restaurant.coverImages.firstOrNull()?.url ?: restaurant.logo?.url,
            contentDescription = restaurant.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x66000000),
                            Color(0xAA000000),
                            Color(0xE6000000)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = restaurant.logo?.url ?: restaurant.coverImages.firstOrNull()?.url,
                contentDescription = "${restaurant.name} logo",
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.size(10.dp))

            Column {
                Text(
                    text = restaurant.name,
                    color = Color.White,
                    fontSize = 24.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val dotColor = when (status) {
                        "Closed" -> Color(0xFFFF6B6B)
                        "Closes soon" -> Color(0xFFFFD54F)
                        else -> Color(0xFF00E676)
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = statusText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodDetailGallerySection(
    images: List<String>,
    selectedIndex: Int,
    isDarkTheme: Boolean,
    onSelectedIndexChange: (Int) -> Unit,
    onRequestFullGallery: () -> Unit
) {
    // val isDarkTheme = isSystemInDarkTheme() // Shadowing removed
    val selectedThumbBorderColor = if (isDarkTheme) Color.White else Color.Black
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(28.dp))
                .galleryHorizontalPageSwipe(
                    images = images,
                    selectedIndex = selectedIndex,
                    setSelectedIndex = onSelectedIndexChange,
                    onTap = onRequestFullGallery
                )
        ) {
            val galleryAnimMs = 320
            val fadeEase = tween<Float>(galleryAnimMs, easing = FastOutSlowInEasing)
            AnimatedContent(
                targetState = selectedIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn(fadeEase))
                            .togetherWith(slideOutHorizontally { -it } + fadeOut(fadeEase))
                    } else {
                        (slideInHorizontally { -it } + fadeIn(fadeEase))
                            .togetherWith(slideOutHorizontally { it } + fadeOut(fadeEase))
                    }
                },
                label = "gallery_image_transition"
            ) { index ->
                AsyncImage(
                    model = images[index],
                    contentDescription = "Restaurant photo ${index + 1}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x22000000),
                                Color(0x66000000),
                                Color(0xAA000000)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_swipe_left),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = Color.White
                    )
                    Text(
                        text = "Swipe for more",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "${selectedIndex + 1}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .padding(top = 10.dp, bottom = 12.dp)
                .galleryHorizontalPageSwipe(
                    images = images,
                    selectedIndex = selectedIndex,
                    setSelectedIndex = onSelectedIndexChange
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(images.size) { index ->
                GalleryPagerMorphDot(
                    isSelected = index == selectedIndex,
                    isDarkTheme = isDarkTheme
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.width(20.dp))
            }
            itemsIndexed(images) { index, imageUrl ->
                val isSelected = index == selectedIndex
                val thumbShape = RoundedCornerShape(22.dp)
                val thumbAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.42f,
                    animationSpec = tween(320, easing = FastOutSlowInEasing),
                    label = "gallery_thumb_alpha"
                )
                Box(
                    modifier = Modifier
                        .size(width = 150.dp, height = 95.dp)
                        .alpha(thumbAlpha)
                        .then(
                            if (isSelected) {
                                Modifier.border(2.dp, selectedThumbBorderColor, thumbShape)
                            } else {
                                Modifier
                            }
                        )
                        .clip(thumbShape)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { onSelectedIndexChange(index) }
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Thumbnail ${index + 1}",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1E1E1E)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.width(20.dp))
            }
        }
    }
}

@Composable
private fun GalleryPagerMorphDot(
    isSelected: Boolean,
    isDarkTheme: Boolean
) {
    val morphDurationMs = 320
    val morphEase = FastOutSlowInEasing
    val dotWidth by animateDpAsState(
        targetValue = if (isSelected) 34.dp else 10.dp,
        animationSpec = tween(morphDurationMs, easing = morphEase),
        label = "gallery_pager_dot_width"
    )
    val dotColor by animateColorAsState(
        targetValue = when {
            isDarkTheme && isSelected -> Color.White
            isDarkTheme && !isSelected -> Color(0xFF343A43)
            !isDarkTheme && isSelected -> Color.Black
            else -> Color.Black.copy(alpha = 0.18f)
        },
        animationSpec = tween(morphDurationMs, easing = morphEase),
        label = "gallery_pager_dot_color"
    )
    Box(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .height(10.dp)
            .width(dotWidth)
            .clip(RoundedCornerShape(10.dp))
            .background(dotColor)
    )
}

private fun Modifier.galleryHorizontalPageSwipe(
    images: List<*>,
    selectedIndex: Int,
    setSelectedIndex: (Int) -> Unit,
    onTap: (() -> Unit)? = null
): Modifier = composed {
    val latestTap = rememberUpdatedState(onTap)
    val latestSet = rememberUpdatedState(setSelectedIndex)
    val swipeThreshold = 60f

    val swipeModifier = Modifier.pointerInput(images, selectedIndex) {
        var totalDrag = 0f
        detectHorizontalDragGestures(
            onHorizontalDrag = { change, dragAmount ->
                change.consume()
                totalDrag += dragAmount
            },
            onDragEnd = {
                when {
                    totalDrag <= -swipeThreshold && selectedIndex < images.lastIndex ->
                        latestSet.value(selectedIndex + 1)
                    totalDrag >= swipeThreshold && selectedIndex > 0 ->
                        latestSet.value(selectedIndex - 1)
                }
                totalDrag = 0f
            },
            onDragCancel = { totalDrag = 0f }
        )
    }

    if (onTap != null) {
        then(
            swipeModifier.pointerInput(Unit) {
                detectTapGestures(
                    onTap = { latestTap.value?.invoke() }
                )
            }
        )
    } else {
        then(swipeModifier)
    }
}

@Composable
private fun FoodDetailStatsRow(restaurant: Restaurant, isDarkTheme: Boolean) {
    val panelBg = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val primaryText = if (isDarkTheme) Color.White else Color(0xFF111418)
    val secondaryText = if (isDarkTheme) Color(0xFF9EA3AE) else Color(0xFF6B7280)
    val divider = if (isDarkTheme) Color.White.copy(alpha = 0.16f) else Color(0xFFE2E6EC)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(panelBg)
            .padding(vertical = 18.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format("%.1f", restaurant.rating),
                    color = primaryText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "★",
                    color = Color(0xFFFFC107),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Rating",
                color = secondaryText,
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .height(72.dp)
                .width(1.dp)
                .background(divider)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${restaurant.ranking}",
                    color = primaryText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "🏆",
                    color = Color(0xFFFFC107),
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Top ${restaurant.ranking} in Kochi One",
                color = secondaryText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FoodDetailInfoSection(restaurant: Restaurant, isDarkTheme: Boolean) {
    val context = LocalContext.current
    val cardBg = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val primaryText = if (isDarkTheme) Color.White else Color(0xFF111418)
    val buttonBorder = if (isDarkTheme) Color(0xFF60656D) else Color(0xFFD0D4DB)
    val cuisineChipBg = if (isDarkTheme) Color(0xFF22252B) else Color(0xFFF2F3F5)
    val cuisineChipBorder = if (isDarkTheme) Color(0xFF31353C) else Color.Black.copy(alpha = 0.10f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (restaurant.cuisine.isNotEmpty()) {
            SectionTitle("Cuisine", isDarkTheme)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(restaurant.cuisine) { item ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(cuisineChipBg)
                            .border(1.dp, cuisineChipBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "\uD83C\uDF74  $item",
                            color = primaryText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }
        SectionTitle("Features", isDarkTheme)
        Spacer(modifier = Modifier.height(10.dp))
        val features = restaurant.features.ifEmpty { listOf("No features available") }
        features.forEachIndexed { index, feature ->
            FeatureRow(feature, isDarkTheme)
            if (index != features.lastIndex) HorizontalLine(isDarkTheme)
        }

        Spacer(modifier = Modifier.height(22.dp))
        SectionTitle("Hours", isDarkTheme)
        Spacer(modifier = Modifier.height(10.dp))
        HoursHeader(restaurant)
        HorizontalLine(isDarkTheme)
        Spacer(modifier = Modifier.height(8.dp))

        DayHoursRow("Monday", restaurant.operatingHours.monday, isDarkTheme)
        DayHoursRow("Tuesday", restaurant.operatingHours.tuesday, isDarkTheme)
        DayHoursRow("Wednesday", restaurant.operatingHours.wednesday, isDarkTheme)
        DayHoursRow("Thursday", restaurant.operatingHours.thursday, isDarkTheme)
        DayHoursRow("Friday", restaurant.operatingHours.friday, isDarkTheme)
        DayHoursRow("Saturday", restaurant.operatingHours.saturday, isDarkTheme)
        DayHoursRow("Sunday", restaurant.operatingHours.sunday, isDarkTheme)

        Spacer(modifier = Modifier.height(18.dp))
        HorizontalLine(isDarkTheme)
        Spacer(modifier = Modifier.height(14.dp))
        SectionTitle("Contact", isDarkTheme)
        Spacer(modifier = Modifier.height(12.dp))

        val contactIconTint = if (isDarkTheme) Color(0xFFA7ADB7) else Color(0xFF6B7280)
        val contactIconSpacing = 18.dp

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location_pin),
                contentDescription = null,
                modifier = Modifier
                    .size(22.dp)
                    .padding(top = 1.dp),
                tint = contactIconTint
            )
            Spacer(modifier = Modifier.width(contactIconSpacing))
            Text(
                text = buildString {
                    append(restaurant.address.street)
                    append(",\n")
                    append(restaurant.address.city)
                    append(", ")
                    append(restaurant.address.state)
                    append(",\n")
                    append(restaurant.address.zipCode)
                },
                modifier = Modifier.weight(1f),
                color = primaryText,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        HorizontalLine(isDarkTheme)
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_call),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = contactIconTint
            )
            Spacer(modifier = Modifier.width(contactIconSpacing))
            Text(
                text = restaurant.contact.phone,
                modifier = Modifier.weight(1f),
                color = primaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_email),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = contactIconTint
            )
            Spacer(modifier = Modifier.width(contactIconSpacing))
            Text(
                text = restaurant.contact.email,
                modifier = Modifier.weight(1f),
                color = primaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        val websiteUrl = restaurant.contact.website?.trim()?.takeIf { it.isNotBlank() }
        if (websiteUrl != null) {
            Button(
                onClick = { openWebsiteInBrowser(context, websiteUrl) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                shape = RoundedCornerShape(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF0095FF)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, buttonBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_browser_chrome),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = LocalContentColor.current
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Visit Website",
                        color = LocalContentColor.current,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        val emailRaw = restaurant.contact.email.trim()
        val canOpenEmail = emailRaw.isNotBlank()
        Button(
            onClick = {
                openEmailComposer(context, emailRaw, restaurant.name)
            },
            enabled = canOpenEmail,
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp),
            shape = RoundedCornerShape(38.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color(0xFF0095FF),
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color(0xFF0095FF).copy(alpha = 0.38f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, buttonBorder)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_email_svgrepo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = LocalContentColor.current
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Email",
                    color = LocalContentColor.current,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionTitle(text: String, isDarkTheme: Boolean) {
    Text(
        text = text,
        color = if (isDarkTheme) Color.White else Color(0xFF111418),
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun FeatureRow(feature: String, isDarkTheme: Boolean) {
    val iconTint = if (isDarkTheme) Color(0xFFA7ADB7) else Color(0xFF4A4F57)
    val textColor = if (isDarkTheme) Color.White else Color(0xFF111418)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = featureIconRes(feature),
            contentDescription = feature,
            modifier = Modifier.size(28.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(iconTint)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = feature,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun featureIconRes(feature: String): Int {
    val normalized = feature.trim().lowercase()
    return when {
        "dine" in normalized -> R.drawable.ic_eat
        "outdoor" in normalized -> R.drawable.ic_outdoor_seating
        "parking" in normalized -> R.drawable.ic_parking
        "wifi" in normalized || "wi-fi" in normalized || "wi fi" in normalized -> R.drawable.ic_wifi
        "bar" in normalized -> R.drawable.ic_bar
        "music" in normalized -> R.drawable.ic_music
        "private" in normalized -> R.drawable.ic_private_dining
        else -> R.drawable.ic_eat
    }
}

@Composable
private fun HorizontalLine(isDarkTheme: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(if (isDarkTheme) Color(0xFF2D3239) else Color(0xFFE2E6EC))
    )
}

@Composable
private fun HoursHeader(restaurant: Restaurant) {
    val todayHours = getTodayHours(restaurant)
    val status = getOpenStatus(todayHours)
    val closeLabel = formatTo12Hour(todayHours.close) ?: "--"
    val statusColor = when (status) {
        "Closed" -> Color(0xFFFF6B6B)
        "Closes soon" -> Color(0xFFFFD54F)
        else -> Color(0xFF39D353)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = status, color = statusColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        if (status != "Closed") {
            Text(
                text = "   · Closes at $closeLabel",
                color = Color(0xFFAFB4BC),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DayHoursRow(day: String, dayHours: DayHours, isDarkTheme: Boolean) {
    val textColor = if (isDarkTheme) Color.White else Color(0xFF111418)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = day, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text(
            text = formatDayHours(dayHours),
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatDayHours(dayHours: DayHours): String {
    if (dayHours.closed) return "Closed"
    val open = formatTo12Hour(dayHours.open) ?: "--"
    val close = formatTo12Hour(dayHours.close) ?: "--"
    return "$open - $close"
}

private fun getTodayHours(restaurant: Restaurant): DayHours {
    val cal = java.util.Calendar.getInstance()
    return when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
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

private fun getOpenStatus(todayHours: DayHours): String {
    val cal = java.util.Calendar.getInstance()
    val nowMinutes =
        cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
    return when {
        todayHours.closed -> "Closed"
        !todayHours.isOpenAtNow(nowMinutes) -> "Closed"
        else -> {
            val mins = todayHours.minutesUntilCloseIfOpen(nowMinutes)
            if (mins != null && mins in 0..60) "Closes soon" else "Open"
        }
    }
}

private fun formatTo12Hour(time24: String?): String? {
    if (time24.isNullOrBlank()) return null
    val parts = time24.split(":")
    if (parts.size < 2) return time24
    val hour24 = parts[0].toIntOrNull() ?: return time24
    val minute = parts[1].take(2)
    val amPm = if (hour24 >= 12) "PM" else "AM"
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    return "$hour12:$minute $amPm"
}

private fun openWebsiteInBrowser(context: Context, raw: String) {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return
    val url = when {
        trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) -> trimmed
        else -> "https://$trimmed"
    }
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun openEmailComposer(context: Context, to: String, restaurantName: String?) {
    val address = to.trim()
    if (address.isEmpty()) return
    val subject = restaurantName?.takeIf { it.isNotBlank() }?.let { name -> "Inquiry: $name" }
    val uri = if (subject.isNullOrBlank()) {
        Uri.parse("mailto:$address")
    } else {
        Uri.parse("mailto:$address?subject=${Uri.encode(subject)}")
    }
    try {
        context.startActivity(Intent(Intent.ACTION_SENDTO, uri))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun FoodDetailActionBar(
    restaurant: Restaurant,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLiked by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }

    // Transparent Glassmorphism style
    val barBg = if (isDarkTheme) Color(0xCC1E1E1E) else Color(0x99FFFFFF)
    val glassBorder = if (isDarkTheme) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)
    val iconTint = if (isDarkTheme) Color.White else Color(0xFF111418)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(barBg, RoundedCornerShape(32.dp))
                .border(1.dp, glassBorder, RoundedCornerShape(32.dp))
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionBarIcon(
                iconRes = R.drawable.ic_call,
                contentDescription = "Call",
                tint = iconTint,
                onClick = { openDialer(context, restaurant.contact.phone) }
            )
            ActionBarIcon(
                iconRes = R.drawable.ic_direction,
                contentDescription = "Directions",
                tint = iconTint,
                onClick = { 
                    openMapDirections(
                        context, 
                        restaurant.location.latitude, 
                        restaurant.location.longitude, 
                        restaurant.name 
                    ) 
                }
            )
            ActionBarIcon(
                iconRes = if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart,
                contentDescription = "Like",
                tint = if (isLiked) Color(0xFFFF4B4B) else iconTint,
                isActive = isLiked,
                animateOnActivate = true,
                activateScale = 1.3f,
                activateDurationMs = 150,
                resetDurationMs = 150,
                onClick = { isLiked = !isLiked }
            )
            ActionBarIcon(
                iconRes = if (isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark,
                contentDescription = "Save",
                tint = if (isSaved) Color(0xFF0095FF) else iconTint,
                isActive = isSaved,
                animateOnActivate = true,
                activateScale = 1.2f,
                activateDurationMs = 100,
                resetDurationMs = 100,
                onClick = { isSaved = !isSaved }
            )
            ActionBarIcon(
                iconRes = R.drawable.ic_share,
                contentDescription = "Share",
                tint = iconTint,
                onClick = { shareRestaurant(context, restaurant) }
            )
        }
    }
}

@Composable
private fun ActionBarIcon(
    iconRes: Int,
    contentDescription: String,
    tint: Color,
    isActive: Boolean = false,
    animateOnActivate: Boolean = false,
    activateScale: Float = 1.25f,
    activateDurationMs: Int = 110,
    resetDurationMs: Int = 100,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    LaunchedEffect(isActive, animateOnActivate) {
        if (!animateOnActivate) return@LaunchedEffect
        if (isActive) {
            // Match the same pop animation feel used in list/session action icons.
            scale.animateTo(activateScale, animationSpec = tween(durationMillis = activateDurationMs))
            scale.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            scale.animateTo(1f, animationSpec = tween(durationMillis = resetDurationMs))
        }
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .scale(scale.value)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(26.dp),
            tint = tint
        )
    }
}

private fun openDialer(context: Context, phone: String) {
    val number = phone.trim().filter { it.isDigit() || it == '+' }
    if (number.isEmpty()) return
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun openMapDirections(context: Context, lat: Double, lng: Double, label: String) {
    val uri = Uri.parse("geo:0,0?q=$lat,$lng(${Uri.encode(label)})")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.setPackage("com.google.android.apps.maps")
    try {
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Fallback to any map app
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun shareRestaurant(context: Context, restaurant: Restaurant) {
    val deepLink = buildKochiDeepLink(KochiLinkType.FOOD, restaurant.bizId)
    val shareText = """
        Check out ${restaurant.name} on Kochi One!
        
        ${restaurant.description}
        
        Rating: ${restaurant.rating} ★
        Address: ${restaurant.address.street}, ${restaurant.address.city}

        $deepLink
        
        Download Kochi One for more!
    """.trimIndent()
    
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    
    try {
        context.startActivity(Intent.createChooser(intent, "Share via"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
