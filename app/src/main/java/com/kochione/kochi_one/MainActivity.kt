package com.kochione.kochi_one


import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.kochione.kochi_one.models.ExplorePost
import com.kochione.kochi_one.ui.theme.KochiOneTheme
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
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val isDarkTheme = isSystemInDarkTheme()
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

    val kochiLocation = LatLng(9.9312, 76.2673)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(kochiLocation, 12f)
    }
    
    val coroutineScope = rememberCoroutineScope()
    
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
            )

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
            if (hasLocationPermission) {
                val glassColor = if (isDarkTheme) Color(0xFF1E1E1E).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.7f)
                val glassBorder = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
                val iconTint = if (isDarkTheme) Color.White else Color.Black

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 220.dp) // Height above collapsed bottom sheet
                        .size(56.dp) // Standard FAB size
                        .clip(RoundedCornerShape(16.dp))
                        .background(glassColor)
                        .border(1.dp, glassBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
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
                        tint = iconTint // Apply dynamic tinting
                    )
                }
            }

            ThreeStateBottomSheet(
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ThreeStateBottomSheet(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val screenHeightDp = configuration.screenHeightDp.dp
    
    val expandedHeight = screenHeightDp * 1.0f // Full screen
    val halfExpandedHeight = screenHeightDp * 0.5f
    val collapsedHeight = 200.dp
    
    val expandedHeightPx = with(density) { expandedHeight.toPx() }
    val halfExpandedHeightPx = with(density) { halfExpandedHeight.toPx() }
    val collapsedHeightPx = with(density) { collapsedHeight.toPx() }
    
    val sheetHeightPx = remember { Animatable(collapsedHeightPx) }
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf("Explore") }
    var activeExplorePost by remember { mutableStateOf<ExplorePost?>(null) }
    
    val isDarkTheme = isSystemInDarkTheme()
    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val dividerColor = if (isDarkTheme) Color.DarkGray else Color.LightGray
    val searchBarBgColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFF0F0F0)
    val dragHandleColor = if (isDarkTheme) Color.Gray else Color.LightGray
    val navBarIconTint = if (isDarkTheme) Color.White else Color.Black
    
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
            // Drag handle
            Box(
                modifier = Modifier
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
                                        selectedTab = "Profile"
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
                                externalSelectedPost = activeExplorePost,
                                onPostSelected = { activeExplorePost = it }
                            )
                        }
                        "Food" -> FoodView()
                        "Play" -> PlayView()
                        "Fitness" -> FitnessView()
                        "Transit" -> TransitView()
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
                NavItem(R.drawable.ic_explore, "Explore", selectedTab == "Explore") { selectedTab = "Explore" }
                NavItem(R.drawable.ic_eat, "Eats", selectedTab == "Food") { selectedTab = "Food" }
                NavItem(R.drawable.ic_play, "Play", selectedTab == "Play") { selectedTab = "Play" }
                NavItem(R.drawable.ic_fitness, "Fitness", selectedTab == "Fitness") { selectedTab = "Fitness" }
                NavItem(R.drawable.ic_transit, "Transit", selectedTab == "Transit") { selectedTab = "Transit" }
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
        modifier = Modifier.clickable { onClick() }
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