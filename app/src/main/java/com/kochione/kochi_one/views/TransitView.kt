package com.kochione.kochi_one.views

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import android.view.HapticFeedbackConstants
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kochione.kochi_one.R
import com.kochione.kochi_one.transit.Metro.MetroStation
import com.kochione.kochi_one.transit.Metro.data.KmrlOpenData
import com.kochione.kochi_one.transit.Metro.data.TrainScheduleEntry
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.kochione.kochi_one.viewmodels.TransitViewModel

import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs

// ─────────────────────────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────────────────────────

private fun Int.toHHMM(): String {
    val totalMins = this / 60
    val h24 = (totalMins / 60) % 24
    val m   = totalMins % 60
    val h12 = if (h24 % 12 == 0) 12 else h24 % 12
    val ampm = if (h24 < 12) "AM" else "PM"
    return "%d:%02d %s".format(h12, m, ampm)
}

private fun nowSeconds(): Int {
    val c = Calendar.getInstance()
    return c.get(Calendar.HOUR_OF_DAY) * 3600 +
           c.get(Calendar.MINUTE) * 60 +
           c.get(Calendar.SECOND)
}

// ─────────────────────────────────────────────────────────────────────────────
//  Root composable
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransitView(isDark: Boolean, transitViewModel: TransitViewModel = viewModel()) {
    val bgColor     = if (isDark) Color(0xFF1A1A1A) else Color.White
    val cardBg      = if (isDark) Color(0xFF272727) else Color(0xFFF5F5F5)
    val textColor   = if (isDark) Color.White else Color(0xFF1A1A1A)
    val dimColor    = textColor.copy(alpha = 0.4f)
    val highlightBg = if (isDark) Color(0xFF363636) else Color(0xFFE8E8E8)
    val accentBlue  = Color(0xFF29B6F6)
    val accentPink  = Color(0xFFF06292)

    val haptic   = LocalHapticFeedback.current
    val stations = KmrlOpenData.stations

    val fromStation by transitViewModel.fromStation.collectAsState()
    val toStation by transitViewModel.toStation.collectAsState()

    var fromExpanded    by remember { mutableStateOf(false) }
    var toExpanded      by remember { mutableStateOf(false) }
    var expandedTripId  by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(stations.size) {
        if (stations.isNotEmpty() && fromStation == null) {
            transitViewModel.setFromStation(stations.first())
            transitViewModel.setToStation(stations.first())
        }
    }

    val fare by remember(fromStation, toStation) {
        derivedStateOf {
            val from = fromStation?.stopId
            val to   = toStation?.stopId
            if (from != null && to != null) KmrlOpenData.getFare(from, to) else null
        }
    }

    val schedule by remember(fromStation, toStation) {
        derivedStateOf {
            val from = fromStation?.stopId
            val to   = toStation?.stopId
            if (from != null && to != null) KmrlOpenData.getSchedule(from, to) else emptyList()
        }
    }

    // Auto-tick every 60 s so upcoming/past labels refresh in real time
    var nowSecs by remember { mutableStateOf(nowSeconds()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000L)
            nowSecs = nowSeconds()
        }
    }

    // Upcoming trains: filter to trains not yet departed, keep last 1 as "just missed" context
    val upcomingSchedule by remember(schedule, nowSecs) {
        derivedStateOf {
            val lastDeparted = schedule.lastOrNull { it.departureFromOrigin < nowSecs }
            val coming       = schedule.filter { it.departureFromOrigin >= nowSecs }
            if (lastDeparted != null) listOf(lastDeparted) + coming else coming
        }
    }

    // Back-press: collapse open pickers / expanded rows before propagating
    val anyOpen = fromExpanded || toExpanded || expandedTripId != null
    BackHandler(enabled = anyOpen) {
        when {
            fromExpanded    -> fromExpanded   = false
            toExpanded      -> toExpanded     = false
            else            -> expandedTripId = null
        }
    }

    // verticalScroll lets the bottom-sheet's nestedScroll expand the sheet, then
    // scrolls the inner content once the sheet is fully open.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(top = 4.dp, bottom = 32.dp)
    ) {

        // ── Header ────────────────────────────────────────────────────────
        Text(
            text       = "Fare & Schedule",
            color      = textColor,
            fontWeight = FontWeight.Bold,
            fontSize   = 20.sp,
            modifier   = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
        )

        // ── From / To card ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg)
        ) {
            Column {
                StopRow(
                    label       = "From",
                    accentColor = accentBlue,
                    stationName = fromStation?.name ?: "Select one",
                    isExpanded  = fromExpanded,
                    textColor   = textColor,
                    dimColor    = dimColor,
                    onClick     = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        fromExpanded = !fromExpanded
                        if (fromExpanded) toExpanded = false
                    }
                )
                HorizontalDivider(
                    modifier  = Modifier.padding(start = 56.dp),
                    color     = dimColor.copy(alpha = 0.2f),
                    thickness = 0.8.dp
                )
                StopRow(
                    label       = "To",
                    accentColor = accentPink,
                    stationName = toStation?.name ?: "Select one",
                    isExpanded  = toExpanded,
                    textColor   = textColor,
                    dimColor    = dimColor,
                    onClick     = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        toExpanded = !toExpanded
                        if (toExpanded) fromExpanded = false
                    }
                )
            }

            // Swap button
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 18.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF404040) else Color(0xFFEAEAEA))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val tmp = fromStation
                        transitViewModel.setFromStation(toStation)
                        transitViewModel.setToStation(tmp)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter           = painterResource(id = R.drawable.ic_direction),
                    contentDescription = "Swap",
                    tint              = textColor,
                    modifier          = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── From picker ───────────────────────────────────────────────────
        if (fromExpanded && stations.isNotEmpty()) {
            StationWheelPicker(
                stations    = stations,
                initialSelected = fromStation,
                cardBg      = cardBg,
                textColor   = textColor,
                dimColor    = dimColor,
                highlightBg = highlightBg,
                onSelect    = { transitViewModel.setFromStation(it) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── To picker ─────────────────────────────────────────────────────
        if (toExpanded && stations.isNotEmpty()) {
            StationWheelPicker(
                stations    = stations,
                initialSelected = toStation,
                cardBg      = cardBg,
                textColor   = textColor,
                dimColor    = dimColor,
                highlightBg = highlightBg,
                onSelect    = { transitViewModel.setToStation(it) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── Fare result ───────────────────────────────────────────────────
        if (fromStation != null && toStation != null) {
            FareResultCard(
                fromStation = fromStation!!,
                toStation   = toStation!!,
                fare        = fare,
                cardBg      = cardBg,
                textColor   = textColor,
                accentBlue  = accentBlue,
                accentPink  = accentPink
            )
        }

        // ── Schedule list ─────────────────────────────────────────────────
        if (schedule.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text       = "Upcoming Trains",
                color      = textColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 26.sp,
                modifier   = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
            ) {
                upcomingSchedule.forEachIndexed { idx, entry ->
                    val isNext   = entry.departureFromOrigin >= nowSecs &&
                                   (idx == 0 || upcomingSchedule[idx - 1].departureFromOrigin < nowSecs)
                    val isPast   = entry.departureFromOrigin < nowSecs
                    val minsAway = (entry.departureFromOrigin - nowSecs) / 60
                    val isExp    = expandedTripId == entry.tripId

                    TrainScheduleRow(
                        entry        = entry,
                        isPast       = isPast,
                        isNext       = isNext,
                        minsAway     = minsAway,
                        isExpanded   = isExp,
                        fromStopId   = fromStation?.stopId ?: "",
                        toStopId     = toStation?.stopId ?: "",
                        nowSecs      = nowSecs,
                        textColor    = textColor,
                        dimColor     = dimColor,
                        accentBlue   = accentBlue,
                        accentPink   = accentPink,
                        isDark       = isDark,
                        cardBg       = cardBg,
                        onTap        = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            expandedTripId = if (isExp) null else entry.tripId
                        }
                    )

                    if (idx < upcomingSchedule.lastIndex) {
                        HorizontalDivider(
                            modifier  = Modifier.padding(start = 16.dp),
                            color     = dimColor.copy(alpha = 0.15f),
                            thickness = 0.6.dp
                        )
                    }
                }
            }
        } else if (fromStation != null && toStation != null && KmrlOpenData.trips.isNotEmpty()) {
            // Trips loaded but no schedule for this pair (same station or reverse direction)
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text      = if (fromStation?.stopId == toStation?.stopId)
                                    "Select different start & end stations"
                                else
                                    "No direct trains for this direction",
                    color     = dimColor,
                    fontSize  = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } // end Column
}

// ─────────────────────────────────────────────────────────────────────────────
//  Stop selector row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StopRow(
    label: String, accentColor: Color, stationName: String,
    isExpanded: Boolean, textColor: Color, dimColor: Color, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp).height(38.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = dimColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(stationName, color = textColor, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        val chevronRotation by animateFloatAsState(
            targetValue    = if (isExpanded) 180f else 0f,
            animationSpec  = tween(durationMillis = 250),
            label          = "chevronRotation"
        )
        Icon(
            painter           = painterResource(id = R.drawable.ic_chevron_down),
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint              = dimColor,
            modifier          = Modifier.size(22.dp).graphicsLayer { rotationZ = chevronRotation }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Wheel-style station picker  (fixed height → safe inside verticalScroll)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StationWheelPicker(
    stations: List<MetroStation>, initialSelected: MetroStation?,
    cardBg: Color, textColor: Color, dimColor: Color, highlightBg: Color,
    onSelect: (MetroStation) -> Unit
) {
    val itemHeightDp  = 56.dp
    val visibleItems  = 5
    val padding       = visibleItems / 2

    val padded        = List(padding) { null } + stations + List(padding) { null }
    val initialIdx    = (stations.indexOfFirst { it.stopId == initialSelected?.stopId }).coerceAtLeast(0)
    val listState     = rememberLazyListState(initialFirstVisibleItemIndex = initialIdx)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val coroutineScope = rememberCoroutineScope()

    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val centerPaddedIdx by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            layoutInfo.visibleItemsInfo.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - viewportCenter) }?.index 
                ?: (listState.firstVisibleItemIndex + padding)
        }
    }

    // 1. Immediate tactile feedback (the "tick")
    LaunchedEffect(listState) {
        snapshotFlow { centerPaddedIdx }.collect { idx ->
            val stIdx = idx - padding
            if (stIdx in stations.indices) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }
    }

    // 2. Debounced state update: Only update the ViewModel when the scroll settles.
    // This prevents expensive global recompositions (Map, Schedule) during the drag.
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val stIdx = centerPaddedIdx - padding
            if (stIdx in stations.indices) {
                onSelect(stations[stIdx])
            }
        }
    }

    // 3. Nested Scroll Trap: Prevent parent (BottomSheet) from moving when interacting with the picker.
    val pickerNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Return Offset.Zero to allow the child (LazyColumn) to receive the scroll first
                return Offset.Zero
            }
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // Consume all leftover scroll to keep the BottomSheet stable
                return available
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth().padding(horizontal = 10.dp)
                .height(itemHeightDp)
                .clip(RoundedCornerShape(14.dp))
                .background(highlightBg)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter).fillMaxWidth()
                .height(itemHeightDp * padding)
                .background(Brush.verticalGradient(listOf(cardBg, Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter).fillMaxWidth()
                .height(itemHeightDp * padding)
                .background(Brush.verticalGradient(listOf(Color.Transparent, cardBg)))
        )
        LazyColumn(
            state         = listState,
            flingBehavior = flingBehavior,
            modifier      = Modifier
                .fillMaxWidth()
                .height(itemHeightDp * visibleItems)
                .nestedScroll(pickerNestedScrollConnection)
        ) {
            itemsIndexed(padded) { index, station ->
                val dist     = abs(index - centerPaddedIdx)
                val alpha    = when (dist) { 0 -> 1f; 1 -> 0.55f; else -> 0.25f }
                val isCenter = dist == 0
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(itemHeightDp)
                        .then(if (station != null) Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch { listState.animateScrollToItem(index - padding) }
                        } else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        station != null -> Text(
                            text       = station.name,
                            color      = textColor.copy(alpha = alpha),
                            fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
                            fontSize   = if (isCenter) 19.sp else 15.sp,
                            textAlign  = TextAlign.Center
                        )
                        index == 0 -> Text("Select one", color = dimColor, fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Fare result card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FareResultCard(
    fromStation: MetroStation, toStation: MetroStation, fare: Double?,
    cardBg: Color, textColor: Color, accentBlue: Color, accentPink: Color
) {
    val context  = LocalContext.current
    val haptic   = LocalHapticFeedback.current
    val fareText = if (fare != null && fare > 0) "\u20B9${fare.toInt()}" else "—"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // ── Main card ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column {
                // From row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(accentBlue)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text     = "From",
                            color    = textColor.copy(alpha = 0.45f),
                            fontSize = 12.sp
                        )
                        Text(
                            text       = fromStation.name,
                            color      = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 18.sp
                        )
                    }
                    // Fare top-right
                    Text(
                        text       = fareText,
                        color      = textColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 26.sp
                    )
                }

                // Connecting line between dots
                Box(
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .width(2.dp)
                        .height(24.dp)
                        .background(textColor.copy(alpha = 0.12f))
                )

                // To row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(accentPink)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text     = "To",
                            color    = textColor.copy(alpha = 0.45f),
                            fontSize = 12.sp
                        )
                        Text(
                            text       = toStation.name,
                            color      = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Book Ticket in WhatsApp button ───────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=919188957488")
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter            = painterResource(R.drawable.ic_chat_bubble),
                    contentDescription = null,
                    tint               = textColor,
                    modifier           = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text       = "Book Ticket in WhatsApp",
                    color      = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Single train schedule row (inline expandable)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TrainScheduleRow(
    entry: TrainScheduleEntry,
    isPast: Boolean, isNext: Boolean, minsAway: Int,
    isExpanded: Boolean,
    fromStopId: String, toStopId: String,
    nowSecs: Int,
    textColor: Color, dimColor: Color,
    accentBlue: Color, accentPink: Color,
    isDark: Boolean, cardBg: Color,
    onTap: () -> Unit
) {
    val green    = Color(0xFF4CAF50)
    val isNow    = isNext && minsAway <= 0
    val rowBg    = if (isNow) green.copy(alpha = 0.12f) else Color.Transparent
    val haptic   = LocalHapticFeedback.current
    val chevRot  by animateFloatAsState(
        targetValue   = if (isExpanded) 180f else 0f,
        animationSpec = tween(250),
        label         = "chev"
    )

    Column {
        // ── Summary row ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTap()
                }
                .background(rowBg)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = entry.departureFromOrigin.toHHMM(),
                    color      = if (isPast) dimColor else textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 22.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = when { isNow -> "Now"; isPast -> "Departed"; else -> "in $minsAway min" },
                    color      = if (isNow) green else dimColor,
                    fontWeight = if (isNow) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize   = 14.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "Arrives at destination at ${entry.arrivalAtDest.toHHMM()}",
                    color = dimColor.copy(alpha = if (isPast) 0.5f else 0.75f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    text  = if (isPast) "Departed" else "Not yet departed",
                    color = dimColor.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            }
            Icon(
                painter            = painterResource(R.drawable.ic_chevron_down),
                contentDescription = null,
                tint               = dimColor.copy(alpha = 0.6f),
                modifier           = Modifier.size(20.dp).graphicsLayer { rotationZ = chevRot }
            )
        }

        // ── Expanded: Route Stations ──────────────────────────────────────
        if (isExpanded) {
            val stops = remember(entry.tripId, fromStopId, toStopId) {
                KmrlOpenData.getTripStops(entry.tripId, fromStopId, toStopId)
            }

            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = 16.dp),
                color     = dimColor.copy(alpha = 0.15f),
                thickness = 0.6.dp
            )
            Text(
                text       = "Route Stations",
                color      = dimColor,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 13.sp,
                modifier   = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                stops.forEachIndexed { idx, (stopName, depTime) ->
                    val isFirst   = idx == 0
                    val isLast    = idx == stops.lastIndex
                    val nextTime  = if (isLast) Int.MAX_VALUE else stops[idx + 1].second
                    val isCurrent = !isFirst && !isLast && depTime <= nowSecs && nowSecs < nextTime
                    val dotColor  = when {
                        isCurrent -> green
                        isFirst   -> accentBlue
                        isLast    -> accentPink
                        else      -> green.copy(alpha = 0.75f)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.width(20.dp).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isFirst) Box(
                                Modifier.align(Alignment.TopCenter)
                                    .width(2.dp).fillMaxHeight(0.5f)
                                    .background(dimColor.copy(alpha = 0.2f))
                            )
                            if (!isLast) Box(
                                Modifier.align(Alignment.BottomCenter)
                                    .width(2.dp).fillMaxHeight(0.5f)
                                    .background(dimColor.copy(alpha = 0.2f))
                            )
                            Box(
                                Modifier.size(if (isFirst || isLast) 13.dp else 11.dp)
                                    .clip(CircleShape).background(dotColor)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = stopName,
                                color      = textColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 15.sp
                            )
                            Text(
                                text = when {
                                    isFirst -> "Starting Point"
                                    isLast  -> "Destination"
                                    else    -> "Arrives at ${depTime.toHHMM()}"
                                },
                                color = when {
                                    isFirst -> accentBlue
                                    isLast  -> accentPink
                                    else    -> dimColor
                                },
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
