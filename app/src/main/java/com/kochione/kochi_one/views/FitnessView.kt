package com.kochione.kochi_one.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kochione.kochi_one.R
import com.kochione.kochi_one.models.DayHours
import com.kochione.kochi_one.models.FitnessVenue
import com.kochione.kochi_one.models.OperatingHours
import com.kochione.kochi_one.viewmodels.FitnessViewModel
import java.util.Calendar

private val FitnessCardCorner = 20.dp
private val FitnessSectionGap = 20.dp
private val FitnessScreenPaddingH = 16.dp
private val FitnessTopRowHeight = 260.dp

private enum class FitnessCardVariant {
    HERO_LEFT,
    STACK_TOP,
    STACK_BOTTOM,
    FULL_CENTRE,
    FULL_HEALTH
}

private data class FitnessCardData(
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val variant: FitnessCardVariant,
    val localImageResId: Int? = null,
    val venue: FitnessVenue? = null
)

@Composable
fun FitnessView(
    isDarkTheme: Boolean,
    viewModel: FitnessViewModel = viewModel(),
    onDetailVisibilityChanged: (Boolean) -> Unit = {},
    onRegisterDismissDetail: ((() -> Unit)?) -> Unit = {}
) {
    val venues by viewModel.venues.collectAsState()
    val categoryThumbnails by viewModel.categoryThumbnails.collectAsState()
    // Theme handled by parameter
    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
    var selectedCard by remember { mutableStateOf<FitnessCardData?>(null) }

    androidx.compose.runtime.LaunchedEffect(selectedCard) {
        onDetailVisibilityChanged(selectedCard != null)
    }

    // System back: dismiss detail → return to list
    BackHandler(enabled = selectedCard != null) { selectedCard = null }

    DisposableEffect(selectedCard) {
        if (selectedCard != null) {
            onRegisterDismissDetail { selectedCard = null }
        } else {
            onRegisterDismissDetail(null)
        }
        onDispose { onRegisterDismissDetail(null) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 88.dp,
                start = FitnessScreenPaddingH,
                end = FitnessScreenPaddingH
            ),
            verticalArrangement = Arrangement.spacedBy(FitnessSectionGap)
        ) {
            item {
                val cards = remember(venues, categoryThumbnails) { buildFitnessCards(venues, categoryThumbnails) }
                val hero = cards[0]
                val stackTop = cards[1]
                val stackBottom = cards[2]

                Column(verticalArrangement = Arrangement.spacedBy(FitnessSectionGap)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(FitnessTopRowHeight),
                        horizontalArrangement = Arrangement.spacedBy(FitnessSectionGap)
                    ) {
                        FitnessFeatureCard(
                            card = hero,
                            modifier = Modifier
                                .weight(0.54f)
                                .fillMaxHeight(),
                            onCardClick = { selectedCard = hero }
                        )
                        Column(
                            modifier = Modifier
                                .weight(0.49f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(FitnessSectionGap)
                        ) {
                            FitnessFeatureCard(
                                card = stackTop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                onCardClick = { selectedCard = stackTop }
                            )
                            FitnessFeatureCard(
                                card = stackBottom,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                onCardClick = { selectedCard = stackBottom }
                            )
                        }
                    }

                    cards.drop(3).forEach { card ->
                        FitnessFeatureCard(
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
        if (selectedCard != null) {
            val detailVenues = buildFitnessDetailVenuesForCard(selectedCard!!, venues)
            if (detailVenues.isNotEmpty()) {
                FitnessVenueFullScreenSheet(venues = detailVenues, isDarkTheme = isDarkTheme)
            } else {
                FitnessVenueEmptyFullScreenSheet(card = selectedCard!!, isDarkTheme = isDarkTheme)
            }
        }
    }
}

@Composable
private fun FitnessFeatureCard(
    card: FitnessCardData,
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
                shape = RoundedCornerShape(FitnessCardCorner),
                spotColor = Color.Black.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(FitnessCardCorner))
            .then(if (onCardClick != null) Modifier.clickable { onCardClick() } else Modifier)
    ) {
        when (card.variant) {
            FitnessCardVariant.FULL_CENTRE -> FitnessCentreCardContent(card)
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
                FitnessCardBottomContent(card)
            }
        }
    }
}

@Composable
private fun BoxScope.FitnessCardBottomContent(card: FitnessCardData) {
    val isStackCard = card.variant == FitnessCardVariant.STACK_TOP || card.variant == FitnessCardVariant.STACK_BOTTOM
    val isMma = card.variant == FitnessCardVariant.STACK_BOTTOM
    val isYoga = card.variant == FitnessCardVariant.STACK_TOP
    val isHealth = card.variant == FitnessCardVariant.FULL_HEALTH
    val isGymHero = card.variant == FitnessCardVariant.HERO_LEFT

    Column(
        modifier = Modifier
            .align(if (isHealth) Alignment.Center else Alignment.BottomStart)
            .padding(
                start = if (isStackCard) 32.dp else 20.dp,
                end = if (isHealth) 50.dp else 18.dp,
                top = if (isHealth) 70.dp else 0.dp,
                bottom = if (isHealth) 20.dp else 16.dp
            )
            .then(if (isHealth) Modifier.fillMaxWidth() else Modifier),
        horizontalAlignment = if (isHealth) Alignment.CenterHorizontally else Alignment.Start
    ) {
        val titleStyle = when (card.variant) {
            FitnessCardVariant.HERO_LEFT ->
                MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            FitnessCardVariant.STACK_TOP, FitnessCardVariant.STACK_BOTTOM ->
                MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            else -> MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
        }

        val subtitleStyle = when (card.variant) {
            FitnessCardVariant.HERO_LEFT -> MaterialTheme.typography.bodyLarge
            FitnessCardVariant.STACK_TOP, FitnessCardVariant.STACK_BOTTOM -> MaterialTheme.typography.bodyMedium
            else -> MaterialTheme.typography.bodyLarge
        }
        val effectiveSubtitleStyle = if (isYoga) MaterialTheme.typography.bodySmall else subtitleStyle

        Text(
            text = card.title,
            style = titleStyle,
            color = Color.White,
            maxLines = if (card.variant == FitnessCardVariant.HERO_LEFT) 2 else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = when {
                isHealth -> Modifier.fillMaxWidth()
                isYoga || isMma -> Modifier.fillMaxWidth().padding(start = 58.dp)
                isStackCard -> Modifier.fillMaxWidth().padding(start = 18.dp)
                else -> Modifier
            },
            textAlign = when {
                isHealth -> androidx.compose.ui.text.style.TextAlign.Center
                isStackCard -> androidx.compose.ui.text.style.TextAlign.Start
                else -> androidx.compose.ui.text.style.TextAlign.Start
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = card.subtitle,
            style = effectiveSubtitleStyle,
            color = Color.White.copy(alpha = 0.95f),
            fontWeight = FontWeight.Medium,
            maxLines = when {
                isGymHero || isMma || isYoga -> 1
                else -> 2
            },
            overflow = TextOverflow.Ellipsis,
            modifier = when {
                isHealth -> Modifier.fillMaxWidth()
                isStackCard -> Modifier.fillMaxWidth().padding(start = if (isYoga) 10.dp else 18.dp)
                else -> Modifier
            },
            textAlign = if (isHealth) androidx.compose.ui.text.style.TextAlign.Center else androidx.compose.ui.text.style.TextAlign.Start
        )
    }
}

@Composable
private fun FitnessCentreCardContent(card: FitnessCardData) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF2B313A), Color(0xFF1C222B), Color(0xFF10151C))
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        )
        val localId = card.localImageResId
        if (localId != null) {
            Image(
                painter = painterResource(id = localId),
                contentDescription = card.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.52f
            )
        } else {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = card.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.52f
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_fitness),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp)
                .size(100.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth(0.62f)) {
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
            FitnessHubActionRow()
        }
    }
}

@Composable
private fun FitnessHubActionRow() {
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
                    painter = painterResource(id = R.drawable.ic_directions_run),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Fitness Hub",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun FitnessVenueEmptyFullScreenSheet(card: FitnessCardData, isDarkTheme: Boolean) {
    val pageBg = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_fitness),
                contentDescription = null,
                tint = textColor.copy(alpha = 0.92f),
                modifier = Modifier.size(58.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No venue found",
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

@Composable
private fun FitnessVenueFullScreenSheet(
    venues: List<FitnessVenue>,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val pageBg     = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val textColor  = if (isDarkTheme) Color.White else Color(0xFF1A1A1A)
    val dimColor   = if (isDarkTheme) Color.White.copy(alpha = 0.62f) else Color(0xFF555555)
    val iconBgColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color(0xFFF0F0F0)
    val iconTintColor = if (isDarkTheme) Color.White.copy(alpha = 0.9f) else Color(0xFF1A1A1A)
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
                    val dayHours = todayHoursFitness(venue.operatingHours)
                    val openNow = isOpenNowFitness(dayHours)
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
                                    color = iconBgColor
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
                                            painter = painterResource(id = R.drawable.ic_fitness),
                                            contentDescription = null,
                                            tint = iconTintColor,
                                            modifier = Modifier
                                                .padding(10.dp)
                                                .fillMaxSize()
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = venue.name,
                                        color = textColor,
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
                                            color = if (openNow) {
                                                if (isDarkTheme) Color(0xFF86EFAC) else Color(0xFF16A34A)
                                            } else {
                                                if (isDarkTheme) Color(0xFFFF6B6B) else Color(0xFFDC2626)
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = " \u00b7 $statusSuffix",
                                            color = dimColor,
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
                                    color = dimColor,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_near_me),
                                    contentDescription = null,
                                    tint = textColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = venue.description.ifBlank { "No description available." },
                            color = dimColor,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
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
                                        .fillMaxWidth(0.52f)
                                        .height(110.dp)
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
                        val actionTint = if (isDarkTheme) Color.White.copy(alpha = 0.76f) else Color.Black.copy(alpha = 0.62f)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FitnessSheetActionIcon(
                                iconRes = R.drawable.ic_call,
                                tint = actionTint,
                                onClick = { callFitnessVenue(context, venue.contact.phone) }
                            )
                            FitnessSheetActionIcon(
                                iconRes = R.drawable.ic_near_me,
                                tint = actionTint,
                                onClick = { openFitnessVenueMap(context, venue.location.latitude, venue.location.longitude, venue.name) }
                            )
                            FitnessSheetActionIcon(
                                iconRes = if (liked) R.drawable.ic_heart_filled else R.drawable.ic_heart,
                                tint = if (liked) Color(0xFFFF3B30) else actionTint,
                                isActive = liked,
                                animateOnActivate = true,
                                onClick = { likedMap[venue.id] = !liked }
                            )
                            FitnessSheetActionIcon(
                                iconRes = if (saved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark,
                                tint = if (saved) Color(0xFFEAB308) else actionTint,
                                isActive = saved,
                                animateOnActivate = true,
                                onClick = { savedMap[venue.id] = !saved }
                            )
                            FitnessSheetActionIcon(
                                iconRes = R.drawable.ic_share,
                                tint = actionTint,
                                onClick = { shareFitnessVenue(context, venue) }
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = textColor.copy(alpha = 0.08f))
                    }
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun FitnessSheetActionIcon(
    iconRes: Int,
    tint: Color = Color.White.copy(alpha = 0.76f),
    isActive: Boolean = false,
    animateOnActivate: Boolean = false,
    onClick: () -> Unit
) {
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    LaunchedEffect(isActive, animateOnActivate) {
        if (!animateOnActivate) return@LaunchedEffect
        if (isActive) {
            scale.animateTo(1.25f, animationSpec = androidx.compose.animation.core.tween(durationMillis = 110))
            scale.animateTo(
                1f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            )
        } else {
            scale.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(durationMillis = 100))
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

private fun todayHoursFitness(operatingHours: OperatingHours): DayHours {
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

private fun isOpenNowFitness(dayHours: DayHours): Boolean {
    if (dayHours.closed) return false
    val open = parseTimeToMinutesFitness(dayHours.open) ?: return false
    val close = parseTimeToMinutesFitness(dayHours.close) ?: return false
    val nowCalendar = Calendar.getInstance()
    val now = nowCalendar.get(Calendar.HOUR_OF_DAY) * 60 + nowCalendar.get(Calendar.MINUTE)
    return if (close > open) now in (open + 1) until close else now > open || now < close
}

private fun parseTimeToMinutesFitness(value: String?): Int? {
    if (value.isNullOrBlank()) return null
    val parts = value.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour * 60 + minute
}

private fun callFitnessVenue(context: android.content.Context, phone: String?) {
    if (phone.isNullOrBlank()) return
    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
        data = android.net.Uri.parse("tel:$phone")
    }
    runCatching { context.startActivity(intent) }
}

private fun openFitnessVenueMap(context: android.content.Context, lat: Double, lon: Double, name: String) {
    val geo = "geo:$lat,$lon?q=$lat,$lon(${android.net.Uri.encode(name)})"
    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(geo))
    runCatching { context.startActivity(intent) }
}

private fun shareFitnessVenue(context: android.content.Context, venue: FitnessVenue) {
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

private fun buildFitnessDetailVenuesForCard(card: FitnessCardData, allVenues: List<FitnessVenue>): List<FitnessVenue> {
    val keys = when (card.variant) {
        FitnessCardVariant.HERO_LEFT -> listOf("gym", "strength", "workout")
        FitnessCardVariant.STACK_TOP -> listOf("yoga")
        FitnessCardVariant.STACK_BOTTOM -> listOf("mma", "boxing", "fight", "martial")
        FitnessCardVariant.FULL_CENTRE -> listOf("fitness", "center", "centre", "studio")
        FitnessCardVariant.FULL_HEALTH -> listOf("club", "health", "wellness")
    }
    val filtered = allVenues.filter { venue ->
        val n = venue.name.lowercase()
        val c = venue.fitnessCategory.lowercase()
        keys.any { key -> n.contains(key) || c.contains(key) }
    }
    return if (filtered.isNotEmpty()) filtered else listOfNotNull(card.venue)
}

private fun buildFitnessCards(
    venues: List<FitnessVenue>,
    categoryThumbnails: Map<String, String>
): List<FitnessCardData> {
    val fallbacks = listOf(
        FitnessCardData(
            title = "Gym",
            subtitle = "Build Your Strength",
            imageUrl = "",
            variant = FitnessCardVariant.HERO_LEFT,
            localImageResId = R.drawable.fitness_hero_gym
        ),
        FitnessCardData(
            title = "Yoga",
            subtitle = "Find Your Balance",
            imageUrl = "",
            variant = FitnessCardVariant.STACK_TOP,
            localImageResId = R.drawable.fitness_card_yoga
        ),
        FitnessCardData(
            title = "MMA",
            subtitle = "Fight Your Way",
            imageUrl = "",
            variant = FitnessCardVariant.STACK_BOTTOM,
            localImageResId = R.drawable.fitness_card_mma
        ),
        FitnessCardData(
            title = "Fitness Centre",
            subtitle = "Transform Your Body",
            imageUrl = "",
            variant = FitnessCardVariant.FULL_CENTRE,
            localImageResId = R.drawable.fitness_card_center_health
        ),
        FitnessCardData(
            title = "Health Clubs",
            subtitle = "Energy is Contagious",
            imageUrl = "",
            variant = FitnessCardVariant.FULL_HEALTH,
            localImageResId = R.drawable.fitness_card_center_health
        )
    )

    fun thumbnailFor(vararg keys: String): String? {
        val normalizedEntries = categoryThumbnails.entries.map { (k, v) -> k.lowercase() to v }
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
        thumbnailFor("gym", "strength", "workout"),
        thumbnailFor("yoga"),
        thumbnailFor("mma", "boxing", "martial", "fight"),
        thumbnailFor("fitness centre", "fitness center", "fitness", "studio"),
        thumbnailFor("health club", "wellness", "health")
    )

    if (venues.isEmpty()) {
        return fallbacks.indices.map { i ->
            val fallback = fallbacks[i]
            val thumb = slotThumbnailUrls[i]
            fallback.copy(
                localImageResId = if (thumb.isNullOrBlank()) fallback.localImageResId else null,
                imageUrl = thumb.orEmpty()
            )
        }
    }

    val usedIds = mutableSetOf<String>()

    fun matches(venue: FitnessVenue, keys: List<String>): Boolean {
        val n = venue.name.lowercase()
        val c = venue.fitnessCategory.lowercase()
        return keys.any { k -> n.contains(k) || c.contains(k) }
    }

    fun takeFirstMatch(keys: List<String>): FitnessVenue? {
        val v = venues.firstOrNull { it.id !in usedIds && matches(it, keys) }
        if (v != null) usedIds.add(v.id)
        return v
    }

    val slotVenues = listOf(
        takeFirstMatch(listOf("gym", "strength", "workout")),
        takeFirstMatch(listOf("yoga")),
        takeFirstMatch(listOf("mma", "boxing", "fight", "martial")),
        takeFirstMatch(listOf("fitness", "center", "centre", "studio")),
        takeFirstMatch(listOf("club", "health", "wellness"))
    )

    val rest = venues.filter { it.id !in usedIds }
    var restIndex = 0

    fun fillSlot(i: Int): FitnessVenue? {
        if (slotVenues[i] != null) return slotVenues[i]
        if (restIndex < rest.size) return rest[restIndex++].also { usedIds.add(it.id) }
        return null
    }

    return fallbacks.indices.map { i ->
        val fb = fallbacks[i]
        val venue = fillSlot(i)
        when {
            i == 0 -> {
                val thumbnailUrl = slotThumbnailUrls[i]
                fb.copy(
                    // Keep hero card copy static on the grid; show API venue details only in full-screen sheet.
                    title = fb.title,
                    subtitle = fb.subtitle,
                    localImageResId = if (thumbnailUrl.isNullOrBlank()) R.drawable.fitness_hero_gym else null,
                    imageUrl = thumbnailUrl.orEmpty(),
                    venue = venue
                )
            }
            i == 2 -> {
                val thumbnailUrl = slotThumbnailUrls[i]
                fb.copy(
                    // Keep MMA card copy static on the grid; show venue details only in full-screen sheet.
                    title = fb.title,
                    subtitle = fb.subtitle,
                    localImageResId = if (thumbnailUrl.isNullOrBlank()) R.drawable.fitness_card_mma else null,
                    imageUrl = thumbnailUrl.orEmpty(),
                    venue = venue
                )
            }
            i == 1 -> {
                val thumbnailUrl = slotThumbnailUrls[i]
                fb.copy(
                    // Keep Yoga card copy static on the grid; show venue details only in full-screen sheet.
                    title = fb.title,
                    subtitle = fb.subtitle,
                    localImageResId = if (thumbnailUrl.isNullOrBlank()) R.drawable.fitness_card_yoga else null,
                    imageUrl = thumbnailUrl.orEmpty(),
                    venue = venue
                )
            }
            i == 3 || i == 4 -> {
                val thumbnailUrl = slotThumbnailUrls[i]
                fb.copy(
                    // Keep lower full-width card copy static on the grid; show venue details only in full-screen sheet.
                    title = fb.title,
                    subtitle = fb.subtitle,
                    localImageResId = if (thumbnailUrl.isNullOrBlank()) R.drawable.fitness_card_center_health else null,
                    imageUrl = thumbnailUrl.orEmpty(),
                    venue = venue
                )
            }
            venue == null -> fb
            else -> {
                fb.copy(
                    title = fb.title,
                    subtitle = fb.subtitle,
                    imageUrl = fb.imageUrl,
                    venue = venue
                )
            }
        }
    }
}