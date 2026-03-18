package com.kochione.kochi_one


import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.tasks.Task
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.kochione.kochi_one.maps.RestaurantMapMarkers
import com.kochione.kochi_one.models.ExplorePost
import com.kochione.kochi_one.ui.theme.KochiOneTheme
import com.kochione.kochi_one.viewmodels.FoodViewModel
import com.kochione.kochi_one.views.ExploreView
import com.kochione.kochi_one.views.FitnessView
import com.kochione.kochi_one.views.FoodView
import com.kochione.kochi_one.views.PlayView
import com.kochione.kochi_one.views.ProfileView
import com.kochione.kochi_one.views.TransitView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs

private val BottomNavTabOrder = listOf("Explore", "Food", "Play", "Fitness", "Transit", "Profile")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        enableEdgeToEdge()
        setContent {
            KochiOneTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val haptic = LocalHapticFeedback.current
    
    var currentHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            delay(60_000)
        }
    }
    val isEveningTime = currentHour >= 18 || currentHour < 6
    val isDarkTheme = isSystemInDarkTheme() || isEveningTime
    val useNightMapStyle = isEveningTime
    val context = androidx.compose.ui.platform.LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    var showLocationRationale by remember { mutableStateOf(false) }
    var selectedBottomTab by remember { mutableStateOf("Explore") }
    val foodViewModel: FoodViewModel = viewModel()

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            showLocationRationale = true
        }
    }

    if (showLocationRationale) {
        AlertDialog(
            onDismissRequest = { /* Don't dismiss without choice */ },
            title = { Text("Location Access", fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.location_rationale_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocationRationale = false
                        permissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Text("Allow", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationRationale = false }) {
                    Text("Skip", color = Color.Gray)
                }
            },
            containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val expandedHeightPx = with(density) { (screenHeightDp * 0.95f).toPx() }
    val halfExpandedHeightPx = with(density) { (screenHeightDp * 0.6f).toPx() }
    val collapsedHeightPx = with(density) { 200.dp.toPx() }
    
    val sheetHeightPx = remember { Animatable(collapsedHeightPx) }
    val currentSheetHeightDp = with(density) { sheetHeightPx.value.toDp() }
    // Capsule starts disappearing when sheet crosses 50% of the total height
    val isSheetExpanded = sheetHeightPx.value > expandedHeightPx * 0.65f

    val kochiLocation = LatLng(9.9312, 76.2673)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(kochiLocation, 12f)
    }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        com.kochione.kochi_one.transit.Metro.data.KmrlOpenData.load(context)
    }
    
    val coroutineScope = rememberCoroutineScope()
    val restaurants by foodViewModel.restaurants.collectAsState()
    val activeTrains by remember { com.kochione.kochi_one.transit.Metro.TrainSimulator.activeTrainsFlow() }.collectAsState(initial = emptyList())

    // When opening Food, keep the map on the user (same as the location pill), not on restaurant bounds.
    androidx.compose.runtime.LaunchedEffect(selectedBottomTab, hasLocationPermission) {
        if (selectedBottomTab != "Food") return@LaunchedEffect
        if (!hasLocationPermission) return@LaunchedEffect
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    com.kochione.kochi_one.utils.LocationRepository.update(location.latitude, location.longitude)
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f),
                            500
                        )
                    }
                }
            }
        } catch (_: SecurityException) {
        }
    }

    // When Transit tab is tapped open the sheet to half height;
    // the user can then scroll up to go full-screen.
    val transitSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    androidx.compose.runtime.LaunchedEffect(selectedBottomTab) {
        // Only open Transit sheet from collapsed → half.
        // If sheet is already at half or full height, leave it untouched regardless of which tab is selected.
        if (selectedBottomTab == "Transit" && sheetHeightPx.value < halfExpandedHeightPx - 10f) {
            sheetHeightPx.animateTo(halfExpandedHeightPx, animationSpec = transitSpring)
        }
    }

    // Back press: collapse sheet → go home → system exit
    val sheetIsExpanded = sheetHeightPx.value > halfExpandedHeightPx + 10f
    val notOnHome      = selectedBottomTab != "Explore"
    BackHandler(enabled = sheetIsExpanded || notOnHome) {
        coroutineScope.launch {
            when {
                sheetIsExpanded -> sheetHeightPx.animateTo(halfExpandedHeightPx, animationSpec = transitSpring)
                notOnHome       -> selectedBottomTab = "Explore"
            }
        }
    }

    // Double-press to exit when already on Explore home with sheet not expanded
    val activity = context as? android.app.Activity
    var lastBackPressMs by remember { mutableStateOf(0L) }
    BackHandler(enabled = !sheetIsExpanded && !notOnHome) {
        val now = System.currentTimeMillis()
        if (now - lastBackPressMs < 2000L) {
            activity?.finish()
        } else {
            lastBackPressMs = now
            android.widget.Toast.makeText(context, "Press back again to exit", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val settingResultRequest = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == android.app.Activity.RESULT_OK) {
            // User agreed to turn on GPS. Fetch location.
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            val client = LocationServices.getSettingsClient(context)
            val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
            
            task.addOnSuccessListener {
                // GPS is already on
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            com.kochione.kochi_one.utils.LocationRepository.update(location.latitude, location.longitude)
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
                        }
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }
            
            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // GPS is off, prompt user to turn it on
                    try {
                        val intentSenderRequest = androidx.activity.result.IntentSenderRequest.Builder(exception.resolution).build()
                        settingResultRequest.launch(intentSenderRequest)
                    } catch (sendEx: android.content.IntentSender.SendIntentException) {
                        // Ignore the error
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // Ignores safe areas edge-to-edge
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = com.google.maps.android.compose.MapUiSettings(
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false // Clean UI
                ),
                properties = com.google.maps.android.compose.MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    mapStyleOptions = if (useNightMapStyle) {
                        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_night)
                    } else {
                        null
                    }
                )
            ) {
                RestaurantMapMarkers(
                    restaurants = restaurants,
                    visible = selectedBottomTab == "Food",
                    onRestaurantMarkerClick = { r ->
                        foodViewModel.fetchRestaurantByBizId(r.bizId)
                    }
                )

                if (selectedBottomTab == "Transit") {
                    com.google.maps.android.compose.Polyline(
                        points = com.kochione.kochi_one.transit.Metro.data.KmrlOpenData.routePoints,
                        color = Color(0xFF03A9F4),
                        width = 14f,
                        geodesic = true,
                        startCap = com.google.android.gms.maps.model.RoundCap(),
                        endCap = com.google.android.gms.maps.model.RoundCap(),
                        jointType = com.google.android.gms.maps.model.JointType.ROUND,
                        zIndex = 100f
                    )
                    
                    com.google.maps.android.compose.Polyline(
                        points = com.kochione.kochi_one.transit.Metro.data.KmrlOpenData.routePoints2,
                        color = Color(0xFF03A9F4),
                        width = 14f,
                        geodesic = true,
                        startCap = com.google.android.gms.maps.model.RoundCap(),
                        endCap = com.google.android.gms.maps.model.RoundCap(),
                        jointType = com.google.android.gms.maps.model.JointType.ROUND,
                        zIndex = 100f
                    )
                    
                    val metroIcon = remember(context) {
                        androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_metro_station)?.let { drawable ->
                            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                            val bitmap = android.graphics.Bitmap.createBitmap(
                                drawable.intrinsicWidth, 
                                drawable.intrinsicHeight, 
                                android.graphics.Bitmap.Config.ARGB_8888
                            )
                            val canvas = android.graphics.Canvas(bitmap)
                            drawable.draw(canvas)
                            com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(bitmap)
                        }
                    }

                    val trainIcon = remember(context) {
                        try {
                            androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_train_top)?.let { drawable ->
                                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                                val bitmap = android.graphics.Bitmap.createBitmap(
                                    drawable.intrinsicWidth, 
                                    drawable.intrinsicHeight, 
                                    android.graphics.Bitmap.Config.ARGB_8888
                                )
                                val canvas = android.graphics.Canvas(bitmap)
                                drawable.draw(canvas)
                                
                                val targetWidth = 120 // slightly increased size
                                val targetHeight = (targetWidth * (drawable.intrinsicHeight.toFloat() / drawable.intrinsicWidth)).toInt().coerceAtLeast(1)
                                val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                                com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap(scaled)
                            }
                        } catch (e: Exception) { null }
                    }

                    com.kochione.kochi_one.transit.Metro.data.KmrlOpenData.stations.forEach { station ->
                        com.google.maps.android.compose.Marker(
                            state = com.google.maps.android.compose.MarkerState(position = station.location),
                            title = station.name,
                            icon = metroIcon,
                            anchor = Offset(0.5f, 0.5f),
                            zIndex = 101f
                        )
                    }

                    activeTrains.forEach { train ->
                        com.google.maps.android.compose.Marker(
                            state = com.google.maps.android.compose.MarkerState(position = train.position),
                            title = "Metro Train ${train.tripId}",
                            icon = trainIcon ?: metroIcon,
                            anchor = Offset(0.5f, 0.5f),
                            rotation = train.bearing - 90f, // minus 90 compensates for train pointing right
                            flat = true,
                            zIndex = 105f
                        )
                    }
                }
            }

            // Hide top status-bar gradient when night map style is active.
            if (!useNightMapStyle) {
                val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(90.dp) // Covers status bars and fades out
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    bgColor.copy(alpha = 0.8f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
            
            // Custom Location Button hovering above the bottom sheet
//            if (hasLocationPermission) {
//                val glassColor = if (isDarkTheme) Color(0xFF1E1E1E).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.7f)
//                val glassBorder = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
//                val iconTint = if (isDarkTheme) Color.White else Color.Black
//
//                Box(
//                    modifier = Modifier
//                        .align(Alignment.BottomEnd)
//                        .padding(end = 16.dp, bottom = 220.dp) // Height above collapsed bottom sheet
//                        .size(56.dp) // Standard FAB size
//                        .clip(RoundedCornerShape(16.dp))
//                        .background(glassColor)
//                        .border(1.dp, glassBorder, RoundedCornerShape(16.dp))
//                        .clickable {
//                            val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
//                            try {
//                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
//                                    if (location != null) {
//                                        val currentLatLng = LatLng(location.latitude, location.longitude)
//                                        coroutineScope.launch {
//                                            cameraPositionState.animate(
//                                                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f),
//                                                1000
//                                            )
//                                        }
//                                    }
//                                }
//                            } catch (e: SecurityException) {
//                                e.printStackTrace()
//                            }
//                        },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_location_custom),
//                        contentDescription = "My Location",
//                        modifier = Modifier.size(24.dp),
//                        tint = iconTint // Apply dynamic tinting
//                    )
//                }
//            }

            androidx.compose.animation.AnimatedVisibility(
                visible = !isSheetExpanded,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    // Float the capsule above the sheet synchronously.
                    .padding(end = 16.dp, bottom = currentSheetHeightDp + 20.dp),
                enter = androidx.compose.animation.scaleIn(animationSpec = androidx.compose.animation.core.tween(800)) 
                        + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(800)),
                exit = androidx.compose.animation.scaleOut(animationSpec = androidx.compose.animation.core.tween(800))
                        + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(800))
            ) {
                val glassColor = if (isDarkTheme) Color(0xFF1E1E1E).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.7f)
                val glassBorder = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
                val iconTint = if (isDarkTheme) Color.White else Color.Black

                Box(
                    modifier = Modifier
                        .height(120.dp) // tall pill
                        .width(56.dp)
                        .clip(RoundedCornerShape(16.dp)) //16
                        .background(glassColor)
                        .border(1.dp, glassBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top half: QR scanner
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clickable {
                                    QrScanner.startScanning(
                                        context = context,
                                        onResult = { rawValue ->
                                            android.widget.Toast.makeText(context, "QR: $rawValue", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_qr),
                                contentDescription = "QR Scanner",
                                modifier = Modifier.size(32.dp),
                                tint = iconTint
                            )
                        }

                        // Divider inside the pill (horizontal)
                        Box(
                            modifier = Modifier
                                .height(1.dp)
                                .fillMaxWidth(0.6f)
                                .background(glassBorder)
                        )

                        // Bottom half: Location
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clickable {
                                    if (!hasLocationPermission) {
                                        showLocationRationale = true
                                    } else {
                                        val fusedLocationClient =
                                            com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                                        try {
                                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                                if (location != null) {
                                                    val currentLatLng = LatLng(location.latitude, location.longitude)
                                                    coroutineScope.launch {
                                                        cameraPositionState.animate(
                                                            com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f),
                                                            1000
                                                        )
                                                    }
                                                }
                                            }
                                        } catch (e: SecurityException) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_location_custom),
                                contentDescription = "My Location",
                                modifier = Modifier.size(24.dp),
                                tint = iconTint
                            )
                        }
                    }
                }
            }


            ThreeStateBottomSheet(
                sheetHeightPx = sheetHeightPx,
                expandedHeightPx = expandedHeightPx,
                halfExpandedHeightPx = halfExpandedHeightPx,
                collapsedHeightPx = collapsedHeightPx,
                isDarkTheme = isDarkTheme,
                selectedTab = selectedBottomTab,
                onSelectedTabChange = { selectedBottomTab = it },
                foodViewModel = foodViewModel,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ThreeStateBottomSheet(
    sheetHeightPx: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
    expandedHeightPx: Float,
    halfExpandedHeightPx: Float,
    collapsedHeightPx: Float,
    isDarkTheme: Boolean,
    selectedTab: String,
    onSelectedTabChange: (String) -> Unit,
    foodViewModel: FoodViewModel,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    var activeExplorePost by remember { mutableStateOf<ExplorePost?>(null) }

    // Clear Explore detail when switching tabs so the back-button header doesn't bleed into other sections
    androidx.compose.runtime.LaunchedEffect(selectedTab) {
        activeExplorePost = null
    }

    // System back: close Explore post detail → return to list
    BackHandler(enabled = activeExplorePost != null) {
        activeExplorePost = null
    }

    val isDarkThemeInternal = isDarkTheme
    val bgColor = if (isDarkThemeInternal) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkThemeInternal) Color.White else Color.Black
    val dividerColor = if (isDarkThemeInternal) Color.DarkGray else Color.LightGray
    val searchBarBgColor = if (isDarkThemeInternal) Color(0xFF2C2C2C) else Color(0xFFF0F0F0)
    val dragHandleColor = if (isDarkThemeInternal) Color.Gray else Color.LightGray
    val navBarIconTint = if (isDarkThemeInternal) Color.White else Color.Black
    val sheetAnimSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val sheetDragModifier = Modifier.pointerInput(Unit) {
        detectVerticalDragGestures(
            onDragEnd = {
                coroutineScope.launch {
                    val currentHeight = sheetHeightPx.value
                    val diffExpanded = abs(currentHeight - expandedHeightPx)
                    val diffHalf = abs(currentHeight - halfExpandedHeightPx)
                    val diffCollapsed = abs(currentHeight - collapsedHeightPx)

                    val targetHeight = when {
                        diffExpanded <= diffHalf && diffExpanded <= diffCollapsed -> expandedHeightPx
                        diffHalf <= diffExpanded && diffHalf <= diffCollapsed -> halfExpandedHeightPx
                        else -> collapsedHeightPx
                    }
                    sheetHeightPx.animateTo(targetHeight, animationSpec = sheetAnimSpec)
                }
            },
            onDragCancel = {
                coroutineScope.launch {
                    sheetHeightPx.animateTo(collapsedHeightPx, animationSpec = sheetAnimSpec)
                }
            }
        ) { change, dragAmount ->
            change.consume()
            coroutineScope.launch {
                // dragAmount is positive when dragging down (decreasing height)
                // dragAmount is negative when dragging up (increasing height)
                val newHeight = (sheetHeightPx.value - dragAmount).coerceIn(collapsedHeightPx, expandedHeightPx)
                sheetHeightPx.snapTo(newHeight)
            }
        }
    }

    val sheetContentNestedScroll = remember(
        sheetHeightPx,
        collapsedHeightPx,
        expandedHeightPx
    ) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Finger moving up => available.y is negative. Expand sheet first.
                if (available.y < 0f && sheetHeightPx.value < expandedHeightPx) {
                    val newHeight =
                        (sheetHeightPx.value - available.y).coerceIn(collapsedHeightPx, expandedHeightPx)
                    coroutineScope.launch { sheetHeightPx.snapTo(newHeight) }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // Let child content consume downward first; collapse sheet with unconsumed drag.
                if (available.y > 0f && sheetHeightPx.value > collapsedHeightPx) {
                    val newHeight =
                        (sheetHeightPx.value - available.y).coerceIn(collapsedHeightPx, expandedHeightPx)
                    coroutineScope.launch { sheetHeightPx.snapTo(newHeight) }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val currentHeight = sheetHeightPx.value
                val diffExpanded = abs(currentHeight - expandedHeightPx)
                val diffHalf = abs(currentHeight - halfExpandedHeightPx)
                val diffCollapsed = abs(currentHeight - collapsedHeightPx)

                if (available.y > 0f) {
                    sheetHeightPx.animateTo(collapsedHeightPx, animationSpec = sheetAnimSpec)
                    return Velocity.Zero
                }

                val targetHeight = when {
                    diffExpanded <= diffHalf && diffExpanded <= diffCollapsed -> expandedHeightPx
                    diffHalf <= diffExpanded && diffHalf <= diffCollapsed -> halfExpandedHeightPx
                    else -> collapsedHeightPx
                }
                sheetHeightPx.animateTo(targetHeight, animationSpec = sheetAnimSpec)
                return Velocity.Zero
            }
        }
    }
    
    // Calculate alpha for inner content based on sheet height
    // Fully visible (alpha 1f) at half expanded, fully hidden (alpha 0f) at collapsed
    val contentAlpha = remember(sheetHeightPx.value, collapsedHeightPx, halfExpandedHeightPx) {
        if (sheetHeightPx.value <= collapsedHeightPx) 0f
        else {
            val progress = (sheetHeightPx.value - collapsedHeightPx) / (halfExpandedHeightPx - collapsedHeightPx)
            progress.coerceIn(0f, 1f)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(with(density) { sheetHeightPx.value.toDp() })
            .shadow(
                elevation = 16.dp, 
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .clickable {
                        coroutineScope.launch {
                            sheetHeightPx.animateTo(halfExpandedHeightPx, animationSpec = sheetAnimSpec)
                        }
                    }
                    .then(sheetDragModifier)
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(dragHandleColor)
                )
            }
            
            // Morphing Top Bar Row
            SharedTransitionLayout {
                AnimatedContent(
                    targetState = activeExplorePost,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                            .togetherWith(fadeOut(animationSpec = tween(400, easing = androidx.compose.animation.core.FastOutSlowInEasing)))
                    },
                    label = "TopBarTransition"
                ) { post ->
                    if (post != null) {
                        // Detail Top Bar: [Back] [Title Pill] [Logo]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Back Button
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(searchBarBgColor)
                                    .clickable { activeExplorePost = null },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_back_outline),
                                    contentDescription = "Back",
                                    tint = textColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Title Pill (Shared Element)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .sharedBounds(
                                        rememberSharedContentState(key = "top_bar_search"),
                                        animatedVisibilityScope = this@AnimatedContent,
                                        boundsTransform = { _, _ -> tween(400) }
                                    )
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(searchBarBgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = post.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Account Logo
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                if (post.accountLogoUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = post.accountLogoUrl,
                                        contentDescription = "Logo",
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    } else {
                        // Standard Search Bar Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Standard Search Bar Row (Shared Element Logic)
                            var searchQuery by remember { mutableStateOf("") }
                            val placeholders = listOf("Coffee nearby", "Best Dessert Spot?","Restaurants near me","Best Pizza Place?","Dinner places","Bakery near me","Best café?","Family restaurant nearby","Romantic dinner place")
                            var placeholderIndex by remember { mutableStateOf(0) }

                            androidx.compose.runtime.LaunchedEffect(Unit) {
                                while (true) {
                                    delay(3000)
                                    placeholderIndex = (placeholderIndex + 1) % placeholders.size
                                }
                            }

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .sharedBounds(
                                        rememberSharedContentState(key = "top_bar_search"),
                                        animatedVisibilityScope = this@AnimatedContent,
                                        boundsTransform = { _, _ -> tween(400) }
                                    ),
                                shape = RoundedCornerShape(25.dp),
                                placeholder = {
                                    AnimatedContent(
                                        targetState = placeholders[placeholderIndex],
                                        transitionSpec = {
                                            (slideInVertically { height -> height } + fadeIn()) togetherWith
                                                    (slideOutVertically { height -> -height } + fadeOut())
                                        },
                                        label = "PlaceholderAnimation"
                                    ) { text ->
                                        Text(
                                            text = text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = textColor.copy(alpha = 0.6f)
                                        )
                                    }
                                },
                                leadingIcon = { Icon(painter = painterResource(id = R.drawable.ic_forum_outline), contentDescription = "Forum Icon", tint = textColor) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = textColor),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = searchBarBgColor,
                                    unfocusedContainerColor = searchBarBgColor,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Profile Image
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable {
                                        onSelectedTabChange("Profile")
                                        coroutineScope.launch {
                                            sheetHeightPx.animateTo(expandedHeightPx, animationSpec = sheetAnimSpec)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = R.drawable.profile_image,
                                    contentDescription = "Profile",
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(22.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
            
            // Content between handle and nav bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes up remaining space as sheet expands
                    .alpha(contentAlpha) // Fade out when collapsing
                    .nestedScroll(sheetContentNestedScroll)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        val fromIdx = BottomNavTabOrder.indexOf(initialState).coerceAtLeast(0)
                        val toIdx = BottomNavTabOrder.indexOf(targetState).coerceAtLeast(0)
                        val forward = toIdx > fromIdx
                        val enterMs = 380
                        val exitMs = 300
                        val ease = FastOutSlowInEasing
                        if (forward) {
                            (slideInHorizontally(
                                initialOffsetX = { w -> (w * 0.2f).toInt() },
                                animationSpec = tween(enterMs, easing = ease)
                            ) + fadeIn(tween(enterMs, easing = ease))).togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { w -> (-w * 0.14f).toInt() },
                                    animationSpec = tween(exitMs, easing = ease)
                                ) + fadeOut(tween(exitMs, easing = ease))
                            )
                        } else {
                            (slideInHorizontally(
                                initialOffsetX = { w -> (-w * 0.2f).toInt() },
                                animationSpec = tween(enterMs, easing = ease)
                            ) + fadeIn(tween(enterMs, easing = ease))).togetherWith(
                                slideOutHorizontally(
                                    targetOffsetX = { w -> (w * 0.14f).toInt() },
                                    animationSpec = tween(exitMs, easing = ease)
                                ) + fadeOut(tween(exitMs, easing = ease))
                            )
                        }
                    },
                    label = "TabTransition"
                ) { tab ->
                    when (tab) {
                        "Explore" -> {
                            ExploreView(
                                isDarkTheme = isDarkThemeInternal,
                                externalSelectedPost = activeExplorePost,
                                onPostSelected = { activeExplorePost = it }
                            )
                        }
                        "Food" -> FoodView(isDarkTheme = isDarkThemeInternal, viewModel = foodViewModel)
                        "Play" -> PlayView(isDarkTheme = isDarkThemeInternal)
                        "Fitness" -> FitnessView(isDarkTheme = isDarkThemeInternal)
                        "Transit" -> TransitView(isDark = isDarkThemeInternal)
                        "Profile" -> ProfileView()
                    }
                }
            }
            
            HorizontalDivider(color = dividerColor)
            
            // Nav Icons fixed at the bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 16.dp), // Extra padding for window bottom edge
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(R.drawable.ic_explore, "Explore", selectedTab == "Explore", isDarkTheme) {
                    onSelectedTabChange("Explore")
                    coroutineScope.launch {
                        sheetHeightPx.animateTo(halfExpandedHeightPx, animationSpec = sheetAnimSpec)
                    }
                }
                NavItem(R.drawable.ic_eat, "Eats", selectedTab == "Food", isDarkTheme) {
                    onSelectedTabChange("Food")
                    coroutineScope.launch {
                        sheetHeightPx.animateTo(halfExpandedHeightPx, animationSpec = sheetAnimSpec)
                    }
                }
                NavItem(R.drawable.ic_play, "Play", selectedTab == "Play", isDarkTheme) {
                    onSelectedTabChange("Play")
                    coroutineScope.launch {
                        sheetHeightPx.animateTo(halfExpandedHeightPx, animationSpec = sheetAnimSpec)
                    }
                }
                NavItem(R.drawable.ic_fitness, "Fitness", selectedTab == "Fitness", isDarkTheme) {
                    onSelectedTabChange("Fitness")
                    coroutineScope.launch {
                        sheetHeightPx.animateTo(halfExpandedHeightPx, animationSpec = sheetAnimSpec)
                    }
                }
                NavItem(R.drawable.ic_transit, "Transit", selectedTab == "Transit", isDarkTheme) {
                    onSelectedTabChange("Transit")
                    coroutineScope.launch {
                        sheetHeightPx.animateTo(halfExpandedHeightPx, animationSpec = sheetAnimSpec)
                    }
                }
            }
        }
    }
}

@Composable
fun NavItem(
    iconResId: Int,
    label: String,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val selectedColor = if (isDarkTheme) Color.White else Color.Black
    val unselectedColor = Color.Gray
    val iconTint = if (isSelected) selectedColor else unselectedColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .sizeIn(minWidth = 60.dp, minHeight = 52.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(20.dp) // Smaller icon size
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, // Smaller text
            color = iconTint
        )
    }
}