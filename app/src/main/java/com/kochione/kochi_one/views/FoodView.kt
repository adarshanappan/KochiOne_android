package com.kochione.kochi_one.views

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kochione.kochi_one.R
import com.kochione.kochi_one.viewmodels.FoodViewModel

@Composable
fun FoodView(isDarkTheme: Boolean, viewModel: FoodViewModel = viewModel()) {
    val restaurants by viewModel.restaurants.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val selectedRestaurant by viewModel.selectedRestaurant.collectAsState()
    val isDetailLoading by viewModel.isDetailLoading.collectAsState()
    val detailErrorMessage by viewModel.detailErrorMessage.collectAsState()
    val detailRequestBizId by viewModel.detailRequestBizId.collectAsState()
    var selectedCategory by remember { mutableStateOf("Trending") }

    // Theme handled by parameter
    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val cardBgColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFE0E0E0)
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val inactiveTextColor = if (isDarkTheme) Color.LightGray else Color.Gray

    val categories = listOf(
        "Trending",
        "Cafe",
        "Restaurant",
        "Buffet & Fine Dining",
        "Restobar",
        "Food Trucks",
        "Juice & Shake",
        "Bakeries and Dessert"
    )
    val categoryRowState = rememberLazyListState()

    LaunchedEffect(selectedCategory) {
        val index = categories.indexOf(selectedCategory)
        if (index >= 0) categoryRowState.animateScrollToItem(index)
    }
    val filteredRestaurants = remember(restaurants, selectedCategory) {
        if (selectedCategory == "Trending") {
            restaurants.sortedBy { it.ranking }
        } else {
            restaurants
                .filter { (it.restaurantType ?: "").equals(selectedCategory, ignoreCase = true) }
                .sortedBy { it.ranking }
        }
    }

    val inDetailMode =
        isDetailLoading || selectedRestaurant != null || detailErrorMessage != null
    if (inDetailMode) {
        BackHandler {
            viewModel.clearSelectedRestaurant()
        }
        Dialog(
            onDismissRequest = {
                viewModel.clearSelectedRestaurant()
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            val density = LocalDensity.current
            val openProgress = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                openProgress.snapTo(0f)
                openProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
                )
            }
            val slidePx = with(density) { 56.dp.toPx() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        transformOrigin = TransformOrigin(0.5f, 0.08f)
                        alpha = openProgress.value
                        translationY = (1f - openProgress.value) * slidePx
                        val s = 0.94f + 0.06f * openProgress.value
                        scaleX = s
                        scaleY = s
                    }
                    .background(bgColor)
            ) {
                when {
                    isDetailLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    selectedRestaurant != null -> {
                        FoodDetailView(restaurant = selectedRestaurant!!, isDarkTheme = isDarkTheme)
                    }

                    detailErrorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = detailErrorMessage ?: "Failed to load details",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    detailRequestBizId?.let { viewModel.fetchRestaurantByBizId(it) }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = cardBgColor,
                                    contentColor = textColor
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(bgColor),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            item {
                Text(
                    text = "Kochi Eats",
                    style = MaterialTheme.typography.headlineLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                val activeTabBgBase = if (isDarkTheme) Color(0xFF2A2F38) else Color(0xFF1F2937)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    state = categoryRowState,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = category == selectedCategory
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) activeTabBgBase else Color.Transparent,
                                    RoundedCornerShape(22.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) Color.White else inactiveTextColor,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
            if (isLoading && restaurants.isEmpty()) {
                items(3) { // Show 3 skeleton cards while initial data fetch happens
                    RestaurantSkeletonCard(isDarkTheme = isDarkTheme)
                }
            } else if (!isLoading && errorMessage != null && restaurants.isEmpty()) {
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
                            onClick = { viewModel.fetchRestaurants() },
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
                if (filteredRestaurants.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 56.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_eat),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = textColor.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "No restaurants found",
                                style = MaterialTheme.typography.headlineMedium,
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Try a different category",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor.copy(alpha = 0.65f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(filteredRestaurants) { restaurant ->
                        RestaurantCard(
                            restaurant = restaurant,
                            isDarkTheme = isDarkTheme,
                            onClick = {
                                viewModel.fetchRestaurantByBizId(restaurant.bizId)
                            }
                        )
                    }
                }
            }
        }
    }
}
