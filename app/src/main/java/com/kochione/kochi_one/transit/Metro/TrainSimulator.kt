package com.kochione.kochi_one.transit.Metro

import com.google.android.gms.maps.model.LatLng
import com.kochione.kochi_one.transit.Metro.data.KmrlOpenData
import com.kochione.kochi_one.transit.Metro.data.ShapePoint
import com.kochione.kochi_one.transit.Metro.data.TripInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class ActiveTrain(val tripId: String, val position: LatLng, val bearing: Float)

object TrainSimulator {
    
    private var lastUpdateTime = 0L
    private val activeTripsCache = mutableListOf<TripInfo>()
    private var lastCacheUpdate = 0L

    fun activeTrainsFlow(): Flow<List<ActiveTrain>> = flow {
        while (true) {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            val currentSeconds = cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND)
            val millisInSecond = now % 1000
            val preciseSeconds = currentSeconds + (millisInSecond / 1000.0)

            // Update active trips cache every 2 seconds
            if (now - lastCacheUpdate > 2000) {
                activeTripsCache.clear()
                for (trip in KmrlOpenData.trips) {
                    if (trip.stopTimes.isEmpty()) continue
                    val firstArrival = trip.stopTimes.first().arrivalTime
                    val lastArrival = trip.stopTimes.last().arrivalTime
                    if (currentSeconds in (firstArrival - 30)..(lastArrival + 30)) {
                        activeTripsCache.add(trip)
                    }
                }
                lastCacheUpdate = now
            }
            
            val activeTrains = mutableListOf<ActiveTrain>()
            
            for (trip in activeTripsCache) {
                val firstArrival = trip.stopTimes.first().arrivalTime
                val lastArrival = trip.stopTimes.last().arrivalTime
                
                if (preciseSeconds in firstArrival.toDouble()..lastArrival.toDouble()) {
                    val shape = KmrlOpenData.shapePointsMap[trip.shapeId] ?: continue
                    
                    var currentDist = 0.0
                    for (i in 0 until trip.stopTimes.size - 1) {
                        val currentStop = trip.stopTimes[i]
                        val nextStop = trip.stopTimes[i + 1]
                        
                        if (preciseSeconds >= currentStop.arrivalTime && preciseSeconds <= nextStop.arrivalTime) {
                            if (preciseSeconds <= currentStop.departureTime) {
                                currentDist = currentStop.distTraveled
                            } else {
                                val duration = nextStop.arrivalTime - currentStop.departureTime
                                if (duration > 0) {
                                    val progress = (preciseSeconds - currentStop.departureTime) / duration
                                    currentDist = currentStop.distTraveled + (nextStop.distTraveled - currentStop.distTraveled) * progress
                                } else {
                                    currentDist = currentStop.distTraveled
                                }
                            }
                            break
                        }
                    }
                    
                    val (pos, bearing) = getLatLngAtDistance(shape, currentDist)
                    activeTrains.add(ActiveTrain(trip.tripId, pos, bearing))
                }
            }
            
            emit(activeTrains)
            delay(16) // 60 FPS for butter-smooth movement
        }
    }

    fun getCurrentTrainPosition(tripId: String): LatLng? {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        val currentSeconds = cal.get(Calendar.HOUR_OF_DAY) * 3600 + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND)
        val millisInSecond = now % 1000
        val preciseSeconds = currentSeconds + (millisInSecond / 1000.0)
        
        val trip = KmrlOpenData.trips.find { it.tripId == tripId } ?: return null
        if (trip.stopTimes.isEmpty()) return null
        
        val firstArrival = trip.stopTimes.first().arrivalTime
        val lastArrival = trip.stopTimes.last().arrivalTime
        
        if (preciseSeconds < firstArrival || preciseSeconds > lastArrival) return null
        
        val shape = KmrlOpenData.shapePointsMap[trip.shapeId] ?: return null
        
        var currentDist = 0.0
        for (i in 0 until trip.stopTimes.size - 1) {
            val currentStop = trip.stopTimes[i]
            val nextStop = trip.stopTimes[i + 1]
            
            if (preciseSeconds >= currentStop.arrivalTime && preciseSeconds <= nextStop.arrivalTime) {
                if (preciseSeconds <= currentStop.departureTime) {
                    currentDist = currentStop.distTraveled
                } else {
                    val duration = nextStop.arrivalTime - currentStop.departureTime
                    if (duration > 0) {
                        val progress = (preciseSeconds - currentStop.departureTime) / duration
                        currentDist = currentStop.distTraveled + (nextStop.distTraveled - currentStop.distTraveled) * progress
                    } else {
                        currentDist = currentStop.distTraveled
                    }
                }
                break
            }
        }
        
        val (pos, _) = getLatLngAtDistance(shape, currentDist)
        return pos
    }

    private fun getLatLngAtDistance(shape: List<ShapePoint>, dist: Double): Pair<LatLng, Float> {
        if (shape.isEmpty()) return LatLng(0.0, 0.0) to 0f
        
        val pos = getPosOnShape(shape, dist)
        // Look 5 meters ahead for smooth bearing, but don't go past the end
        val lookAheadDist = (dist + 0.005).coerceAtMost(shape.last().dist)
        val aheadPos = getPosOnShape(shape, lookAheadDist)
        
        val bearing = if (pos == aheadPos) {
            // If at the very end, use the last segment's bearing
            if (shape.size >= 2) bearing(shape[shape.size - 2].latLng, shape.last().latLng) else 0f
        } else {
            bearing(pos, aheadPos)
        }
        
        return pos to bearing
    }

    private fun getPosOnShape(shape: List<ShapePoint>, dist: Double): LatLng {
        if (dist <= shape.first().dist) return shape.first().latLng
        if (dist >= shape.last().dist) return shape.last().latLng

        for (i in 0 until shape.size - 1) {
            val p1 = shape[i]
            val p2 = shape[i + 1]
            if (dist >= p1.dist && dist <= p2.dist) {
                val progress = if (p2.dist == p1.dist) 0.0 else (dist - p1.dist) / (p2.dist - p1.dist)
                return interpolateLatLng(p1.latLng, p2.latLng, progress)
            }
        }
        return shape.last().latLng
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
