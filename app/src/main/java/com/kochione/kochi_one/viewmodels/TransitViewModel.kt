package com.kochione.kochi_one.viewmodels

import androidx.lifecycle.ViewModel
import com.kochione.kochi_one.transit.Metro.MetroStation
import com.kochione.kochi_one.transit.Metro.data.KmrlOpenData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class TransitViewModel : ViewModel() {
    private val _fromStation = MutableStateFlow<MetroStation?>(null)
    val fromStation: StateFlow<MetroStation?> = _fromStation.asStateFlow()

    private val _toStation = MutableStateFlow<MetroStation?>(null)
    val toStation: StateFlow<MetroStation?> = _toStation.asStateFlow()

    private val _showSimulatedTrains = MutableStateFlow(false)
    val showSimulatedTrains: StateFlow<Boolean> = _showSimulatedTrains.asStateFlow()

    private val _showPinkRoute = MutableStateFlow(true)
    val showPinkRoute: StateFlow<Boolean> = _showPinkRoute.asStateFlow()

    fun setFromStation(station: MetroStation?) {
        _fromStation.value = station
    }

    fun setToStation(station: MetroStation?) {
        _toStation.value = station
    }

    fun toggleShowSimulatedTrains() {
        _showSimulatedTrains.value = !_showSimulatedTrains.value
    }

    fun getUpcomingTripIds(): List<String> {
        val from = _fromStation.value?.stopId ?: return emptyList()
        val to = _toStation.value?.stopId ?: return emptyList()
        
        val schedule = KmrlOpenData.getSchedule(from, to)
        val nowSecs = nowSeconds()
        
        // Find trains that are about to depart or have departed but haven't reached destination
        // For simplicity, let's take the next 2 that depart from 'fromStation' after now.
        return schedule
            .filter { it.departureFromOrigin >= nowSecs }
            .take(2)
            .map { it.tripId }
    }

    private fun nowSeconds(): Int {
        val c = Calendar.getInstance()
        return c.get(Calendar.HOUR_OF_DAY) * 3600 +
               c.get(Calendar.MINUTE) * 60 +
               c.get(Calendar.SECOND)
    }
}
