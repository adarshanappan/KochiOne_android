package com.kochione.kochi_one.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kochione.kochi_one.R
import com.kochione.kochi_one.ui.components.SkeletonBox
import com.kochione.kochi_one.models.DayHours
import com.kochione.kochi_one.models.OperatingHours
import com.kochione.kochi_one.models.PlayVenue
import com.kochione.kochi_one.viewmodels.PlayViewModel
import java.util.Calendar

private val CardCorner = 20.dp
private val SectionGap = 20.dp
private val ScreenPaddingH = 16.dp
private val TopRowHeight = 260.dp

private enum class PlayCardVariant {
    HERO_LEFT,
    STACK_TOP,
    STACK_BOTTOM,
    FULL_FUN,
    FULL_GAME,
    FULL_SNOOKER
}

private data class PlayCardData(
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val variant: PlayCardVariant,
    val localImageResId: Int? = null,
    val venue: PlayVenue? = null
)

@Composable
fun PlayView(
    viewModel: PlayViewModel = viewModel(),
    onDetailVisibilityChanged: (Boolean) -> Unit = {},
    onRegisterDismissDetail: ((() -> Unit)?) -> Unit = {}
) {
    val venues by viewModel.venues.collectAsState()
    val categoryThumbnails by viewModel.categoryThumbnails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val isDarkTheme = isSystemInDarkTheme()
    // Match bottom sheet surface (#1E1E1E) — avoid near-black #121212 around cards
    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val cardBgColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFE0E0E0)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    var selectedCard by remember { mutableStateOf<PlayCardData?>(null) }

    androidx.compose.runtime.LaunchedEffect(selectedCard) {
        onDetailVisibilityChanged(selectedCard != null)
    }

    DisposableEffect(selectedCard) {
        if (selectedCard != null) {
            onRegisterDismissDetail { selectedCard = null }
        } else {
            onRegisterDismissDetail(null)
        }
        onDispose {
            onRegisterDismissDetail(null)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 88.dp,
                start = ScreenPaddingH,
                end = ScreenPaddingH
            ),
            verticalArrangement = Arrangement.spacedBy(SectionGap)
        ) {
            if (isLoading && venues.isEmpty()) {
                item {
                    PlayViewCardsSkeleton(isDarkTheme = isDarkTheme)
                }
            } else if (!isLoading && errorMessage != null && venues.isEmpty()) {
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
                            onClick = { viewModel.fetchPlayVenues() },
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
            } else {
                item {
                    val cards = remember(venues, categoryThumbnails) {
                        buildPlayCards(venues, categoryThumbnails)
                    }
                    val hero = cards[0]
                    val stackTop = cards[1]
                    val stackBottom = cards[2]

                    Column(verticalArrangement = Arrangement.spacedBy(SectionGap)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(TopRowHeight),
                            horizontalArrangement = Arrangement.spacedBy(SectionGap)
                        ) {
                            PlayFeatureCard(
                                card = hero,
                                modifier = Modifier
                                    .weight(0.50f)
                                    .fillMaxHeight(),
                                onCardClick = { selectedCard = hero }
                            )
                            Column(
                                modifier = Modifier
                                    .weight(0.50f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(SectionGap)
                            ) {
                                PlayFeatureCard(
                                    card = stackTop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    onCardClick = { selectedCard = stackTop }
                                )
                                PlayFeatureCard(
                                    card = stackBottom,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    onCardClick = { selectedCard = stackBottom }
                                )
                            }
                        }

                        cards.drop(3).forEach { card ->
                            PlayFeatureCard(
                                card = card,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(172.dp),
                                onCardClick = { selectedCard = card }
                            )
                        }
                    }
                }
            }
        }
        AnimatedContent(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2f),
            targetState = selectedCard,
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = 280)
                    ),
                    initialContentExit = slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(durationMillis = 260)
                    )
                )
            },
            label = "play-details-transition"
        ) { card ->
            if (card != null) {
                val detailVenues = buildDetailVenuesForCard(card, venues)
                if (detailVenues.isNotEmpty()) {
                    PlayVenueFullScreenSheet(venues = detailVenues)
                } else {
                    PlayVenueEmptyFullScreenSheet(card = card)
                }
            } else {
                Spacer(modifier = Modifier.size(0.dp))
            }
        }
    }
}

@Composable
private fun PlayViewCardsSkeleton(isDarkTheme: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(SectionGap)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(TopRowHeight),
            horizontalArrangement = Arrangement.spacedBy(SectionGap)
        ) {
            SkeletonBox(
                modifier = Modifier
                    .weight(0.50f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(CardCorner),
                isDarkTheme = isDarkTheme
            )
            Column(
                modifier = Modifier
                    .weight(0.50f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(SectionGap)
            ) {
                repeat(2) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(CardCorner),
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
        repeat(2) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp),
                shape = RoundedCornerShape(CardCorner),
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
private fun PlayVenueEmptyFullScreenSheet(
    card: PlayCardData
) {
    val isDarkTheme = isSystemInDarkTheme()
    val pageBg = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val titleColor = if (isDarkTheme) Color.White.copy(alpha = 0.92f) else Color(0xFF1A1A1A)
    val subtitleColor = if (isDarkTheme) Color.White.copy(alpha = 0.58f) else Color(0xFF666666)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_no_venues_field),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(58.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No venues found",
                        color = titleColor,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Check back soon",
                        color = subtitleColor,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayFeatureCard(
    card: PlayCardData,
    modifier: Modifier = Modifier,
    onCardClick: (() -> Unit)? = null
) {
    val textGradient = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color(0x44000000),
            Color(0xCC000000)
        )
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(CardCorner),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(CardCorner))
            .then(if (onCardClick != null) Modifier.clickable { onCardClick() } else Modifier)
    ) {
        when (card.variant) {
            PlayCardVariant.FULL_SNOOKER -> SnookerCardContent(card)
            PlayCardVariant.FULL_GAME -> GameCentreCardContent(card)
            else -> {
                val localId = card.localImageResId
                if (localId != null) {
                    Image(
                        painter = painterResource(id = localId),
                        contentDescription = card.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AsyncImage(
                        model = card.imageUrl,
                        contentDescription = card.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                        .background(textGradient)
                )
                PlayCardBottomContent(card = card)
            }
        }
    }
}

@Composable
private fun PlayVenueFullScreenSheet(
    venues: List<PlayVenue>
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    val pageBg = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val likedMap = remember { mutableStateMapOf<String, Boolean>() }
    val savedMap = remember { mutableStateMapOf<String, Boolean>() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(venues) { venue ->
                    val dayHours = todayHours(venue.operatingHours)
                    val openNow = isOpenNow(dayHours)
                    val statusText = if (openNow) "Open" else "Closed"
                    val statusSuffix = when {
                        dayHours.closed -> "closed"
                        openNow && !dayHours.close.isNullOrBlank() -> "opens"
                        !openNow && !dayHours.open.isNullOrBlank() -> "opens"
                        else -> "hours unavailable"
                    }
                    val liked = likedMap[venue.id] == true
                    val saved = savedMap[venue.id] == true

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(46.dp),
                                    shape = CircleShape,
                                    color = Color(0xFF2A2A2A)
                                ) {
                                    val logo = venue.logo?.url
                                    if (!logo.isNullOrBlank()) {
                AsyncImage(
                                            model = logo,
                                            contentDescription = venue.name,
                    modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_play),
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                                                .padding(10.dp)
                        .fillMaxSize()
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = venue.name,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "\u2022",
                                            color = if (openNow) Color(0xFF22C55E) else Color(0xFFFF4D4F),
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = statusText,
                                            color = if (openNow) Color(0xFF86EFAC) else Color(0xFFFF6B6B),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = " \u00b7 $statusSuffix",
                                            color = Color.White.copy(alpha = 0.62f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${"%.1f".format(venue.rating)} km",
                                    color = Color.White.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_near_me),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = venue.description.ifBlank { "No description available." },
                            color = Color.White.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val coverUrls = venue.coverImages
                            ?.mapNotNull { img -> img.url.takeIf { it.isNotBlank() } }
                            .orEmpty()
                        val maxOuterCoverImages = 4
                        val displayedCoverUrls = coverUrls.take(maxOuterCoverImages)
                        if (displayedCoverUrls.isNotEmpty()) {
                            if (displayedCoverUrls.size == 1) {
                                AsyncImage(
                                    model = displayedCoverUrls[0],
                                    contentDescription = venue.name,
                                    modifier = Modifier
                                        .width(190.dp)
                                        .height(122.dp)
                                        .clip(RoundedCornerShape(14.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    displayedCoverUrls.chunked(2).forEach { rowUrls ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowUrls.forEach { url ->
                                                AsyncImage(
                                                    model = url,
                                                    contentDescription = venue.name,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(120.dp)
                                                        .clip(RoundedCornerShape(14.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            if (rowUrls.size == 1) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SheetActionIcon(
                                iconRes = R.drawable.ic_call,
                                onClick = { callVenue(context, venue.contact.phone) }
                            )
                            SheetActionIcon(
                                iconRes = R.drawable.ic_near_me,
                                onClick = { openVenueMap(context, venue.location.latitude, venue.location.longitude, venue.name) }
                            )
                            SheetActionIcon(
                                iconRes = if (liked) R.drawable.ic_heart_filled else R.drawable.ic_heart,
                                tint = if (liked) Color(0xFFFF3B30) else Color.White.copy(alpha = 0.76f),
                                isActive = liked,
                                animateOnActivate = true,
                                onClick = { likedMap[venue.id] = !liked }
                            )
                            SheetActionIcon(
                                iconRes = if (saved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark,
                                isActive = saved,
                                animateOnActivate = true,
                                onClick = { savedMap[venue.id] = !saved }
                            )
                            SheetActionIcon(
                                iconRes = R.drawable.ic_share,
                                onClick = { shareVenue(context, venue) }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    }
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

private fun buildDetailVenuesForCard(card: PlayCardData, allVenues: List<PlayVenue>): List<PlayVenue> {
    if (card.variant == PlayCardVariant.HERO_LEFT) {
        val soccerVenues = allVenues.filter { venue ->
            val n = venue.name.lowercase()
            val c = venue.playCategory.lowercase()
            n.contains("soccer") || n.contains("football") || n.contains("futsal") ||
                c.contains("soccer") || c.contains("football") || c.contains("futsal")
        }
        return if (soccerVenues.isNotEmpty()) soccerVenues else listOfNotNull(card.venue)
    }
    return listOfNotNull(card.venue)
}

@Composable
private fun CircleIconButton(
    size: androidx.compose.ui.unit.Dp = 44.dp,
    backgroundColor: Color = Color(0xFF2C2C2C),
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(size)
            .clickable { onClick() },
        shape = CircleShape,
        color = backgroundColor
    ) {
        Box(contentAlignment = Alignment.Center) { icon() }
    }
}

@Composable
private fun SheetActionIcon(
    iconRes: Int,
    tint: Color = Color.White.copy(alpha = 0.76f),
    isActive: Boolean = false,
    animateOnActivate: Boolean = false,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isActive, animateOnActivate) {
        if (!animateOnActivate) return@LaunchedEffect
        if (isActive) {
            scale.animateTo(
                targetValue = 1.25f,
                animationSpec = tween(durationMillis = 110)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        } else {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 100)
            )
        }
    }

    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        tint = tint,
        modifier = Modifier
            .size(48.dp)
            .scale(scale.value)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(12.dp)
    )
}

private fun todayHours(operatingHours: OperatingHours): DayHours {
    return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> operatingHours.monday
        Calendar.TUESDAY -> operatingHours.tuesday
        Calendar.WEDNESDAY -> operatingHours.wednesday
        Calendar.THURSDAY -> operatingHours.thursday
        Calendar.FRIDAY -> operatingHours.friday
        Calendar.SATURDAY -> operatingHours.saturday
        Calendar.SUNDAY -> operatingHours.sunday
        else -> operatingHours.monday
    }
}

private fun isOpenNow(dayHours: DayHours): Boolean {
    if (dayHours.closed) return false
    val open = parseTimeToMinutes(dayHours.open) ?: return false
    val close = parseTimeToMinutes(dayHours.close) ?: return false
    val nowCalendar = Calendar.getInstance()
    val now = nowCalendar.get(Calendar.HOUR_OF_DAY) * 60 + nowCalendar.get(Calendar.MINUTE)
    return if (close > open) now in (open + 1) until close else now > open || now < close
}

private fun parseTimeToMinutes(value: String?): Int? {
    if (value.isNullOrBlank()) return null
    val parts = value.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour * 60 + minute
}

private fun callVenue(context: android.content.Context, phone: String?) {
    if (phone.isNullOrBlank()) return
    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
        data = android.net.Uri.parse("tel:$phone")
    }
    runCatching { context.startActivity(intent) }
}

private fun openVenueMap(context: android.content.Context, lat: Double, lon: Double, name: String) {
    val geo = "geo:$lat,$lon?q=$lat,$lon(${android.net.Uri.encode(name)})"
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(geo))
    runCatching { context.startActivity(intent) }
}

private fun shareVenue(context: android.content.Context, venue: PlayVenue) {
    val shareText = buildString {
        append("Check out ${venue.name} on Kochi One!\n")
        append(venue.description)
        append("\n\nLocation: ${venue.address.street}, ${venue.address.city}")
    }
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_SUBJECT, venue.name)
        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
}

@Composable
private fun BoxScope.PlayCardBottomContent(card: PlayCardData) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        val titleStyle = when (card.variant) {
            PlayCardVariant.HERO_LEFT ->
                MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            PlayCardVariant.STACK_TOP, PlayCardVariant.STACK_BOTTOM ->
                MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            else ->
                MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
        }
        val subtitleStyle = when (card.variant) {
            PlayCardVariant.HERO_LEFT -> MaterialTheme.typography.bodyLarge
            PlayCardVariant.STACK_TOP, PlayCardVariant.STACK_BOTTOM -> MaterialTheme.typography.bodyMedium
            else -> MaterialTheme.typography.bodyLarge
        }

        Text(
            text = card.title,
            style = titleStyle,
            color = Color.White,
            maxLines = if (card.variant == PlayCardVariant.HERO_LEFT) 2 else 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = card.subtitle,
            style = subtitleStyle,
            color = Color.White.copy(alpha = 0.95f),
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        when (card.variant) {
            PlayCardVariant.FULL_FUN -> {
                Spacer(modifier = Modifier.height(14.dp))
                FunActivitiesActionRow()
            }
            else -> Unit
        }
    }
}

@Composable
private fun FunActivitiesActionRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.22f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Explore More",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun GameCentreActionRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.16f),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_game_controller),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Gaming Hub",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun SnookerCardContent(card: PlayCardData) {
    val snookerGreen = Color(0xFF1B4332)
    val snookerGradient = Brush.horizontalGradient(
//        0f to Color(0xFF1B4332),
//        0.38f to Color(0xFF2D6A4F),
        0.72f to Color(0xFF163B2F),
        1f to Color(0xFF0A1F18)
    )
    val appsTint = Color(0xFFE3E3E3)
    val hasBackendImage = card.imageUrl.isNotBlank()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (hasBackendImage) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = card.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 1f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            0f to Color.Black.copy(alpha = 0.38f),
                            0.45f to Color.Black.copy(alpha = 0.12f),
                            1f to Color.Transparent
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(snookerGradient)
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_apps),
            contentDescription = null,
            tint = appsTint,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
                .size(120.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.58f)) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.88f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SnookerActionRow(chevronTint = snookerGreen)
        }
    }
}

@Composable
private fun SnookerActionRow(chevronTint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = Color(0xFF065F46),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0A0F0D).copy(alpha = 0.38f),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_apps),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Tables",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun GameCentreCardContent(card: PlayCardData) {
    val gameCentreGradient = Brush.horizontalGradient(
        colors = listOf(
//            Color(0xFFDC2626),
            Color(0xFF991B1B),
            Color(0xFF450A0A),
            Color(0xFF000000)
        )
    )
    val controllerTint = Color(0xFFE3E3E3)
    val hasBackendImage = card.imageUrl.isNotBlank()

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasBackendImage) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = card.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 1f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            0f to Color.Black.copy(alpha = 0.40f),
                            0.45f to Color.Black.copy(alpha = 0.12f),
                            1f to Color.Transparent
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gameCentreGradient)
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_game_controller),
            contentDescription = null,
            tint = controllerTint,
                    modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp)
                .size(120.dp)
        )
        Column(
                    modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.58f)) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.92f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            GameCentreActionRow()
        }
    }
}

private fun buildPlayCards(
    venues: List<PlayVenue>,
    categoryThumbnails: Map<String, String>
): List<PlayCardData> {
    val variants = listOf(
        PlayCardVariant.HERO_LEFT,
        PlayCardVariant.STACK_TOP,
        PlayCardVariant.STACK_BOTTOM,
        PlayCardVariant.FULL_FUN,
        PlayCardVariant.FULL_GAME,
        PlayCardVariant.FULL_SNOOKER
    )
    val fallbacks = listOf(
        PlayCardData(
            title = "Soccer",
            subtitle = "Every Kick Counts",
            imageUrl = "",

            variant = PlayCardVariant.HERO_LEFT,
            localImageResId = R.drawable.play_hero_soccer
        ),
        PlayCardData(
            title = "Cricket",
            subtitle = "Your Daily Innings",
            imageUrl = "",

            variant = PlayCardVariant.STACK_TOP,
            localImageResId = R.drawable.play_card_cricket
        ),
        PlayCardData(
            title = "Badminton",
            subtitle = "Smash Your Day",
            imageUrl = "",

            variant = PlayCardVariant.STACK_BOTTOM,
            localImageResId = R.drawable.play_card_badminton
        ),
        PlayCardData(
            title = "Fun Activities",
            subtitle = "Explore the City's Most Exciting Moments",
            imageUrl = "",

            variant = PlayCardVariant.FULL_FUN,
            localImageResId = R.drawable.play_card_fun_activities
        ),
        PlayCardData(
            title = "Game Centre",
            subtitle = "Where Play Begins",
            imageUrl = "",

            variant = PlayCardVariant.FULL_GAME
        ),
        PlayCardData(
            title = "Snooker",
            subtitle = "Precision & Focus",
            imageUrl = "",

            variant = PlayCardVariant.FULL_SNOOKER
        )
    )

    val usedIds = mutableSetOf<String>()
    fun thumbnailFor(vararg keys: String): String? {
        val normalizedEntries = categoryThumbnails.entries.map { (k, v) ->
            k.lowercase() to v
        }
        return keys.firstNotNullOfOrNull { key ->
            val normalizedKey = key.lowercase()
            normalizedEntries.firstOrNull { (categoryKey, imageUrl) ->
                imageUrl.isNotBlank() && (
                    categoryKey == normalizedKey ||
                        categoryKey.contains(normalizedKey) ||
                        normalizedKey.contains(categoryKey)
                    )
            }?.second
        }
    }
    val slotThumbnailUrls = listOf(
        thumbnailFor("soccer", "football", "futsal"),
        thumbnailFor("cricket"),
        thumbnailFor("badminton"),
        thumbnailFor("fun activities", "fun", "activities", "adventure"),
        thumbnailFor("game centre", "game center", "gaming", "arcade", "game"),
        thumbnailFor("snooker", "pool", "billiards", "billiard")
    )
    fun matches(venue: PlayVenue, keys: List<String>): Boolean {
        val n = venue.name.lowercase()
        val c = venue.playCategory.lowercase()
        return keys.any { k -> n.contains(k) || c.contains(k) }
    }
    fun takeFirstMatch(keys: List<String>): PlayVenue? {
        val v = venues.firstOrNull { it.id !in usedIds && matches(it, keys) }
        if (v != null) usedIds.add(v.id)
        return v
    }
    // Keep top cards strict so data does not jump categories:
    // - Soccer takes soccer-match, else first available venue.
    // - Cricket/Badminton only take explicit category matches.
    val soccerVenue = takeFirstMatch(listOf("soccer", "football", "futsal"))
        ?: venues.firstOrNull { it.id !in usedIds }?.also { usedIds.add(it.id) }
    val slotVenues = listOf(
        soccerVenue,
        takeFirstMatch(listOf("cricket")),
        takeFirstMatch(listOf("badminton")),
        takeFirstMatch(listOf("fun", "activity", "activities", "adventure", "go-kart", "kart")),
        takeFirstMatch(listOf("game", "gaming", "arcade", "console")),
        takeFirstMatch(listOf("snooker", "pool", "billiard"))
    )
    fun fillSlot(i: Int): PlayVenue? {
        // Strict category mapping only: do not spill one category into other cards.
        return slotVenues[i]
    }

    return variants.indices.map { i ->
        val fb = fallbacks[i]
        val venue = fillSlot(i)
        when {
            i == 0 -> {
                val thumbnailUrl = slotThumbnailUrls[i]
            fb.copy(
                    // Keep hero card text concise in-card; full API details are shown in the sheet.
                    title = fb.title,
                    subtitle = fb.subtitle,
                    localImageResId = if (thumbnailUrl.isNullOrBlank()) R.drawable.play_hero_soccer else null,
                    imageUrl = thumbnailUrl.orEmpty(),
                    venue = venue
                )
            }
            i == 1 -> {
                val thumbnailUrl = slotThumbnailUrls[i]
                fb.copy(
                    title = fb.title,
                    subtitle = fb.subtitle,
                    localImageResId = if (thumbnailUrl.isNullOrBlank()) R.drawable.play_card_cricket else null,
                    imageUrl = thumbnailUrl.orEmpty(),
                    venue = venue
                )
            }
            i == 2 -> {
                val thumbnailUrl = slotThumbnailUrls[i]
                fb.copy(
                    title = fb.title,
                    subtitle = fb.subtitle,
                    localImageResId = if (thumbnailUrl.isNullOrBlank()) R.drawable.play_card_badminton else null,
                    imageUrl = thumbnailUrl.orEmpty(),
                    venue = venue
                )
            }
            i == 3 -> {
                val thumbnailUrl = slotThumbnailUrls[i]
                fb.copy(
                    title = fb.title,
                    subtitle = fb.subtitle,
                    localImageResId = if (thumbnailUrl.isNullOrBlank()) R.drawable.play_card_fun_activities else null,
                    imageUrl = thumbnailUrl.orEmpty(),
                    venue = venue
                )
            }
            i == 4 || i == 5 -> {
                val thumbnailUrl = slotThumbnailUrls[i]
                fb.copy(
                    title = fb.title,
                    subtitle = fb.subtitle,
                    localImageResId = null,
                    imageUrl = thumbnailUrl.orEmpty(),
                    venue = venue
                )
            }
            venue == null -> fb
            else -> fb.copy(
                title = venue.name.ifBlank { fb.title },
                subtitle = venue.description.ifBlank { fb.subtitle },
                imageUrl = venue.coverImages?.firstOrNull()?.url ?: fb.imageUrl,
                venue = venue
            )
        }
    }
}
