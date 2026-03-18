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
    private val _selectedRestaurant = MutableStateFlow<Restaurant?>(null)
    val selectedRestaurant: StateFlow<Restaurant?> = _selectedRestaurant.asStateFlow()
    private val _isDetailLoading = MutableStateFlow(false)
    val isDetailLoading: StateFlow<Boolean> = _isDetailLoading.asStateFlow()
    private val _detailErrorMessage = MutableStateFlow<String?>(null)
    val detailErrorMessage: StateFlow<String?> = _detailErrorMessage.asStateFlow()
    private val _detailRequestBizId = MutableStateFlow<String?>(null)
    val detailRequestBizId: StateFlow<String?> = _detailRequestBizId.asStateFlow()

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

    fun fetchRestaurantByBizId(bizId: String) {
        if (bizId.isBlank()) {
            _detailErrorMessage.value = "Invalid restaurant id"
            return
        }
        viewModelScope.launch {
            _detailRequestBizId.value = bizId
            _isDetailLoading.value = true
            _detailErrorMessage.value = null
            try {
                val response = RetrofitClient.foodInstance.getRestaurantByBizId(bizId)
                if (response.status == "success") {
                    _selectedRestaurant.value = response.data.restaurant
                } else {
                    _detailErrorMessage.value = "Failed to load restaurant details"
                }
            } catch (e: Exception) {
                _detailErrorMessage.value = "Network error: ${e.localizedMessage}"
            } finally {
                _isDetailLoading.value = false
            }
        }
    }

    fun clearSelectedRestaurant() {
        _selectedRestaurant.value = null
        _isDetailLoading.value = false
        _detailErrorMessage.value = null
        _detailRequestBizId.value = null
    }
}
