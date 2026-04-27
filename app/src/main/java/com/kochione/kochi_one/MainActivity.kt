package com.kochione.kochi_one


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.kochione.kochi_one.models.ExplorePost
import com.kochione.kochi_one.transit.Metro.data.KmrlOpenData
import com.kochione.kochi_one.ui.theme.KochiOneTheme
import com.kochione.kochi_one.viewmodels.FoodViewModel
import com.kochione.kochi_one.viewmodels.PlayViewModel
import com.kochione.kochi_one.views.ExploreView
import com.kochione.kochi_one.views.FitnessView
import com.kochione.kochi_one.views.FoodView
import com.kochione.kochi_one.views.PlayView
import com.kochione.kochi_one.views.ProfileView
import com.kochione.kochi_one.views.TransitView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

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
    // Create PlayViewModel as soon as the home screen loads so Play APIs run in the background
    // before the user opens the Play tab (otherwise first open paid full network + TLS cold start).
    val playViewModel = viewModel<PlayViewModel>()
    val foodViewModel = viewModel<FoodViewModel>()
    val restaurants by foodViewModel.restaurants.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        KmrlOpenData.load(context)
    }

    var currentHour by remember { mutableIntStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            delay(60_000)
        }
    }
    val isEveningTime = currentHour >= 18 || currentHour < 6
    val isDarkTheme = isSystemInDarkTheme() || isEveningTime

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

    var selectedTab by remember { mutableStateOf("Explore") }
    var lastBackPressTime by remember { mutableLongStateOf(0L) }


    var showLocationRationale by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            QrScanner.startScanning(
                context = context,
                onResult = { rawValue ->
                    android.widget.Toast.makeText(context, "QR: $rawValue", android.widget.Toast.LENGTH_SHORT).show()
                },
                onFailure = {
                    android.widget.Toast.makeText(context, "Unable to open scanner", android.widget.Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            android.widget.Toast.makeText(
                context,
                "Camera permission denied",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

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
            title = { 
                Text(
                    text = "Location Access", 
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color.Black
                ) 
            },
            text = { 
                Text(
                    text = stringResource(R.string.location_rationale_description),
                    color = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.8f)
                ) 
            },
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

    val isZoomedOut by remember {
        derivedStateOf { cameraPositionState.position.zoom < 15.5f }
    }
    val isVeryZoomedOut by remember {
        derivedStateOf { cameraPositionState.position.zoom < 13.0f }
    }
    
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        if (sheetHeightPx.value > halfExpandedHeightPx + 20f) {
            coroutineScope.launch {
                sheetHeightPx.animateTo(halfExpandedHeightPx)
            }
        } else if (selectedTab != "Explore") {
            selectedTab = "Explore"
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                (context as? android.app.Activity)?.finish()
            } else {
                lastBackPressTime = currentTime
                android.widget.Toast.makeText(context, "Press back again to exit", android.widget.Toast.LENGTH_SHORT).show()
            }
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
                    isMyLocationEnabled = hasLocationPermission
                )
            ) {
                // Drawing the Metro Line only when Transit tab is active
                if (selectedTab == "Transit" && KmrlOpenData.metroLinePoints.isNotEmpty()) {
                    Polyline(
                        points = KmrlOpenData.metroLinePoints,
                        color = Color(0xFF00B5E2), // Kochi Metro Blue
                        width = 12f,
                        jointType = com.google.android.gms.maps.model.JointType.ROUND,
                        startCap = com.google.android.gms.maps.model.RoundCap(),
                        endCap = com.google.android.gms.maps.model.RoundCap()
                    )
                }

                // Drawing Station Markers only when Transit tab is active
                if (selectedTab == "Transit") {
                    val smallIcon = remember(context) {
                        bitmapDescriptorFromVector(context, R.drawable.ic_metro_stop_pin, 32)
                    }
                    val verySmallIcon = remember(context) {
                        bitmapDescriptorFromVector(context, R.drawable.ic_metro_stop_pin, 16)
                    }

                    KmrlOpenData.stations.forEach { station ->
                        val transitIcon = when {
                            isVeryZoomedOut -> verySmallIcon
                            isZoomedOut -> smallIcon
                            else -> remember(station.name, isDarkTheme) {
                                createMarkerWithLabel(context, R.drawable.ic_metro_stop_pin, station.name, isDarkTheme)
                            }
                        }

                        Marker(
                            state = MarkerState(position = station.location),
                            title = station.name,
                            snippet = "Metro Station",
                            icon = transitIcon,
                            alpha = when {
                                isVeryZoomedOut -> 0.6f
                                isZoomedOut -> 0.8f
                                else -> 1.0f
                            }
                        )
                    }
                }

                // Drawing Food Markers only when Food tab is active
                if (selectedTab == "Food") {
                    restaurants.forEach { restaurant ->
                        val logoUrl = restaurant.logo?.url ?: restaurant.coverImages.firstOrNull()?.url
                        val foodIcon = rememberRestaurantMarker(context, logoUrl, isDarkTheme)
                        
                        Marker(
                            state = MarkerState(position = LatLng(restaurant.location.latitude, restaurant.location.longitude)),
                            title = restaurant.name,
                            snippet = restaurant.restaurantType,
                            icon = foodIcon,
                            onClick = {
                                foodViewModel.fetchRestaurantByBizId(restaurant.bizId)
                                true
                            }
                        )
                    }
                }
            }

            // Translucent Gradient for Status Bar icon visibility
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

            if (hasLocationPermission) {
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
                            .clip(RoundedCornerShape(16.dp))
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
                                        val hasCameraPermission =
                                            androidx.core.content.ContextCompat.checkSelfPermission(
                                                context,
                                                android.Manifest.permission.CAMERA
                                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                        if (hasCameraPermission) {
                                            if (selectedTab == "Transit") {
                                                android.widget.Toast.makeText(context, "Opening Transit Scanner...", android.widget.Toast.LENGTH_SHORT).show()
                                                // Specific transit scanner logic would go here
                                            } else {
                                                QrScanner.startScanning(
                                                    context = context,
                                                    onResult = { rawValue ->
                                                        android.widget.Toast.makeText(context, "QR: $rawValue", android.widget.Toast.LENGTH_SHORT).show()
                                                    },
                                                    onFailure = {
                                                        android.widget.Toast.makeText(context, "Unable to open scanner", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                )
                                            }
                                        } else {
                                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                androidx.compose.animation.AnimatedContent(
                                    targetState = if (selectedTab == "Transit") R.drawable.ic_transit else R.drawable.ic_qr,
                                    transitionSpec = {
                                        (androidx.compose.animation.scaleIn(animationSpec = androidx.compose.animation.core.tween(500)) + 
                                         androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(500)))
                                            .togetherWith(androidx.compose.animation.scaleOut(animationSpec = androidx.compose.animation.core.tween(500)) + 
                                                          androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500)))
                                    },
                                    label = "IconMorph"
                                ) { iconId ->
                                    Icon(
                                        painter = painterResource(id = iconId),
                                        contentDescription = if (iconId == R.drawable.ic_transit) "Transit Scanner" else "QR Scanner",
                                        modifier = Modifier.size(if (iconId == R.drawable.ic_qr) 33.dp else 26.dp),
                                        tint = iconTint
                                    )
                                }
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
            }


            ThreeStateBottomSheet(
                sheetHeightPx = sheetHeightPx,
                expandedHeightPx = expandedHeightPx,
                halfExpandedHeightPx = halfExpandedHeightPx,
                collapsedHeightPx = collapsedHeightPx,
                playViewModel = playViewModel,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int, sizeDp: Int? = null): BitmapDescriptor? {
    return ContextCompat.getDrawable(context, vectorResId)?.run {
        val density = context.resources.displayMetrics.density
        val targetWidth = if (sizeDp != null) (sizeDp * density).toInt() else intrinsicWidth
        val targetHeight = if (sizeDp != null) (sizeDp * density).toInt() else intrinsicHeight
        
        setBounds(0, 0, targetWidth, targetHeight)
        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

private fun createMarkerWithLabel(context: Context, vectorResId: Int, label: String, isDark: Boolean): BitmapDescriptor? {
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    val density = context.resources.displayMetrics.density
    
    val fontSize = 12f * density
    val padding = 4f * density
    
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
        textSize = fontSize
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        // Add a subtle shadow for better legibility on a map
        setShadowLayer(2f, 1f, 1f, if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
    }
    
    val textBounds = Rect()
    paint.getTextBounds(label, 0, label.length, textBounds)
    
    val iconWidth = drawable.intrinsicWidth
    val iconHeight = drawable.intrinsicHeight
    val textWidth = textBounds.width()
    val textHeight = textBounds.height()
    
    val bitmapWidth = Math.max(iconWidth, textWidth).toInt() + (padding * 2).toInt()
    val bitmapHeight = (iconHeight + textHeight + padding * 2).toInt()
    
    val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Draw icon centered at top
    val iconLeft = (bitmapWidth - iconWidth) / 2
    drawable.setBounds(iconLeft, 0, iconLeft + iconWidth, iconHeight)
    drawable.draw(canvas)
    
    // Draw text centered below icon
    val textX = (bitmapWidth / 2f)
    val textY = (iconHeight + textHeight + padding)
    canvas.drawText(label, textX, textY, paint)
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ThreeStateBottomSheet(
    sheetHeightPx: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
    expandedHeightPx: Float,
    halfExpandedHeightPx: Float,
    collapsedHeightPx: Float,
    playViewModel: PlayViewModel,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    var activeExplorePost by remember { mutableStateOf<ExplorePost?>(null) }
    var playOrFitnessDetailOpen by remember { mutableStateOf(false) }
    var dismissPlayFitnessDetail by remember { mutableStateOf<(() -> Unit)?>(null) }

    val isDarkTheme = isSystemInDarkTheme()
    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val dividerColor = if (isDarkTheme) Color.DarkGray else Color.LightGray
    val searchBarBgColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF0F0F0)
    val dragHandleColor = if (isDarkTheme) Color.Gray else Color.LightGray
    val navBarIconTint = if (isDarkTheme) Color.White else Color.Black
    val bottomNavReservedHeight = 72.dp
    
    // Calculate alpha for inner content based on sheet height
    // Fully visible (alpha 1f) at half expanded, fully hidden (alpha 0f) at collapsed
    val contentAlpha = remember(sheetHeightPx.value, collapsedHeightPx, halfExpandedHeightPx) {
        if (sheetHeightPx.value <= collapsedHeightPx) 0f
        else {
            val progress = (sheetHeightPx.value - collapsedHeightPx) / (halfExpandedHeightPx - collapsedHeightPx)
            progress.coerceIn(0f, 1f)
        }
    }

    androidx.compose.runtime.LaunchedEffect(selectedTab) {
        playOrFitnessDetailOpen = false
        dismissPlayFitnessDetail = null
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // available.y < 0 means scrolling up
                if (available.y < 0 && sheetHeightPx.value < expandedHeightPx) {
                    coroutineScope.launch {
                        sheetHeightPx.animateTo(expandedHeightPx)
                    }
                    // We don't necessarily consume the scroll here if we want the list 
                    // to also start scrolling, but usually we want the sheet to expand first.
                    // Returning Offset.Zero means we don't consume it.
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(with(density) { sheetHeightPx.value.toDp() })
            .nestedScroll(nestedScrollConnection)
            .pointerInput(Unit) {
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
                            sheetHeightPx.animateTo(targetHeight)
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            sheetHeightPx.animateTo(collapsedHeightPx)
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
            // Drag handle area - Clicking here sets height to mid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // No ripple for a cleaner feel
                    ) {
                        coroutineScope.launch {
                            sheetHeightPx.animateTo(halfExpandedHeightPx)
                        }
                    }
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
                                    painter = painterResource(id = R.drawable.ic_chevron_right),
                                    contentDescription = "Back",
                                    tint = textColor,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .scale(scaleX = -1f, scaleY = 1f)
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
                        // Standard Search Bar Row — [Back?] [Capsule] [Profile]; detail mode only: back + wider capsule (10.dp inset/side)
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

                            val detailBar = playOrFitnessDetailOpen
                            // Same height for back circle and search capsule in detail bar (and normal search bar)
                            val searchBarCapsuleHeight = 50.dp
                            if (detailBar) {
                                Box(
                                    modifier = Modifier
                                        .size(searchBarCapsuleHeight)
                                        .clip(CircleShape)
                                        .background(searchBarBgColor)
                                        .clickable { dismissPlayFitnessDetail?.invoke() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_chevron_left),
                                        contentDescription = "Back",
                                        tint = textColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            val searchCapsuleHorizontalPadding =
                                if (detailBar) 8.dp else 0.dp
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = searchCapsuleHorizontalPadding)
                                    .height(searchBarCapsuleHeight)
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
                            
                            Spacer(modifier = Modifier.width(if (detailBar) 2.dp else 12.dp))
                            
                            // Profile Image
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { 
                                        onTabSelected("Profile")
                                        coroutineScope.launch {
                                            sheetHeightPx.animateTo(expandedHeightPx)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Profile",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
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
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "TabTransition"
                ) { tab ->
                    when (tab) {
                        "Explore" -> {
                            ExploreView(
                                isDarkTheme = isDarkTheme,
                                externalSelectedPost = activeExplorePost,
                                onPostSelected = { activeExplorePost = it }
                            )
                        }
                        "Food" -> FoodView(isDarkTheme = isDarkTheme)
                        "Play" -> PlayView(
                            isDarkTheme = isDarkTheme,
                            viewModel = playViewModel,
                            onDetailVisibilityChanged = { playOrFitnessDetailOpen = it },
                            onRegisterDismissDetail = { dismissPlayFitnessDetail = it }
                        )
                        "Fitness" -> FitnessView(
                            isDarkTheme = isDarkTheme,
                            onDetailVisibilityChanged = { playOrFitnessDetailOpen = it },
                            onRegisterDismissDetail = { dismissPlayFitnessDetail = it }
                        )
                        "Transit" -> TransitView(isDark = isDarkTheme)
                        "Profile" -> ProfileView()
                    }
                }
            }
            
            HorizontalDivider(color = dividerColor)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .height(bottomNavReservedHeight)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 16.dp), // Extra padding for window bottom edge
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(R.drawable.ic_explore, "Explore", selectedTab == "Explore") {
                    onTabSelected("Explore")
                    coroutineScope.launch { sheetHeightPx.animateTo(halfExpandedHeightPx) }
                }
                NavItem(R.drawable.ic_eat, "Eats", selectedTab == "Food") {
                    onTabSelected("Food")
                    coroutineScope.launch { sheetHeightPx.animateTo(halfExpandedHeightPx) }
                }
                NavItem(R.drawable.ic_play, "Play", selectedTab == "Play") {
                    onTabSelected("Play")
                    coroutineScope.launch { sheetHeightPx.animateTo(halfExpandedHeightPx) }
                }
                NavItem(R.drawable.ic_fitness, "Fitness", selectedTab == "Fitness") {
                    onTabSelected("Fitness")
                    coroutineScope.launch { sheetHeightPx.animateTo(halfExpandedHeightPx) }
                }
                NavItem(R.drawable.ic_transit, "Transit", selectedTab == "Transit") {
                    onTabSelected("Transit")
                    coroutineScope.launch { sheetHeightPx.animateTo(halfExpandedHeightPx) }
                }
            }
        }
    }
}

@Composable
fun NavItem(iconResId: Int, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val selectedColor = if (isDark) Color.White else Color.Black
    val unselectedColor = Color.Gray
    val iconTint = if (isSelected) selectedColor else unselectedColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() }
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

@Composable
fun rememberRestaurantMarker(context: Context, url: String?, isDarkTheme: Boolean): BitmapDescriptor? {
    var descriptor by remember(url, isDarkTheme) { mutableStateOf<BitmapDescriptor?>(null) }
    
    LaunchedEffect(url, isDarkTheme) {
        if (url == null) {
            descriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            return@LaunchedEffect
        }
        
        val loader = coil.ImageLoader(context)
        val request = coil.request.ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false) // Required for drawing to Canvas
            .build()
            
        val result = (loader.execute(request) as? coil.request.SuccessResult)?.drawable
        if (result != null) {
            val density = context.resources.displayMetrics.density
            val size = (45 * density).toInt()
            val pinHeight = (55 * density).toInt()
            
            val bitmap = Bitmap.createBitmap(size, pinHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            
            val pinColor = 0xFFFF9500.toInt() // Vibrant Orange
            val logoBgColor = Color.White.toArgb()
            
            // 1. Draw Integrated Pin Shape (Tip + Outer Circle)
            paint.color = pinColor
            paint.style = Paint.Style.FILL
            
            // Draw Tip
            val path = android.graphics.Path()
            path.moveTo(size / 2f, pinHeight.toFloat())
            path.lineTo(size / 2f - (10 * density), size.toFloat() - (8 * density))
            path.lineTo(size / 2f + (10 * density), size.toFloat() - (8 * density))
            path.close()
            canvas.drawPath(path, paint)
            
            // Draw Outer Circle
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
            
            // 2. Draw Inner White Circle (The "well" for the logo)
            paint.color = logoBgColor
            val innerPadding = 3 * density
            canvas.drawCircle(size / 2f, size / 2f, size / 2f - innerPadding, paint)
            
            // 3. Draw Logo
            val logoPadding = 5 * density
            val logoSize = (size - (logoPadding * 2)).toInt()
            val logoOffset = logoPadding
            
            val logoBitmap = Bitmap.createBitmap(logoSize, logoSize, Bitmap.Config.ARGB_8888)
            val logoCanvas = Canvas(logoBitmap)
            val logoPath = android.graphics.Path()
            logoPath.addCircle(logoSize / 2f, logoSize / 2f, logoSize / 2f, android.graphics.Path.Direction.CCW)
            logoCanvas.clipPath(logoPath)
            
            result.setBounds(0, 0, logoSize, logoSize)
            result.draw(logoCanvas)
            
            canvas.drawBitmap(logoBitmap, logoOffset, logoOffset, null)
            
            descriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
        } else {
            // Placeholder/Fallback
            descriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
        }
    }
    
    return descriptor
}