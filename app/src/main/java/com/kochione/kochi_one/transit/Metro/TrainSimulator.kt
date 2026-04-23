package com.kochione.kochi_one.transit.Metro

import com.google.android.gms.maps.model.LatLng
import com.kochione.kochi_one.transit.Metro.data.KmrlOpenData
import com.kochione.kochi_one.transit.Metro.data.ShapePoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class ActiveTrain(val tripId: String, val position: LatLng, val bearing: Float)

object TrainSimulator {
    
    fun activeTrainsFlow(): Flow<List<ActiveTrain>> = flow {
        while (true) {
            val cal = Calendar.getInstance()
            val currentSeconds = cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND)
            
            val activeTrains = mutableListOf<ActiveTrain>()
            
            for (trip in KmrlOpenData.trips) {
                if (trip.stopTimes.isEmpty()) continue
                
                val firstArrival = trip.stopTimes.first().arrivalTime
                val lastArrival = trip.stopTimes.last().arrivalTime
                
                if (currentSeconds in firstArrival..lastArrival) {
                    val shape = KmrlOpenData.shapePointsMap[trip.shapeId] ?: continue
                    
                    var currentDist = 0.0
                    for (i in 0 until trip.stopTimes.size - 1) {
                        val currentStop = trip.stopTimes[i]
                        val nextStop = trip.stopTimes[i + 1]
                        
                        if (currentSeconds in currentStop.arrivalTime..nextStop.arrivalTime) {
                            if (currentSeconds <= currentStop.departureTime) {
                                // Dwelling at station
                                currentDist = currentStop.distTraveled
                            } else {
                                // Moving between stations
                                val progress = (currentSeconds - currentStop.departureTime).toDouble() / (nextStop.arrivalTime - currentStop.departureTime)
                                currentDist = currentStop.distTraveled + (nextStop.distTraveled - currentStop.distTraveled) * progress
                            }
                            break
                        }
                    }
                    
                    val (pos, bearing) = getLatLngAtDistance(shape, currentDist)
                    activeTrains.add(ActiveTrain(trip.tripId, pos, bearing))
                }
            }
            
            emit(activeTrains)
            delay(200) // Update 5 times a second for smoother marker movement
        }
    }

    private fun getLatLngAtDistance(shape: List<ShapePoint>, dist: Double): Pair<LatLng, Float> {
        if (shape.isEmpty()) return LatLng(0.0, 0.0) to 0f
        if (dist <= shape.first().dist) return shape.first().latLng to bearing(shape[0].latLng, shape.getOrNull(1)?.latLng ?: shape[0].latLng)
        if (dist >= shape.last().dist) return shape.last().latLng to bearing(shape[shape.size-2].latLng, shape.last().latLng)

        for (i in 0 until shape.size - 1) {
            val p1 = shape[i]
            val p2 = shape[i + 1]
            if (dist >= p1.dist && dist <= p2.dist) {
                val progress = if (p2.dist == p1.dist) 0.0 else (dist - p1.dist) / (p2.dist - p1.dist)
                val interpolated = interpolateLatLng(p1.latLng, p2.latLng, progress)
                return interpolated to bearing(p1.latLng, p2.latLng)
            }
        }
        return shape.last().latLng to 0f
    }

    private fun interpolateLatLng(from: LatLng, to: LatLng, fraction: Double): LatLng {
        val lat = from.latitude + (to.latitude - from.latitude) * fraction
        val lng = from.longitude + (to.longitude - from.longitude) * fraction
        return LatLng(lat, lng)
    }

    private fun bearing(from: LatLng, to: LatLng): Float {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(to.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        val y = sin(dLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
        val bearing = Math.toDegrees(atan2(y, x)).toFloat()
        return (bearing + 360) % 360
    }
}
