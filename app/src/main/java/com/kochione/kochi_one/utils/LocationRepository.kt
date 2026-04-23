package com.kochione.kochi_one.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton that exposes the user's last known GPS coordinates to any Composable.
 * Updated from MainScreen whenever a location fix is obtained.
 */
object LocationRepository {
    private val _location = MutableStateFlow<Pair<Double, Double>?>(null)
    val location: StateFlow<Pair<Double, Double>?> = _location.asStateFlow()

    fun update(lat: Double, lon: Double) {
        _location.value = Pair(lat, lon)
    }
}
