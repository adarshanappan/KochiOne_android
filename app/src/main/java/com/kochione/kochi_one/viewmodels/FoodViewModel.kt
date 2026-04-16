package com.kochione.kochi_one.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kochione.kochi_one.api.RetrofitClient
import com.kochione.kochi_one.models.Restaurant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodViewModel : ViewModel() {
    private val _restaurants = MutableStateFlow<List<Restaurant>>(emptyList())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchRestaurants()
    }

    fun fetchRestaurants() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.foodInstance.getRestaurants()
                if (response.status == "success") {
                    _restaurants.value = response.data.restaurants
                } else {
                    val elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime < 7000) {
                        kotlinx.coroutines.delay(7000 - elapsedTime)
                    }
                    _errorMessage.value = "Failed to load restaurants"
                }
            } catch (e: Exception) {
                val elapsedTime = System.currentTimeMillis() - startTime
                if (elapsedTime < 7000) {
                    kotlinx.coroutines.delay(7000 - elapsedTime)
                }
                _errorMessage.value = "Network error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
