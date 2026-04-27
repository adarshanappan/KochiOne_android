package com.kochione.kochi_one


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
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
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
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
import com.kochione.kochi_one.transit.Metro.TrainSimulator
import com.kochione.kochi_one.transit.Metro.data.KmrlOpenData
import com.kochione.kochi_one.ui.theme.KochiOneTheme
import com.kochione.kochi_one.viewmodels.FoodViewModel
import com.kochione.kochi_one.viewmodels.PlayViewModel
import com.kochione.kochi_one.viewmodels.TransitViewModel
import com.kochione.kochi_one.views.ExploreView
import com.kochione.kochi_one.views.FitnessView
import com.kochione.kochi_one.views.FoodView
import com.kochione.kochi_one.views.PlayView
import com.kochione.kochi_one.views.ProfileEditView
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
            val context = androidx.compose.ui.platform.LocalContext.current
            var currentHour by remember { mutableIntStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) }
            androidx.compose.runtime.LaunchedEffect(Unit) {
                while (true) {
                    currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                    delay(60_000)
                }
            }
            val isEveningTime = currentHour >= 18 || currentHour < 6
            val prefs = remember { context.getSharedPreferences("kochi_one_prefs", android.content.Context.MODE_PRIVATE) }
            
            var selectedTheme by androidx.compose.runtime.saveable.rememberSaveable { 
                mutableStateOf(prefs.getString("selected_theme", "Dark") ?: "Dark") 
            }
            var isAutomatic by androidx.compose.runtime.saveable.rememberSaveable { 
                mutableStateOf(prefs.getBoolean("is_automatic", true)) 
            }
            
            val isDarkTheme = if (isAutomatic) isEveningTime else (selectedTheme == "Dark")

            // State for Circular Reveal
            var revealTargetDark by remember { mutableStateOf<Boolean?>(null) }
            var revealOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
            val revealProgress = remember { androidx.compose.animation.core.Animatable(0f) }
            val scope = androidx.compose.runtime.rememberCoroutineScope()

            val view = androidx.compose.ui.platform.LocalView.current
            if (!view.isInEditMode) {
                androidx.compose.runtime.SideEffect {
                    val window = (context as android.app.Activity).window
                    val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
                    // During reveal, we want the status bar to match the target theme
                    insetsController.isAppearanceLightStatusBars = !(revealTargetDark ?: isDarkTheme)
                }
            }

            val configuration = LocalConfiguration.current
            val density = LocalDensity.current
            val screenHeightDp = configuration.screenHeightDp.dp
            val expandedHeightPx = with(density) { (screenHeightDp * 0.95f).toPx() }
            val halfExpandedHeightPx = with(density) { (screenHeightDp * 0.6f).toPx() }
            val collapsedHeightPx = with(density) { 200.dp.toPx() }

            var selectedTab by remember { mutableStateOf("Explore") }
            val sheetHeightPx = remember { androidx.compose.animation.core.Animatable(collapsedHeightPx) }
            var activeExplorePost by remember { mutableStateOf<ExplorePost?>(null) }
            var playOrFitnessDetailOpen by remember { mutableStateOf(false) }
            var dismissPlayFitnessDetail by remember { mutableStateOf<(() -> Unit)?>(null) }

            val kochiLocation = LatLng(9.9312, 76.2673)
            val cameraPositionState = rememberCameraPositionState {
                position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(kochiLocation, 12f)
            }
            val revealCameraPositionState = rememberCameraPositionState {
                position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(kochiLocation, 12f)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // Base Layer (Always there)
                KochiOneTheme(darkTheme = isDarkTheme) {
                    MainScreen(
                        isDarkTheme = isDarkTheme,
                        selectedTheme = selectedTheme,
                        onThemeSelected = { newTheme, offset -> 
                            val nextDark = newTheme == "Dark"
                            if (nextDark != isDarkTheme) {
                                revealOffset = offset
                                revealTargetDark = nextDark
                                // Sync the reveal map position with the current map position
                                revealCameraPositionState.move(
                                    com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(cameraPositionState.position)
                                )
                                scope.launch {
                                    revealProgress.snapTo(0f)
                                    revealProgress.animateTo(1f, androidx.compose.animation.core.tween(1500))
                                    // Update actual state after animation
                                    selectedTheme = newTheme
                                    isAutomatic = false 
                                    prefs.edit().putString("selected_theme", newTheme).putBoolean("is_automatic", false).apply()
                                    revealTargetDark = null
                                    revealProgress.snapTo(0f)
                                }
                            }
                        },
                        isAutomatic = isAutomatic,
                        onAutomaticToggle = { it, offset -> 
                            val nextDark = if (it) isEveningTime else (selectedTheme == "Dark")
                            if (nextDark != isDarkTheme) {
                                // Trigger reveal
                                revealOffset = offset ?: androidx.compose.ui.geometry.Offset.Zero
                                revealTargetDark = nextDark
                                revealCameraPositionState.move(
                                    com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(cameraPositionState.position)
                                )
                                scope.launch {
                                    revealProgress.snapTo(0f)
                                    revealProgress.animateTo(1f, androidx.compose.animation.core.tween(1500))
                                    isAutomatic = it
                                    prefs.edit().putBoolean("is_automatic", it).apply()
                                    revealTargetDark = null
                                    revealProgress.snapTo(0f)
                                }
                            } else {
                                isAutomatic = it
                                prefs.edit().putBoolean("is_automatic", it).apply()
                            }
                        },
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        sheetHeightPx = sheetHeightPx,
                        expandedHeightPx = expandedHeightPx,
                        halfExpandedHeightPx = halfExpandedHeightPx,
                        collapsedHeightPx = collapsedHeightPx,
                        activeExplorePost = activeExplorePost,
                        onExplorePostSelected = { activeExplorePost = it },
                        playOrFitnessDetailOpen = playOrFitnessDetailOpen,
                        onPlayOrFitnessDetailOpenChanged = { playOrFitnessDetailOpen = it },
                        dismissPlayFitnessDetail = dismissPlayFitnessDetail,
                        onRegisterDismissDetail = { dismissPlayFitnessDetail = it },
                        cameraPositionState = cameraPositionState
                    )
                }

                // Reveal Layer (Only during animation)
                revealTargetDark?.let { targetDark ->
                    KochiOneTheme(darkTheme = targetDark) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawWithContent {
                                    val canvasWidth = size.width
                                    val canvasHeight = size.height
                                    val maxRadius = kotlin.math.hypot(
                                        kotlin.math.max(revealOffset.x, canvasWidth - revealOffset.x).toDouble(),
                                        kotlin.math.max(revealOffset.y, canvasHeight - revealOffset.y).toDouble()
                                    ).toFloat()
                                    
                                    val path = Path().apply {
                                        val radius = maxRadius * revealProgress.value
                                        addOval(
                                            androidx.compose.ui.geometry.Rect(
                                                revealOffset.x - radius,
                                                revealOffset.y - radius,
                                                revealOffset.x + radius,
                                                revealOffset.y + radius
                                            )
                                        )
                                    }
                                    
                                    clipPath(path) {
                                        this@drawWithContent.drawContent()
                                    }
                                }
                        ) {
                            MainScreen(
                                isDarkTheme = targetDark,
                                selectedTheme = selectedTheme,
                                onThemeSelected = { _, _ -> },
                                isAutomatic = isAutomatic,
                                onAutomaticToggle = { _, _ -> },
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                sheetHeightPx = sheetHeightPx,
                                expandedHeightPx = expandedHeightPx,
                                halfExpandedHeightPx = halfExpandedHeightPx,
                                collapsedHeightPx = collapsedHeightPx,
                                activeExplorePost = activeExplorePost,
                                onExplorePostSelected = { activeExplorePost = it },
                                playOrFitnessDetailOpen = playOrFitnessDetailOpen,
                                onPlayOrFitnessDetailOpenChanged = { playOrFitnessDetailOpen = it },
                                dismissPlayFitnessDetail = dismissPlayFitnessDetail,
                                onRegisterDismissDetail = { dismissPlayFitnessDetail = it },
                                cameraPositionState = revealCameraPositionState
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    selectedTheme: String,
    onThemeSelected: (String, androidx.compose.ui.geometry.Offset) -> Unit,
    isAutomatic: Boolean,
    onAutomaticToggle: (Boolean, androidx.compose.ui.geometry.Offset?) -> Unit,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    sheetHeightPx: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
    expandedHeightPx: Float,
    halfExpandedHeightPx: Float,
    collapsedHeightPx: Float,
    activeExplorePost: ExplorePost?,
    onExplorePostSelected: (ExplorePost?) -> Unit,
    playOrFitnessDetailOpen: Boolean,
    onPlayOrFitnessDetailOpenChanged: (Boolean) -> Unit,
    dismissPlayFitnessDetail: (() -> Unit)?,
    onRegisterDismissDetail: ((() -> Unit)?) -> Unit,
    cameraPositionState: com.google.maps.android.compose.CameraPositionState
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val haptic = LocalHapticFeedback.current
    // Create PlayViewModel as soon as the home screen loads so Play APIs run in the background
    // before the user opens the Play tab (otherwise first open paid full network + TLS cold start).
    val playViewModel = viewModel<PlayViewModel>()
    val foodViewModel = viewModel<FoodViewModel>()
    val transitViewModel = viewModel<TransitViewModel>()
    val restaurants by foodViewModel.restaurants.collectAsState()


    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        KmrlOpenData.load(context)
    }

    // Theme logic moved to setContent

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

    var lastBackPressTime by remember { mutableLongStateOf(0L) }


    var showLocationRationale by remember { mutableStateOf(false) }
    var showCameraRationale by remember { mutableStateOf(false) }

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
        // All rationales are now shown only on click
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
                    Text("Not now", color = Color.Gray)
                }
            },
            containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showCameraRationale) {
        AlertDialog(
            onDismissRequest = { showCameraRationale = false },
            title = {
                Text(
                    text = "Camera Access",
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.camera_rationale_description),
                    color = (if (isDarkTheme) Color.White else Color.Black).copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCameraRationale = false
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Allow", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCameraRationale = false }) {
                    Text("Not now", color = Color.Gray)
                }
            },
            containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    val currentSheetHeightDp = with(density) { sheetHeightPx.value.toDp() }
    // Capsule starts disappearing when sheet crosses 50% of the total height
    val isSheetExpanded = sheetHeightPx.value > expandedHeightPx * 0.65f



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
            onTabSelected("Explore")
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
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f),
                                2500
                            )
                        }
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
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f),
                                    2500
                                )
                            }
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
                if (selectedTab == "Transit") {
                    // Draw both directions of the metro line
                    val lines = listOf(KmrlOpenData.routePoints, KmrlOpenData.routePoints2)
                    lines.forEach { points ->
                        if (points.isNotEmpty()) {
                            Polyline(
                                points = points,
                                color = Color(0xFF0072BC), // Kochi Metro Darker Blue
                                width = 12f,
                                jointType = com.google.android.gms.maps.model.JointType.ROUND,
                                startCap = com.google.android.gms.maps.model.RoundCap(),
                                endCap = com.google.android.gms.maps.model.RoundCap()
                            )
                        }
                    }
                    
                    // Fallback to metroLinePoints if shapes aren't loaded
                    if (KmrlOpenData.routePoints.isEmpty() && KmrlOpenData.metroLinePoints.isNotEmpty()) {
                        Polyline(
                            points = KmrlOpenData.metroLinePoints,
                            color = Color(0xFF0072BC),
                            width = 12f,
                            jointType = com.google.android.gms.maps.model.JointType.ROUND,
                            startCap = com.google.android.gms.maps.model.RoundCap(),
                            endCap = com.google.android.gms.maps.model.RoundCap()
                        )
                    }

                    // Render Pink Route
                    val showPinkRoute by transitViewModel.showPinkRoute.collectAsState()
                    if (showPinkRoute && KmrlOpenData.pinkRoutePoints.isNotEmpty()) {
                        Polyline(
                            points = KmrlOpenData.pinkRoutePoints,
                            color = Color(0xFFFF7895), // Light Red
                            width = 12f,
                            jointType = com.google.android.gms.maps.model.JointType.ROUND,
                            startCap = com.google.android.gms.maps.model.RoundCap(),
                            endCap = com.google.android.gms.maps.model.RoundCap()
                        )
                    }
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

                // Drawing Active Trains only when Transit tab is active and simulation is on
                if (selectedTab == "Transit") {
                    val showSimulatedTrains by transitViewModel.showSimulatedTrains.collectAsState()
                    if (showSimulatedTrains) {
                        val activeTrains by remember { TrainSimulator.activeTrainsFlow() }.collectAsState(initial = emptyList())
                        val upcomingTripIds = transitViewModel.getUpcomingTripIds()
                        
                        val trainIcon = remember(context) {
                            bitmapDescriptorFromVector(context, R.drawable.ic_train_top, 50)
                        }
                        val yellowTrainIcon = remember(context) {
                            createCircleMarker(context, Color(0xFFFFD600), 12) // Slightly darker yellow
                        }
                        
                        val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "blink")
                        val blinkAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                animation = androidx.compose.animation.core.tween(800, easing = androidx.compose.animation.core.FastOutSlowInEasing),
                                repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                            ),
                            label = "blinkAlpha"
                        )

                        activeTrains.filter { it.tripId in upcomingTripIds }.forEach { train ->
                            val isTrainZoomedOut = cameraPositionState.position.zoom < 14.5f
                            Marker(
                                state = MarkerState(position = train.position),
                                icon = if (isTrainZoomedOut) yellowTrainIcon else trainIcon,
                                rotation = if (isTrainZoomedOut) 0f else train.bearing - 90f,
                                anchor = Offset(0.5f, 0.5f),
                                flat = !isTrainZoomedOut,
                                alpha = if (isTrainZoomedOut) blinkAlpha else 1f,
                                zIndex = 100f,
                                onClick = { true }
                            )
                        }
                    }
                }
            }

            // Animate camera to start station when simulation is toggled on
            val showSimulatedTrains by transitViewModel.showSimulatedTrains.collectAsState()
            val fromStation by transitViewModel.fromStation.collectAsState()
            LaunchedEffect(showSimulatedTrains) {
                if (showSimulatedTrains) {
                    fromStation?.location?.let { loc ->
                        cameraPositionState.animate(
                            com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(loc, 15.5f),
                            1200
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

            androidx.compose.animation.AnimatedVisibility(
                visible = !isSheetExpanded,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    // Float the capsule above the sheet synchronously.
                    .padding(end = 16.dp, bottom = currentSheetHeightDp + 20.dp),
                enter = androidx.compose.animation.scaleIn(
                    initialScale = 0.8f,
                    animationSpec = androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) + androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(500)
                ),
                exit = androidx.compose.animation.scaleOut(
                    targetScale = 0.8f,
                    animationSpec = androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                ) + androidx.compose.animation.fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(500)
                )
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
                                    if (selectedTab == "Transit") {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        
                                        val upcomingTripIds = transitViewModel.getUpcomingTripIds()
                                        val firstTripId = upcomingTripIds.firstOrNull()
                                        val trainPos = firstTripId?.let { TrainSimulator.getCurrentTrainPosition(it) }
                                        
                                        if (trainPos != null) {
                                            coroutineScope.launch {
                                                cameraPositionState.animate(
                                                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(trainPos, 15.5f),
                                                    1200
                                                )
                                            }
                                            if (!transitViewModel.showSimulatedTrains.value) {
                                                transitViewModel.toggleShowSimulatedTrains()
                                            }
                                            android.widget.Toast.makeText(context, "Finding your train...", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            transitViewModel.toggleShowSimulatedTrains()
                                            val msg = if (transitViewModel.showSimulatedTrains.value) "Showing upcoming trains..." else "Hiding trains"
                                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        val hasCameraPermission =
                                            androidx.core.content.ContextCompat.checkSelfPermission(
                                                context,
                                                android.Manifest.permission.CAMERA
                                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                        if (hasCameraPermission) {
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
                                            showCameraRationale = true
                                        }
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
                                    contentDescription = if (iconId == R.drawable.ic_transit) "Find Train" else "QR Scanner",
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
                                    if (hasLocationPermission) {
                                        val fusedLocationClient =
                                            com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                                        try {
                                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                                if (location != null) {
                                                    val currentLatLng = LatLng(location.latitude, location.longitude)
                                                    coroutineScope.launch {
                                                        cameraPositionState.animate(
                                                            com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f),
                                                            1500
                                                        )
                                                    }
                                                }
                                            }
                                        } catch (e: SecurityException) {
                                            e.printStackTrace()
                                        }
                                    } else {
                                        showLocationRationale = true
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
                playViewModel = playViewModel,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                transitViewModel = transitViewModel,
                isDarkTheme = isDarkTheme,
                selectedTheme = selectedTheme,
                onThemeSelected = onThemeSelected,
                isAutomatic = isAutomatic,
                onAutomaticToggle = onAutomaticToggle,
                activeExplorePost = activeExplorePost,
                onExplorePostSelected = onExplorePostSelected,
                playOrFitnessDetailOpen = playOrFitnessDetailOpen,
                onPlayOrFitnessDetailOpenChanged = onPlayOrFitnessDetailOpenChanged,
                dismissPlayFitnessDetail = dismissPlayFitnessDetail,
                onRegisterDismissDetail = onRegisterDismissDetail,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

        }
    }
}

private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int, sizeDp: Int? = null): BitmapDescriptor? {
    return ContextCompat.getDrawable(context, vectorResId)?.run {
        val density = context.resources.displayMetrics.density
        val targetWidth: Int
        val targetHeight: Int
        
        if (sizeDp != null) {
            val ratio = intrinsicWidth.toFloat() / intrinsicHeight.toFloat()
            if (intrinsicWidth >= intrinsicHeight) {
                targetWidth = (sizeDp * density).toInt()
                targetHeight = (targetWidth / ratio).toInt()
            } else {
                targetHeight = (sizeDp * density).toInt()
                targetWidth = (targetHeight * ratio).toInt()
            }
        } else {
            targetWidth = intrinsicWidth
            targetHeight = intrinsicHeight
        }
        
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
    
    val textBounds = android.graphics.Rect()
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

private fun createCircleMarker(context: Context, color: Color, sizeDp: Int): BitmapDescriptor? {
    val density = context.resources.displayMetrics.density
    val sizePx = (sizeDp * density).toInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgb()
        style = Paint.Style.FILL
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
    }
    
    val center = sizePx / 2f
    val radius = (sizePx / 2f) - (1f * density)
    
    canvas.drawCircle(center, center, radius, paint)
    canvas.drawCircle(center, center, radius, strokePaint)
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ThreeStateBottomSheet(
    sheetHeightPx: androidx.compose.animation.core.Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
    expandedHeightPx: Float,
    halfExpandedHeightPx: Float,
    collapsedHeightPx: Float,
    playViewModel: PlayViewModel,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    transitViewModel: TransitViewModel,
    isDarkTheme: Boolean,
    selectedTheme: String,
    onThemeSelected: (String, androidx.compose.ui.geometry.Offset) -> Unit,
    isAutomatic: Boolean,
    onAutomaticToggle: (Boolean, androidx.compose.ui.geometry.Offset?) -> Unit,
    activeExplorePost: ExplorePost?,
    onExplorePostSelected: (ExplorePost?) -> Unit,
    playOrFitnessDetailOpen: Boolean,
    onPlayOrFitnessDetailOpenChanged: (Boolean) -> Unit,
    dismissPlayFitnessDetail: (() -> Unit)?,
    onRegisterDismissDetail: ((() -> Unit)?) -> Unit,
    modifier: Modifier = Modifier
) {

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // Using isDarkTheme parameter passed from parent
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
        onPlayOrFitnessDetailOpenChanged(false)
        onRegisterDismissDetail(null)
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Let child handle pre-scroll
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // If child didn't consume upward scroll (meaning it's at the end), expand sheet
                if (available.y < 0 && sheetHeightPx.value < expandedHeightPx) {
                    coroutineScope.launch {
                        sheetHeightPx.animateTo(expandedHeightPx)
                    }
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
                                    .clickable { onExplorePostSelected(null) },
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
                        androidx.compose.animation.AnimatedVisibility(
                            visible = selectedTab != "Profile" && selectedTab != "ProfileEdit",
                            enter = fadeIn() + slideInVertically { -it },
                            exit = fadeOut() + slideOutVertically { -it }
                        ) {
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
                                    Image(
                                        painter = painterResource(id = R.drawable.profile_image),
                                        contentDescription = "Profile",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
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
                                onPostSelected = { onExplorePostSelected(it) }
                            )
                        }
                        "Food" -> FoodView(isDarkTheme = isDarkTheme)
                        "Play" -> PlayView(
                            isDarkTheme = isDarkTheme,
                            viewModel = playViewModel,
                            onDetailVisibilityChanged = { onPlayOrFitnessDetailOpenChanged(it) },
                            onRegisterDismissDetail = { onRegisterDismissDetail(it) }
                        )
                        "Fitness" -> FitnessView(
                            isDarkTheme = isDarkTheme,
                            onDetailVisibilityChanged = { onPlayOrFitnessDetailOpenChanged(it) },
                            onRegisterDismissDetail = { onRegisterDismissDetail(it) }
                        )
                        "Transit" -> TransitView(isDark = isDarkTheme, transitViewModel = transitViewModel)
                        "Profile" -> ProfileView(
                            isDarkTheme = isDarkTheme,
                            selectedTheme = selectedTheme,
                            onThemeSelected = onThemeSelected,
                            isAutomatic = isAutomatic,
                            onAutomaticToggle = onAutomaticToggle,
                            onEditClick = { onTabSelected("ProfileEdit") }
                        )
                        "ProfileEdit" -> ProfileEditView(
                            isDarkTheme = isDarkTheme,
                            onBack = { onTabSelected("Profile") },
                            onSave = { onTabSelected("Profile") }
                        )
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
fun NavItem(iconResId: Int, label: String, isSelected: Boolean, isDark: Boolean, onClick: () -> Unit) {
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