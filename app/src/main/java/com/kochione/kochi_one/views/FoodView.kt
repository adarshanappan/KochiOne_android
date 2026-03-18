package com.kochione.kochi_one.views

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kochione.kochi_one.viewmodels.FoodViewModel

@Composable
fun FoodView(viewModel: FoodViewModel = viewModel()) {
    val restaurants by viewModel.restaurants.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val isDarkTheme = isSystemInDarkTheme()
    val bgColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
    val cardBgColor = if (isDarkTheme) Color(0xFF2C2C2C) else Color(0xFFE0E0E0)
    val textColor = if (isDarkTheme) Color.White else Color.Black

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
            if (isLoading && restaurants.isEmpty()) {
                items(3) { // Show 3 skeleton cards while initial data fetch happens
                    RestaurantSkeletonCard()
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
                items(restaurants) { restaurant ->
                    RestaurantCard(restaurant = restaurant)
                }
            }
        }
    }
}
